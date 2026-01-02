package com.prayertimemuter.models

data class City(
    val id: Int,
    val country: String = "",
    val city: String = "",
    val region: String? = null
) {
    override fun toString(): String {
        val parts = listOf(region, city, country)
            .filter { !it.isNullOrBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "Bilinmiyor"
    }
}
