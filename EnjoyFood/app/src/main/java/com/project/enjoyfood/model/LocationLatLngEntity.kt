package com.project.enjoyfood.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationLatLngEntity(
    var latitude: Float = 0f,
    var longitude: Float = 0f
): Parcelable
