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

package com.github.patrick.twipe.plugin

import com.github.patrick.twipe.socket.TwipeSocketClient
import org.bukkit.plugin.java.JavaPlugin
import java.net.URLEncoder
import java.util.Timer
import java.util.logging.Logger

/**
 * Twipe plugin
 */
class TwipePlugin : JavaPlugin() {
    /**
     * Called on startup
     */
    override fun onLoad() {
        saveDefaultConfig()

        pluginLogger = logger
        timer = Timer()
        twipVersion = config.getString("twip-version")

        try {
            config.getConfigurationSection("streamers").apply {
                val streamers = getKeys(false)
                val count = streamers.count()

                streamers.forEachIndexed { index, streamer ->
                    val section = getConfigurationSection(streamer)
                    val key = section.getString("id")
                    val token = URLEncoder.encode(section.getString("token"), Charsets.UTF_8)

                    logger.info("Loading ${index + 1} / $count - $streamer")
                    TwipeSocketClient(streamer, key, token)
                }

                logger.info("Done loading $count streamers")
            }
        } catch (throwable: Throwable) {
            logger.info("Caught exception while reading configuration. Please check config.yml.")
            pluginLoader.disablePlugin(this)
        }
    }

    internal companion object {
        internal lateinit var pluginLogger: Logger
            private set

        internal lateinit var timer: Timer
            private set

        internal lateinit var twipVersion: String
            private set
    }
}