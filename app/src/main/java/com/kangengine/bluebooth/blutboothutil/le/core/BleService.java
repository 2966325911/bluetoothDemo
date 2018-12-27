package com.kangengine.bluebooth.blutboothutil.le.core;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Keep;
import android.util.Log;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

@SuppressLint({"NewApi"})
public class BleService extends Service {

   private static final String TAG = BleService.class.getSimpleName();
   private BluetoothAdapter mBluetoothAdapter;
   protected BleManager mBleManager;
   protected BleScanner scanner;
   private Handler mHandler;
   private final Object mLock = new Object();
   private final CallbacksHandler.EventBusCallback bleServiceBehavior = new CallbacksHandler.EventBusCallback() {
      @Override
      public void onDeviceDisconnected() {
         super.onDeviceDisconnected();
         this.restartScanIfNecessary();
      }
      @Override
      public void onLinklossOccur() {
         super.onLinklossOccur();
         this.restartScanIfNecessary();
      }
      @Override
      public void onDeviceReady() {
         super.onDeviceReady();
         Log.d(BleService.TAG, "Device init all done");
      }
      private void restartScanIfNecessary() {
         BleService.this.scanner.restartScan();
      }
   };
   private final ScanCallback scanCallBack = new ScanCallback() {

      private List scanResults;

      @Override
      @Keep
      public void onScanResult(int callbackType, ScanResult result) {}
      @Keep
      public void onBatchScanResults(List results) {
         this.scanResults = new ArrayList(results);
         Iterator var2 = this.scanResults.iterator();

         while(var2.hasNext()) {
            ScanResult result = (ScanResult)var2.next();
            Log.d(BleService.TAG, "onBatchScanResults(), Device=" + result.getDevice() + ", mDeviceName=" + (result.getScanRecord() != null?result.getScanRecord().getDeviceName():result.getDevice().getName()) + ", mRssi=" + result.getRssi());
            BleService.this.onDeviceScanned(result);
         }

      }
      @Keep
      public void onScanFailed(int errorCode) {}
   };


   public IBinder onBind(Intent intent) {
      return new LocalBinder();
   }

   public boolean onUnbind(Intent intent) {
      return super.onUnbind(intent);
   }

   public void onCreate() {
      BluetoothManager mBluetoothManager = (BluetoothManager)this.getSystemService("bluetooth");
      if(mBluetoothManager == null) {
         Log.e(TAG, "Unable to initialize BluetoothManager.");
      } else {
         this.mBluetoothAdapter = mBluetoothManager.getAdapter();
         if(this.mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
         } else {
            this.scanner = new BleScanner(this, this.scanCallBack);
            this.mHandler = new Handler(this.getMainLooper());
         }
      }
   }

   public int onStartCommand(Intent intent, int flags, int startId) {
      this.enableBluetoothAdapter();
      return 1;
   }

   public void onDestroy() {
      super.onDestroy();
      if(this.mBleManager != null) {
         this.mBleManager.close();
         this.mBleManager = null;
      }

      this.scanner.stopScan();
      this.scanner = null;
      if(this.mHandler != null) {
         this.mHandler.removeCallbacksAndMessages((Object)null);
      }

      this.mBluetoothAdapter = null;
      Log.i(TAG, "Service destroyed");
   }

   private void enableBluetoothAdapter() {
      this.mBluetoothAdapter.enable();
   }

   public final int getConnectionState() {
      return this.mBleManager == null?0:this.mBleManager.getConnectionState();
   }

   protected void onDeviceScanned(ScanResult result) {
      final BluetoothDevice device = result.getDevice();
      if(this.mBleManager != null && !this.mBleManager.isConnected() && this.mBleManager.connectable(device, result.getRssi(), result.getScanRecord())) {
         this.scanner.stopScan();
         Object var3 = this.mLock;
         synchronized(this.mLock) {
            this.mHandler.postDelayed(new Runnable() {
               @Keep
               public void run() {
                  if(BleService.this.mBleManager != null) {
                     boolean isConnectionInitiated = BleService.this.mBleManager.connect(device);
                     Log.d(BleService.TAG, "Connection initialized:" + isConnectionInitiated);
                  }

               }
            }, 300L);
         }
      }

   }

   protected final boolean setManager(BleManager manager) {
      return this.setManager(manager, this.bleServiceBehavior);
   }

   protected final boolean setManager(BleManager manager, BleManagerCallbacks callbacks) {
      if(this.mBleManager != null && this.mBleManager.equals(manager) && this.mBleManager.isConnected()) {
         return false;
      } else {
         if(this.mBleManager != null && this.mBleManager.isConnected()) {
            this.mBleManager.close();
         }

         this.mBleManager = manager;
         if(manager != null) {
            manager.setGattCallbacks(callbacks);
            this.scanner.startScan(false);
         }

         return true;
      }
   }

   protected final void stopScan() {
      this.scanner.stopScan();
   }

   protected final void startScan(boolean autoEnable) {
      this.scanner.startScan(autoEnable);
   }


   public class LocalBinder extends Binder {

      public BleService getService() {
         return BleService.this;
      }

      public boolean setBleManager(BleManager manager) {
         return BleService.this.setManager(manager);
      }

      public boolean setBleManager(BleManager manager, BleManagerCallbacks callbacks) {
         return BleService.this.setManager(manager, callbacks);
      }

      public int getConnectionState() {
         return BleService.this.getConnectionState();
      }

      public void scanBleDevice(boolean autoEnable) {
         BleService.this.startScan(autoEnable);
      }

      public void scanBleDevice() {
         BleService.this.startScan(false);
      }

      public void stopScanBleDevice() {
         BleService.this.stopScan();
      }

      public void disconnect() {
         if(BleService.this.mBleManager != null && BleService.this.mBleManager.isConnected()) {
            BleService.this.mBleManager.disconnect();
         }

      }

      public void setIntervals(int scanInterval, int pauseInterval) {
         BleService.this.scanner.setIntervals(scanInterval, pauseInterval);
      }

      public BluetoothDevice getDevice() {
         return BleService.this.mBleManager != null?BleService.this.mBleManager.getBluetoothDevice():null;
      }

      public void setScanFilters(List scanFilters) {
         if(BleService.this.scanner != null) {
            BleService.this.scanner.setFilters(scanFilters);
         }

      }
   }
}
