package com.kangengine.bluebooth.blutboothutil.le.device.hts;


import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface HTSManagerCallbacks extends BleManagerCallbacks {

   void onHTValueReceived(double var1);
}
