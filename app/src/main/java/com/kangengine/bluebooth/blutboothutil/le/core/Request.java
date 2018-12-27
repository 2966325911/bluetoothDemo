package com.kangengine.bluebooth.blutboothutil.le.core;

import android.bluetooth.BluetoothGattCharacteristic;

public class Request {

   final Type type;
   final BluetoothGattCharacteristic characteristic;
   final byte[] value;


   private Request(Type type, BluetoothGattCharacteristic characteristic) {
      this.type = type;
      this.characteristic = characteristic;
      this.value = null;
   }

   private Request(Type type, BluetoothGattCharacteristic characteristic, byte[] value) {
      this.type = type;
      this.characteristic = characteristic;
      this.value = value;
   }

   public static Request newReadRequest(BluetoothGattCharacteristic characteristic) {
      return new Request(Type.READ, characteristic);
   }

   public static Request newWriteRequest(BluetoothGattCharacteristic characteristic, byte[] value) {
      return new Request(Type.WRITE, characteristic, value);
   }

   public static Request newEnableNotificationsRequest(BluetoothGattCharacteristic characteristic) {
      return new Request(Type.ENABLE_NOTIFICATIONS, characteristic);
   }

   public static Request newEnableIndicationsRequest(BluetoothGattCharacteristic characteristic) {
      return new Request(Type.ENABLE_INDICATIONS, characteristic);
   }

   static enum Type {

      WRITE("WRITE", 0),
      READ("READ", 1),
      ENABLE_NOTIFICATIONS("ENABLE_NOTIFICATIONS", 2),
      ENABLE_INDICATIONS("ENABLE_INDICATIONS", 3);
      // $FF: synthetic field
      private static final Type[] $VALUES = new Type[]{WRITE, READ, ENABLE_NOTIFICATIONS, ENABLE_INDICATIONS};


      private Type(String var1, int var2) {}

   }
}
