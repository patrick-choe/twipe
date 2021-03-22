package com.github.patrick.twipe.data

/**
 * Slot Machine data
 *
 * @param config slot machine configuration
 * @param result the index of slot machine result. Note: index starts from 1, not 0.
 * @param items list of slot machine options
 * @param rewardId id of the slot machine
 */
data class SlotMachineData internal constructor(
    val config: SlotMachineConfig,
    val result: Long,
    val items: Collection<String>,
    val rewardId: Long
)

/**
 * Slot Machine configuration data
 *
 * @param duration slot machine duration
 * @param point pair of options and point (not sure yet)
 * @param sound not sure yet
 */
data class SlotMachineConfig internal constructor(
    val duration: Long,
    val point: Collection<Pair<String, Long?>>,
    val sound: Collection<String> // Twip PRO 가 없어서 어떤 내용이 들어가는지 알 길이 없습니다 ㅠㅠ
)