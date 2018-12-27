package com.kangengine.bluebooth.blutboothutil.le.device.bpm;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;


import com.kangengine.bluebooth.blutboothutil.databean.BPMData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class BPMManager extends BleManager {

   public static final String YUWELL_BP = "Yuwell BloodPressure";
   private BluetoothGattCharacteristic mBPMCharacteristic;
   private BluetoothGattCharacteristic mICPCharacteristic;
   private BluetoothGattCharacteristic mDateTimeCharacteristic;
   private BluetoothGattCharacteristic mCurrentTimeCharacteristic;
   private static BPMManager managerInstance = null;
   private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mBPService = gatt.getService(Service.BLOOD_PRESSURE_MEASUREMMENT);
         if(mBPService != null) {
            BPMManager.this.mBPMCharacteristic = mBPService.getCharacteristic(Characteristic.BLOOD_PRESSURE);
            BPMManager.this.mICPCharacteristic = mBPService.getCharacteristic(Characteristic.ICP);
            BPMManager.this.mDateTimeCharacteristic = mBPService.getCharacteristic(Characteristic.DATE_TIME);
         }

         BluetoothGattService mCurrentTime = gatt.getService(Service.CURRENT_TIME);
         if(mCurrentTime != null) {
            BPMManager.this.mCurrentTimeCharacteristic = mCurrentTime.getCharacteristic(Characteristic.CURRENT_TIME);
         }

         return BPMManager.this.mBPMCharacteristic != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         if(BPMManager.this.mICPCharacteristic != null) {
            requests.push(Request.newEnableNotificationsRequest(BPMManager.this.mICPCharacteristic));
         }

         requests.push(Request.newEnableIndicationsRequest(BPMManager.this.mBPMCharacteristic));
         if(BPMManager.this.mCurrentTimeCharacteristic != null || BPMManager.this.mDateTimeCharacteristic != null) {
            requests.push(this.setDateTime());
         }

         return requests;
      }
      protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.onBpChange(characteristic);
      }
      protected void onCharacteristicIndicated(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.onBpChange(characteristic);
      }
      protected void onDeviceDisconnected() {
         BPMManager.this.mBPMCharacteristic = null;
         BPMManager.this.mICPCharacteristic = null;
         BPMManager.this.mDateTimeCharacteristic = null;
         BPMManager.this.mCurrentTimeCharacteristic = null;
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
         if(BPMManager.this.mDateTimeCharacteristic != null) {
            characteristic = BPMManager.this.mDateTimeCharacteristic;
            byteSize = 7;
         }

         if(BPMManager.this.mCurrentTimeCharacteristic != null) {
            characteristic = BPMManager.this.mCurrentTimeCharacteristic;
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
      private void onBpChange(BluetoothGattCharacteristic characteristic) {
         byte offset = 0;
         int var15 = offset + 1;
         int flags = characteristic.getIntValue(17, offset).intValue();
         int unit = flags & 1;
         boolean timestampPresent = (flags & 2) > 0;
         boolean pulseRatePresent = (flags & 4) > 0;
         boolean userIdPresent = (flags & 8) > 0;
         boolean measurementStatusPresent = (flags & 16) > 0;
         float systolic = 0.0F;
         float diastolic = 0.0F;
         float meanArterialPressure = 0.0F;
         BPMData data = new BPMData();
         float var16;
         if(Characteristic.BLOOD_PRESSURE.equals(characteristic.getUuid())) {
            systolic = characteristic.getFloatValue(50, var15).floatValue();
            diastolic = characteristic.getFloatValue(50, var15 + 2).floatValue();
            meanArterialPressure = characteristic.getFloatValue(50, var15 + 4).floatValue();
            var15 += 6;
            data.sbp = (int)systolic;
            data.dbp = (int)diastolic;
            data.meanArterialPressure = meanArterialPressure;
         } else if(Characteristic.ICP.equals(characteristic.getUuid())) {
            var16 = characteristic.getFloatValue(50, var15).floatValue();
            var15 += 6;
            ((BPMManagerCallbacks)BPMManager.this.mCallbacks).onIntermediateCuffPressureRead(var16, unit);
            return;
         }

         if(timestampPresent) {
            Calendar pulseRate = Calendar.getInstance();
            pulseRate.set(1, characteristic.getIntValue(18, var15).intValue());
            pulseRate.set(2, characteristic.getIntValue(17, var15 + 2).intValue() - 1);
            pulseRate.set(5, characteristic.getIntValue(17, var15 + 3).intValue());
            pulseRate.set(11, characteristic.getIntValue(17, var15 + 4).intValue());
            pulseRate.set(12, characteristic.getIntValue(17, var15 + 5).intValue());
            pulseRate.set(13, characteristic.getIntValue(17, var15 + 6).intValue());
            var15 += 7;
            data.measureTime = pulseRate.getTime();
         }

         var16 = 0.0F;
         if(pulseRatePresent) {
            var16 = characteristic.getFloatValue(50, var15).floatValue();
            var15 += 2;
            data.pulseRate = (int)var16;
         }

         if(userIdPresent) {
            data.userId = characteristic.getIntValue(17, var15).intValue();
            ++var15;
         }

         if(measurementStatusPresent) {
            int status = characteristic.getIntValue(18, var15).intValue();
            data.bodyMovementDetection = Boolean.valueOf((status & 1) > 0);
            data.cuffFitDetection = Boolean.valueOf((status & 2) > 0);
            data.irregularPulseDetection = Boolean.valueOf((status & 4) > 0);
            data.improperMeasurementPosition = Boolean.valueOf((status & 16) > 0);
         }

         ((BPMManagerCallbacks)BPMManager.this.mCallbacks).onBloodPressureMeasurementRead(data);
      }
   };


   protected BPMManager(Context context) {
      super(context);
   }

   public static synchronized BPMManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new BPMManager(context);
      }

      return managerInstance;
   }

   public static boolean isSpecfiedDevice(String deviceName) {
      return "Yuwell BloodPressure".equals(deviceName) || !TextUtils.isEmpty(deviceName) && deviceName.contains("Yuwell BP");
   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattCallback;
   }

   public void setReadBattery(boolean readBattery) {
      this.mGattCallback.setReadBattery(readBattery);
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      return super.connectable(device, rssi, scanRecord) && isSpecfiedDevice(device.getName());
   }

   public void onClose() {
      this.mBPMCharacteristic = null;
      this.mICPCharacteristic = null;
      this.mDateTimeCharacteristic = null;
      this.mCurrentTimeCharacteristic = null;
   }

}
