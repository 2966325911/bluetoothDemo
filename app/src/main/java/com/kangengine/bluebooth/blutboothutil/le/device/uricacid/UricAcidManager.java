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

import com.kangengine.bluebooth.UricAcidCovertUtil;
import com.kangengine.bluebooth.blutboothutil.databean.UricAcidData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

/**
 * @author : Vic
 * time    : 2018-12-26 18:59
 * desc    : 尿酸
 */
public class UricAcidManager extends BleManager {

    private static final String TAG = UricAcidManager.class.getSimpleName();
    private BluetoothGattCharacteristic mGMCharacteristic;
    private BluetoothGattCharacteristic mDateTimeCharacteristic;
//    private BluetoothGattCharacteristic mCurrentTimeCharacteristic;
    private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;

    private static UricAcidManager managerInstance = null;
    private boolean updateData = false;
    private final BleManager.BleManagerGattCallback mGattCallback = new BleManager.BleManagerGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
            BluetoothGattService mBGService = gatt.getService(Service.URIC_ACID_MEASUREMENT);
            if(mBGService != null) {
                UricAcidManager.this.mDateTimeCharacteristic = mBGService.getCharacteristic(Characteristic.DATE_TIME);
                UricAcidManager.this.mGMCharacteristic = mBGService.getCharacteristic(Characteristic.URIC_ACID);
            }

//            BluetoothGattService mCurrentTime = gatt.getService(Service.CURRENT_TIME);
//            if(mCurrentTime != null) {
//                UricAcidManager.this.mCurrentTimeCharacteristic = mCurrentTime.getCharacteristic(Characteristic.CURRENT_TIME);
//            }

            return mBGService != null;
        }
        @Override
        protected Queue initGatt(BluetoothGatt gatt) {
            LinkedList requests = new LinkedList();
            requests.push(Request.newEnableNotificationsRequest(UricAcidManager.this.mGMCharacteristic));
//            if(UricAcidManager.this.mCurrentTimeCharacteristic != null || UricAcidManager.this.mDateTimeCharacteristic != null) {
//                requests.push(this.setDateTime());
//            }

            return requests;
        }
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG,"onCharacteristicNotified");
            this.onBgChange(characteristic);
        }
        @Override
        protected void onDeviceDisconnected() {
//            UricAcidManager.this.mCurrentTimeCharacteristic = null;
            UricAcidManager.this.mDateTimeCharacteristic = null;
            UricAcidManager.this.mGMCharacteristic = null;
            updateData = false;
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
            if(UricAcidManager.this.mDateTimeCharacteristic != null) {
                characteristic = UricAcidManager.this.mDateTimeCharacteristic;
                byteSize = 7;
            }

//            if(UricAcidManager.this.mCurrentTimeCharacteristic != null) {
//                characteristic = UricAcidManager.this.mCurrentTimeCharacteristic;
//                byteSize = 10;
//            }

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
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        private void onBgChange(BluetoothGattCharacteristic characteristic) {
            UricAcidData uricAcidData = new UricAcidData();
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
            uricAcidData.time = calendar.getTime();
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
                uricAcidData.value = Utils.multiply2(status1*1000, 3);

                if(!updateData) {
                    ((UricAcidManagerCallbacks)UricAcidManager.this.mCallbacks).onUricAcidDataRead(uricAcidData);
                    updateData = true;
                }

            }

            if(sensorStatusAnnunciationPresent) {
                status = characteristic.getIntValue(18, offset1).intValue();
                offset1 += 2;
            }

        }
    };


    protected UricAcidManager(Context context) {
        super(context);
    }

    public static synchronized UricAcidManager getInstance(Context context) {
        if(managerInstance == null) {
            managerInstance = new UricAcidManager(context);
        }

        return managerInstance;
    }

    public static boolean isSpecfiedDevice(String deviceName) {
        return !TextUtils.isEmpty(deviceName) && deviceName.contains("BeneCheck");
    }

    @Override
    protected BleManager.BleManagerGattCallback getGattCallback() {
        return this.mGattCallback;
    }

    @Override
    public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        return super.connectable(device, rssi, scanRecord) && isSpecfiedDevice(device.getName());
    }

    public void setReadBattery(boolean readBattery) {
        this.mGattCallback.setReadBattery(readBattery);
    }

    @Override
    public void onClose() {
//        this.mCurrentTimeCharacteristic = null;
        this.mDateTimeCharacteristic = null;
        this.mGMCharacteristic = null;
    }

}
