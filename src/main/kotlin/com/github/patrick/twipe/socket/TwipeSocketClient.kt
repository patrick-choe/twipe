/*
 * Copyright (C) 2020 PatrickKR
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact me on <mailpatrickkr@gmail.com>
 */

package com.github.patrick.twipe.socket

import com.github.patrick.twipe.event.AsyncTwipDonateEvent
import com.github.patrick.twipe.plugin.TwipePlugin
import com.github.patrick.twipe.plugin.TwipePlugin.Companion.timer
import com.github.patrick.twipe.plugin.TwipePlugin.Companion.twipVersion
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
 * @param streamer streamer name, id, or any identifier for AsyncTwipDonateEvent
 * @param key twip alert box id
 */
internal class TwipeSocketClient(streamer: String, key: String) {
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
        }.createSocket(twipAddress.format(key, twipVersion)).apply {
            addListener(object : WebSocketAdapter() {
                override fun onConnected(websocket: WebSocket, headers: MutableMap<String, MutableList<String>>) {
                    TwipePlugin.logger.info("Connected to twip - $streamer")
                }

                override fun onDisconnected(websocket: WebSocket, serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame, closedByServer: Boolean) {
                    TwipePlugin.logger.warning("Disconnected from Twip - $streamer")
                    TwipeSocketClient(streamer, key)
                }

                override fun onTextMessage(websocket: WebSocket, text: String) {
                    when (text.substring(0, 1).toInt()) { // engine.io protocol
                        0 -> { // open
                            val interval = parse(text.substring(1)).asJsonObject.getAsJsonPrimitive("pingInterval").asLong
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    websocket.sendText("2")
                                }
                            }, interval, interval)
                        }
                        4 -> { // message
                            when (text.substring(1, 2).toInt()) { // socket.io protocol
                                2 -> { // event
                                    val json = parse(text.substring(2)).asJsonArray
                                    when (json[0].asJsonPrimitive.asString) { // socket.io event
                                        "new donate" -> { // twip donation
                                            val donation = json[1].asJsonObject

                                            val amount = donation.getAsJsonPrimitive("amount").asInt
                                            val comment = donation.getAsJsonPrimitive("comment").asString
                                            val nickname = donation.getAsJsonPrimitive("nickname").asString

                                            Bukkit.getPluginManager().callEvent(AsyncTwipDonateEvent(streamer, amount, comment, nickname))
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
        private const val twipAddress = "wss://io.mytwip.net/socket.io/?alertbox_key=%s&version=%s&transport=websocket"

        @Suppress("DEPRECATION")
        private fun parse(json: String): JsonElement {
            return requireNotNull(if (newGson) {
                JsonParser.parseString(json)
            } else {
                JsonParser().parse(json)
            })
        }

        private val newGson by lazy {
            JsonParser::class.java.constructors.any { constructor ->
                constructor.getAnnotation(Deprecated::class.java) != null
            }
        }
    }
}