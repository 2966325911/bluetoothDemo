package com.kangengine.bluebooth.blutboothutil.databean;


import com.kangengine.bluebooth.blutboothutil.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class ECGData {

   private Date date;
   private float heartRate;
   private float pR;
   private float QT;
   private float rV;
   private float pV;
   private float tV;
   private float stV;
   private String stAssessment;
   private String arrhythima;
   private String waveQuality;
   private String heartRateAssessment;


   public ECGData() {
      this.date = new Date();
      this.stAssessment = "00";
      this.arrhythima = "0";
      this.waveQuality = "0";
      this.heartRateAssessment = "000";
   }

   public ECGData(byte[] bytes) {
      byte offset = 0;
      Calendar calendar = Calendar.getInstance();
      calendar.set(1, Utils.unsignedByteToInt(bytes[0]) + 2000);
      calendar.set(2, Utils.unsignedByteToInt(bytes[1]) - 1);
      calendar.set(5, Utils.unsignedByteToInt(bytes[2]));
      calendar.set(11, Utils.unsignedByteToInt(bytes[3]));
      calendar.set(12, Utils.unsignedByteToInt(bytes[4]));
      calendar.set(13, Utils.unsignedByteToInt(bytes[5]));
      this.date = calendar.getTime();
      int offset1 = offset + 7;
      String result = Utils.byteToBit(bytes[offset1]);
      this.stAssessment = result.substring(0, 2);
      this.arrhythima = result.substring(2, 3);
      this.waveQuality = result.substring(3, 4);
      this.heartRateAssessment = result.substring(4, 7);
      offset1 += 7;
      this.heartRate = (float)Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) / 10.0F;
      offset1 += 2;
      this.pR = (float)(Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) * 2);
      offset1 += 2;
      this.QT = (float)(Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) * 2);
      offset1 += 2;
      this.rV = (float)Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) / 1000.0F;
      offset1 += 2;
      this.pV = (float)Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) / 1000.0F;
      offset1 += 2;
      this.tV = (float)Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) / 1000.0F;
      offset1 += 2;
      this.stV = (float)Utils.unsignedBytesToInt(bytes[offset1], bytes[offset1 + 1]) / 1000.0F;
   }

   public Date getDate() {
      return this.date;
   }

   public float getHeartRate() {
      return this.heartRate;
   }

   public float getpR() {
      return this.pR;
   }

   public float getQT() {
      return this.QT;
   }

   public float getrV() {
      return this.rV;
   }

   public float getpV() {
      return this.pV;
   }

   public float gettV() {
      return this.tV;
   }

   public float getStV() {
      return this.stV;
   }

   public String getStAssessment() {
      return this.stAssessment;
   }

   public String getArrhythima() {
      return this.arrhythima;
   }

   public String getWaveQuality() {
      return this.waveQuality;
   }

   public String getHeartRateAssessment() {
      return this.heartRateAssessment;
   }

   public void setHeartRate(float heartRate) {
      this.heartRate = heartRate;
   }

   public void setpR(float pR) {
      this.pR = pR;
   }

   public void setQT(float QT) {
      this.QT = QT;
   }

   public void setrV(float rV) {
      this.rV = rV;
   }

   public void setpV(float pV) {
      this.pV = pV;
   }

   public void settV(float tV) {
      this.tV = tV;
   }

   public void setStV(float stV) {
      this.stV = stV;
   }

   public String toString() {
      String str = "心率：" + this.heartRate + "\n";
      str = str + "ST段：" + this.stV + "\n";
      str = str + "PR间期：" + this.pR + "ms\n";
      str = str + "QT间期：" + this.QT + "ms\n";
      str = str + "P波：" + this.pV + "mv\n";
      str = str + "R波：" + this.rV + "mv\n";
      str = str + "T波：" + this.tV + "mv\n";
      return str;
   }
}
