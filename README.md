# MBAudienceAndroid

## Installation

### Installation with gradle

This plugin <u>only works with the latest Kotlin version of MBurger Client SDK</u> so make sure to ad Kotlin Android Studio plugin and Kotlin dependencies to your Android project.

Add this repository to your project level `build.gradle` file under allprojects:

```
maven { url "https://dl.bintray.com/mumbleideas/MBurger-Android/" }
```

Then add **MBurger Kotlin** dependency to your `app build.gradle` file:

```
implementation 'mumble.mburger:android_kt:0.5.0'
```

Lastly add `MBMessages` library:

```
implementation 'mumble.mburger:mbaudience-android:0.1.0'
```
