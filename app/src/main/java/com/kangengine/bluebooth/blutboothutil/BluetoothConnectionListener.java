package com.kangengine.bluebooth.blutboothutil;


public interface BluetoothConnectionListener {

   void onDeviceConnected();

   void onDeviceDisconnected();

   void onDeviceConnectionFailed();
}
