package com.kangengine.bluebooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kangengine.bluebooth.blutboothutil.databean.BPMData;
import com.kangengine.bluebooth.blutboothutil.databean.BloodOxygenData;
import com.kangengine.bluebooth.blutboothutil.databean.CholesterolData;
import com.kangengine.bluebooth.blutboothutil.databean.GlucoseData;
import com.kangengine.bluebooth.blutboothutil.databean.UricAcidData;
import com.kangengine.bluebooth.blutboothutil.databean.UrineData;
import com.kangengine.bluebooth.blutboothutil.databean.WeightData;
import com.kangengine.bluebooth.blutboothutil.le.core.BleService;
import com.kangengine.bluebooth.blutboothutil.le.core.CallbacksHandler;
import com.kangengine.bluebooth.blutboothutil.le.device.bpm.BPMManager;
import com.kangengine.bluebooth.blutboothutil.le.device.chol.CholManager;
import com.kangengine.bluebooth.blutboothutil.le.device.gls.GlucoseManager;
import com.kangengine.bluebooth.blutboothutil.le.device.hts.ThermometerManager;
import com.kangengine.bluebooth.blutboothutil.le.device.oxi.OximeterManager;
import com.kangengine.bluebooth.blutboothutil.le.device.scale.ScaleManager;
import com.kangengine.bluebooth.blutboothutil.le.device.uricacid.UricAcidManager;
import com.kangengine.bluebooth.blutboothutil.le.device.urine.UAManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_LOCATION = 0;
    private static final int REQUEST_ENABLE_BT = 0x2016;

    private TextView mLog;
    private MenuItem menuRead;
    private Button btnOn;
    private Button btnOff;

    private BleService.LocalBinder serviceBinder;

    private UAManager mUAManager;
    private CholManager mCholManager;
    private boolean isChol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        initViews();

        mUAManager = UAManager.getInstance(getApplicationContext());
        mCholManager = CholManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menuRead = menu.findItem(R.id.menu_read);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bpm: // 血压计
                Log.d(TAG,"=====血压计");
                menuRead.setVisible(false);
                serviceBinder.setBleManager(BPMManager.getInstance(getApplicationContext()), mCallbacksHandler);
                return true;
            case R.id.menu_glu: // 血糖仪
                Log.d(TAG,"=====血糖仪");
                menuRead.setVisible(false);
                serviceBinder.setBleManager(GlucoseManager.getInstance(getApplicationContext()), mCallbacksHandler);
                return true;
            case R.id.menu_chol: // 血脂仪
                Log.d(TAG,"=====血脂仪");
                isChol = true;
                serviceBinder.setBleManager(mCholManager, mCallbacksHandler);
                menuRead.setVisible(true);
                return true;
            case R.id.menu_oxy:
                Log.d(TAG,"=====血氧仪");
                menuRead.setVisible(false); // 血氧仪
                serviceBinder.setBleManager(OximeterManager.getInstance(getApplicationContext()), mCallbacksHandler);
                return true;
            case R.id.menu_temp:
                Log.d(TAG,"=====体温计");
                menuRead.setVisible(false); // 体温计
                serviceBinder.setBleManager(ThermometerManager.getInstance(getApplicationContext()), mCallbacksHandler);
                return true;
            case R.id.menu_urine:
                Log.d(TAG,"=====尿液分析仪");
                isChol = false;
                serviceBinder.setBleManager(mUAManager, mCallbacksHandler);
                menuRead.setVisible(true); // 尿液分析仪
                return true;
            case R.id.menu_read:
                if (isChol) {
                    mCholManager.readCholData(); // 读取血脂仪测量结果
                } else {
                    mUAManager.readLastData(); // 读取尿液分析仪测量结果
                }
                return true;
            case R.id.menu_weight:
                menuRead.setVisible(false);
                serviceBinder.setBleManager(ScaleManager.getInstance(getApplicationContext()),mCallbacksHandler);
                return true;
            case R.id.menu_uric_acid:
                Log.d(TAG,"=====尿酸");
                menuRead.setVisible(false);
                serviceBinder.setBleManager(UricAcidManager.getInstance(getApplicationContext()),mCallbacksHandler);
                return true;
            default:
                menuRead.setVisible(false);
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        mLog = findViewById(R.id.status);
        btnOn = findViewById(R.id.bluetooth_on);
        btnOff = findViewById(R.id.btn_off);
        //打开蓝牙
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothManagerLocal.turnOnBluetooth();
            }
        });

        //关闭蓝牙
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothManagerLocal.turnOffBluetooth();
//				startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            }
        });
    }

    private void appendLog(String text) {
        mLog.setText(mLog.getText() + "\n" + text);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            enableBluetoothAdapterIfNot();
        }
    }

    private void enableBluetoothAdapterIfNot() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                bindBleService();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bindBleService();
            } else {
                Log.e(TAG, "Permission denied!");
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                // User chose not to enable Bluetooth.
            } else if (resultCode == RESULT_OK) {
                bindBleService();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void bindBleService() {
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private CallbacksHandler.DefaultCallbacks mCallbacksHandler = new CallbacksHandler.DefaultCallbacks() {

        @Override
        public void onDeviceConnected() {
            Log.d(TAG,"Device connected, " + serviceBinder.getDevice());
            showData("Device connected, " + serviceBinder.getDevice());
        }

        @Override
        public void onDeviceDisconnected() {
            Log.d(TAG,"Device disconnected");
            showData("Device disconnected");
            serviceBinder.scanBleDevice();
        }

        @Override
        public void onLinklossOccur() {
            Log.d(TAG,"onLinklossOccur");
            serviceBinder.scanBleDevice();
        }

        @Override
        public void onDeviceReady() {
            Log.d(TAG,"Gatt init done");
            showData("Gatt init done");
        }

        @Override
        public void onBloodPressureMeasurementRead(BPMData bpmData) {
            Log.d(TAG,"血压=" + bpmData.toJsonString());
            showData(bpmData.toJsonString());
        }

        @Override
        public void onGlucoseMeasurementRead(GlucoseData data) {
            Log.d(TAG,"血糖===" + data.toJsonString());
            showData(data.toJsonString());
        }

        @Override
        public void onOxygenDataRead(BloodOxygenData data) {
            Log.d(TAG,"血氧===" + data.toJsonString());
            showData(data.toJsonString());
        }

        @Override
        public void onCholRead(CholesterolData data) {
//		    data.time = new Date();
            Log.d(TAG,"血脂===" + data.toJsonString());
            showData(data.toJsonString());
        }

        @Override
        public void onUrineDataRead(UrineData data) {
            Log.d(TAG,"尿液===" + data.toString());
            Log.d(TAG,"timstamp==" + data.time.getTime()/1000);
            showData(data.toString());
        }

        @Override
        public void onHTValueReceived(double value) {
            Log.d(TAG,"体温===" + value);
            showData("Temperature:" + value);
        }

        @Override
        public void onScaleDataRead(WeightData weightData) {
            Log.d(TAG, "体重: " + weightData.toString());
            showData("WeightData:" + weightData.toString());
        }

        @Override
        public void onScaleMeasureFinished() {
            Log.d(TAG, "体重测量完成");
        }

        @Override
        public void onUricAcidDataRead(UricAcidData data) {
            super.onUricAcidDataRead(data);
            Log.d(TAG,"尿酸==" +data.toJsonString());
            showData("uric_acid:" + data.toJsonString());
        }

        private void showData(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appendLog(text);
                }
            });
        }
    };

    /**
     * Service连接回调函数
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG,"onServiceConnected");
            serviceBinder = (BleService.LocalBinder) service;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                serviceBinder.setIntervals(20, 5);
            }

            try {
                serviceBinder.setBleManager(BPMManager.getInstance(getApplicationContext()), mCallbacksHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Activity创建时判断当前蓝牙服务连接状态
            // connection state 参见#android.bluetooth.BluetoothProfile四种状态
            int state = serviceBinder.getConnectionState();
            appendLog("State:" + state);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG,"onServiceDisconnected");
            serviceBinder = null;
        }
    };
}
