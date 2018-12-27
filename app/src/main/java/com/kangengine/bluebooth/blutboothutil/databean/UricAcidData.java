package com.kangengine.bluebooth.blutboothutil.databean;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * @author : Vic
 * time    : 2018-12-27 11:15
 * desc    :尿酸
 */
public class UricAcidData {
    public double value;
    public Date time;


    public String toJsonString() {
        HashMap map = new HashMap();
        map.put("uric", Double.valueOf(this.value));
        map.put("time", Long.valueOf(this.time != null ? this.time.getTime() : System.currentTimeMillis()));
        return (new JSONObject(map)).toString();
    }
}
