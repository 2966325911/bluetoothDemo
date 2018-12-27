package com.kangengine.bluebooth.blutboothutil.databean;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class GlucoseData {

   public float value;
   public Date time;


   public String toJsonString() {
      HashMap map = new HashMap();
      map.put("glu", Float.valueOf(this.value));
      map.put("time", Long.valueOf(this.time.getTime()));
      return (new JSONObject(map)).toString();
   }
}
