package com.kangengine.bluebooth.blutboothutil.le.device.bpm;


import com.kangengine.bluebooth.blutboothutil.databean.BPMData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface BPMManagerCallbacks extends BleManagerCallbacks {

   int UNIT_mmHG = 0;
   int UNIT_kPa = 1;


   void onBloodPressureMeasurementRead(BPMData var1);

   void onIntermediateCuffPressureRead(float var1, int var2);
}
