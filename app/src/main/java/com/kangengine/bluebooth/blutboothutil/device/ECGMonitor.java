package com.kangengine.bluebooth.blutboothutil.device;

import android.os.Message;


import com.kangengine.bluebooth.blutboothutil.OnDataRead;
import com.kangengine.bluebooth.blutboothutil.WriteData;
import com.kangengine.bluebooth.blutboothutil.databean.ECGData;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import org.greenrobot.eventbus.EventBus;

public class ECGMonitor implements OnDataRead {

   public static final String DEVICE_NAME = "ECG:HC-201B";
   private static final byte[] ACK_HELLO = new byte[]{(byte)85, (byte)1, (byte)1, (byte)-86, (byte)10};
   private static final byte[] ACK_TOTAL_LENGTH = new byte[]{(byte)85, (byte)2, (byte)0, (byte)0, (byte)10};
   private int n;
   private int timeN = 0;
   private byte packageN = 0;
   private int packageLen = 30;
   private byte temp1;
   private byte byteLenH = 0;
   private byte byteLenL = 0;
   private byte[] dataArray;
   private WriteData writeData;


   public ECGMonitor(WriteData writeData) {
      this.writeData = writeData;
   }

   public void onRead(int len, byte[] data) {
      ++this.n;
      if(this.packageN == 0 && this.timeN == 0 && this.n == 1) {
         this.dataArray = new byte[28];
      }

      this.addData(data[0]);
      if(this.timeN == 3 && this.n == 9) {
         this.byteLenL = data[0];
      }

      if(this.timeN == 3 && this.n == 10) {
         this.byteLenH = data[0];
      }

      if(this.timeN == 3 && this.n == 2) {
         this.temp1 = data[0];
      }

      if(this.timeN == 3 && this.n == 11) {
         this.packageLen = Utils.unsignedBytesToInt(this.byteLenL, this.byteLenH) + 10;
      }

      if(len > 0) {
         byte[] b1;
         int var4;
         int var5;
         byte b;
         if(this.n == 4 && data[0] == 85 && this.timeN == 0) {
            b1 = ACK_HELLO;
            var4 = b1.length;

            for(var5 = 0; var5 < var4; ++var5) {
               b = b1[var5];
               this.write(b);
            }

            this.n = 0;
            this.timeN = 1;
         }

         if(data[0] == 2 && this.timeN == 1) {
            b1 = ACK_TOTAL_LENGTH;
            var4 = b1.length;

            for(var5 = 0; var5 < var4; ++var5) {
               b = b1[var5];
               this.write(b);
            }

            this.timeN = 2;
         }

         if((data[0] == 3 || data[0] == 4) && this.timeN == 2) {
            this.n = 2;
            this.timeN = 3;
            this.temp1 = data[0];
            this.packageN = 0;
         }

         if(this.n == this.packageLen && this.timeN == 3) {
            ++this.packageN;
            b1 = new byte[]{(byte)85, this.temp1, this.packageN, (byte)0, (byte)10};
            byte[] var8 = b1;
            var5 = b1.length;

            for(int var9 = 0; var9 < var5; ++var9) {
               byte b2 = var8[var9];
               this.write(b2);
            }

            this.n = 0;
            if(this.packageN == 30) {
               this.timeN = 0;
               this.packageN = 0;
               this.decodeData();
            }
         }
      }

   }

   private void addData(byte b) {
      if(this.packageN == 0 && this.n > 10 && this.n < 39) {
         this.dataArray[this.n - 11] = b;
      }

   }

   private void decodeData() {
      ECGData ecgData = new ECGData(this.dataArray);
      Message message = new Message();
      message.what = 4611;
      message.obj = ecgData;
      EventBus.getDefault().post(message);
   }

   private void write(byte b) {
      if(this.writeData != null) {
         this.writeData.write(b);
      }

   }

}
