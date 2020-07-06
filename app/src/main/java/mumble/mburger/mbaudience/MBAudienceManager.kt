package mumble.mburger.mbaudience

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import mumble.mburger.mbaudience.MBAudienceConstants.MBAudienceConstants
import mumble.mburger.mbaudience.MBAudienceData.MBTag
import mumble.mburger.mbaudience.MBAudienceTasks.MBAudienceAsyncTask_AddLocation
import mumble.mburger.mbaudience.MBAudienceTasks.MBAudienceAsyncTask_Create
import mumble.mburger.sdk.kt.Common.MBCommonMethods
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

internal class MBAudienceManager {

    companion object {

        var initialized: Boolean = false

        var push_enabled = true
        var mobile_user_id: String? = null
        var custom_id: String? = null
        var userLatitude: Double = (-1).toDouble()
        var userLongitude: Double = (-1).toDouble()
        var userTags: ArrayList<MBTag>? = null

        fun init(context: Context) {
            mobile_user_id = MBCommonMethods.getSharedPreferences(context)!!.getString("mobile_user_id", null)
            custom_id = MBCommonMethods.getSharedPreferences(context)!!.getString("custom_id", null)
            userLatitude = (-1).toDouble()
            userLongitude = (-1).toDouble()
            userTags = deJsonizeTags(context)
            initialized = true
            setLastSession(context)
        }

        /**SEND DATA**/
        fun sendData(context: Context) {
            if (initialized) {
                MBAudienceAsyncTask_Create(context, push_enabled, mobile_user_id, custom_id,
                        latitude = userLatitude, longitude = userLongitude, tags = userTags).execute()

                saveData(context)
            }
        }

        fun sendPositionData(context: Context) {
            if (initialized) {
                MBAudienceAsyncTask_AddLocation(context, userLatitude, userLongitude).execute()
            }
        }

        /**SET DATA**/
        fun setLastSession(context: Context) {
            if (initialized) {
                MBCommonMethods.getSharedPreferencesEditor(context)!!.putLong(MBAudienceConstants.PROPERTY_MBAUDIENCE_LAST_SESSION,
                        System.currentTimeMillis()).apply()
            }
        }

        fun addSession(context: Context) {
            if (initialized) {
                val sessions = getSessions(context) + 1
                MBCommonMethods.getSharedPreferencesEditor(context)!!.putInt(MBAudienceConstants.PROPERTY_MBAUDIENCE_SESSIONS, sessions).apply()
            }
        }

        fun setSessionTime(context: Context, time: Long) {
            if (initialized) {
                MBAudienceAsyncTask_Create(context, push_enabled, mobile_user_id, custom_id,
                        latitude = userLatitude, longitude = userLongitude, tags = userTags, sessions_time = time).execute()
            }
        }

        fun setPushEnabled(context: Context, push_enabled: Boolean) {
            if (initialized) {
                this.push_enabled = push_enabled
                MBAudienceAsyncTask_Create(context, push_enabled, mobile_user_id, custom_id,
                        latitude = userLatitude, longitude = userLongitude, tags = userTags).execute()
            }
        }

        fun setPosition(context: Context, latitude: Double, longitude: Double) {
            if (initialized) {
                this.userLatitude = latitude
                this.userLongitude = longitude
                sendData(context)
                sendPositionData(context)
            }
        }

        /**GET DATA**/
        fun getSessions(context: Context): Int {
            if (initialized) {
                return MBCommonMethods.getSharedPreferences(context)!!.getInt(MBAudienceConstants.PROPERTY_MBAUDIENCE_SESSIONS, 0)
            }

            return -1
        }

        fun getLastSession(context: Context): Long {
            if (initialized) {
                val date = MBCommonMethods.getSharedPreferences(context)!!.getLong(MBAudienceConstants.PROPERTY_MBAUDIENCE_LAST_SESSION, -1)
                return TimeUnit.MILLISECONDS.toSeconds(date)
            }

            return -1L
        }

        /**INTERNAL SAVES & UTILS**/
        internal fun saveData(context: Context) {
            MBCommonMethods.getSharedPreferencesEditor(context)!!.putString("mobile_user_id", mobile_user_id).apply()
            MBCommonMethods.getSharedPreferencesEditor(context)!!.putString("custom_id", custom_id).apply()
            MBCommonMethods.getSharedPreferencesEditor(context)!!.putString("userTags", jsonizeTags(userTags)).apply()
        }

        internal fun jsonizeTags(userTags: ArrayList<MBTag>?): String? {
            if (userTags != null) {
                val jArr = JSONArray()
                for (tag in userTags) {
                    val jObj = JSONObject()
                    jObj.put("key", tag.key)
                    jObj.put("value", tag.value)
                    jArr.put(jObj)
                }

                return jArr.toString()
            }

            return null
        }

        internal fun deJsonizeTags(context: Context): ArrayList<MBTag>? {
            val sUserTags = MBCommonMethods.getSharedPreferences(context)!!.getString("userTags", null)
            if (sUserTags != null) {
                val tags = ArrayList<MBTag>()
                val jTags = JSONArray(sUserTags)
                for (i in 0 until jTags.length()) {
                    val jObj = jTags.getJSONObject(i)
                    tags.add(MBTag(jObj.getString("key"), jObj.getString("value")))
                }

                return tags
            }

            return null
        }

        fun isLocationPermissionTaken(context: Context): Boolean {
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        }
    }
}