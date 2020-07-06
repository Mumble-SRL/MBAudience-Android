package mumble.mburger.mbaudience.MBAudienceTasks

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.core.content.ContextCompat
import mumble.mburger.mbaudience.MBAudienceConstants.MBAudienceConstants
import mumble.mburger.mbaudience.MBAudienceData.MBTag
import mumble.mburger.mbaudience.MBAudienceManager
import mumble.mburger.sdk.kt.Common.MBApiManager.MBAPIManager4
import mumble.mburger.sdk.kt.Common.MBApiManager.MBApiManagerConfig
import mumble.mburger.sdk.kt.Common.MBApiManager.MBApiManagerUtils
import mumble.mburger.sdk.kt.Common.MBCommonMethods
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
        putValuesAndCall()
        if (MBApiManagerUtils.hasMapOkResults(map, false)) {
            result = MBApiManagerConfig.RESULT_OK
        } else {
            if (map!!.containsKey(MBApiManagerConfig.AM_RESULT)) {
                result = map!![MBApiManagerConfig.AM_RESULT] as Int
            } else {
                result = MBApiManagerConfig.COMMON_INTERNAL_ERROR
            }

            if (map!!.containsKey(MBApiManagerConfig.AM_ERROR)) {
                error = map!![MBApiManagerConfig.AM_ERROR] as String
            } else {
                error = MBCommonMethods.getErrorMessageFromResult(weakContext.get()!!, result)
            }
        }
        return null
    }

    override fun onPostExecute(postResult: Void?) {
    }

    fun putValuesAndCall() {
        val app_version = weakContext.get()!!.packageManager.getPackageInfo(weakContext.get()!!.packageName, 0).versionCode.toString()

        val values = ContentValues()
        values.put("device_id", MBCommonMethods.getDeviceId(weakContext.get()!!))
        values.put("platform", "android")
        values.put("push_enabled", push_enabled.toString())
        values.put("location_enabled", isLocationPermissionTaken().toString())
        values.put("locale", Locale.getDefault().language)
        values.put("app_version", app_version)
        values.put("sessions", MBAudienceManager.getSessions(weakContext.get()!!))
        values.put("sessions_time", sessions_time.toString())
        values.put("last_session", MBAudienceManager.getLastSession(weakContext.get()!!).toString())

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

        if (tags != null) {
            values.put("tags", formatTags())
        }

        map = MBAPIManager4.callApi(weakContext.get()!!, MBAudienceConstants.API_CREATE_DEVICE, values,
                MBApiManagerConfig.MODE_POST, false, false)
    }

    fun isLocationPermissionTaken(): Boolean {
        return (ContextCompat.checkSelfPermission(weakContext.get()!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(weakContext.get()!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    fun formatTags(): String {
        val jArr = JSONArray()
        for (tag in tags!!) {
            val jObj = JSONObject()
            jObj.put("key", tag.key)
            jObj.put("value", tag.value)
            jArr.put(jObj)
        }

        return jArr.toString()
    }

}
