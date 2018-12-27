package com.kangengine.bluebooth.blutboothutil.le.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Keep;
import android.util.Log;

import com.kangengine.bluebooth.blutboothutil.le.constants.Characteristic;
import com.kangengine.bluebooth.blutboothutil.le.constants.GattError;
import com.kangengine.bluebooth.blutboothutil.le.constants.Service;
import com.kangengine.bluebooth.blutboothutil.utils.ParserUtils;

import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;

public abstract class BleManager<E extends BleManagerCallbacks> {
    private static final String TAG = "BleManager";
    private final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private static final String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private static final String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private static final String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private static final String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private Context mContext;
    protected E mCallbacks;
    private boolean isUserDisconnected;
    private int mConnectionState = 0;
    private boolean mConnected;
    private BluetoothDevice mDevice;
    private BleManager.OnConnectListener onConnectListener;

    public BleManager(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.isUserDisconnected = false;
    }

    protected Context getContext() {
        return this.mContext;
    }

    protected abstract BleManager<E>.BleManagerGattCallback getGattCallback();

    public void setGattCallbacks(E callbacks) {
        this.mCallbacks = callbacks;
    }

    protected boolean shouldAutoConnect() {
        return false;
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mConnected) {
            return false;
        } else {
            if (this.mBluetoothGatt != null) {
                Log.d("BleManager", "gatt.close()");
                this.mBluetoothGatt.close();
                this.mBluetoothGatt = null;
            }

            boolean autoConnect = this.shouldAutoConnect();
            this.isUserDisconnected = !autoConnect;
            Log.v("BleManager", "Connecting...");
            Log.d("BleManager", "gatt = device.connectGatt(autoConnect = " + autoConnect + ")");
            this.mBluetoothGatt = device.connectGatt(this.mContext, autoConnect, this.getGattCallback());
            this.mConnectionState = 1;
            this.mDevice = device;
            this.mCallbacks.onDeviceConnecting();
            return true;
        }
    }

    public void disconnect() {
        this.isUserDisconnected = true;
        if (this.mConnected && this.mBluetoothGatt != null) {
            Log.v("BleManager", "Disconnecting...");
            Log.d("BleManager", "gatt.disconnect()");
            this.mBluetoothGatt.disconnect();
        }

    }

    public void close() {
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }

        this.isUserDisconnected = false;
        this.mConnected = false;
        this.mConnectionState = 0;
        this.mDevice = null;
        this.onClose();
    }

    public int getConnectionState() {
        return this.mConnectionState;
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    public BluetoothDevice getBluetoothDevice() {
        return this.mDevice;
    }

    public void onClose() {
    }

    protected final boolean enableNotifications(BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = this.mBluetoothGatt;
        if (gatt != null && characteristic != null) {
            int properties = characteristic.getProperties();
            if ((properties & 16) == 0) {
                return false;
            } else {
                gatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(this.CLIENT_CHARACTERISTIC_CONFIG);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Log.v("BleManager", "Enabling notifications for " + characteristic.getUuid());
                    Log.d("BleManager", "gatt.writeDescriptor(" + this.CLIENT_CHARACTERISTIC_CONFIG + ", value=0x01-00)");
                    return gatt.writeDescriptor(descriptor);
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    protected final boolean enableIndications(BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = this.mBluetoothGatt;
        if (gatt != null && characteristic != null) {
            int properties = characteristic.getProperties();
            if ((properties & 32) == 0) {
                return false;
            } else {
                gatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(this.CLIENT_CHARACTERISTIC_CONFIG);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    Log.v("BleManager", "Enabling indications for " + characteristic.getUuid());
                    Log.d("BleManager", "gatt.writeDescriptor(" + this.CLIENT_CHARACTERISTIC_CONFIG + ", value=0x02-00)");
                    return gatt.writeDescriptor(descriptor);
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    protected final boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = this.mBluetoothGatt;
        if (gatt != null && characteristic != null) {
            int properties = characteristic.getProperties();
            if ((properties & 2) == 0) {
                return false;
            } else {
                Log.v("BleManager", "Reading characteristic " + characteristic.getUuid());
                Log.d("BleManager", "gatt.readCharacteristic(" + characteristic.getUuid() + ")");
                return gatt.readCharacteristic(characteristic);
            }
        } else {
            return false;
        }
    }

    public final boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = this.mBluetoothGatt;
        if (gatt != null && characteristic != null) {
            int properties = characteristic.getProperties();
            if ((properties & 12) == 0) {
                return false;
            } else {
                Log.v("BleManager", "Writing characteristic " + characteristic.getUuid());
                Log.d("BleManager", "gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
                return gatt.writeCharacteristic(characteristic);
            }
        } else {
            return false;
        }
    }

    public boolean connectable(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
        return this.onConnectListener == null || this.onConnectListener.connectable(device, rssi, scanRecord);
    }

    public void setOnConnectListener(BleManager.OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }

    public abstract class BleManagerGattCallback extends BluetoothGattCallback {
        public final String TAG = BleManager.BleManagerGattCallback.class.getSimpleName();
        private Queue<Request> mInitQueue;
        private boolean mInitInProgress;
        private boolean readBattery;

        public BleManagerGattCallback() {
        }

        protected abstract boolean isRequiredServiceSupported(BluetoothGatt var1);

        protected abstract Queue<Request> initGatt(BluetoothGatt var1);

        protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        protected void onCharacteristicIndicated(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                Log.i(this.TAG, "Services Discovered");
                if (this.isRequiredServiceSupported(gatt)) {
                    Log.v(this.TAG, "Primary service found");
                    this.mInitInProgress = true;
                    this.mInitQueue = this.initGatt(gatt);
                    if (!this.readBatteryLevel(gatt)) {
                        this.nextRequest();
                    }
                } else {
                    Log.w(this.TAG, "Device is not supported");
                }
            } else {
                Log.e(this.TAG, "onServicesDiscovered error " + status);
            }

        }

        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Log.d(this.TAG, "[Callback] Connection state changed with status: " + status + " and new state: " + newState + " (" + this.stateToString(newState) + ")");
            BleManager.this.mConnectionState = newState;
            if (status == 0 && newState == 2) {
                Log.i(this.TAG, "Connected to GATT server.");
                BleManager.this.mConnected = true;
                BleManager.this.mCallbacks.onDeviceConnected();
                BleManager.this.mHandler.postDelayed(new Runnable() {
                    @Keep
                    public void run() {
                        Log.i(BleManagerGattCallback.this.TAG, "Attempting to start service discovery:" + gatt.discoverServices());
                    }
                }, 600L);
            } else {
                BleManager.this.mConnected = false;
                if (newState == 0) {
                    if (status != 0) {
                        Log.w(this.TAG, "Error: (0x" + Integer.toHexString(status) + "): " + GattError.parseConnectionError(status));
                    }

                    this.onDeviceDisconnected();
                    BleManager.this.mConnected = false;
                    if (BleManager.this.isUserDisconnected) {
                        Log.i(this.TAG, "Disconnected");
                        BleManager.this.mCallbacks.onDeviceDisconnected();
                        BleManager.this.close();
                    } else {
                        Log.w(this.TAG, "Connection lost");
                        BleManager.this.mCallbacks.onLinklossOccur();
                    }
                }
            }

        }

        protected abstract void onDeviceDisconnected();

        public final void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                Log.i(this.TAG, "Read Response received from " + characteristic.getUuid() + ", value: " + ParserUtils.parse(characteristic));
                if (this.isBatteryLevelCharacteristic(characteristic)) {
                    int batteryValue = characteristic.getIntValue(17, 0);
                    BleManager.this.mCallbacks.onBatteryValueReceived(batteryValue);
                    this.nextRequest();
                } else {
                    this.onCharacteristicRead(gatt, characteristic);
                    this.nextRequest();
                }
            } else if (status == 5) {
                if (gatt.getDevice().getBondState() != 10) {
                    Log.w(this.TAG, "Phone has lost bonding information");
                }
            } else {
                Log.e(this.TAG, "onCharacteristicRead error " + status);
            }

        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                Log.i(this.TAG, "Data written to " + characteristic.getUuid() + ", value: " + ParserUtils.parse(characteristic.getValue()));
                this.onCharacteristicWrite(gatt, characteristic);
                this.nextRequest();
            } else if (status == 5) {
                if (gatt.getDevice().getBondState() != 10) {
                    Log.w(this.TAG, "Phone has lost bonding information");
                }
            } else {
                Log.e(this.TAG, "onCharacteristicRead error " + status);
            }

        }

        public final void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == 0) {
                Log.i(this.TAG, "Data written to descr. " + descriptor.getUuid() + ", value: " + ParserUtils.parse(descriptor));
                this.nextRequest();
            } else if (status == 5) {
                if (gatt.getDevice().getBondState() != 10) {
                    Log.w(this.TAG, "Phone has lost bonding information");
                }
            } else {
                Log.e(this.TAG, "onDescriptorWrite error " + status);
            }

        }

        public final void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String data = ParserUtils.parse(characteristic);
            BluetoothGattDescriptor cccd = characteristic.getDescriptor(BleManager.this.CLIENT_CHARACTERISTIC_CONFIG);
            boolean notifications = cccd == null || cccd.getValue() == null || cccd.getValue().length != 2 || cccd.getValue()[0] == 1;
            if (notifications) {
                Log.i(this.TAG, "Notification received from " + characteristic.getUuid() + ", value: " + data);
                this.onCharacteristicNotified(gatt, characteristic);
            } else {
                Log.i(this.TAG, "Indication received from " + characteristic.getUuid() + ", value: " + data);
                this.onCharacteristicIndicated(gatt, characteristic);
            }

        }

        protected void setCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleManager.this.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(value);
            gatt.writeDescriptor(descriptor);
        }

        public boolean equals(Object o) {
            return this.getClass().getSimpleName().equals(o.getClass().getSimpleName());
        }

        public void setReadBattery(boolean readBattery) {
            this.readBattery = readBattery;
        }

        public Context getContext() {
            return BleManager.this.mContext;
        }

        private boolean readBatteryLevel(BluetoothGatt gatt) {
            if (this.readBattery) {
                BluetoothGattService batteryService = gatt.getService(Service.BATTERY);
                if (batteryService == null) {
                    return false;
                } else {
                    BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic(Characteristic.BATTERY_LEVEL);
                    return batteryLevelCharacteristic == null ? false : gatt.readCharacteristic(batteryLevelCharacteristic);
                }
            } else {
                return false;
            }
        }

        private void nextRequest() {
            Queue<Request> requests = this.mInitQueue;
            Request request = (Request)requests.poll();
            if (request == null) {
                if (this.mInitInProgress) {
                    this.mInitInProgress = false;
                    this.onDeviceReady();
                }

            } else {
                switch(request.type) {
                    case READ:
                        BleManager.this.readCharacteristic(request.characteristic);
                        break;
                    case WRITE:
                        BluetoothGattCharacteristic characteristic = request.characteristic;
                        characteristic.setValue(request.value);
                        BleManager.this.writeCharacteristic(characteristic);
                        break;
                    case ENABLE_NOTIFICATIONS:
                        BleManager.this.enableNotifications(request.characteristic);
                        break;
                    case ENABLE_INDICATIONS:
                        BleManager.this.enableIndications(request.characteristic);
                }

            }
        }

        public void onDeviceReady() {
            BleManager.this.mCallbacks.onDeviceReady();
        }

        private boolean isBatteryLevelCharacteristic(BluetoothGattCharacteristic characteristic) {
            return characteristic == null ? false : Characteristic.BATTERY_LEVEL.equals(characteristic.getUuid());
        }

        private String stateToString(int state) {
            switch(state) {
                case 1:
                    return "CONNECTING";
                case 2:
                    return "CONNECTED";
                case 3:
                    return "DISCONNECTING";
                default:
                    return "DISCONNECTED";
            }
        }
    }

    public interface OnConnectListener {
        boolean connectable(BluetoothDevice var1, int var2, ScanRecord var3);
    }
}