package com.kangengine.bluebooth.blutboothutil.le.device.chol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.kangengine.bluebooth.blutboothutil.databean.CholesterolData;
import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManager;
import com.kangengine.bluebooth.blutboothutil.le.core.Request;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public class CholManager extends BleManager<CholManagerCallbacks> {
    private static final String TAG = CholManager.class.getSimpleName();
    private BluetoothGattCharacteristic writeCharacteristic = null;
    private BluetoothGattCharacteristic readCharacteristic = null;
    private int num = 0;
    private StringBuilder builder;
    private static CholManager mInstance;
    private final BleManager<CholManagerCallbacks>.BleManagerGattCallback mGattCallback = new BleManager<CholManagerCallbacks>.BleManagerGattCallback() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
            BluetoothGattService mCholService = gatt.getService(Service.CHOLESTEROL);
            if (mCholService != null) {
                List<BluetoothGattCharacteristic> characteristics = mCholService.getCharacteristics();
                Iterator var4 = characteristics.iterator();

                while(var4.hasNext()) {
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic)var4.next();
                    if (characteristic.getUuid().equals(Characteristic.CHOLESTEROL_READ)) {
                        if (characteristic.getProperties() == 4) {
                            CholManager.this.writeCharacteristic = characteristic;
                        } else {
                            CholManager.this.readCharacteristic = characteristic;
                            this.setCharacteristic(gatt, characteristic, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        }
                    }
                }
            }

            return mCholService != null;
        }

        protected Queue<Request> initGatt(BluetoothGatt gatt) {
            LinkedList<Request> requests = new LinkedList();
            requests.push(Request.newEnableNotificationsRequest(CholManager.this.readCharacteristic));
            return requests;
        }

        protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            this.decodeData(characteristic);
        }

        protected void onDeviceDisconnected() {
            CholManager.this.readCharacteristic = null;
            CholManager.this.writeCharacteristic = null;
        }

        private void decodeData(BluetoothGattCharacteristic characteristic) {
            if (Characteristic.CHOLESTEROL_READ.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();

                String strData;
                try {
                    strData = new String(data, "UTF-8");
                } catch (UnsupportedEncodingException var14) {
                    Log.e(this.TAG, "error", var14);
                    strData = "";
                }

                CholManager.this.builder.append(strData);
                if (CholManager.this.builder.toString().endsWith("\r\n\r\n")) {
                    String dataStr = CholManager.this.builder.toString().replaceAll("null", "");
                    CholesterolData cholesterolData = new CholesterolData();
                    String unit = dataStr.contains("mg/dL") ? "mg/dL" : "mmol/L";
                    cholesterolData.unit = unit;
                    String[] dataArray = dataStr.split("\r\n");

                    try {
                        cholesterolData.time = this.sdf.parse(dataArray[0].replaceAll("\\(Y-M-D\\)", ""));
                    } catch (ParseException var13) {
                        var13.printStackTrace();
                    }

                    for(int i = 2; i < dataArray.length; ++i) {
                        String[] subData = dataArray[i].split(":");
                        if (subData.length == 2) {
                            String value = subData[1].replace(unit, "").trim();
                            String var11 = subData[0];
                            byte var12 = -1;
                            switch(var11.hashCode()) {
                                case 71376:
                                    if (var11.equals("HDL")) {
                                        var12 = 1;
                                    }
                                    break;
                                case 75220:
                                    if (var11.equals("LDL")) {
                                        var12 = 3;
                                    }
                                    break;
                                case 2067714:
                                    if (var11.equals("CHOL")) {
                                        var12 = 0;
                                    }
                                    break;
                                case 2583580:
                                    if (var11.equals("TRIG")) {
                                        var12 = 2;
                                    }
                            }

                            switch(var12) {
                                case 0:
                                    cholesterolData.chol = value;
                                    break;
                                case 1:
                                    cholesterolData.hdl = value;
                                    break;
                                case 2:
                                    cholesterolData.trig = value;
                                    break;
                                case 3:
                                    cholesterolData.ldl = value;
                            }
                        }
                    }

                    ((CholManagerCallbacks)CholManager.this.mCallbacks).onCholRead(cholesterolData);
                }
            }

        }
    };

    public static CholManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CholManager(context);
        }

        return mInstance;
    }

    private CholManager(Context context) {
        super(context);
    }

    protected BleManager<CholManagerCallbacks>.BleManagerGattCallback getGattCallback() {
        return this.mGattCallback;
    }

    public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        return "iGate".equals(device.getName());
    }

    public void readCholData() {
        this.num = 0;
        this.builder = new StringBuilder();
        if (this.writeCharacteristic != null) {
            this.writeCharacteristic.setValue("GET".getBytes());
            this.writeCharacteristic(this.writeCharacteristic);
        }

    }
}
