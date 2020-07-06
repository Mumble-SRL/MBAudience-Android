package mumble.mburger.mbaudience

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.TimeUnit

class MBAudienceLifecycleListener(var context: Context) : LifecycleObserver {

    var startTime: Long = -1L

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        startTime = System.currentTimeMillis()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        var diff = System.currentTimeMillis() - startTime
        val sessionTime = TimeUnit.MILLISECONDS.toSeconds(diff)
        MBAudience.setSessionTime(context, sessionTime)
    }
}