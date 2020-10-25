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

package com.github.patrick.twipe.plugin

import com.github.patrick.twipe.socket.TwipeSocketClient
import org.bukkit.plugin.java.JavaPlugin
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
        TwipePlugin.logger = logger
        timer = Timer()
        saveDefaultConfig()
        twipVersion = requireNotNull(config.getString("twip-version"))
        requireNotNull(config.getConfigurationSection("keys")).apply {
            val keys = getKeys(false)
            val count = keys.count()
            keys.forEachIndexed { index, streamer ->
                logger.info("Loading ${index + 1} / $count - $streamer")
                TwipeSocketClient(streamer, getString(streamer))
            }
            logger.info("Loaded $count streamers")
        }
    }

    internal companion object {
        lateinit var logger: Logger
            private set

        lateinit var twipVersion: String
            private set

        lateinit var timer: Timer
            private set
    }
}