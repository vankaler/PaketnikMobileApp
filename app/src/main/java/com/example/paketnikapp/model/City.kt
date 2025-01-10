package com.example.paketnikapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class City(
    @SerialName("Mesto")
    val name: String,

    @SerialName("Naslov")
    val address: String,

    @SerialName("Opis lokacije")
    val locationDescription: String,

    @SerialName("Število paketnikov")
    val numberOfLockers: Int,

    @SerialName("Izročilna pošta")
    val postOfficeCode: Int,

    @SerialName("Latitude")
    val latitude: String,

    @SerialName("Longitude")
    val longitude: String
) : Parcelable
