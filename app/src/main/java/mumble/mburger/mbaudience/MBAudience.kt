package mumble.mburger.mbaudience

import android.content.Context
import androidx.fragment.app.FragmentActivity
import mumble.mburger.sdk.kt.MBPlugins.MBPlugin

class MBAudience : MBPlugin(){

    override var id: String? = "MBAudience"
    override var order: Int = -1
    override var delayInSeconds: Long = 0
    override var error: String? = null
    override var initialized: Boolean = false

    override fun init(context: Context) {
        super.init(context)
    }

    override fun doStart(activity: FragmentActivity) {
        super.doStart(activity)
        if (initialized) {
        }
    }
}