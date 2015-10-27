###一、文件目录说明

  本套Mynt-sdk包含，使用api文档、依赖jar、功能演示demo。
  
  * doc 目录问API文档，需用浏览器打开index.html进行文档浏览查询。

  * jars 目录下有sdk所需要的jar包，ble-sdk、和mynt-sdk.jar 使用中都需要用到，开发应用时需加入项目libs下面。

  * mynt-demo为 sdk-demo 功能演示，演示项目采用anroid-studio 构建。

###二、演示demo说明

  演示demo使用android-studio构建，使用提供的ble-sdk、mynt-sdk jar完成对设备的扫描、连接、配对、信号读取、电量读取、报警响铃功能、点击事件相应演示。
  
 * 在项目中需要加入如下蓝牙操作权限
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

 * libs中为依赖jar。

 * 项目使用MyMyntManger 集成 MyntManager ,并且使用单例实现MyMyntManger项目唯一保证扫描和连接配对都是一个MyntManager 对功能操作。
   在实例初始化时，重新设置了扫描参数，如果不重新，则mynt-sdk使用系统默认扫描参数。

 * MainActivity 中演示了通过调研startSearch开启扫描，stopSearch停止扫描，并且添加FoundCallback 回调处理发现设备。点击指定设备会
    进入设备功能演示页面。功能演示页面有两个页面，页面1：MyntCallBackDemoActivity 演示通过设置MyntManager 相关回调接口完成设备连接、配对等功能。
    页面2：MyntListenerCallBackDemoActivity演示怎么通过MyntListner 来实现设备连接、配对等功能。



       
  