package mumble.mburger.mbaudience

/**Listener for plugin initialization**/
interface MBAudienceLocationAdded {
    fun onMBLocationAdded(latitude:Double, longitude:Double)
}