package com.kangengine.bluebooth.blutboothutil.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public class ParserUtils {

   private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


   public static String parse(BluetoothGattCharacteristic characteristic) {
      return parse(characteristic.getValue());
   }

   public static String parse(BluetoothGattDescriptor descriptor) {
      return parse(descriptor.getValue());
   }

   public static String parse(byte[] data) {
      if(data != null && data.length != 0) {
         char[] out = new char[data.length * 3 - 1];

         for(int j = 0; j < data.length; ++j) {
            int v = data[j] & 255;
            out[j * 3] = HEX_ARRAY[v >>> 4];
            out[j * 3 + 1] = HEX_ARRAY[v & 15];
            if(j != data.length - 1) {
               out[j * 3 + 2] = 45;
            }
         }

         return "(0x) " + new String(out);
      } else {
         return "";
      }
   }

}
