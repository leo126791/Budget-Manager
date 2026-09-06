package com.example.debit.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Locale

object GpsLocationUtils {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @Suppress("DEPRECATION")
    fun getCurrentGpsLocationName(context: Context, onResult: (locationName: String) -> Unit) {
        if (!hasLocationPermission(context)) {
            onResult("")
            return
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val location: Location? = try {
                locationManager?.let { lm ->
                    lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                }
            } catch (_: SecurityException) {
                null
            }

            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        val addr = addresses.firstOrNull()
                        val name = formatAddressName(addr)
                        onResult(name)
                    }
                } else {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val addr = addresses?.firstOrNull()
                    val name = formatAddressName(addr)
                    onResult(name)
                }
            } else {
                onResult("GPS 定位地點")
            }
        } catch (_: Exception) {
            onResult("GPS 定位地點")
        }
    }

    private fun formatAddressName(address: Address?): String {
        if (address == null) return "GPS 定位地點"
        val feature = address.featureName
        val thoroughfare = address.thoroughfare
        val subLocality = address.subLocality
        val locality = address.locality

        return when {
            !feature.isNullOrBlank() && feature != thoroughfare -> feature
            !thoroughfare.isNullOrBlank() && !subLocality.isNullOrBlank() -> "$subLocality $thoroughfare"
            !thoroughfare.isNullOrBlank() -> thoroughfare
            !subLocality.isNullOrBlank() -> subLocality
            !locality.isNullOrBlank() -> locality
            else -> address.getAddressLine(0) ?: "GPS 定位地點"
        }
    }
}
