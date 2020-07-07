package mumble.mburger.mbaudience.MBAudienceTasks

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import mumble.mburger.mbaudience.MBAudienceConstants.MBAudienceConstants
import mumble.mburger.mbaudience.MBAudienceData.MBTag
import mumble.mburger.mbaudience.MBAudienceManager
import mumble.mburger.sdk.kt.Common.MBApiManager.MBAPIManager4
import mumble.mburger.sdk.kt.Common.MBApiManager.MBApiManagerConfig
import mumble.mburger.sdk.kt.Common.MBApiManager.MBApiManagerUtils
import mumble.mburger.sdk.kt.Common.MBCommonMethods
import org.apache.commons.text.StringEscapeUtils
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*

/**
 * Task to create/update user
 *
 * @author Enrico Ori
 * @version {@value MBIAMConstants#version}
 */
internal class MBAudienceAsyncTask_Create : AsyncTask<Void, Void, Void> {

    /**
     * Context reference used to send data to Activity/Fragment
     */
    var weakContext: WeakReference<Context>

    var push_enabled = true
    var mobile_user_id: String? = null
    var custom_id: String? = null
    var sessions_time = -1L
    var latitude = (-1).toDouble()
    var longitude = (-1).toDouble()
    var tags: ArrayList<MBTag>? = null

    private var result = MBApiManagerConfig.COMMON_INTERNAL_ERROR
    private var error: String? = null
    private var map: MutableMap<String, Any?>? = null

    constructor(context: Context, push_enabled: Boolean = false, mobile_user_id: String? = null, custom_id: String? = null,
                sessions_time: Long = -1L, latitude: Double = (-1).toDouble(), longitude: Double = (-1).toDouble(), tags: ArrayList<MBTag>? = null) {

        this.weakContext = WeakReference(context)
        this.push_enabled = push_enabled
        this.mobile_user_id = mobile_user_id
        this.custom_id = custom_id
        this.sessions_time = sessions_time
        this.latitude = latitude
        this.longitude = longitude
        this.tags = tags
    }

    override fun doInBackground(vararg params: Void?): Void? {
        if (weakContext.get() != null) {
            putValuesAndCall()
            if (MBApiManagerUtils.hasMapOkResults(map, false)) {
                result = MBApiManagerConfig.RESULT_OK
            } else {
                result = if (map!!.containsKey(MBApiManagerConfig.AM_RESULT)) {
                    map!![MBApiManagerConfig.AM_RESULT] as Int
                } else {
                    MBApiManagerConfig.COMMON_INTERNAL_ERROR
                }

                error = if (map!!.containsKey(MBApiManagerConfig.AM_ERROR)) {
                    map!![MBApiManagerConfig.AM_ERROR] as String
                } else {
                    MBCommonMethods.getErrorMessageFromResult(weakContext.get()!!, result)
                }
            }
        }

        return null
    }

    override fun onPostExecute(postResult: Void?) {
    }

    fun putValuesAndCall() {
        val device_id = MBCommonMethods.getDeviceId(weakContext.get()!!)
        val platform = "android"
        val sPush_enabled = push_enabled.toString()
        val locationPermission = MBAudienceManager.isLocationPermissionTaken(weakContext.get()!!).toString()
        val locale = Locale.getDefault().language
        val app_version = weakContext.get()!!.packageManager.getPackageInfo(weakContext.get()!!.packageName, 0).versionCode.toString()
        val nSessions = MBAudienceManager.getSessions(weakContext.get()!!).toString()
        val sSession_time = if (sessions_time == -1L) {
            "0"
        } else {
            sessions_time.toString()
        }

        val last_session = MBAudienceManager.getLastSession(weakContext.get()!!).toString()

        if (!tags.isNullOrEmpty()) {
            val builder = StringBuilder("{")
            builder.append("\"device_id\":\"$device_id\",")
            builder.append("\"platform\":\"$platform\",")
            builder.append("\"push_enabled\":\"$sPush_enabled\",")
            builder.append("\"location_enabled\":\"$locationPermission\",")
            builder.append("\"locale\":\"$locale\",")
            builder.append("\"app_version\":\"$app_version\",")
            builder.append("\"sessions\":\"$nSessions\",")
            builder.append("\"sessions_time\":\"$sSession_time\",")
            builder.append("\"last_session\":\"$last_session\"")

            if (mobile_user_id != null) {
                val escaped_mobile_user_id = StringEscapeUtils.escapeJson(mobile_user_id)

                builder.append(",")
                builder.append("\"mobile_user_id\":\"$escaped_mobile_user_id\"")
            }

            if ((latitude != (-1).toDouble()) && (longitude != (-1).toDouble())) {
                builder.append(",")
                builder.append("\"latitude\":\"$latitude\",")
                builder.append("\"longitude\":\"$longitude\"")
            }

            if (custom_id != null) {
                val escaped_custom_id = StringEscapeUtils.escapeJson(custom_id)

                builder.append(",")
                builder.append("\"custom_id\":\"$escaped_custom_id\"")
            }

            builder.append(",\"tags\": ${formatTags()}")

            builder.append("}")

            map = MBAPIManager4.callApi(weakContext.get()!!, MBAudienceConstants.API_CREATE_DEVICE, ContentValues(),
                    MBApiManagerConfig.MODE_POST, false, false, dataString = builder.toString())
        } else {
            val values = ContentValues()
            values.put("device_id", device_id)
            values.put("platform", "android")
            values.put("push_enabled", sPush_enabled)
            values.put("location_enabled", locationPermission)
            values.put("locale", locale)
            values.put("app_version", app_version)
            values.put("sessions", nSessions)
            values.put("sessions_time", sSession_time)
            values.put("last_session", last_session)

            if (mobile_user_id != null) {
                values.put("mobile_user_id", mobile_user_id.toString())
            }

            if ((latitude != (-1).toDouble()) && (longitude != (-1).toDouble())) {
                values.put("latitude", latitude.toString())
                values.put("longitude", longitude.toString())
            }

            if (custom_id != null) {
                values.put("custom_id", custom_id.toString())
            }

            map = MBAPIManager4.callApi(weakContext.get()!!, MBAudienceConstants.API_CREATE_DEVICE, values,
                    MBApiManagerConfig.MODE_POST, false, false)
        }
    }

    fun formatTags(): JSONArray {
        val jArr = JSONArray()
        for (tag in tags!!) {
            val jObj = JSONObject()
            jObj.put("tag", StringEscapeUtils.escapeJson(tag.key))
            jObj.put("value", StringEscapeUtils.escapeJson(tag.value))
            jArr.put(jObj)
        }

        return jArr
    }
}