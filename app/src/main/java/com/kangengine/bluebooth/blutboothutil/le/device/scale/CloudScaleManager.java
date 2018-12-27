package com.kangengine.bluebooth.blutboothutil.le.device.scale;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.holtek.libHTBodyfat.HTPeopleGeneral;
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

public class CloudScaleManager extends BleManager {

   private BluetoothGattCharacteristic mBodyCompositionMeasurement;
   private BluetoothGattCharacteristic mDateTimeCharacteristic;
   private static CloudScaleManager managerInstance = null;
   private int gender;
   private int height;
   private int age;
   private long last;
   private final BleManager.BleManagerGattCallback mGattcallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mBodyCompositionService = gatt.getService(Service.BODY_COMPOSITION);
         if(mBodyCompositionService != null) {
            CloudScaleManager.this.mBodyCompositionMeasurement = mBodyCompositionService.getCharacteristic(Characteristic.BODY_COMPOSITION_MEASUREMENT);
            BluetoothGattService mCurrentTime = gatt.getService(Service.CURRENT_TIME);
            if(mCurrentTime != null) {
               CloudScaleManager.this.mDateTimeCharacteristic = mCurrentTime.getCharacteristic(Characteristic.DATE_TIME);
            }
         }

         return mBodyCompositionService != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         if(CloudScaleManager.this.mBodyCompositionMeasurement != null) {
            requests.push(Request.newEnableIndicationsRequest(CloudScaleManager.this.mBodyCompositionMeasurement));
         }

         if(CloudScaleManager.this.mDateTimeCharacteristic != null) {
            requests.push(this.setDateTime());
         }

         return requests;
      }
      protected void onDeviceDisconnected() {
         CloudScaleManager.this.mBodyCompositionMeasurement = null;
         CloudScaleManager.this.mDateTimeCharacteristic = null;
      }
      protected void onCharacteristicIndicated(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.handleStandardGatt(characteristic);
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
         if(CloudScaleManager.this.mDateTimeCharacteristic != null) {
            characteristic = CloudScaleManager.this.mDateTimeCharacteristic;
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
      private void handleStandardGatt(BluetoothGattCharacteristic characteristic) {
         if(Characteristic.BODY_COMPOSITION_MEASUREMENT.equals(characteristic.getUuid())) {
            long current = System.currentTimeMillis();
            if(current - CloudScaleManager.this.last < 1000L) {
               Log.d(this.TAG, "Received 2nd time");
               ((ScaleManagerCallbacks)CloudScaleManager.this.mCallbacks).onScaleMeasureFinished();
               return;
            }

            CloudScaleManager.this.last = current;
            byte[] value = characteristic.getValue();
            int weightRaw = characteristic.getIntValue(18, 11).intValue();
            double weight = (double)weightRaw * 0.01D;
            int impedance = Utils.unsignedByteToInt(value[15]) + (Utils.unsignedByteToInt(value[16]) << 8) + (Utils.unsignedByteToInt(value[17]) << 16);
            HTPeopleGeneral bodyfat = new HTPeopleGeneral(weight, (double)CloudScaleManager.this.height, CloudScaleManager.this.gender, CloudScaleManager.this.age, impedance);
            if(bodyfat.getBodyfatParameters() == 0) {
               ((ScaleManagerCallbacks)CloudScaleManager.this.mCallbacks).onScaleDataRead(WeightParser.parse(weight, bodyfat));
            } else {
               ((ScaleManagerCallbacks)CloudScaleManager.this.mCallbacks).onScaleDataRead(WeightParser.parse(weight));
            }
         }

      }
   };


   private CloudScaleManager(Context context) {
      super(context);
   }

   public static synchronized CloudScaleManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new CloudScaleManager(context);
      }

      return managerInstance;
   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattcallback;
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      String name = device.getName();
      return !TextUtils.isEmpty(name) && name.contains("Yuwell Scale");
   }

   public void onClose() {
      this.mBodyCompositionMeasurement = null;
      this.mDateTimeCharacteristic = null;
   }

   public void writeUserInfo(int g, int h, int a) {
      this.gender = g;
      this.height = h;
      this.age = a;
   }

}
