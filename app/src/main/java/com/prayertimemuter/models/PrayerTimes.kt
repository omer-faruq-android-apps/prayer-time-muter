package com.prayertimemuter.models

data class PrayerTimes(
    val date: String,
    val fajr: String,
    val sun: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
) {
    fun asList(): List<Pair<String, String>> = listOf(
        "İmsak" to fajr,
        "Güneş" to sun,
        "Öğle" to dhuhr,
        "İkindi" to asr,
        "Akşam" to maghrib,
        "Yatsı" to isha
    )
}
