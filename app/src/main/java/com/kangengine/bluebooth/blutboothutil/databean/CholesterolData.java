package com.kangengine.bluebooth.blutboothutil.databean;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class CholesterolData {

   public String unit;
   public String chol;
   public String hdl;
   public String trig;
   public String ldl;
   public Date time;


   public String toJsonString() {
      HashMap map = new HashMap();
      map.put("unit", this.unit);
      map.put("chol", this.chol);
      map.put("hdl", this.hdl);
      map.put("trig", this.trig);
      map.put("ldl", this.ldl);
      map.put("time", Long.valueOf(this.time == null?(new Date()).getTime():this.time.getTime()));
      return (new JSONObject(map)).toString();
   }
}
