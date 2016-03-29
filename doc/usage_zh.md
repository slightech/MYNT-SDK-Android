
# MYNT SDK 使用手册


## 1) 要求

**需要声明的权限：**

    <!-- Require to using BLE (Bluetooth low energy) -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

    <!-- Require if support uploading nearby devices to the anti-lost network -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />


## 2) 如何控制小觅设备

实例化`MyntManager`进行操作即可。分为搜索和控制两步：

* `startSearch`进行搜索，通过`FoundCallback`获得搜索到的小觅设备。
* `connect`进行连接，通过`PairCallback`监听连接配对、通过`EventCallback`监听设备事件。


## 3) 如何为防丢网络贡献一份力量

在`Application`中，启用搜索即可：

    public class MyApplication extends Application {

        @Override
        public void onCreate() {
            super.onCreate();
            // 搜索小觅，即会上报附近发现设备到防丢网络
            new MyntManager(this).startSearch();
        }

        //...
    }

然后，当蓝牙开启时，即会搜索附近的小觅并上报到防丢网络。

`MyntParams`可设置搜索间隔等，`Nearby`功能则提供了更多支持。


## 4) ProGuard混淆规则

    -keep class com.slighetch.** { *; }
