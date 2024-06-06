package com.jiuntian.hydratedme.model

class PetData(
    val hp: Int = 100,
    val exp: Int = 0,
    val type: PetType = PetType.CAT
)

enum class PetType(val printableName: String) {
    CAT("Cat"),
    DOG("Dog")
}