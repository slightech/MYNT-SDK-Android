###1. The file directory

   This set of Mynt - SDK contains, the use of the API documentation, dependent jar, functional demo demo.
   
   * Doc directory ask API documentation, need a browser open index. The HTML document browsing queries.
   * The jars had what it took to the SDK directory jars, ble SDK, and mynt SDK. The jar to use need, develop application to join the project under the   libs
   * The mynt - demo for the SDK - demo function demonstration, demo project the android - studio building.

###2. The demo shows  

  Demonstrate the demo using android - studio building, using the provided ble SDK, mynt SDK jar complete scan on the equipments, connection, and power, matching, signal read read, alarm bell ring function, click event presentation accordingly.

 * In the project need to add bluetooth permissions:
       <uses-permission android:name="android.permission.BLUETOOTH" />
       <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * For the dependent jar in libs
 * Use MyntManager MyMyntManger integration, project, and use the singleton implementation MyMyntManger project is the only guarantee a scan and      connection matching is a MyntManager function operation.


  * MainActivity demonstrates through research startSearch open scanning, stopSearch stop scanning, and add FoundCallback callback handling found equipment. Click on the specified device
Enter the equipment function demo page. Function demo page has two activity, activity 1: MyntCallBackDemoActivity demonstration by setting the MyntManager callback interface to complete the relevant equipment connection, matching, etc.
Activity 2: MyntListenerCallBackDemoActivity demonstrate how to achieve by MyntListner device to connect, matching, etc.
   