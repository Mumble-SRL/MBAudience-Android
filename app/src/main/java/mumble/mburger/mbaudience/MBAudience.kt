package mumble.mburger.mbaudience

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.location.*
import mumble.mburger.mbaudience.MBAudienceData.MBTag
import mumble.mburger.sdk.kt.MBPlugins.MBPlugin
import mumble.mburger.sdk.kt.MBPlugins.MBPluginsManager

class MBAudience : MBPlugin() {

    override var id: String? = "MBAudience"
    override var order: Int = -1
    override var delayInSeconds: Long = 0
    override var error: String? = null
    override var initialized: Boolean = false

    var initListener: MBAudiencePluginInitialized? = null

    /**
     * Initialization of the plugin
     */
    override fun init(context: Context) {
        super.init(context)
        MBAudienceManager.init(context)
        MBAudienceManager.addSession(context)

        ProcessLifecycleOwner.get().lifecycle
                .addObserver(MBAudienceLifecycleListener(context))

        initialized = true
        isAutomationConnected = false
        initListener?.onMBAudienceInitialized()
        locationAddedListener = null
    }

    /**
     * First start of the plugin
     */
    override fun doStart(activity: FragmentActivity) {
        super.doStart(activity)
        if (initialized) {
            MBAudienceManager.sendData(activity.applicationContext)
        }
    }

    companion object {
        var locationAddedListener: MBAudienceLocationAdded? = null

        var locationClient: FusedLocationProviderClient? = null
        var locationCallback: LocationCallback? = null

        var isAutomationConnected = false

        /**
         * Set the start date of the last session
         */
        internal fun setLastSession(context: Context) {
            MBAudienceManager.setLastSession(context)
        }

        /**
         * Set if user has push notification enabled
         */
        fun setPushEnabled(context: Context, push_enabled: Boolean) {
            MBAudienceManager.push_enabled = push_enabled
            MBAudienceManager.sendData(context)
        }

        /**
         * Set a custom ID
         */
        fun setCustomID(context: Context, custom_id: String) {
            MBAudienceManager.custom_id = custom_id
            MBAudienceManager.sendData(context)
        }

        /**
         * Clear custom ID
         */
        fun removeCustomID(context: Context) {
            MBAudienceManager.custom_id = null
            MBAudienceManager.sendData(context)
        }

        /**
         * Get CustomID set previously, or return null
         */
        fun getCustomID(): String? {
            return MBAudienceManager.custom_id
        }

        /**
         * Set the user_id connected with Auth MBurger
         */
        fun setMobileUserId(context: Context, mobile_user_id: String) {
            MBAudienceManager.mobile_user_id = mobile_user_id
            MBAudienceManager.sendData(context)
        }

        /**
         * Clear the user_id connected with Auth MBurger
         */
        fun removeMobileUserId(context: Context) {
            MBAudienceManager.mobile_user_id = null
            MBAudienceManager.sendData(context)
        }

        /**
         * Get the current mobile_user_id
         */
        fun getMobileUserId(): String? {
            return MBAudienceManager.mobile_user_id
        }

        /**
         * Set session time from foreground to background
         */
        fun setSessionTime(context: Context, sessionTime: Long) {
            MBAudienceManager.setSessionTime(context, sessionTime)
        }

        /**
         * Set user position (needs permission)
         */
        fun setPosition(context: Context, latitude: Double, longitude: Double) {
            MBAudienceManager.setPosition(context, latitude, longitude)
            locationAddedListener?.onMBLocationAdded(latitude, longitude)
        }

        /**
         * Add/update a single tag
         */
        fun addTag(context: Context, key: String, value: String) {
            if (MBAudienceManager.userTags == null) {
                MBAudienceManager.userTags = ArrayList()
            }

            var found = false
            val curTags = MBAudienceManager.userTags!!
            for (i in 0 until curTags.size) {
                val tag = curTags[i]
                if (tag.key == key) {
                    tag.value = value
                    found = true
                    break
                }
            }

            if (!found) {
                MBAudienceManager.userTags?.add(MBTag(key, value))
            }

            MBPluginsManager.tagChanged(key, value)
            MBAudienceManager.sendData(context)
        }

        /**
         * Add/update an array of tags
         */
        fun addTags(context: Context, tags: ArrayList<MBTag>) {
            if (MBAudienceManager.userTags == null) {
                MBAudienceManager.userTags = ArrayList()
            }

            val curTags = MBAudienceManager.userTags!!
            for (tag in tags) {
                var found = false
                for (i in 0 until curTags.size) {
                    val curTag = curTags[i]
                    if (tag.key == curTag.key) {
                        curTag.value = tag.value
                        found = true
                        break
                    }
                }

                if (!found) {
                    MBAudienceManager.userTags?.add(tag)
                }
            }

            for (i in 0 until curTags.size) {
                val curTag = curTags[i]
                MBPluginsManager.tagChanged(curTag.key, curTag.value)
            }

            MBAudienceManager.userTags!!.addAll(tags)
            MBAudienceManager.sendData(context)
        }

        /**
         * Remove a tag given the key
         */
        fun removeTag(context: Context, key: String) {
            if (MBAudienceManager.userTags != null) {
                val tags = MBAudienceManager.userTags!!
                for (i in 0 until tags.size) {
                    val tag = tags[i]
                    if (tag.key == key) {
                        tags.removeAt(i)
                        break
                    }
                }
            }

            MBPluginsManager.tagRemoved(key)
            MBAudienceManager.sendData(context)
        }

        /**
         * Remove all tags
         */
        fun clearTags(context: Context) {
            if (MBAudienceManager.userTags != null) {
                val tags = MBAudienceManager.userTags!!
                for (i in 0 until tags.size) {
                    val tag = tags[i]
                    MBPluginsManager.tagRemoved(tag.key)
                }
            }

            MBAudienceManager.userTags = null
            MBAudienceManager.sendData(context)
        }

        /**
         * Start location updates request, is killed when app goes background
         */
        fun startLocationUpdates(context: Context) {
            if ((locationCallback == null) || (locationClient == null)) {
                locationClient = LocationServices.getFusedLocationProviderClient(context)

                val mLocationRequest = LocationRequest.create()
                mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                mLocationRequest.setExpirationDuration(4000)

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        if (locationResult != null) {
                            var mostAccurate: Location? = null
                            for (location in locationResult.locations) {
                                if (mostAccurate == null) {
                                    mostAccurate = location
                                } else {
                                    if (location.accuracy < mostAccurate.accuracy) {
                                        mostAccurate = location
                                    }
                                }
                            }

                            if (mostAccurate != null) {
                                MBAudienceManager.setPosition(context, mostAccurate.latitude, mostAccurate.longitude)
                                locationAddedListener?.onMBLocationAdded(mostAccurate.latitude, mostAccurate.longitude)
                            }
                        }
                    }
                }

                if ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    locationClient?.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper())
                }
            }
        }

        /**
         * Stop location updates
         */
        fun stopLocationUpdates() {
            locationClient?.removeLocationUpdates(locationCallback)
            locationClient = null
            locationCallback = null
        }
    }
}