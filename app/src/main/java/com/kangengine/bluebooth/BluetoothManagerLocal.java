package com.kangengine.bluebooth;

import android.bluetooth.BluetoothAdapter;

/**
 * @author : Vic
 * time    : 2018-12-13 15:45
 * desc    :
 */
public class BluetoothManagerLocal {

    /**
     * 判断蓝牙是否可用
     * @return
     */
    public static boolean isBluetoothSupported(){
        return BluetoothAdapter.getDefaultAdapter() != null ? true : false;
    }


    /**
     * 判断蓝牙是否打开
     * @return
     */
    public static boolean isBluetoothEnabled(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    /**
     * 打开蓝牙
     * @return
     */
    public static boolean turnOnBluetooth(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            return  bluetoothAdapter.enable();
        }
        return false;
    }

    /**
     * 关闭蓝牙
     * @return
     */
    public static boolean turnOffBluetooth(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            return bluetoothAdapter.disable();
        }
        return false;
    }

}
