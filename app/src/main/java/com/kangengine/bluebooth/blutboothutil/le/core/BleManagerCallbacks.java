package com.kangengine.bluebooth.blutboothutil.le.core;


public interface BleManagerCallbacks {

   void onDeviceConnected();

   void onDeviceDisconnected();

   void onDeviceConnecting();

   void onLinklossOccur();

   void onDeviceReady();

   void onBatteryValueReceived(int var1);
}
