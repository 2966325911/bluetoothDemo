package com.kangengine.bluebooth.blutboothutil.databean;


import com.kangengine.bluebooth.blutboothutil.utils.UrineDataDecode;
import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class UrineData {

   public String leu;
   public String bld;
   public String ph;
   public String pro;
   public String ubg;
   public String nit;
   public String vc;
   public String glu;
   public String bil;
   public String ket;
   public String sg;
   public Date time;


   public UrineData() {
      this.time = new Date();
   }

   public UrineData(byte[] data) {
      int ymd = (Utils.unsignedByteToInt(data[10]) << 8) + Utils.unsignedByteToInt(data[11]);
      int lmh = (Utils.unsignedByteToInt(data[12]) << 8) + Utils.unsignedByteToInt(data[13]);
      this.setDate(ymd & 127, ymd >> 7 & 15, ymd >> 11 & 31, lmh & 31, lmh >> 5 & 63);
      this.leu = UrineDataDecode.leu(lmh >> 11 & 7);
      int bppun = (Utils.unsignedByteToInt(data[14]) << 8) + Utils.unsignedByteToInt(data[15]);
      this.nit = UrineDataDecode.nit(bppun & 7);
      this.ubg = UrineDataDecode.ubg(bppun >> 3 & 7);
      this.pro = UrineDataDecode.pro(bppun >> 6 & 7);
      this.ph = UrineDataDecode.ph(bppun >> 9 & 7);
      this.bld = UrineDataDecode.bld(bppun >> 12 & 7);
      int vgbks = (Utils.unsignedByteToInt(data[16]) << 8) + Utils.unsignedByteToInt(data[17]);
      this.vc = UrineDataDecode.vc(vgbks >> 12 & 7);
      this.glu = UrineDataDecode.glu(vgbks >> 9 & 7);
      this.bil = UrineDataDecode.bil(vgbks >> 6 & 7);
      this.ket = UrineDataDecode.ket(vgbks >> 3 & 7);
      this.sg = UrineDataDecode.sg(vgbks & 7);
   }

   private void setDate(int year, int month, int day, int hour, int min) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(1, year + 2000);
      calendar.set(2, month - 1);
      calendar.set(5, day);
      calendar.set(11, hour);
      calendar.set(12, min);
      calendar.set(13, 0);
      this.time = calendar.getTime();
   }

   public String toString() {
      String str = "";
      str = str + "LEU：" + this.leu + "\n";
      str = str + "BLD：" + this.bld + "\n";
      str = str + "PH：" + this.ph + "\n";
      str = str + "PRO：" + this.pro + "\n";
      str = str + "UBG：" + this.ubg + "\n";
      str = str + "NIT：" + this.nit + "\n";
      str = str + "VC：" + this.vc + "\n";
      str = str + "GLU：" + this.glu + "\n";
      str = str + "BIL：" + this.bil + "\n";
      str = str + "KET：" + this.ket + "\n";
      str = str + "SG：" + this.sg + "\n";
      return str;
   }
}
