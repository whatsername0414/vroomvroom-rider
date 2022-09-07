package com.vroomvroomrider.android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.vmadalin.easypermissions.EasyPermissions
import com.vroomvroomrider.android.utils.Constants.DEFAULT_SERVER_TIME_FORMAT
import com.vroomvroomrider.android.utils.Constants.LAYER_ID
import com.vroomvroomrider.android.utils.Constants.SOURCE_ID
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    class SafeClickListener(
        private var defaultInterval: Int = 3000,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    fun Activity.hideSoftKeyboard() {
        currentFocus?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun formatStringToDate(originalTime: String, pattern: String): String {
        val form = SimpleDateFormat(DEFAULT_SERVER_TIME_FORMAT, Locale.US)
        val date: Date?
        var result = ""

        try {
            date = form.parse(originalTime)
            result = SimpleDateFormat(pattern, Locale.US).format(date!!)

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return result

    }

    fun hasLocationPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    fun requestLocationPermission(hostFragment: Fragment) {
        EasyPermissions.requestPermissions(
            hostFragment,
            "You need to accept location permissions for this app to properly work.",
            Constants.PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun createLocationRequest(activity: Activity, listener: () -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        val taskVerifyLocationSetting = client.checkLocationSettings(builder.build())

        taskVerifyLocationSetting.addOnCompleteListener {
            listener()
        }
        taskVerifyLocationSetting.addOnSuccessListener {  }

        taskVerifyLocationSetting.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity,
                        Constants.REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun NavController.safeNavigate(directions: NavDirections) {
        try {
            navigate(directions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun NavController.safeNavigate(directionsId: Int) {
        try {
            navigate(directionsId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getImageUrl(imageName: String): String {
        return "http://192.168.1.4:5000/public/images/${imageName}"
    }
}