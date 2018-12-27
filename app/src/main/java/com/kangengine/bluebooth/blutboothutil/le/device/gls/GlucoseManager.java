package com.kangengine.bluebooth.blutboothutil.le.device.gls;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;


import com.kangengine.bluebooth.blutboothutil.databean.GlucoseData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class GlucoseManager extends BleManager {

   private static final String TAG = GlucoseManager.class.getSimpleName();
   private static final int UNIT_kgpl = 0;
   private static final int UNIT_molpl = 1;
   public static final String YUWELL_GLUCOSE = "Yuwell Glucose";
   private BluetoothGattCharacteristic mGMCharacteristic;
   private BluetoothGattCharacteristic mDateTimeCharacteristic;
   private BluetoothGattCharacteristic mCurrentTimeCharacteristic;
   private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;
   private static final int OP_CODE_REPORT_STORED_RECORDS = 1;
   private static final int OP_CODE_DELETE_STORED_RECORDS = 2;
   private static final int OP_CODE_ABORT_OPERATION = 3;
   private static final int OP_CODE_REPORT_NUMBER_OF_RECORDS = 4;
   private static final int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
   private static final int OP_CODE_RESPONSE_CODE = 6;
   private static final int OPERATOR_NULL = 0;
   private static final int OPERATOR_ALL_RECORDS = 1;
   private static final int OPERATOR_LESS_THEN_OR_EQUAL = 2;
   private static final int OPERATOR_GREATER_THEN_OR_EQUAL = 3;
   private static final int OPERATOR_WITHING_RANGE = 4;
   private static final int OPERATOR_FIRST_RECORD = 5;
   private static final int OPERATOR_LAST_RECORD = 6;
   private static GlucoseManager managerInstance = null;
   private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mBGService = gatt.getService(Service.BLOOD_GLUCOSE_MEASUREMMENT);
         if(mBGService != null) {
            GlucoseManager.this.mDateTimeCharacteristic = mBGService.getCharacteristic(Characteristic.DATE_TIME);
            GlucoseManager.this.mGMCharacteristic = mBGService.getCharacteristic(Characteristic.BLOOD_GLUCOSE);
         }

         BluetoothGattService mCurrentTime = gatt.getService(Service.CURRENT_TIME);
         if(mCurrentTime != null) {
            GlucoseManager.this.mCurrentTimeCharacteristic = mCurrentTime.getCharacteristic(Characteristic.CURRENT_TIME);
         }

         return mBGService != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         requests.push(Request.newEnableNotificationsRequest(GlucoseManager.this.mGMCharacteristic));
         if(GlucoseManager.this.mCurrentTimeCharacteristic != null || GlucoseManager.this.mDateTimeCharacteristic != null) {
            requests.push(this.setDateTime());
         }

         return requests;
      }
      protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.onBgChange(characteristic);
      }
      protected void onDeviceDisconnected() {
         GlucoseManager.this.mCurrentTimeCharacteristic = null;
         GlucoseManager.this.mDateTimeCharacteristic = null;
         GlucoseManager.this.mGMCharacteristic = null;
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
         if(GlucoseManager.this.mDateTimeCharacteristic != null) {
            characteristic = GlucoseManager.this.mDateTimeCharacteristic;
            byteSize = 7;
         }

         if(GlucoseManager.this.mCurrentTimeCharacteristic != null) {
            characteristic = GlucoseManager.this.mCurrentTimeCharacteristic;
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
      private void onBgChange(BluetoothGattCharacteristic characteristic) {
         GlucoseData glucoseData = new GlucoseData();
         byte offset = 0;
         int flags = characteristic.getIntValue(17, offset).intValue();
         int offset1 = offset + 1;
         boolean timeOffsetPresent = (flags & 1) > 0;
         boolean typeAndLocationPresent = (flags & 2) > 0;
         boolean concentrationUnit = (flags & 4) > 0;
         boolean sensorStatusAnnunciationPresent = (flags & 8) > 0;
         boolean contextInfoFollows = (flags & 16) > 0;
         int sequenceNumber = characteristic.getIntValue(18, offset1).intValue();
         offset1 += 2;
         int year = characteristic.getIntValue(18, offset1 + 0).intValue();
         int month = characteristic.getIntValue(17, offset1 + 2).intValue() - 1;
         int day = characteristic.getIntValue(17, offset1 + 3).intValue();
         int hours = characteristic.getIntValue(17, offset1 + 4).intValue();
         int minutes = characteristic.getIntValue(17, offset1 + 5).intValue();
         int seconds = characteristic.getIntValue(17, offset1 + 6).intValue();
         offset1 += 7;
         Calendar calendar = Calendar.getInstance();
         calendar.set(year, month, day, hours, minutes, seconds);
         glucoseData.time = calendar.getTime();
         int status;
         if(timeOffsetPresent) {
            status = characteristic.getIntValue(34, offset1).intValue();
            offset1 += 2;
         }

         if(typeAndLocationPresent) {
            float status1 = characteristic.getFloatValue(50, offset1).floatValue();
            int typeAndLocation = characteristic.getIntValue(17, offset1 + 2).intValue();
            int type = (typeAndLocation & 240) >> 4;
            int sampleLocation = typeAndLocation & 15;
            offset1 += 3;
            glucoseData.value = Utils.multiply(status1, 1000.0F);
            ((GlucoseManagerCallbacks)GlucoseManager.this.mCallbacks).onGlucoseMeasurementRead(glucoseData);
         }

         if(sensorStatusAnnunciationPresent) {
            status = characteristic.getIntValue(18, offset1).intValue();
            offset1 += 2;
         }

      }
   };


   protected GlucoseManager(Context context) {
      super(context);
   }

   public static synchronized GlucoseManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new GlucoseManager(context);
      }

      return managerInstance;
   }

   public static boolean isSpecfiedDevice(String deviceName) {
      return "Yuwell Glucose".equals(deviceName) || !TextUtils.isEmpty(deviceName) && deviceName.contains("Yuwell BG");
   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattCallback;
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      return super.connectable(device, rssi, scanRecord) && isSpecfiedDevice(device.getName());
   }

   public void setReadBattery(boolean readBattery) {
      this.mGattCallback.setReadBattery(readBattery);
   }

   public void onClose() {
      this.mCurrentTimeCharacteristic = null;
      this.mDateTimeCharacteristic = null;
      this.mGMCharacteristic = null;
   }

   private void sendToHosmart(String val) {
      Intent intent = new Intent("com.hosmart.nurse.gatherdata");
      JSONArray array = new JSONArray();
      JSONObject object = new JSONObject();

      try {
         object.put("ItemCode", "07");
         object.put("ItemValue", val);
      } catch (JSONException var6) {
         var6.printStackTrace();
      }

      array.put(object);
      intent.putExtra("GatherData", array.toString());
      this.getContext().sendBroadcast(intent);
   }

}
