package com.kangengine.bluebooth.blutboothutil.le.constants;

import java.util.UUID;

public interface Service {

   UUID BLOOD_GLUCOSE_MEASUREMMENT = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
   UUID BLOOD_PRESSURE_MEASUREMMENT = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
   UUID BATTERY = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
   UUID CHOLESTEROL = UUID.fromString("C14D2C0A-401F-B7A9-841F-E2E93B80F631");
   UUID OXIMETER = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
   UUID URINE_MEASUREMENT = UUID.fromString("000018f0-0000-1000-8000-00805f9b34fb");
   UUID URINE_MEASUREMENT_NEW = OXIMETER;
   UUID THERMOMETER = UUID.fromString("0000ffb0-0000-1000-8000-00805f9b34fb");
   UUID CURRENT_TIME = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
   UUID HEALTH_THERMOMETER = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
   UUID WEIGHT_SCALE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
   UUID BODY_COMPOSITION = UUID.fromString("0000181B-0000-1000-8000-00805f9b34fb");

   UUID URIC_ACID_MEASUREMENT = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");


}
