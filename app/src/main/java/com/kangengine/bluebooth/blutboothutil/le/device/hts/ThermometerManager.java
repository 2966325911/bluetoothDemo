package com.kangengine.bluebooth.blutboothutil.le.device.hts;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class ThermometerManager extends BleManager {

   private static final String TAIDOC = "TAIDOC";
   private static final int HIDE_MSB_8BITS_OUT_OF_32BITS = 16777215;
   private static final int HIDE_MSB_8BITS_OUT_OF_16BITS = 255;
   private static final int SHIFT_LEFT_8BITS = 8;
   private static final int SHIFT_LEFT_16BITS = 16;
   private static final int GET_BIT24 = 4194304;
   private static final int FIRST_BIT_MASK = 1;
   private BluetoothGattCharacteristic mMeasurement;
   private BluetoothGattCharacteristic mHTMeasurement;
   private long last;
   private static ThermometerManager managerInstance = null;
   private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
      protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
         BluetoothGattService mService = gatt.getService(Service.THERMOMETER);
         BluetoothGattService mHTService = gatt.getService(Service.HEALTH_THERMOMETER);
         if(mService != null) {
            ThermometerManager.this.mMeasurement = mService.getCharacteristic(Characteristic.THERMOMETER);
         }

         if(mHTService != null) {
            ThermometerManager.this.mHTMeasurement = mHTService.getCharacteristic(Characteristic.HT_MEASUREMENT);
         }

         return mService != null || mHTService != null;
      }
      protected Queue initGatt(BluetoothGatt gatt) {
         LinkedList requests = new LinkedList();
         if(ThermometerManager.this.mMeasurement != null) {
            requests.push(Request.newEnableNotificationsRequest(ThermometerManager.this.mMeasurement));
         }

         if(ThermometerManager.this.mHTMeasurement != null) {
            requests.push(Request.newEnableIndicationsRequest(ThermometerManager.this.mHTMeasurement));
         }

         return requests;
      }
      protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         if(Characteristic.THERMOMETER.equals(characteristic.getUuid())) {
            byte[] bytes = characteristic.getValue();
            if(bytes.length >= 10 && bytes[0] == -1 && bytes[1] == -2 && bytes[5] == 101) {
               long current = System.currentTimeMillis();
               if(current - ThermometerManager.this.last < 500L) {
                  Log.d(this.TAG, "Received 2nd time");
                  return;
               }

               ThermometerManager.this.last = current;
               double temperature = (double)((float)(Utils.unsignedByteToInt(bytes[6]) + Utils.unsignedByteToInt(bytes[7]) * 256) / 10.0F);
               ((HTSManagerCallbacks)ThermometerManager.this.mCallbacks).onHTValueReceived(this.roundHalfUpWithScale1(temperature));
            }
         }

      }
      protected void onCharacteristicIndicated(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
         if(Characteristic.HT_MEASUREMENT.equals(characteristic.getUuid())) {
            double tempValue = this.decodeTemperature(characteristic.getValue());
            ((HTSManagerCallbacks)ThermometerManager.this.mCallbacks).onHTValueReceived(this.roundHalfUpWithScale1(tempValue));
         }

      }
      protected void onDeviceDisconnected() {
         ThermometerManager.this.mMeasurement = null;
         ThermometerManager.this.mHTMeasurement = null;
      }
      private double decodeTemperature(byte[] data) {
         byte flag = data[0];
         byte exponential = data[4];
         short firstOctet = this.convertNegativeByteToPositiveShort(data[1]);
         short secondOctet = this.convertNegativeByteToPositiveShort(data[2]);
         short thirdOctet = this.convertNegativeByteToPositiveShort(data[3]);
         int mantissa = (thirdOctet << 16 | secondOctet << 8 | firstOctet) & 16777215;
         mantissa = this.getTwosComplimentOfNegativeMantissa(mantissa);
         double temperatureValue = (double)mantissa * Math.pow(10.0D, (double)exponential);
         if((flag & 1) != 0) {
            temperatureValue = (double)((float)((98.6D * temperatureValue - 32.0D) * 0.5555555555555556D));
         }

         return temperatureValue;
      }
      private short convertNegativeByteToPositiveShort(byte octet) {
         return octet < 0?(short)(octet & 255):(short)octet;
      }
      private int getTwosComplimentOfNegativeMantissa(int mantissa) {
         return (mantissa & 4194304) != 0?((~mantissa & 16777215) + 1) * -1:mantissa;
      }
      private double roundHalfUpWithScale1(double value) {
         BigDecimal b = new BigDecimal(value);
         return b.setScale(1, 4).doubleValue();
      }
   };


   private ThermometerManager(Context context) {
      super(context);
   }

   public static synchronized ThermometerManager getInstance(Context context) {
      if(managerInstance == null) {
         managerInstance = new ThermometerManager(context);
      }

      return managerInstance;
   }

   protected BleManager.BleManagerGattCallback getGattCallback() {
      return this.mGattCallback;
   }

   public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
      String deviceName = device.getName();
      return super.connectable(device, rssi, scanRecord) && ("MEDXING-IRT".equalsIgnoreCase(device.getName()) || !TextUtils.isEmpty(deviceName) && deviceName.contains("TAIDOC"));
   }

   public void onClose() {
      this.mMeasurement = null;
      this.mHTMeasurement = null;
   }

}
