package com.mikeschvedov.weatherms.Models

data class LocationData(
    val city: String,
    val continent: String,
    val continentCode: String,
    val countryCode: String,
    val countryName: String,
    val latitude: Double,
    val locality: String,
    val localityInfo: LocalityInfo,
    val localityLanguageRequested: String,
    val longitude: Double,
    val plusCode: String,
    val postcode: String,
    val principalSubdivision: String,
    val principalSubdivisionCode: String
)