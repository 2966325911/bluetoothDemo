package com.kangengine.bluebooth.blutboothutil.databean;

import org.json.JSONObject;

import java.util.HashMap;

public class BloodOxygenData {

   public int pulseRate;
   public int saturation;
   public float pi;
   public int pulseWave;


   public String toJsonString() {
      HashMap map = new HashMap();
      map.put("pulseRate", Integer.valueOf(this.pulseRate));
      map.put("saturation", Integer.valueOf(this.saturation));
      map.put("pi", Float.valueOf(this.pi));
      map.put("pulseWave", Integer.valueOf(this.pulseWave));
      return (new JSONObject(map)).toString();
   }
}
