/*
 * Copyright (C) 2021 PatrickKR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.patrick.twipe.socket

import com.github.patrick.twipe.data.SlotMachineConfig
import com.github.patrick.twipe.data.SlotMachineData
import com.github.patrick.twipe.data.SubscriptionTier
import com.github.patrick.twipe.event.AsyncTwipCheerEvent
import com.github.patrick.twipe.event.AsyncTwipDonateEvent
import com.github.patrick.twipe.event.AsyncTwipFollowEvent
import com.github.patrick.twipe.event.AsyncTwipHostingEvent
import com.github.patrick.twipe.event.AsyncTwipRedemptionEvent
import com.github.patrick.twipe.event.AsyncTwipSubscribeEvent
import com.github.patrick.twipe.plugin.TwipePlugin
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import org.bukkit.Bukkit
import java.security.cert.X509Certificate
import java.util.TimerTask
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * This class represents the twipe client itself.
 *
 * @param streamer streamer name, id, or any identifier for AsyncTwipEvent
 * @param key twip alert box id
 * @param token twip alert box token
 */
internal class TwipeSocketClient(streamer: String, key: String, token: String) {
    init {
        requireNotNull(WebSocketFactory().apply {
            sslContext = try {
                SSLContext.getInstance("TLS").apply {
                    init(null, arrayOf(object : X509TrustManager {
                        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}

                        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}

                        override fun getAcceptedIssuers(): Array<X509Certificate>? {
                            return null
                        }
                    }), null)
                }
            } catch (exception: Exception) {
                throw RuntimeException("Failed to initialize SSLContext", exception)
            }

            verifyHostname = false
        }.createSocket(twipAddress.format(key, TwipePlugin.twipVersion, token)).apply {
            addListener(object : WebSocketAdapter() {
                override fun onConnected(websocket: WebSocket, headers: MutableMap<String, MutableList<String>>) {
                    TwipePlugin.pluginLogger.info("Connected to twip - $streamer")
                }

                override fun onDisconnected(websocket: WebSocket, serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame, closedByServer: Boolean) {
                    TwipePlugin.pluginLogger.warning("Disconnected from Twip - $streamer")

                    runCatching {
                        disconnect()
                    }

                    TwipeSocketClient(streamer, key, token)
                }

                override fun onTextMessage(websocket: WebSocket, text: String) {
                    when (text.engineIOProtocol) {
                        // engine.io protocol

                        // 0: open
                        // 1: close
                        // 2: ping
                        // 3: pong
                        // 4: message
                        // 5: upgrade
                        // 6: noop

                        0 -> { // engine.io open
                            val open = text.socketIOContent
                            val interval = open["pingInterval"].asLong

                            TwipePlugin.timer.schedule(object : TimerTask() {
                                override fun run() {
                                    websocket.sendText("2")
                                }
                            }, interval, interval)
                        }

                        4 -> { // engine.io message
                            when (text.socketIOProtocol) {
                                // socket.io protocol

                                // 0: connect
                                // 1: disconnect
                                // 2: event
                                // 3: ack
                                // 4: connect error
                                // 5: binary event
                                // 6: binary ack

                                2 -> { // event
                                    val event = text.engineIOContent
                                    when (event.name) { // socket.io event
                                        "new donate" -> { // twip donation
                                            val donation = event.content

                                            val amount = donation["amount"].asLong
                                            val slotMachineData = donation["slotmachine_data"].asSlotMachineData
                                            val comment = donation["comment"].asString
                                            val nickname = donation["nickname"].asString

                                            Bukkit.getPluginManager().callEvent(AsyncTwipDonateEvent(streamer, amount, slotMachineData, comment, nickname))
                                        }

                                        "new cheer" -> { // twitch cheer
                                            val cheer = event.content

                                            val amount = cheer["amount"].asLong
                                            val comment = cheer["comment"].asString
                                            val nickname = cheer["nickname"].asString

                                            Bukkit.getPluginManager().callEvent(AsyncTwipCheerEvent(streamer, amount, comment, nickname))
                                        }

                                        "new follow" -> { // twitch follow
                                            val follow = event.content

                                            val nickname = follow["nickname"].asString

                                            Bukkit.getPluginManager()
                                                .callEvent(AsyncTwipFollowEvent(streamer, nickname))
                                        }

                                        "new sub" -> { // twitch subscription
                                            val subscribe = event.content

                                            val months = subscribe["months"].asLong
                                            val tier = subscribe["method"].asTier
                                            val message = subscribe["message"].asString
                                            val username = subscribe["username"].asString

                                            Bukkit.getPluginManager().callEvent(AsyncTwipSubscribeEvent(streamer, months, tier, message, username))
                                        }

                                        "new hosting" -> { // twitch hosting
                                            val hosting = event.content

                                            val viewers = hosting["viewers"].asNotNullLong
                                            val username = hosting["username"].asString

                                            Bukkit.getPluginManager().callEvent(AsyncTwipHostingEvent(streamer, viewers, username))
                                        }

                                        "new redemption" -> { // twip redemption
                                            val redemption = event.content

                                            val rewardId = redemption["reward_id"].asString.toLong()
                                            val rewardName = redemption["reward_name"].asString
                                            val slotMachineData = redemption["slotmachine_data"]?.asSlotMachineData
                                            val comment = redemption["comment"].asString
                                            val nickname = redemption["nickname"].asString

                                            Bukkit.getPluginManager().callEvent(AsyncTwipRedemptionEvent(streamer, rewardId, rewardName, slotMachineData, comment, nickname))
                                        }

                                        "version not match" -> { // twip outdated
                                            runCatching {
                                                disconnect()
                                            }

                                            throw RuntimeException("Current version ${TwipePlugin.twipVersion} not supported." +
                                                    "Please update plugin, or contact developer for help.")
                                        }

                                        "TOKEN_EXPIRED" -> { // twip token expired
                                            runCatching {
                                                disconnect()
                                            }

                                            throw RuntimeException("$streamer's token has expired. Please get a new token")
                                        }

                                        "reload" -> { // twip reload
                                            TwipePlugin.pluginLogger.info("Reconnecting to Twip - $streamer")

                                            runCatching {
                                                disconnect()
                                            }

                                            TwipeSocketClient(streamer, key, token)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }).connect()
    }

    private companion object {
        private const val twipAddress = "wss://io.mytwip.net/socket.io/?alertbox_key=%s&version=%s&token=%s&transport=websocket"

        @JvmStatic
        private val parser by lazy {
            JsonParser()
        }

        @JvmStatic
        private val String.engineIOProtocol
            get() = substring(0, 1).toInt()

        @JvmStatic
        private val String.socketIOProtocol
            get() = substring(1, 2).toInt()

        @JvmStatic
        private val String.socketIOContent
            get() = parser.parse(substring(1)).asJsonObject

        @JvmStatic
        private val String.engineIOContent
            get() = parser.parse(substring(2)).asJsonArray

        @JvmStatic
        private val JsonArray.name
            get() = this[0].asString

        @JvmStatic
        private val JsonArray.content
            get() = this[1].asJsonObject

        @JvmStatic
        private val JsonElement.asNotNullLong
            get() = if (isJsonNull) 0 else asLong

        @JvmStatic
        private val JsonElement?.asSlotMachineData
            get() = if (this == null || isJsonNull) null else asJsonObject.let { slotMachine ->
                val slotMachineConfig = slotMachine["config"].asJsonObject.let { config ->
                    val duration = config["duration"].asLong

                    val point = config["point"].asJsonObject.run {
                        entrySet().map { entry ->
                            Pair(entry.key, if (entry.value.isJsonNull) null else entry.value.asJsonPrimitive.asLong)
                        }
                    }

                    val sound = config["sound"].asJsonArray.map { element ->
                        element.asString
                    }

                    SlotMachineConfig(duration, point, sound)
                }

                val result = slotMachine["gotcha"].asLong

                val items = slotMachine["items"].asJsonArray.map { element ->
                    element.asString
                }

                val rewardId = slotMachine["reward_id"].asString.toLong()

                SlotMachineData(slotMachineConfig, result - 1, items, rewardId)
            }

        @JvmStatic
        private val JsonElement.asTier
            get() = when (asString) {
                "1000" -> SubscriptionTier.TIER1
                "2000" -> SubscriptionTier.TIER2
                "3000" -> SubscriptionTier.TIER3
                else -> SubscriptionTier.UNKNOWN
            }
    }
}