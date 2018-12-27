package com.kangengine.bluebooth.blutboothutil.le.constants;


public interface BleMessage {

   int STATE_CHANGE = 4096;
   int DEVICE_READY = 4608;
   int BP_DATA = 4097;
   int BG_DATA = 4098;
   int ICP_DATA = 4099;
   int BATTERY = 4100;
   int OXI_DATA = 4101;
   int CHOLE_DATA = 4102;
   int URINE_DATA = 4103;
   int THERMO_DATA = 4104;
   int WEIGHT_DATA = 4105;
   int WEIGHT_ERROR = 4112;
   int WEIGHT_SECOND = 4113;
   int DEVICE_FOUND = 4114;
   String ACTION_BLE = "com.yuwell.bluetooth.ACTION_BLE";

}
