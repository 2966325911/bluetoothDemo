package com.kangengine.bluebooth.blutboothutil;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {
    private static final String TAG = BluetoothService.class.getSimpleName();
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private String deviceName;
    private BluetoothService.AcceptThread mSecureAcceptThread;
    private BluetoothService.AcceptThread mInsecureAcceptThread;
    private BluetoothService.ConnectThread mConnectThread;
    private BluetoothService.ConnectedThread mConnectedThread;
    private int mState = 0;
    private static final UUID UUID_ANDROID_DEVICE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID UUID_OTHER_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    private boolean isAndroid = false;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private int bufferSize = 256;
    private EventBus mEventBus;
    private OnDataRead onDataRead;

    public BluetoothService(boolean isAndroid) {
        this.isAndroid = isAndroid;
        this.mEventBus = EventBus.getDefault();
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.mState + " -> " + state);
        this.mState = state;
        if (this.isConnected && this.mState != 3) {
            this.postMessage(4608, 0);
            this.isConnected = false;
        }

        if (!this.isConnecting && this.mState == 2) {
            this.isConnecting = true;
        }

    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.setState(1);
        if (this.mSecureAcceptThread == null) {
            this.mSecureAcceptThread = new BluetoothService.AcceptThread(true);
            this.mSecureAcceptThread.start();
        }

        if (this.mInsecureAcceptThread == null) {
            this.mInsecureAcceptThread = new BluetoothService.AcceptThread(false);
            this.mInsecureAcceptThread.start();
        }

    }

    public synchronized void connect(BluetoothDevice device) {
        this.connect(device, true);
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        this.deviceName = device.getName();
        Log.d(TAG, "connect to: " + device);
        if (this.mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectThread = new BluetoothService.ConnectThread(device, secure);
        this.mConnectThread.start();
        this.setState(2);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        if (this.mSecureAcceptThread != null) {
            this.mSecureAcceptThread.cancel();
            this.mSecureAcceptThread = null;
        }

        if (this.mInsecureAcceptThread != null) {
            this.mInsecureAcceptThread.cancel();
            this.mInsecureAcceptThread = null;
        }

        this.mConnectedThread = new BluetoothService.ConnectedThread(socket, socketType);
        this.mConnectedThread.start();
        this.postMessage(4608, 0);
        this.setState(3);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        if (this.mSecureAcceptThread != null) {
            this.mSecureAcceptThread.cancel();
            this.mSecureAcceptThread = null;
        }

        if (this.mInsecureAcceptThread != null) {
            this.mInsecureAcceptThread.cancel();
            this.mInsecureAcceptThread = null;
        }

        this.setState(0);
    }

    public void write(byte[] out) {
        BluetoothService.ConnectedThread r;
        synchronized(this) {
            if (this.mState != 3) {
                return;
            }

            r = this.mConnectedThread;
        }

        r.write(out);
    }

    public void write(int oneByte) {
        BluetoothService.ConnectedThread r;
        synchronized(this) {
            if (this.mState != 3) {
                return;
            }

            r = this.mConnectedThread;
        }

        r.write(oneByte);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setOnDataRead(OnDataRead onDataRead) {
        this.onDataRead = onDataRead;
    }

    private void connectionFailed() {
        this.postMessage(4609, -1);
    }

    private void connectionLost() {
        this.postMessage(4610, -1);
    }

    private void postMessage(int what, int arg1) {
        Message message = new Message();
        message.what = what;
        message.arg1 = arg1;
        message.obj = this.deviceName;
        this.mEventBus.post(message);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(BluetoothService.TAG, "create ConnectedThread: " + socketType);
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException var7) {
                Log.e(BluetoothService.TAG, "temp sockets not created", var7);
            }

            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(BluetoothService.TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[BluetoothService.this.bufferSize];

            while(true) {
                try {
                    int bytes = this.mmInStream.read(buffer);
                    if (BluetoothService.this.onDataRead != null) {
                        BluetoothService.this.onDataRead.onRead(bytes, buffer);
                    }
                } catch (IOException var4) {
                    Log.e(BluetoothService.TAG, "disconnected from " + BluetoothService.this.deviceName);
                    BluetoothService.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
            } catch (IOException var3) {
                Log.e(BluetoothService.TAG, "Exception during write", var3);
            }

        }

        public void write(int oneByte) {
            try {
                this.mmOutStream.write(oneByte);
            } catch (IOException var3) {
                Log.e(BluetoothService.TAG, "Exception during write", var3);
            }

        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", var2);
            }

        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            this.mSocketType = secure ? "Secure" : "Insecure";

            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(BluetoothService.this.isAndroid ? BluetoothService.UUID_ANDROID_DEVICE : BluetoothService.UUID_OTHER_DEVICE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(BluetoothService.this.isAndroid ? BluetoothService.UUID_ANDROID_DEVICE : BluetoothService.UUID_OTHER_DEVICE);
                }
            } catch (IOException var6) {
                Log.e(BluetoothService.TAG, "Socket Type: " + this.mSocketType + "create() failed", var6);
            }

            this.mmSocket = tmp;
        }

        public void run() {
            Log.i(BluetoothService.TAG, "BEGIN mConnectThread SocketType:" + this.mSocketType);
            this.setName("ConnectThread" + this.mSocketType);
            if (BluetoothService.this.mAdapter.isDiscovering()) {
                BluetoothService.this.mAdapter.cancelDiscovery();
            }

            try {
                this.mmSocket.connect();
            } catch (IOException var6) {
                try {
                    this.mmSocket.close();
                } catch (IOException var4) {
                    Log.e(BluetoothService.TAG, "unable to close() " + this.mSocketType + " socket during connection failure", var4);
                }

                Log.e(BluetoothService.TAG, "connect to " + this.mmDevice.getName() + " failed");
                BluetoothService.this.connectionFailed();
                return;
            }

            BluetoothService var1 = BluetoothService.this;
            synchronized(BluetoothService.this) {
                BluetoothService.this.mConnectThread = null;
            }

            BluetoothService.this.connected(this.mmSocket, this.mmDevice, this.mSocketType);
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                Log.e(BluetoothService.TAG, "close() of connect " + this.mSocketType + " socket failed", var2);
            }

        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            this.mSocketType = secure ? "Secure" : "Insecure";

            try {
                if (secure) {
                    tmp = BluetoothService.this.mAdapter.listenUsingRfcommWithServiceRecord("BluetoothChatSecure", BluetoothService.this.isAndroid ? BluetoothService.UUID_ANDROID_DEVICE : BluetoothService.UUID_OTHER_DEVICE);
                } else {
                    tmp = BluetoothService.this.mAdapter.listenUsingInsecureRfcommWithServiceRecord("BluetoothChatInsecure", BluetoothService.this.isAndroid ? BluetoothService.UUID_ANDROID_DEVICE : BluetoothService.UUID_OTHER_DEVICE);
                }
            } catch (IOException var5) {
                Log.e(BluetoothService.TAG, "Socket Type: " + this.mSocketType + "listen() failed", var5);
            }

            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d(BluetoothService.TAG, "Socket Type: " + this.mSocketType + "BEGIN mAcceptThread" + this);
            this.setName("AcceptThread" + this.mSocketType);
            BluetoothSocket socket = null;

            while(BluetoothService.this.mState != 3) {
                try {
                    socket = this.mmServerSocket.accept();
                } catch (IOException var7) {
                    Log.e(BluetoothService.TAG, "Socket Type: " + this.mSocketType + "accept() failed", var7);
                    break;
                }

                if (socket != null) {
                    BluetoothService var2 = BluetoothService.this;
                    synchronized(BluetoothService.this) {
                        switch(BluetoothService.this.mState) {
                            case 0:
                            case 3:
                                try {
                                    socket.close();
                                } catch (IOException var5) {
                                    Log.e(BluetoothService.TAG, "Could not close unwanted socket", var5);
                                }
                                break;
                            case 1:
                            case 2:
                                BluetoothService.this.connected(socket, socket.getRemoteDevice(), this.mSocketType);
                        }
                    }
                }
            }

            Log.i(BluetoothService.TAG, "END mAcceptThread, socket Type: " + this.mSocketType);
        }

        public void cancel() {
            Log.d(BluetoothService.TAG, "Socket Type" + this.mSocketType + "cancel " + this);

            try {
                this.mmServerSocket.close();
            } catch (IOException var2) {
                Log.e(BluetoothService.TAG, "Socket Type" + this.mSocketType + "close() of server failed", var2);
            }

        }
    }

    public interface State {
        int STATE_CONNECTED = 0;
        int STATE_DISCONNECTED = 1;
    }
}
