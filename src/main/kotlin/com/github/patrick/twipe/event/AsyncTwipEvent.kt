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

@file:Suppress("unused")

package com.github.patrick.twipe.event

import com.github.patrick.twipe.data.SlotMachineData
import com.github.patrick.twipe.data.SubscriptionTier
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event is called when a twipe event was made.
 *
 * @param streamer streamer name, id, or any identifier
 */
sealed class AsyncTwipEvent(val streamer: String) : Event(true)

/**
 * This event is called when a donation was made.
 *
 * @param streamer streamer name, id, or any identifier
 * @param amount donation amount, in South Korean won
 * @param slotMachineData slot machine data, null if donation is not slot machine.
 * @param comment donation message
 * @param nickname nickname of donor
 */
class AsyncTwipDonateEvent internal constructor(streamer: String, val amount: Long, val slotMachineData: SlotMachineData?, val comment: String, val nickname: String) : AsyncTwipEvent(streamer) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

/**
 * This event is called when a cheer was made.
 *
 * @param streamer streamer name, id, or any identifier
 * @param amount cheer amount, in Twitch bits
 * @param comment cheer message
 * @param nickname nickname of cheerer
 */
class AsyncTwipCheerEvent internal constructor(streamer: String, val amount: Long, val comment: String, val nickname: String) : AsyncTwipEvent(streamer) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

/**
 * This event is called when a follow was made.
 *
 * @param streamer streamer name, id, or any identifier
 * @param nickname nickname of follower
 */
class AsyncTwipFollowEvent internal constructor(streamer: String, val nickname: String) : AsyncTwipEvent(streamer) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

/**
 * This event is called when a subscription was made.
 *
 * @param streamer streamer name, id, or any identifier
 * @param months total follow months
 * @param tier subscription tier
 * @param message subscription message
 * @param username twitch username of subscriber
 */
class AsyncTwipSubscribeEvent internal constructor(streamer: String, val months: Long, val tier: SubscriptionTier, val message: String, val username: String) : AsyncTwipEvent(streamer) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

/**
 * This event is called when a hosting was made.
 *
 * @param streamer streamer name, id, or any identifier
 * @param viewers number of viewers
 * @param username twitch username of host
 */
class AsyncTwipHostingEvent internal constructor(streamer: String, val viewers: Long, val username: String) : AsyncTwipEvent(streamer) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}

/**
 * This event is called when a redemption was made.
 *
 * @param streamer streamer name, id, or any identifier
 * @param rewardId id of the reward
 * @param rewardName name of the reward
 * @param slotMachineData slot machine data, null if redemption is not slot machine.
 * @param comment redemption message
 * @param nickname nickname of redeemer
 */
class AsyncTwipRedemptionEvent internal constructor(streamer: String, val rewardId: Long, val rewardName: String, val slotMachineData: SlotMachineData?, val comment: String, val nickname: String) : AsyncTwipEvent(streamer) {
    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}