package com.kangengine.bluebooth.blutboothutil.le.device.scale;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.holtek.libHTBodyfat.HTPeopleGeneral;
import com.kangengine.bluebooth.blutboothutil.databean.WeightData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;
import com.kangengine.bluebooth.blutboothutil.utils.WeightParser;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class ScaleManager extends BleManager {

   private static final String TAG = ScaleManager.class.getSimpleName();
   private BluetoothGattCharacteristic readCharacteristic;
   private BluetoothGattCharacteristic characteristicToWrite;
   private BluetoothGattCharacteristic mBodyCompositionMeasurement;
   private BluetoothGattCharacteristic mDateTimeCharacteristic;
   private static ScaleManager managerInstance = null;
   private int gender;
   private int height;
   private int age;
   private long last;
   private final BleManager.BleManagerGattCallback mGattcallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mWeightService = gatt.getService(Service.WEIGHT_SCALE);
         BluetoothGattService mBodyCompositionService = gatt.getService(Service.BODY_COMPOSITION);
         if(mBodyCompositionService != null) {
            ScaleManager.this.mBodyCompositionMeasurement = mBodyCompositionService.getCharacteristic(Characteristic.BODY_COMPOSITION_MEASUREMENT);
            BluetoothGattService mCurrentTime = gatt.getService(Service.CURRENT_TIME);
            if(mCurrentTime != null) {
               ScaleManager.this.mDateTimeCharacteristic = mCurrentTime.getCharacteristic(Characteristic.DATE_TIME);
            }
         } else if(mWeightService != null) {
            ScaleManager.this.readCharacteristic = mWeightService.getCharacteristic(Characteristic.WEIGHT_READ);
            ScaleManager.this.characteristicToWrite = mWeightService.getCharacteristic(Characteristic.WEIGHT_WRITE);
         }

         return mBodyCompositionService != null || mWeightService != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         if(ScaleManager.this.readCharacteristic != null) {
            requests.push(Request.newEnableNotificationsRequest(ScaleManager.this.readCharacteristic));
         }

         if(ScaleManager.this.mBodyCompositionMeasurement != null) {
            requests.push(Request.newEnableIndicationsRequest(ScaleManager.this.mBodyCompositionMeasurement));
         }

         if(ScaleManager.this.mDateTimeCharacteristic != null) {
            requests.push(this.setDateTime());
         }

         return requests;
      }
      protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.handleWeightData(characteristic);
      }
      protected void onCharacteristicIndicated(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.handleStandardGatt(characteristic);
      }
      protected void onDeviceDisconnected() {
         ScaleManager.this.readCharacteristic = null;
         ScaleManager.this.characteristicToWrite = null;
         ScaleManager.this.mBodyCompositionMeasurement = null;
         ScaleManager.this.mDateTimeCharacteristic = null;
      }
      private Request setDateTime() {
         Calendar calendar = Calendar.getInstance();
         int year = calendar.get(1);
         int month = calendar.get(2) + 1;
         int day = calendar.get(5);
         int hour = calendar.get(11);
         int min = calendar.get(12);
         int sec = calendar.get(13);
         BluetoothGattCharacteristic characteristic = null;
         byte byteSize = 0;
         if(ScaleManager.this.mDateTimeCharacteristic != null) {
            characteristic = ScaleManager.this.mDateTimeCharacteristic;
            byteSize = 10;
         }

         byte[] array = new byte[byteSize];
         array[0] = (byte)(year & 255);
         array[1] = (byte)(year >> 8 & 255);
         array[2] = (byte)(month & 255);
         array[3] = (byte)(day & 255);
         array[4] = (byte)(hour & 255);
         array[5] = (byte)(min & 255);
         array[6] = (byte)(sec & 255);
         return Request.newWriteRequest(characteristic, array);
      }
      private void handleWeightData(BluetoothGattCharacteristic characteristic) {
         if(Characteristic.WEIGHT_READ.equals(characteristic.getUuid())) {
            long current = System.currentTimeMillis();
            if(current - ScaleManager.this.last < 1000L) {
               Log.d(this.TAG, "Received 2nd time");
               ((ScaleManagerCallbacks)ScaleManager.this.mCallbacks).onScaleMeasureFinished();
               return;
            }

            ScaleManager.this.last = current;
            byte[] value = characteristic.getValue();
            int head = Utils.unsignedByteToInt(value[0]);
            if(head == 253) {
               if(value.length == 8) {
                  switch(Utils.unsignedByteToInt(value[7])) {
                  case 49:
                     ScaleManager.this.writeUserInfo(ScaleManager.this.gender, ScaleManager.this.height, ScaleManager.this.age);
                  case 50:
                  case 52:
                  default:
                     break;
                  case 51:
                  case 53:
                     ((ScaleManagerCallbacks)ScaleManager.this.mCallbacks).onScaleError();
                  }
               }
            } else {
               ScaleManager.this.last = current;
               WeightData result = WeightParser.parse(value);
               ((ScaleManagerCallbacks)ScaleManager.this.mCallbacks).onScaleDataRead(result);
            }
         }

      }
      private void handleStandardGatt(BluetoothGattCharacteristic characteristic) {
         if(Characteristic.BODY_COMPOSITION_MEASUREMENT.equals(characteristic.getUuid())) {
            long current = System.currentTimeMillis();
            if(current - ScaleManager.this.last < 1000L) {
               Log.d(this.TAG, "Received 2nd time");
               ((ScaleManagerCallbacks)ScaleManager.this.mCallbacks).onScaleMeasureFinished();
               return;
            }

            ScaleManager.this.last = current;
            byte[] value = characteristic.getValue();
            int weightRaw = characteristic.getIntValue(18, 11).intValue();
            double weight = (double)weightRaw * 0.01D;
            int impedance = Utils.unsignedByteToInt(value[15]) + (Utils.unsignedByteToInt(value[16]) << 8) + (Utils.unsignedByteToInt(value[17]) << 16);
            HTPeopleGeneral bodyfat = new HTPeopleGeneral(weight, (double)ScaleManager.this.height, ScaleManager.this.gender, ScaleManager.this.age, impedance);
            if(bodyfat.getBodyfatParameters() == 0) {
               ((ScaleManagerCallbacks)ScaleManager.this.mCallbacks).onScaleDataRead(WeightParser.parse(weight, bodyfat));
            } else {
               ((ScaleManagerCallbacks)ScaleManager.this.mCallbacks).onScaleDataRead(WeightParser.parse(weight));
            }
         }

      }
   };


   private ScaleManager(Context context) {
      super(context);
   }

   public static synchronized ScaleManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new ScaleManager(context);
      }

      return managerInstance;
   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattcallback;
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      String name = device.getName();
      return TextUtils.isEmpty(name)?false:super.connectable(device, rssi, scanRecord) && ("Electronic Scale".equals(device.getName()) || name.contains("Yuwell Scale"));
   }

   public void poweroff() {
      if(this.characteristicToWrite != null) {
         this.characteristicToWrite.setValue(new byte[]{(byte)-3, (byte)53, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)53});
         this.writeCharacteristic(this.characteristicToWrite);
      }

   }

   public void onClose() {
      this.readCharacteristic = null;
      this.characteristicToWrite = null;
      this.mBodyCompositionMeasurement = null;
      this.mDateTimeCharacteristic = null;
   }

   public void writeUserInfo(int g, int h, int a) {
      this.gender = g;
      this.height = h;
      this.age = a;
      if(this.characteristicToWrite != null) {
         this.characteristicToWrite.setValue(new byte[8]);
         this.characteristicToWrite.setValue(254, 17, 0);
         this.characteristicToWrite.setValue(1, 17, 1);
         this.characteristicToWrite.setValue(this.gender, 17, 2);
         this.characteristicToWrite.setValue(0, 17, 3);
         this.characteristicToWrite.setValue(this.height, 17, 4);
         this.characteristicToWrite.setValue(this.age, 17, 5);
         this.characteristicToWrite.setValue(1, 17, 6);
         int check = 0;
         byte[] bytes = this.characteristicToWrite.getValue();

         for(int i = 1; i < 7; ++i) {
            check ^= bytes[i];
         }

         this.characteristicToWrite.setValue(check, 17, 7);
         this.writeCharacteristic(this.characteristicToWrite);
      }

   }

}
