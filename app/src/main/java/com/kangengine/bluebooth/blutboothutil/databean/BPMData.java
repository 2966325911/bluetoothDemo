package com.kangengine.bluebooth.blutboothutil.databean;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

public class BPMData {

   public int sbp;
   public int dbp;
   public float meanArterialPressure;
   public int pulseRate;
   public Date measureTime;
   public int userId;
   public Boolean bodyMovementDetection;
   public Boolean cuffFitDetection;
   public Boolean irregularPulseDetection;
   public Boolean improperMeasurementPosition;


   public String toJsonString() {
      HashMap map = new HashMap();
      map.put("sbp", Integer.valueOf(this.sbp));
      map.put("dbp", Integer.valueOf(this.dbp));
      map.put("pulseRate", Integer.valueOf(this.pulseRate));
      map.put("time", Long.valueOf(this.measureTime.getTime()));
      map.put("userId", Integer.valueOf(this.userId));
      map.put("bodyMovementDetection", String.valueOf(this.bodyMovementDetection));
      map.put("cuffFitDetection", String.valueOf(this.cuffFitDetection));
      map.put("irregularPulseDetection", String.valueOf(this.irregularPulseDetection));
      map.put("improperMeasurementPosition", String.valueOf(this.improperMeasurementPosition));
      return (new JSONObject(map)).toString();
   }
}
