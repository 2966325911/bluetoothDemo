package com.kangengine.bluebooth.blutboothutil.le.device.oxi;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;


import com.kangengine.bluebooth.blutboothutil.databean.BloodOxygenData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class OximeterManager extends BleManager {

   public static final String TAG = OximeterManager.class.getSimpleName();
   private BluetoothGattCharacteristic mMeasurement;
   private static OximeterManager managerInstance = null;
   private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mOxiService = gatt.getService(Service.OXIMETER);
         if(mOxiService != null) {
            OximeterManager.this.mMeasurement = mOxiService.getCharacteristic(Characteristic.OXIMETER);
         }

         return mOxiService != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         requests.push(Request.newEnableNotificationsRequest(OximeterManager.this.mMeasurement));
         return requests;
      }
      protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.decodeData(characteristic);
      }
      protected void onDeviceDisconnected() {
         OximeterManager.this.mMeasurement = null;
      }
      private boolean handlePackage56(BluetoothGattCharacteristic characteristic, BloodOxygenData data) {
         int waveHead = characteristic.getIntValue(17, 0).intValue();
         int packageId = characteristic.getIntValue(17, 2).intValue();
         if(waveHead == 254 && packageId == 86) {
            int pulseWave = characteristic.getIntValue(17, 3).intValue();
            int warning = characteristic.getIntValue(17, 4).intValue();
            data.pulseWave = pulseWave;
            return (warning & 2) >> 1 == 1;
         } else {
            return false;
         }
      }
      private void handlePackage55(BluetoothGattCharacteristic characteristic, BloodOxygenData data, byte[] value, int startOffset) {
         int waveHead = characteristic.getIntValue(17, startOffset).intValue();
         int packageId = characteristic.getIntValue(17, startOffset + 2).intValue();
         if(waveHead == 254 && packageId == 85) {
            int pr = (Utils.unsignedByteToInt(value[startOffset + 3]) << 8) + Utils.unsignedByteToInt(value[startOffset + 4]);
            int spo2 = characteristic.getIntValue(17, startOffset + 5).intValue();
            float pi = (float)((Utils.unsignedByteToInt(value[startOffset + 6]) << 8) + Utils.unsignedByteToInt(value[startOffset + 7])) / 1000.0F;
            data.pulseRate = pr;
            data.saturation = spo2;
            data.pi = pi;
         }

      }
      private void decodeData(BluetoothGattCharacteristic characteristic) {
         if(Characteristic.OXIMETER.equals(characteristic.getUuid())) {
            byte[] value = characteristic.getValue();
            BloodOxygenData data = new BloodOxygenData();
            boolean sensorOff;
            if(value.length == 8) {
               sensorOff = this.handlePackage56(characteristic, data);
               if(!sensorOff) {
                  ((OximeterManagerCallbacks)OximeterManager.this.mCallbacks).onOxygenDataRead(data);
               } else {
                  ((OximeterManagerCallbacks)OximeterManager.this.mCallbacks).onSensorOff();
               }
            } else if(value.length == 10) {
               this.handlePackage55(characteristic, data, value, 0);
               if(data.saturation < 101) {
                  ((OximeterManagerCallbacks)OximeterManager.this.mCallbacks).onOxygenDataRead(data);
               }
            } else if(value.length >= 16) {
               sensorOff = this.handlePackage56(characteristic, data);
               this.handlePackage55(characteristic, data, value, 8);
               if(!sensorOff) {
                  if(data.saturation < 101) {
                     ((OximeterManagerCallbacks)OximeterManager.this.mCallbacks).onOxygenDataRead(data);
                  }
               } else {
                  ((OximeterManagerCallbacks)OximeterManager.this.mCallbacks).onSensorOff();
               }
            }
         }

      }
   };


   private OximeterManager(Context context) {
      super(context);
   }

   public static synchronized OximeterManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new OximeterManager(context);
      }

      return managerInstance;
   }

   public static boolean isSpecfiedDevice(String deviceName) {
      return !TextUtils.isEmpty(deviceName) && (deviceName.contains("Tv2") || deviceName.contains("DualSpp") || deviceName.contains("Yuwell Oxi"));
   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattCallback;
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      String name = device.getName();
      return TextUtils.isEmpty(name)?false:super.connectable(device, rssi, scanRecord) && isSpecfiedDevice(name);
   }

}
