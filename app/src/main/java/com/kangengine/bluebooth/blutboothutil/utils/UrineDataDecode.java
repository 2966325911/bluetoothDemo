package com.kangengine.bluebooth.blutboothutil.utils;


public class UrineDataDecode {

   public static String leu(int i) {
      return offsetMinusOne(i);
   }

   public static String nit(int i) {
      return i == 0?"-":"+";
   }

   public static String ubg(int i) {
      return offsetZero(i);
   }

   public static String pro(int i) {
      return offsetMinusOne(i);
   }

   public static String ph(int i) {
      switch(i) {
      case 0:
         return "5.0";
      case 1:
         return "6.0";
      case 2:
         return "6.5";
      case 3:
         return "7.0";
      case 4:
         return "7.5";
      case 5:
         return "8.0";
      case 6:
         return "8.5";
      default:
         return null;
      }
   }

   public static String bld(int i) {
      return offsetMinusOne(i);
   }

   public static String sg(int i) {
      switch(i) {
      case 0:
         return "1.000";
      case 1:
         return "1.005";
      case 2:
         return "1.010";
      case 3:
         return "1.015";
      case 4:
         return "1.020";
      case 5:
         return "1.025";
      case 6:
         return "1.030";
      default:
         return null;
      }
   }

   public static String ket(int i) {
      return offsetMinusOne(i);
   }

   public static String bil(int i) {
      return offsetZero(i);
   }

   public static String glu(int i) {
      return offsetMinusOne(i);
   }

   public static String vc(int i) {
      return offsetMinusOne(i);
   }

   private static String offsetZero(int i) {
      switch(i) {
      case 0:
         return "-";
      default:
         return formatInt(i, 0);
      }
   }

   public static String offsetMinusOne(int i) {
      switch(i) {
      case 0:
         return "-";
      case 1:
         return "+-";
      default:
         return formatInt(i, -1);
      }
   }

   private static String formatInt(int i, int offset) {
      return "+" + (i + offset);
   }
}
