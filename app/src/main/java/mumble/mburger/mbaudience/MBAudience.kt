package mumble.mburger.mbaudience

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ProcessLifecycleOwner
import mumble.mburger.sdk.kt.Common.MBCommonMethods
import mumble.mburger.sdk.kt.MBPlugins.MBPlugin

class MBAudience : MBPlugin() {

    override var id: String? = "MBAudience"
    override var order: Int = -1
    override var delayInSeconds: Long = 0
    override var error: String? = null
    override var initialized: Boolean = false

    override fun init(context: Context) {
        super.init(context)
        MBAudienceManager.init()
        MBAudienceManager.addSession(context)

        ProcessLifecycleOwner.get().lifecycle
                .addObserver(MBAudienceLifecycleListener(context))

        initialized = true
    }

    override fun doStart(activity: FragmentActivity) {
        super.doStart(activity)
        if (initialized) {
            MBAudienceManager.sendInitialData(activity.applicationContext)
        }
    }

    companion object {
        fun setCustomID(context: Context, custom_id: String) {
            MBAudienceManager.custom_id = custom_id
            MBAudienceManager.sendData(context)
        }

        fun removeCustomID(context: Context) {
            MBAudienceManager.custom_id = null
            MBAudienceManager.sendData(context)
        }

        fun setMobileUserId(context: Context) {
            MBAudienceManager.mobile_user_id = MBCommonMethods.getAccessToken(context)
            MBAudienceManager.sendData(context)
        }

        fun removeMobileUserId(context: Context) {
            MBAudienceManager.mobile_user_id = null
            MBAudienceManager.sendData(context)
        }

        fun setSessionTime(context: Context, sessionTime: Long) {
            MBAudienceManager.setSessionTime(context, sessionTime)
        }
    }
}