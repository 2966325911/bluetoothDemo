package com.kangengine.bluebooth.blutboothutil.le.device.urine;


import com.kangengine.bluebooth.blutboothutil.databean.UrineData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface UAManagerCallbacks extends BleManagerCallbacks {

   void onUrineDataRead(UrineData var1);
}
