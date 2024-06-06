package com.jiuntian.hydratedme.model

import java.util.*

data class DrinkRecord(val time:Date = Date(),
                       val volume: Int = 0,
                       val type: DrinkType = DrinkType.WATER,
                       val expGain: Int = 0)

enum class DrinkType(val printableName: String) {
    WATER("Water"),
    TEA("Tea"),
    COFFEE("Coffee"),
    SOFT_DRINK("Soft Drink"),
    ALCOHOL("Alcohol"),
    OTHER("Other")
}