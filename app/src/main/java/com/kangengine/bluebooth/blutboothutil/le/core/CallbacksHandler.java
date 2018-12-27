package com.kangengine.bluebooth.blutboothutil.le.core;

import android.os.Message;

import com.kangengine.bluebooth.blutboothutil.databean.BPMData;
import com.kangengine.bluebooth.blutboothutil.databean.BloodOxygenData;
import com.kangengine.bluebooth.blutboothutil.databean.CholesterolData;
import com.kangengine.bluebooth.blutboothutil.databean.GlucoseData;
import com.kangengine.bluebooth.blutboothutil.databean.UrineData;
import com.kangengine.bluebooth.blutboothutil.databean.WeightData;
import com.kangengine.bluebooth.blutboothutil.le.device.bpm.BPMManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.le.device.chol.CholManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.le.device.gls.GlucoseManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.le.device.hts.HTSManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.le.device.oxi.OximeterManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.le.device.scale.ScaleManagerCallbacks;
import com.kangengine.bluebooth.blutboothutil.le.device.urine.UAManagerCallbacks;

import org.greenrobot.eventbus.EventBus;

public class CallbacksHandler {
    public CallbacksHandler() {
    }

    static class EventBusCallback extends CallbacksHandler.DefaultCallbacks {
        EventBusCallback() {
        }

        @Override
        public void onDeviceConnected() {
            postConnectionState(2);
        }

        @Override
        public void onDeviceDisconnected() {
            postConnectionState(0);
        }

        @Override
        public void onDeviceConnecting() {
            postConnectionState(1);
        }

        @Override
        public void onDeviceReady() {
            postMessage(4608);
        }

        @Override
        public void onBatteryValueReceived(int value) {
            postMessage(4100, value);
        }

        @Override
        public void onBloodPressureMeasurementRead(BPMData bpmData) {
            postMessage(4097, bpmData);
        }

        @Override
        public void onHTValueReceived(double value) {
            postMessage(4104, value);
        }

        @Override
        public void onOxygenDataRead(BloodOxygenData data) {
            int[] array = new int[]{data.pulseRate, data.saturation, (int)data.pi};
            postMessage(4101, array);
        }

        @Override
        public void onScaleDataRead(WeightData data) {
            postMessage(4105, data);
        }

        @Override
        public void onScaleError() {
            postMessage(4112);
        }

        @Override
        public void onScaleMeasureFinished() {
            postMessage(4113);
        }

        @Override
        public void onUrineDataRead(UrineData data) {
            postMessage(4103, data);
        }

        @Override
        public void onGlucoseMeasurementRead(GlucoseData data) {
            postMessage(4098, data);
        }

        @Override
        public void onCholRead(CholesterolData data) {
            postMessage(4102, data);
        }

        private static void postConnectionState(int state) {
            postMessage(4096, state);
        }

        private static void postMessage(int what) {
            Message event = new Message();
            event.what = what;
            EventBus.getDefault().post(event);
        }

        private static void postMessage(int what, int arg) {
            Message event = new Message();
            event.what = what;
            event.arg1 = arg;
            EventBus.getDefault().post(event);
        }

        private static void postMessage(int what, Object obj) {
            Message event = new Message();
            event.what = what;
            event.obj = obj;
            EventBus.getDefault().post(event);
        }
    }

    public static class DefaultCallbacks implements BleManagerCallbacks, BPMManagerCallbacks, GlucoseManagerCallbacks, CholManagerCallbacks, HTSManagerCallbacks, OximeterManagerCallbacks, ScaleManagerCallbacks, UAManagerCallbacks {
        public DefaultCallbacks() {
        }

        @Override
        public void onDeviceConnected() {
        }

        @Override
        public void onDeviceDisconnected() {
        }

        @Override
        public void onDeviceConnecting() {
        }

        @Override
        public void onLinklossOccur() {
        }

        @Override
        public void onDeviceReady() {
        }

        @Override
        public void onBatteryValueReceived(int value) {
        }

        @Override
        public void onBloodPressureMeasurementRead(BPMData bpmData) {
        }

        @Override
        public void onIntermediateCuffPressureRead(float cuffPressure, int unit) {
        }

        @Override
        public void onCholRead(CholesterolData data) {
        }

        @Override
        public void onGlucoseMeasurementRead(GlucoseData data) {
        }

        @Override
        public void onHTValueReceived(double value) {
        }

        @Override
        public void onOxygenDataRead(BloodOxygenData data) {
        }

        @Override
        public void onSensorOff() {
        }

        @Override
        public void onScaleDataRead(WeightData data) {
        }

        @Override
        public void onScaleError() {
        }

        @Override
        public void onScaleMeasureFinished() {
        }

        @Override
        public void onUrineDataRead(UrineData data) {
        }
    }
}
