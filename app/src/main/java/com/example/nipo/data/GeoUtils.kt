package com.example.nipo.data

import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /** Rounds coordinates to ~100m precision (3 decimal places) to avoid exposing an exact location. */
    fun coarsen(lat: Double, lng: Double, precision: Int = 3): GeoPoint =
        GeoPoint(round(lat, precision), round(lng, precision))

    private fun round(value: Double, precision: Int): Double {
        val factor = 10.0.pow(precision)
        return Math.round(value * factor) / factor
    }

    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
}
