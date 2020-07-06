package mumble.mburger.mbaudience

import android.content.Context
import mumble.mburger.mbaudience.MBAudienceConstants.MBAudienceConstants
import mumble.mburger.mbaudience.MBAudienceData.MBTag
import mumble.mburger.mbaudience.MBAudienceTasks.MBAudienceAsyncTask_Create
import mumble.mburger.sdk.kt.Common.MBCommonMethods
import java.util.concurrent.TimeUnit

internal class MBAudienceManager {

    companion object {

        var push_enabled = true
        var mobile_user_id: String? = null
        var custom_id: String? = null
        var userLatitude: Double = (-1).toDouble()
        var userLongitude: Double = (-1).toDouble()
        var userTags: ArrayList<MBTag>? = null

        fun init() {
            mobile_user_id = null
            custom_id = null
            userLatitude = (-1).toDouble()
            userLongitude = (-1).toDouble()
            userTags = null
        }

        fun setLastSession(context: Context) {
            MBCommonMethods.getSharedPreferencesEditor(context)!!.putLong(MBAudienceConstants.PROPERTY_MBAUDIENCE_SESSIONS, System.currentTimeMillis()).apply()
        }

        fun addSession(context: Context) {
            val sessions = getSessions(context) + 1
            MBCommonMethods.getSharedPreferencesEditor(context)!!.putInt(MBAudienceConstants.PROPERTY_MBAUDIENCE_SESSIONS, sessions).apply()
        }

        fun getSessions(context: Context): Int {
            return MBCommonMethods.getSharedPreferences(context)!!.getInt(MBAudienceConstants.PROPERTY_MBAUDIENCE_SESSIONS, 0)
        }

        fun getLastSession(context: Context): Long {
            val date = MBCommonMethods.getSharedPreferences(context)!!.getLong(MBAudienceConstants.PROPERTY_MBAUDIENCE_LAST_SESSION, -1)
            return TimeUnit.MILLISECONDS.toSeconds(date)
        }

        fun sendInitialData(context: Context) {
            MBAudienceAsyncTask_Create(context).execute()
        }

        fun setSessionTime(context: Context, time: Long) {
            MBAudienceAsyncTask_Create(context, push_enabled, mobile_user_id, custom_id, latitude = userLatitude, longitude = userLongitude,
                    tags = userTags, sessions_time = time).execute()
        }

        fun sendData(context: Context) {
            MBAudienceAsyncTask_Create(context, push_enabled, mobile_user_id, custom_id, latitude = userLatitude, longitude = userLongitude,
                    tags = userTags).execute()
        }
    }
}