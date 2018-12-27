package com.kangengine.bluebooth.blutboothutil.le.device.uricacid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.le.device.hts.HTSManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

/**
 * @author : Vic
 * time    : 2018-12-26 18:59
 * desc    : 尿酸
 */
public class UricAcidManager extends BleManager {
    
    private BluetoothGattCharacteristic mMeasurement;
    private BluetoothGattCharacteristic mUricAcidMeasurement;
    private long last;
    private static UricAcidManager managerInstance = null;
    private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
            BluetoothGattService mService = gatt.getService(Service.THERMOMETER);
            BluetoothGattService mHTService = gatt.getService(Service.HEALTH_THERMOMETER);
            if(mService != null) {
                UricAcidManager.this.mMeasurement = mService.getCharacteristic(Characteristic.THERMOMETER);
            }

            if(mHTService != null) {
                UricAcidManager.this.mUricAcidMeasurement = mHTService.getCharacteristic(Characteristic.HT_MEASUREMENT);
            }

            return mService != null || mHTService != null;
        }
        @Override
        protected Queue initGatt(BluetoothGatt gatt) {
            LinkedList requests = new LinkedList();
            if(UricAcidManager.this.mMeasurement != null) {
                requests.push(Request.newEnableNotificationsRequest(UricAcidManager.this.mMeasurement));
            }

            if(UricAcidManager.this.mUricAcidMeasurement != null) {
                requests.push(Request.newEnableIndicationsRequest(UricAcidManager.this.mUricAcidMeasurement));
            }

            return requests;
        }
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(Characteristic.THERMOMETER.equals(characteristic.getUuid())) {
                byte[] bytes = characteristic.getValue();
                if(bytes.length >= 10 && bytes[0] == -1 && bytes[1] == -2 && bytes[5] == 101) {
                    long current = System.currentTimeMillis();
                    if(current - UricAcidManager.this.last < 500L) {
                        Log.d(this.TAG, "Received 2nd time");
                        return;
                    }

                    UricAcidManager.this.last = current;
                    double temperature = (double)((float)(Utils.unsignedByteToInt(bytes[6]) + Utils.unsignedByteToInt(bytes[7]) * 256) / 10.0F);
                    ((HTSManagerCallbacks)UricAcidManager.this.mCallbacks).onHTValueReceived(this.roundHalfUpWithScale1(temperature));
                }
            }

        }
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected void onCharacteristicIndicated(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(Characteristic.HT_MEASUREMENT.equals(characteristic.getUuid())) {
                double tempValue = this.decodeUricAcidData(characteristic.getValue());
                ((HTSManagerCallbacks)UricAcidManager.this.mCallbacks).onHTValueReceived(this.roundHalfUpWithScale1(tempValue));
            }

        }
        @Override
        protected void onDeviceDisconnected() {
            UricAcidManager.this.mMeasurement = null;
            UricAcidManager.this.mUricAcidMeasurement = null;
        }
        private double decodeUricAcidData(byte[] data) {
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


    private UricAcidManager(Context context) {
        super(context);
    }

    public static synchronized UricAcidManager getInstance(Context context) {
        if(managerInstance == null) {
            managerInstance = new UricAcidManager(context);
        }

        return managerInstance;
    }

    @Override
    protected BleManager.BleManagerGattCallback getGattCallback() {
        return this.mGattCallback;
    }

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        String deviceName = device.getName();
        return super.connectable(device, rssi, scanRecord) && ("MEDXING-IRT".equalsIgnoreCase(device.getName()) || !TextUtils.isEmpty(deviceName) && deviceName.contains("TAIDOC"));
    }

    @Override
    public void onClose() {
        this.mMeasurement = null;
        this.mUricAcidMeasurement = null;
    }
}
