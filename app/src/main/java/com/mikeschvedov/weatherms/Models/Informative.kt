package com.mikeschvedov.weatherms.Models

data class Informative(
    val description: String,
    val geonameId: Int,
    val isoCode: String,
    val name: String,
    val order: Int,
    val wikidataId: String
)