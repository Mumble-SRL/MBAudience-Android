# MBAudienceAndroid

## Installation

### Installation with gradle

This plugin <u>only works with the latest Kotlin version of MBurger Client SDK</u> so make sure to ad Kotlin Android Studio plugin and Kotlin dependencies to your Android project.

Add this repository to your project level `build.gradle` file under `allprojects`:

```
maven { url "https://dl.bintray.com/mumbleideas/MBurger-Android/" }
```

Then add **MBurger Kotlin** dependency to your `app build.gradle` file:

```
implementation 'mumble.mburger:android_kt:0.5.0'
```

Lastly add `MBAudience` library:

```
implementation 'mumble.mburger:mbaudience-android:0.2.4'
```



### Initialization

To initialize MBMessages you need to add `MBMessages` to the array of plugins of `MBurger`.

```kotlin
val plugins = ArrayList<MBPlugin>()
val plugin = MBAudience()
plugins.add(plugin)

MBurger.initialize(applicationContext, "MBURGER_KEY", false, plugins)
```

Then you need to say MBurger to initialize the plugins with the in initPlugins method from MBurger class. If you wish you can add a listener to know when the plugin have been initialized.

```kotlin
val listener: MBAudiencePluginInitialized
plugin.initListener = listener

override fun onMBAudienceInitialized() {
   //Plugin has been initialized correctly, can be started
}
```

Once you've done this ask MBurger to start plugins in your main activity (be aware that it has to be an `AppCompatActivity`), then, in app messages will be fetched automatically and showed, if they need to be showed.

```kotlin
MBurger.startPlugins(activity)
```



# Tracked data

Below are described all the data that are tracked by the MBAudience SDK and that you will be able to use from the [MBurger](https://mburger.cloud/) dashboard. Most of the data are tracked automatically, for a couples a little setup by the app is neccessary.

- **app_version**: The current version of the app (version code).
- **locale**: The locale of the phone, the value returned by `Locale.getDefault().language`.
- **sessions**: An incremental number indicating the number of time the user opens the app, this number is incremented at each startup.
- **sessions_time**: The total time the user has been on the app, tracked by a custom implementation of the `LifecycleObserver`. This time is paused when the app goes in background (using `onMoveToBackground`) .and it's resumed when the app return active (using `onMoveToForeground`).
- **last_session**: The start date of the last session.
- **push_enabled**: If push notifications are enabled or not; must be set manually with: `MBAudience.setPushEnabled(context: Context, push_enabled: Boolean)`, the default value is `true`
- **location_enabled**: If user has given permissions to use location data or not; this is true if `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION` is enabled while app in foreground.
- **mobile_user_id**: The user id of the user curently logged in MBurger.
- **custom_id**: A custom id that can be used to filter further.
- **tags**: An array of tags.
- **latitude, longitude**: The latitude and longitude of the last place visited by this device.



## Tags

You can set tags to assign to a user/device (e.g. if user has done an action set a tag), so you can target those users later:

To set a tag or a group of tags:

```kotlin
MBAudience.addTag(context:Context, key: String, value: String)
MBAudience.addTags(context:Context, tags: ArrayList<MBTag>)
```

To remove it or clear all:

```kotlin
MBAudience.removeTag(context:Context, key: String)
MBAudience.clearTags(context:Context)
```



## Custom Id

You can set a custom id in order to track/target users with id coming from different platforms.

To set a custom id:

```kotlin
MBAudience.setCustomID(context: Context, custom_id: String)
```

To remove it:

```kotlin
MBAudience.removeCustomID(context: Context)
```

To retrieve the current saved id:

```kotlin
MBAudience.getCustomID()
```



## Mobile User Id

This is the id of the user currently logged in MBurger using MBAuth. At the moment the mobile user id is **not sent automatically** when a user log in/log out with MBAuth. It will be implemented in the future but at the moment you have to set and remove it manually when the user completes the login flow and when he logs out.

To set the mobile user id:

```kotlin
MBAudience.setMobileUserId(context: Context, mobile_user_id: String)
```

To remove it, if the user logs out:

```kotlin
MBAudience.removeMobileUserId(context: Context)
```

To get the currently saved mobile user id:

```kotlin
MBAudience.getMobileUserId()
```



## Location Data

MBAudience let you track and target user based on their location, the framework uses a foreground `FusedLocationProviderClient` with priority `PRIORITY_BALANCED_POWER_ACCURACY` which **is killed** the moment the app goes in background:

```kotlin
MBAudience.startLocationUpdates(context: Context)
```

To stop monitoring location changes you have to call:

```kotlin
MBAudience.stopLocationUpdates()
```

If you wish to track user position while app is in background you need to **implement your own location service**, then when you have a new location you can use this API to send it to the framework:

```kotlin
MBAudience.setPosition(context: Context, latitude: Double, longitude: Double)
```