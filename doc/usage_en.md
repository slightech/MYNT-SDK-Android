
# The MYNT SDK Usage

## 1) Requirements

**Permissions should be declared in `AndroidManifest.xml`:**

    <!-- Require to using BLE (Bluetooth low energy) -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

    <!-- Require if support uploading nearby devices to the anti-lost network -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

**`build.gradle` if target Android 6.0 (API level 23) or higher：**

    android {
        <!-- Require Apache HTTP client APIs -->
        useLibrary 'org.apache.http.legacy'

        <!-- If target API 23 or higher, only when the bluetooth and location are both on can the devices be scanned. However, lower the API 23, only bluetooth is required on. -->
        defaultConfig {
            targetSdkVersion 22
        }
    }

Reference: [Android 6.0 Changes](http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html)


## 2) How to control the MYNTs

Instantiate `MyntManager` class then using it to search and control MYNTs.

* Call `startSearch` to search the MYNTs, and get the result from the `FoundCallback`.
* Call `connect` to connect the MYNTs, then control them with `MyntManager`.
    - Set `PairCallback` to listen the connect process.
    - Set `EventCallback` to listen the device events.


## 3) How to contribute the anti-lost network

Using `MyntManager` to start search in your `Application`:

    public class MyApplication extends Application {

        @Override
        public void onCreate() {
            super.onCreate();
            // Searching MYNTs here could help people who lost the things.
            new MyntManager(this).startSearch();
        }

        //...
    }

Then will upload nearby MYNTs to the anti-lost network when the bluetooth is available.

* Set the `MyntParams` to change the search internal etc.
* Get the `Nearby` feature to bind location provider or upload by yourself.


## 4) About ProGuard rules

    -keep class com.slighetch.** { *; }
