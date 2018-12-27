package com.kangengine.bluebooth.blutboothutil.utils;

import java.math.BigDecimal;

public class Utils {

   public static int decimalToHex(int n) {
      return Integer.parseInt(Integer.toHexString(n), 16);
   }

   public static float multiply(float v1, float v2) {
      BigDecimal b1 = new BigDecimal(Float.toString(v1));
      BigDecimal b2 = new BigDecimal(Float.toString(v2));
      return b1.multiply(b2).setScale(1, 1).floatValue();
   }

   public static double multiply2(float data, int digit) {
      BigDecimal b   =   new  BigDecimal(data);
      double  endResult = 0;
      try {
         endResult   =   b.setScale(digit,  BigDecimal.ROUND_HALF_UP).doubleValue();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return endResult;
   }

   private static String retainOneDecimal(String val) {
      if(val.endsWith(".")) {
         val = val.substring(0, val.length() - 1);
      }

      String[] strArr = val.split("\\.");
      if(strArr.length == 2 && strArr[1].length() > 1) {
         val = strArr[0] + "." + strArr[1].substring(0, 1);
      }

      return val;
   }

   public static String byteToBit(byte b) {
      return "" + (byte)(b >> 7 & 1) + (byte)(b >> 6 & 1) + (byte)(b >> 5 & 1) + (byte)(b >> 4 & 1) + (byte)(b >> 3 & 1) + (byte)(b >> 2 & 1) + (byte)(b >> 1 & 1) + (byte)(b >> 0 & 1);
   }

   public static int bitToInt(String s) {
      return Integer.valueOf(s, 2).intValue();
   }

   public static int unsignedBytesToInt(byte b0, byte b1) {
      return unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8);
   }

   public static int unsignedByteToInt(byte b) {
      return b & 255;
   }
}
