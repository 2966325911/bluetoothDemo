package com.kangengine.bluebooth.blutboothutil.le.device.scale;


import com.kangengine.bluebooth.blutboothutil.databean.WeightData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface ScaleManagerCallbacks extends BleManagerCallbacks {

   void onScaleDataRead(WeightData var1);

   void onScaleError();

   void onScaleMeasureFinished();
}
