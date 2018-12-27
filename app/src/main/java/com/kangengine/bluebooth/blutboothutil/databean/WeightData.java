package com.kangengine.bluebooth.blutboothutil.databean;

import java.text.DecimalFormat;

public class WeightData {

   private static DecimalFormat dfc = new DecimalFormat("#.#");
   private String type;
   private String level;
   private int sex;
   private int age;
   private int height;
   private String weight;
   private String fat;
   private String bones;
   private String muscle;
   private String visceralFat;
   private String waterRate;
   private String bmr;
   private int bodyAge;


   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getLevel() {
      return this.level;
   }

   public void setLevel(String level) {
      this.level = level;
   }

   public int getSex() {
      return this.sex;
   }

   public void setSex(int sex) {
      this.sex = sex;
   }

   public int getAge() {
      return this.age;
   }

   public void setAge(int age) {
      this.age = age;
   }

   public int getHeight() {
      return this.height;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public String getWeight() {
      return this.weight;
   }

   public void setWeight(String weight) {
      this.weight = weight;
   }

   public String getBones() {
      return this.bones;
   }

   public void setBones(String bones) {
      this.bones = bones;
   }

   public String getMuscle() {
      return this.muscle;
   }

   public void setMuscle(String muscle) {
      this.muscle = muscle;
   }

   public String getVisceralFat() {
      return this.visceralFat;
   }

   public void setVisceralFat(String visceralFat) {
      this.visceralFat = visceralFat;
   }

   public String getWaterRate() {
      return this.waterRate;
   }

   public void setWaterRate(String waterRate) {
      this.waterRate = waterRate;
   }

   public String getBmr() {
      return this.bmr;
   }

   public void setBmr(String bmr) {
      this.bmr = bmr;
   }

   public String getFat() {
      return this.fat;
   }

   public void setFat(String fat) {
      this.fat = fat;
   }

   public int getBodyAge() {
      return this.bodyAge;
   }

   public void setBodyAge(int bodyAge) {
      this.bodyAge = bodyAge;
   }

   public String getBmi() {
      if(this.height == 0) {
         return "0";
      } else {
         float weightF = Float.valueOf(this.weight).floatValue();
         return String.format("%.1f", new Object[]{Float.valueOf(weightF / (float)(this.height * this.height) * 10000.0F)});
      }
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("体重：" + this.weight + "kg\n");
      sb.append("骨骼：" + this.bones + "%\n");
      sb.append("脂肪：" + this.fat + "%\n");
      sb.append("肌肉：" + this.muscle + "%\n");
      sb.append("水分：" + this.waterRate + "%\n");
      sb.append("内脏脂肪：" + this.visceralFat + "\n");
      sb.append("BMR:" + this.bmr + "kcl");
      return sb.toString();
   }

}
