package com.mikeschvedov.weatherms.Models

data class LocalityInfo(
    val administrative: List<Administrative>,
    val informative: List<Informative>
)