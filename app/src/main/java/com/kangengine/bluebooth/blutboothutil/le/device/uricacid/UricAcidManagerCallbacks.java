package com.kangengine.bluebooth.blutboothutil.le.device.uricacid;

import com.kangengine.bluebooth.blutboothutil.databean.UricAcidData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleManagerCallbacks;

/**
 * @author : Vic
 * time    : 2018-12-26 18:56
 * desc    : 尿酸
 */
public interface UricAcidManagerCallbacks extends BleManagerCallbacks {
    /**
     * 读取到尿酸数据
     * @param data
     */
    void onUricAcidDataRead( UricAcidData data);
}
