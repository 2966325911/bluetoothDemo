package com.kangengine.bluebooth.blutboothutil.utils;

import com.holtek.libHTBodyfat.HTPeopleGeneral;
import com.kangengine.bluebooth.blutboothutil.databean.WeightData;

import java.text.DecimalFormat;

public class WeightParser {

   private static DecimalFormat dfc = new DecimalFormat("#.#");


   public static WeightData parse(byte[] data) {
      int v = data[0] & 255;
      String typeRec = "脂肪秤";
      if(v == 207) {
         typeRec = "脂肪秤";
      } else if(v == 206) {
         typeRec = "人体秤";
      } else if(v == 203) {
         typeRec = "婴儿秤";
      } else if(v == 202) {
         typeRec = "厨房秤";
      }

      int level = data[1] >> 4 & 15;
      int group = data[1] & 15;
      String levelRec = "普通";
      if(level == 0) {
         levelRec = "普通";
      } else if(level == 1) {
         levelRec = "业余";
      } else if(level == 2) {
         levelRec = "专业";
      }

      int sex = data[2] >> 7 & 1;
      int age = data[2] & 127;
      int height = data[3] & 255;
      int weight = data[4] << 8 | data[5] & 255;
      float scale = 0.1F;
      if(v == 207) {
         scale = 0.1F;
      } else if(v == 206) {
         scale = 0.1F;
      } else if(v == 203) {
         scale = 0.01F;
      } else if(v == 202) {
         scale = 0.001F;
      }

      float weightRec = scale * (float)weight;
      if(weightRec < 0.0F) {
         weightRec *= -1.0F;
      }

      int zhifang = data[6] << 8 | data[7] & 255;
      float zhifangRate = (float)((double)zhifang * 0.1D);
      int guge = data[8] & 255;
      float bonesWeight = (float)guge * 0.1F;
      int jirou = data[9] << 8 | data[10] & 255;
      float jirouRate = (float)((double)jirou * 0.1D);
      int neizanglevel = data[11] & 255;
      int water = data[12] << 8 | data[13];
      float waterRate = (float)((double)water * 0.1D);
      int hot = data[14] << 8 | data[15] & 255;
      int bodyAge = 0;
      if(data.length == 17) {
         bodyAge = data[16] & 255;
      }

      WeightData weightData = new WeightData();
      weightData.setType(typeRec);
      weightData.setLevel(levelRec);
      weightData.setSex(sex);
      weightData.setAge(age);
      weightData.setHeight(height);
      weightData.setWeight(dfc.format(weightRec < 0.0F?(double)(-weightRec):(double)weightRec));
      weightData.setFat(dfc.format(zhifangRate < 0.0F?(double)(-zhifangRate):(double)zhifangRate));
      weightData.setBones(dfc.format(bonesWeight < 0.0F?(double)(-bonesWeight):(double)bonesWeight));
      weightData.setMuscle(dfc.format(jirouRate < 0.0F?(double)(-jirouRate):(double)jirouRate));
      weightData.setVisceralFat(dfc.format(neizanglevel < 0?(long)(-neizanglevel):(long)neizanglevel));
      weightData.setWaterRate(dfc.format(waterRate < 0.0F?(double)(-waterRate):(double)waterRate));
      weightData.setBmr(dfc.format(hot < 0?(long)(-hot):(long)hot));
      weightData.setBodyAge(bodyAge);
      return weightData;
   }

   public static WeightData parse(double weight, HTPeopleGeneral bodyfat) {
      WeightData weightData = new WeightData();
      weightData.setHeight((int)bodyfat.heightCm);
      weightData.setWeight(dfc.format(weight));
      weightData.setFat(dfc.format(bodyfat.bodyfatPercentage));
      weightData.setBones(dfc.format(bodyfat.boneKg));
      weightData.setMuscle(dfc.format(bodyfat.muscleKg));
      weightData.setVisceralFat(String.valueOf(bodyfat.VFAL));
      weightData.setWaterRate(dfc.format(bodyfat.waterPercentage));
      weightData.setBmr(String.valueOf(bodyfat.BMR));
      return weightData;
   }

   public static WeightData parse(double weight) {
      WeightData weightData = new WeightData();
      weightData.setWeight(dfc.format(weight));
      weightData.setFat("0.0");
      weightData.setBones("0.0");
      weightData.setMuscle("0.0");
      weightData.setVisceralFat("0");
      weightData.setWaterRate("0.0");
      weightData.setBmr("0");
      return weightData;
   }

}
