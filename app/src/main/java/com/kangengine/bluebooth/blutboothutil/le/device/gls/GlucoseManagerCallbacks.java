package com.kangengine.bluebooth.blutboothutil.le.device.gls;


import com.kangengine.bluebooth.blutboothutil.databean.GlucoseData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface GlucoseManagerCallbacks extends BleManagerCallbacks {

   void onGlucoseMeasurementRead(GlucoseData var1);
}
