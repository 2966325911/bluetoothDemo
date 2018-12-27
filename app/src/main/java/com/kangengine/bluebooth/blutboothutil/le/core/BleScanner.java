package com.kangengine.bluebooth.blutboothutil.le.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Keep;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.support.v18.scanner.ScanSettings.Builder;

public class BleScanner {

   private static final String TAG = "BleScanner";
   private int scanInterval = 0;
   private int pauseInterval = 0;
   private boolean isScanning;
   private boolean isLoopingScanning;
   private WeakReference context;
   private Handler mHandler;
   private BluetoothAdapter mBluetoothAdapter;
   private ScanCallback scanCallBack;
   private List mFilters;
   private Runnable scanRunnable = new Runnable() {
      @Keep
      public void run() {
         BleScanner.this.startScanInternal(BleScanner.this.mFilters);
         if(BleScanner.this.mHandler != null && BleScanner.this.scanInterval > 0) {
            BleScanner.this.mHandler.postDelayed(BleScanner.this.scanSleepRunnable, (long)BleScanner.this.scanInterval);
         }

      }
   };
   private Runnable scanSleepRunnable = new Runnable() {
      @Keep
      public void run() {
         BleScanner.this.stopScanInternal();
         if(BleScanner.this.mHandler != null && BleScanner.this.pauseInterval > 0) {
            BleScanner.this.mHandler.postDelayed(BleScanner.this.scanRunnable, (long)BleScanner.this.pauseInterval);
         }

      }
   };


   public BleScanner(Context context, ScanCallback scanCallBack) {
      this.context = new WeakReference(context);
      this.scanCallBack = scanCallBack;
      this.mHandler = new Handler(context.getMainLooper());
      BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService("bluetooth");
      if(bluetoothManager != null) {
         this.mBluetoothAdapter = bluetoothManager.getAdapter();
      }

   }

   public void setIntervals(int scanInterval, int pauseInterval) {
      if(scanInterval > 0 && pauseInterval > 0) {
         this.scanInterval = scanInterval * 1000;
         this.pauseInterval = pauseInterval * 1000;
      } else {
         throw new IllegalArgumentException("scanInterval and pauseInterval must be > 0");
      }
   }

   public final void startScan(boolean autoEnable) {
      if(!this.isLoopingScanning) {
         this.startScanInternal(this.mFilters);
         if(this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages((Object)null);
            if(this.scanInterval > 0) {
               this.mHandler.postDelayed(this.scanSleepRunnable, (long)this.scanInterval);
            }
         }

         this.isLoopingScanning = true;
         Log.d("BleScanner", "Start to scan...");
      }

   }

   public final void restartScan() {
      this.mHandler.postDelayed(new Runnable() {
         @Keep
         public void run() {
            BleScanner.this.startScan(false);
         }
      }, 500L);
   }

   private void startScanInternal(List filters) {
      if(!this.isScanning) {
         if(!this.isBluetoothEnabled()) {
            if(this.context.get() != null) {
               Toast.makeText((Context)this.context.get(), "请打开蓝牙后再继续", 0).show();
            }
         } else {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            ScanSettings settings = (new Builder()).setScanMode(2).setReportDelay(1000L).setUseHardwareBatchingIfSupported(false).build();
            scanner.startScan(filters, settings, this.scanCallBack);
            Log.i("BleScanner", "Start BLE scan internal");
            this.isScanning = true;
         }
      } else {
         Log.e("BleScanner", "Scanner already started");
      }

   }

   public final void stopScan() {
      if(this.isLoopingScanning) {
         this.isLoopingScanning = false;
         this.stopScanInternal();
         if(this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages((Object)null);
         }

         Log.d("BleScanner", "Stop scanning...");
      }

   }

   public void setFilters(List filters) {
      this.mFilters = filters;
   }

   private void stopScanInternal() {
      if(this.isScanning) {
         Log.i("BleScanner", "Stop BLE scan internal");
         BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
         scanner.stopScan(this.scanCallBack);
         this.isScanning = false;
      } else {
         Log.e("BleScanner", "Scanner already stopped");
      }

   }

   private boolean isBluetoothEnabled() {
      return this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled();
   }
}
