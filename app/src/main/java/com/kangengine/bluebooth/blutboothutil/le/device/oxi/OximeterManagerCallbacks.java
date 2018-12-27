package com.kangengine.bluebooth.blutboothutil.le.device.oxi;


import com.kangengine.bluebooth.blutboothutil.databean.BloodOxygenData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface OximeterManagerCallbacks extends BleManagerCallbacks {

   void onOxygenDataRead(BloodOxygenData var1);

   void onSensorOff();
}
