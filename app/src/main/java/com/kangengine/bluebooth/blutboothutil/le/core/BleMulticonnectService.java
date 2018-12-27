package com.kangengine.bluebooth.blutboothutil.le.core;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.util.Log;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BleMulticonnectService extends Service {

   private static final String TAG = "BleMulticonnectService";
   private static final int MAX_CONNECTIONS = 5;
   private BluetoothAdapter mBluetoothAdapter;
   private BleScanner scanner;
   private Handler mHandler;
   private List mBleManagers;
   @Nullable
   private BleManager mCurrentControlManager;
   private final Object mLock = new Object();
   private boolean rescanIfDisconnected = true;
   private final CallbacksHandler.EventBusCallback bleServiceBehavior = new CallbacksHandler.EventBusCallback() {
      @Override
      public void onDeviceDisconnected() {
         if(BleMulticonnectService.this.mCurrentControlManager != null && !BleMulticonnectService.this.mCurrentControlManager.isConnected()) {
            super.onDeviceDisconnected();
            this.restartScanIfNecessary();
         }

         BleMulticonnectService.this.recycleManager();
      }
      @Override
      public void onLinklossOccur() {
         if(BleMulticonnectService.this.mCurrentControlManager != null && !BleMulticonnectService.this.mCurrentControlManager.isConnected()) {
            super.onLinklossOccur();
            this.restartScanIfNecessary();
         }

         BleMulticonnectService.this.recycleManager();
      }
      @Override
      public void onDeviceReady() {
         super.onDeviceReady();
         Log.d("BleMulticonnectService", "Device init all done");
      }
      private void restartScanIfNecessary() {
         if(BleMulticonnectService.this.rescanIfDisconnected) {
            BleMulticonnectService.this.scanner.restartScan();
         }

      }
   };
   private final ScanCallback scanCallBack = new ScanCallback() {

      private List scanResults;

      public void onScanResult(int callbackType, ScanResult result) {}
      public void onBatchScanResults(List results) {
         this.scanResults = new ArrayList(results);
         Iterator var2 = this.scanResults.iterator();

         while(var2.hasNext()) {
            ScanResult result = (ScanResult)var2.next();
            Log.d("BleMulticonnectService", "onBatchScanResults(), Device=" + result.getDevice() + ", mDeviceName=" + (result.getScanRecord() != null?result.getScanRecord().getDeviceName():result.getDevice().getName()) + ", mRssi=" + result.getRssi());
            BleMulticonnectService.this.onDeviceScanned(result);
         }

      }
      public void onScanFailed(int errorCode) {}
   };


   @Nullable
   public IBinder onBind(Intent intent) {
      return new LocalBinder();
   }

   public boolean onUnbind(Intent intent) {
      return super.onUnbind(intent);
   }

   public void onCreate() {
      BluetoothManager mBluetoothManager = (BluetoothManager)this.getSystemService("bluetooth");
      if(mBluetoothManager == null) {
         Log.e("BleMulticonnectService", "Unable to initialize BluetoothManager.");
      } else {
         this.mBluetoothAdapter = mBluetoothManager.getAdapter();
         if(this.mBluetoothAdapter == null) {
            Log.e("BleMulticonnectService", "Unable to obtain a BluetoothAdapter.");
         }
      }

      this.mBleManagers = new ArrayList();
      this.scanner = new BleScanner(this, this.scanCallBack);
      this.mHandler = new Handler(this.getMainLooper());
   }

   public int onStartCommand(Intent intent, int flags, int startId) {
      this.enableBluetoothAdapter();
      return 1;
   }

   public void onDestroy() {
      super.onDestroy();
      if(this.mBleManagers != null && !this.mBleManagers.isEmpty()) {
         Iterator var1 = this.mBleManagers.iterator();

         while(var1.hasNext()) {
            BleManager manager = (BleManager)var1.next();
            manager.close();
         }

         this.mBleManagers.clear();
      }

      this.mBleManagers = null;
      this.scanner.stopScan();
      this.scanner = null;
      if(this.mHandler != null) {
         this.mHandler.removeCallbacksAndMessages((Object)null);
      }

      this.mBluetoothAdapter = null;
      Log.i("BleMulticonnectService", "Service destroyed");
   }

   private void enableBluetoothAdapter() {
      this.mBluetoothAdapter.enable();
   }

   protected void onDeviceScanned(ScanResult result) {
      final BluetoothDevice device = result.getDevice();
      if(this.mCurrentControlManager != null && !this.mCurrentControlManager.isConnected() && this.mCurrentControlManager.connectable(device, result.getRssi(), result.getScanRecord())) {
         this.scanner.stopScan();
         Object var3 = this.mLock;
         synchronized(this.mLock) {
            this.mHandler.postDelayed(new Runnable() {
               @Keep
               public void run() {
                  if(BleMulticonnectService.this.mCurrentControlManager != null) {
                     boolean isConnectionInitiated = BleMulticonnectService.this.mCurrentControlManager.connect(device);
                     Log.d("BleMulticonnectService", "Connection initialized:" + isConnectionInitiated);
                  }

               }
            }, 300L);
         }
      }

   }

   private void recycleManager() {
      if(this.mBleManagers.size() == 5) {
         Iterator iterator = this.mBleManagers.iterator();

         while(iterator.hasNext()) {
            BleManager manager = (BleManager)iterator.next();
            if(manager != this.mCurrentControlManager && !manager.isConnected()) {
               manager.close();
               iterator.remove();
            }
         }
      }

   }

   public class LocalBinder extends Binder {

      public BleMulticonnectService getService() {
         return BleMulticonnectService.this;
      }

      public boolean addBleManager(@Nullable BleManager manager) {
         BleMulticonnectService.this.recycleManager();
         if(BleMulticonnectService.this.mBleManagers.size() < 5) {
            BleMulticonnectService.this.mCurrentControlManager = manager;
            if(manager == null) {
               return true;
            } else if(BleMulticonnectService.this.mBleManagers.contains(manager)) {
               if(manager.isConnected()) {
                  return false;
               } else {
                  BleMulticonnectService.this.scanner.startScan(false);
                  return true;
               }
            } else {
               BleMulticonnectService.this.mBleManagers.add(manager);
               BleMulticonnectService.this.mCurrentControlManager.setGattCallbacks(BleMulticonnectService.this.bleServiceBehavior);
               BleMulticonnectService.this.scanner.startScan(false);
               return true;
            }
         } else {
            Log.d("BleMulticonnectService", "Reached max connections ");
            return false;
         }
      }

      public int getConnectionState() {
         return BleMulticonnectService.this.mCurrentControlManager == null?0:BleMulticonnectService.this.mCurrentControlManager.getConnectionState();
      }

      public void scanBleDevice(boolean autoEnable) {
         BleMulticonnectService.this.scanner.startScan(autoEnable);
      }

      public void stopScanBleDevice() {
         BleMulticonnectService.this.scanner.stopScan();
      }

      public void setIntervals(int scanInterval, int pauseInterval) {
         BleMulticonnectService.this.scanner.setIntervals(scanInterval, pauseInterval);
      }
   }
}
