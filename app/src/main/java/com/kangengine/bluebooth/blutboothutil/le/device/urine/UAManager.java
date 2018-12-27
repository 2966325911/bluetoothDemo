package com.kangengine.bluebooth.blutboothutil.le.device.urine;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.kangengine.bluebooth.blutboothutil.databean.UrineData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class UAManager extends BleManager {

   private static final byte[] DEVICE_CONFIRM = new byte[]{(byte)-109, (byte)-114, (byte)8, (byte)0, (byte)8, (byte)1, (byte)67, (byte)79, (byte)78, (byte)84, (byte)69};
   private static final byte[] READ_SINGLE_DATA = new byte[]{(byte)-109, (byte)-114, (byte)4, (byte)0, (byte)8, (byte)4, (byte)16};
   private boolean confirm = false;
   private byte[] data;
   private BluetoothGattCharacteristic characteristicToWrite = null;
   private BluetoothGattCharacteristic notifyCharacteristic = null;
   private static UAManager managerInstance = null;
   private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mUrineService = gatt.getService(Service.URINE_MEASUREMENT);
         if(mUrineService != null) {
            UAManager.this.characteristicToWrite = mUrineService.getCharacteristic(Characteristic.URINE_WRITE);
            UAManager.this.notifyCharacteristic = mUrineService.getCharacteristic(Characteristic.URINE_INDICATE);
         } else {
            mUrineService = gatt.getService(Service.URINE_MEASUREMENT_NEW);
            if(mUrineService != null) {
               UAManager.this.characteristicToWrite = mUrineService.getCharacteristic(Characteristic.URINE_NEW);
               UAManager.this.notifyCharacteristic = UAManager.this.characteristicToWrite;
            }
         }

         return mUrineService != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         requests.push(Request.newEnableNotificationsRequest(UAManager.this.notifyCharacteristic));
         requests.push(Request.newWriteRequest(UAManager.this.characteristicToWrite, UAManager.DEVICE_CONFIRM));
         return requests;
      }
      protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         this.decodeData(characteristic);
      }
      protected void onDeviceDisconnected() {
         UAManager.this.characteristicToWrite = null;
         UAManager.this.notifyCharacteristic = null;
      }
      private void setTime() {
         Calendar calendar = Calendar.getInstance();
         int year = calendar.get(1) - 2000;
         int month = calendar.get(2) + 1;
         int day = calendar.get(5);
         int hour = calendar.get(11);
         int min = calendar.get(12);
         int checkSum = 19 + year + month + day + hour + min;
         byte[] array = new byte[]{(byte)-109, (byte)-114, (byte)9, (byte)0, (byte)8, (byte)2, (byte)Utils.decimalToHex(year), (byte)Utils.decimalToHex(month), (byte)Utils.decimalToHex(day), (byte)Utils.decimalToHex(hour), (byte)Utils.decimalToHex(min), (byte)Utils.decimalToHex(checkSum)};
         if(UAManager.this.characteristicToWrite != null) {
            UAManager.this.characteristicToWrite.setValue(array);
            UAManager.this.writeCharacteristic(UAManager.this.characteristicToWrite);
         }

      }
      private void decodeData(BluetoothGattCharacteristic characteristic) {
         if(UAManager.this.notifyCharacteristic.getUuid().equals(characteristic.getUuid())) {
            byte[] value = characteristic.getValue();
            int headerH = characteristic.getIntValue(17, 0).intValue();
            int headerL = characteristic.getIntValue(17, 1).intValue();
            if(headerH == 147 && headerL == 142) {
               if(value.length > 6) {
                  switch(value[5]) {
                  case 1:
                     UAManager.this.confirm = true;
                     this.setTime();
                     break;
                  case 4:
                     if(value.length == 19) {
                        System.arraycopy(value, 0, UAManager.this.data, 0, value.length);
                        this.analyse();
                     } else {
                        UAManager.this.data = new byte[19];
                        System.arraycopy(value, 0, UAManager.this.data, 0, value.length);
                     }
                  }
               }
            } else {
               System.arraycopy(value, 0, UAManager.this.data, 19 - value.length, value.length);
               this.analyse();
            }
         }

      }
      private void analyse() {
         if(UAManager.this.data.length == 19 && 16 != UAManager.this.data[18]) {
            UrineData urineData = new UrineData(UAManager.this.data);
            ((UAManagerCallbacks)UAManager.this.mCallbacks).onUrineDataRead(urineData);
         }

      }
   };


   private UAManager(Context context) {
      super(context);
   }

   public static synchronized UAManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new UAManager(context);
      }

      return managerInstance;
   }

   public void readLastData() {
      this.data = null;
      if(this.characteristicToWrite != null) {
         this.characteristicToWrite.setValue(READ_SINGLE_DATA);
         this.writeCharacteristic(this.characteristicToWrite);
      }

   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattCallback;
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      return "BLE-EMP-Ui".equals(device.getName());
   }

}
