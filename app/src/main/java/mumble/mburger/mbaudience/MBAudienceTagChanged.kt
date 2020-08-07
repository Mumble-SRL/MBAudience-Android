package mumble.mburger.mbaudience

/**Listener for plugin initialization**/
interface MBAudienceTagChanged {
    fun onMBAudienceTagChanged(tag: String, value: String)
}