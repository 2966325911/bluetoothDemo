package com.kangengine.bluebooth.blutboothutil.le.device.chol;


import com.kangengine.bluebooth.blutboothutil.databean.CholesterolData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

public interface CholManagerCallbacks extends BleManagerCallbacks {

   void onCholRead(CholesterolData var1);
}
