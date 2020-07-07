package mumble.mburger.mbaudience.MBAudienceTasks

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import mumble.mburger.mbaudience.MBAudienceConstants.MBAudienceConstants
import mumble.mburger.sdk.kt.Common.MBApiManager.MBAPIManager4
import mumble.mburger.sdk.kt.Common.MBApiManager.MBApiManagerConfig
import mumble.mburger.sdk.kt.Common.MBApiManager.MBApiManagerUtils
import mumble.mburger.sdk.kt.Common.MBCommonMethods
import java.lang.ref.WeakReference

/**
 * Task to add position to user
 *
 * @author Enrico Ori
 * @version {@value MBIAMConstants#version}
 */
internal class MBAudienceAsyncTask_AddLocation : AsyncTask<Void, Void, Void> {

    /**
     * Context reference used to send data to Activity/Fragment
     */
    var weakContext: WeakReference<Context>

    var latitude = (-1).toDouble()
    var longitude = (-1).toDouble()

    private var result = MBApiManagerConfig.COMMON_INTERNAL_ERROR
    private var error: String? = null
    private var map: MutableMap<String, Any?>? = null

    constructor(context: Context, latitude: Double, longitude: Double) {
        this.weakContext = WeakReference(context)
        this.latitude = latitude
        this.longitude = longitude
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
        val values = ContentValues()
        values.put("device_id", MBCommonMethods.getDeviceId(weakContext.get()!!))
        values.put("latitude", latitude.toString())
        values.put("longitude", longitude.toString())

        map = MBAPIManager4.callApi(weakContext.get()!!, MBAudienceConstants.API_ADD_LOCATION, values,
                MBApiManagerConfig.MODE_POST, false, false)
    }
}