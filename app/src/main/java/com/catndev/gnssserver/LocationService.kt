package com.catndev.gnssserver

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.core.app.ActivityCompat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round
import kotlin.system.exitProcess


interface LocationCallbacks {
    fun satInfo(satellitesInFix: Int, satList: List<LocationService.Satellite>)
    fun setTTFF(ttffMillis: Int)
    fun setSats(data: ArrayList<LocationService.Satellite>)
    fun setUTC(date: Date)
}

class LocationService: Service(), LocationListener {

    inner class Satellite {

        var azimuth: Float = .0f
        var carrierBand: String = "??"
        var cn0: Float = .0f
        var constellationType: Int = 0
        var elevation: Float = .0f
        var id: Int = 0
        var inFix: Boolean = false

    }

    var isRunning = false
        private set
    private var mGnssStatusCallback: GnssStatus.Callback
    private lateinit var mLocationManager: LocationManager
    private val mBinder: IBinder = ServiceBinder()
    private var mContext: Context? = null
    private var mLocationCallbacks: LocationCallbacks? = null

    inner class ServiceBinder: Binder() {
        fun getService(): LocationService {
            return this@LocationService
        }
    }

    init {
        mGnssStatusCallback = object : GnssStatus.Callback() {
            /**
             * Called when GNSS system has started.
             */
            override fun onStarted() {}

            /**
             * Called when GNSS system has stopped.
             */
            override fun onStopped() {}

            /**
             * Called when the GNSS system has received its first fix since starting.
             *
             * @param ttffMillis the time from start to first fix in milliseconds.
             */
            override fun onFirstFix(ttffMillis: Int) {
                mLocationCallbacks?.setTTFF(ttffMillis)
            }

            /**
             * Called periodically to report GNSS satellite status.
             *
             * @param status the current status of all satellites.
             */
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                if (status.satelliteCount == 0) return
                var inFixCount = 0
                val satellitesList = ArrayList<Satellite>(status.satelliteCount)
                for (i in 0 until status.satelliteCount) {
                    val satellite = Satellite()
                    satellite.azimuth = status.getAzimuthDegrees(i)
                    satellite.constellationType = status.getConstellationType(i)
                    satellite.elevation = status.getElevationDegrees(i)
                    satellite.id = status.getSvid(i)
                    satellite.cn0 = status.getCn0DbHz(i)
                    if (status.hasCarrierFrequencyHz(i)) {
                        satellite.carrierBand = getCarrierBand(satellite.constellationType, status.getCarrierFrequencyHz(i))
                    }
                    if (status.usedInFix(i)) {
                        satellite.inFix = true
                        inFixCount++
                    }
                    satellitesList.add(satellite)
                }

                mLocationCallbacks?.satInfo(inFixCount, satellitesList)
                mLocationCallbacks?.setSats(satellitesList)
            }
        }
    }


    fun getCarrierBand(constellationType: Int, carrier: Float): String{
        val frequency = BigDecimal(carrier * 0.000001).setScale(4, RoundingMode.HALF_EVEN).toDouble()
        when (constellationType) {
            GnssStatus.CONSTELLATION_GPS -> {
                when (frequency) {
                    1575.42 -> return "L1"
                    1227.60 -> return "L2"
                    1176.45 -> return "L5"
                }
            }
            GnssStatus.CONSTELLATION_GLONASS -> {
                if ((1598.0624 < frequency) && (frequency < 1609.3125)) return "L1"
                else if ((1242.9374 < frequency) && (frequency < 1251.6876)) return "L2"
                else if (frequency == 1202.025) return "L3"
            }
            GnssStatus.CONSTELLATION_GALILEO -> {
                when (frequency) {
                    1575.42 -> return "E1"
                    1176.45 -> return "E5a"
                    1207.14 -> return "E5b"
                    1191.795 -> return "E5 AltBOC"
                    1278.75 -> return  "E6"
                }
            }
            GnssStatus.CONSTELLATION_BEIDOU -> {
                when (frequency) {
                    1561.098 -> return "B1I"
                    1268.52 -> return "B3I"
                    1575.42 -> return "B1C"
                    1176.45 -> return "B2a"
                    1207.14 -> return "B2b"
                }
            }
            GnssStatus.CONSTELLATION_IRNSS -> {
                when (frequency) {
                    1176.45 -> return "L5"
                }
            }
            GnssStatus.CONSTELLATION_SBAS -> {
                when (frequency) {
                    1575.42 -> return "L1"
                    1176.45 -> return "L5"
                }
            }
            GnssStatus.CONSTELLATION_QZSS -> {
                when (frequency) {
                    1575.42 -> return "L1"
                    1227.60 -> return "L2"
                    1176.45 -> return "L5"
                    1278.75 -> return "L6"
                }
            }
            GnssStatus.CONSTELLATION_UNKNOWN -> {
                when (frequency) {
                    1575.42 -> return "L1"
                    1227.60 -> return "L2"
                    1176.45 -> return "L5"
                }
            }
        }
        return "??"
    }
// TODO: Уведомления
    private fun mCreateNotification() {
        val notificationBuilder = Notification.Builder(this, "GServer channel")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onLocationChanged(location: Location) {
        mLocationCallbacks!!.setUTC(Date(location.time))
        location.latitude
        location.longitude
        location.altitude
        location.speed
        location.bearing
        location.accuracy
        location.bearingAccuracyDegrees
        location.speedAccuracyMetersPerSecond
        location.verticalAccuracyMeters
        location.accuracy

        //TODO("Not yet implemented")
    }


    override fun onCreate() {
        super.onCreate()
    }

    fun setCallbacks(locationCallbacks: LocationCallbacks?) {
        if (locationCallbacks != null) {
            mLocationCallbacks = locationCallbacks
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isSpeedRequired = true
        criteria.isAltitudeRequired = true
        criteria.isBearingRequired = true
        criteria.isCostAllowed = true
        criteria.powerRequirement = Criteria.POWER_HIGH
        val bestProvider = mLocationManager.getBestProvider(criteria, true)
        if (bestProvider != null && bestProvider.isNotEmpty()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                exitProcess(0)
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this)
        }
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback, Handler(mainLooper))
        isRunning = true
        return START_STICKY
    }


    fun setContext(context: Context) {
        mContext = context
    }
}