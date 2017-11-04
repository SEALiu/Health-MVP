package cn.sealiu.health.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.DataBean;
import cn.sealiu.health.data.local.HealthDbHelper;
import cn.sealiu.health.data.response.MiniResponse;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.Fun;
import cn.sealiu.health.util.ProtocolMsg;
import cn.sealiu.health.util.SampleGattAttributes;
import cn.sealiu.health.util.UnboxResponseProtocol;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.data.local.DataPersistenceContract.DataEntry;
import static cn.sealiu.health.data.local.DataStatusPersistenceContract.DataStatusEntry;
import static cn.sealiu.health.util.ProtocolMsg.RS_ACK;
import static cn.sealiu.health.util.ProtocolMsg.RS_EXECUTE_STATUS;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/16.
 */

public class UserPresenter implements UserContract.Presenter {
    private static final String TAG = "UserPresenter";
    private String dataCache = "";
    private String highMid = "", lowMid = "";
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int currentFixIndex = -1;

    // 1: realtime data;
    // 2: history data;
    private int realtimeOrHistoryDataFlag = 1;
    public StringBuilder historyStringBuilder;

    private BluetoothAdapter mBluetoothAdapter;

    private Handler mHandler;

    private final UserContract.View mUserView;

    @Override
    public void start() {

    }

    public UserPresenter(UserContract.View view) {
        mUserView = checkNotNull(view);
        mUserView.setPresenter(this);
        mHandler = new Handler();
    }

    @Override
    public void checkBluetoothSupport(Context context) {
        mUserView.setLoadingIndicator(true);

        BluetoothManager bm = (BluetoothManager)
                context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bm.getAdapter();
        if (mBluetoothAdapter == null) {
            mUserView.showInfo(R.string.bt_not_support);
            mUserView.delayExit();
            return;
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mUserView.showInfo(R.string.ble_not_support);
            mUserView.delayExit();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mUserView.changeMenuBluetoothIcon(R.drawable.ic_bluetooth_disabled_black_24dp);
            mUserView.openBluetooth();
        }

        mUserView.setLoadingIndicator(false);
    }

    @Override
    public void requestBaseInfo() {
        mUserView.requestDeviceStatus();
    }

    @Override
    public void requestDeviceHighMID() {
        if (sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "").equals("")) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_HIGH_MID);
        }
    }

    @Override
    public void requestDeviceLowMID() {
        if (sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "").equals("")) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_LOW_MID);
        }
    }

    @Override
    public void requestDeviceEnableDate() {
        mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_ENABLE_DATE);
    }

    @Override
    public void requestChannelNum() {
        if (sharedPref.getInt(MainActivity.DEVICE_CHANNEL_NUM, 0) == 0) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_CHANNEL_NUM);
        }
    }

    @Override
    public void requestChannelOne() {
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_ONE, "").equals("")) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_CHANNEL_ONE);
        }
    }

    @Override
    public void requestChannelTwo() {
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_TWO, "").equals("")) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_CHANNEL_TWO);
        }
    }

    @Override
    public void requestChannelThree() {
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_THREE, "").equals("")) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_CHANNEL_THREE);
        }
    }

    @Override
    public void requestChannelFour() {
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_FOUR, "").equals("")) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_CHANNEL_FOUR);
        }
    }

    @Override
    public void requestDeviceSlope() {
        if (sharedPref.getFloat(MainActivity.DEVICE_SLOPE, 0f) == 0f) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_SLOPE);
        }
    }

    @Override
    public void requestDeviceOffset() {
        if (sharedPref.getFloat(MainActivity.DEVICE_OFFSET, 0f) == 0f) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_OFFSET);
        }
    }

    @Override
    public void requestSamplingFrequency() {
        if (sharedPref.getInt(MainActivity.DEVICE_SAMPLING_FREQUENCY, 0) == 0) {
            mUserView.requestDeviceParam(ProtocolMsg.DEVICE_PARAM_SAMPLING_RATE);
        }
    }

    @Override
    public void startRealtime() {
        //mUserView.showInfo("start realtime");
        mUserView.requestRealtime(true);
    }

    @Override
    public void stopRealtime() {
        //mUserView.showInfo("stop realtime");
        mUserView.requestRealtime(false);
    }

    @Override
    public void syncTime() {
        mUserView.setSyncTime();
    }

    // 从 datastatus.tb 中找到为同步的数据，按天上传数据至服务器
    @Override
    public void syncLocalDataDaily(final HealthDbHelper dbHelper, String time) {
        String mid = sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "");
        Map<String, Object> historyDataMap = new HashMap<>();
        int count = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + DataEntry.TABLE_NAME + " WHERE " +
                DataEntry.COLUMN_NAME_MID + " = '" + mid +
                "' AND " + DataEntry.COLUMN_NAME_TIME + " LIKE '" + time + "%'";

        Cursor c = db.rawQuery(sql, null);
        List<DataBean> dataBeans = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                DataBean bean = new DataBean(mid,
                        c.getInt(c.getColumnIndex(DataEntry.COLUMN_NAME_SEQUENCE)),
                        c.getString(c.getColumnIndex(DataEntry.COLUMN_NAME_AA)),
                        c.getString(c.getColumnIndex(DataEntry.COLUMN_NAME_BB)),
                        c.getString(c.getColumnIndex(DataEntry.COLUMN_NAME_CC)),
                        c.getString(c.getColumnIndex(DataEntry.COLUMN_NAME_DD)),
                        c.getString(c.getColumnIndex(DataEntry.COLUMN_NAME_TIME))
                );

                count++;

                dataBeans.add(bean);
            } while (c.moveToNext());
            c.close();
        }

        historyDataMap.put("count", count);
        historyDataMap.put("data", dataBeans);

        // 格式化
        String uploadDataJson = new Gson().toJson(historyDataMap);

        doUploadHistoryData(uploadDataJson, dbHelper, time);
    }

    private void doUploadHistoryData(String uploadDataJson, final HealthDbHelper dbHelper, final String syncDate) {
        String serveIp = sharedPref.getString(BaseActivity.SERVER_IP, "");
        if (serveIp.equals("")) {
            mUserView.showInfo("当前为离线模式，无法上传数据");
            mUserView.hideProgressDialog();
            return;
        }

        if (D) Log.e(TAG, serveIp);
//        if (D) Log.e(TAG, "data: " + uploadDataJson);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upLoad", uploadDataJson)
                .build();

        Request uploadDataRequest = new Request.Builder()
                .url(serveIp + "/data/upLoadData")
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(uploadDataRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mUserView.hideProgressDialog();
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserView.showInfo("upload history data interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                if (D) Log.d(TAG, json);

                MiniResponse miniResponse = new Gson().fromJson(json, MiniResponse.class);

                if (miniResponse.getStatus().equals("200") || miniResponse.getStatus().equals("400")) {
                    updateDataStatus(dbHelper, syncDate);
                } else {
                    if (D) Log.e(TAG, "upload history data interface error");
                }
            }
        });
    }

    private void updateDataStatus(HealthDbHelper dbHelper, String time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataStatusEntry.COLUMN_NAME_SYNC, 1);

        int result = db.update(DataStatusEntry.TABLE_NAME,
                values,
                DataStatusEntry.COLUMN_NAME_TIME + "='" + time + "'",
                null);
        if (D) Log.e(TAG, "update result: " + result);

        mUserView.uploadHistoryData();
    }

    @Override
    public void onGattServicesDiscovered() {
        int delay = 500;
        final int period = 500;

        // request device mid, if not complete;
        if (sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "").equals("")) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestDeviceHighMID();
                }
            }, delay);
            delay += period;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestDeviceLowMID();
                }
            }, delay);
            delay += period;
        }

        /*
        // request device channel number, if not exist;
        if (sharedPref.getInt(MainActivity.DEVICE_CHANNEL_NUM, 0) == 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestChannelNum();
                }
            }, delay);
            delay += period;
        }

        // request device channel naming, if not exist;
        // channel one
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_ONE, "").equals("")) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestChannelOne();
                }
            }, delay);
            delay += period;
        }

        // channel two
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_TWO, "").equals("")) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestChannelTwo();
                }
            }, delay);
            delay += period;
        }

        // channel three
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_THREE, "").equals("")) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestChannelThree();
                }
            }, delay);
            delay += period;
        }

        // channel four
        if (sharedPref.getString(MainActivity.DEVICE_CHANNEL_FOUR, "").equals("")) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestChannelFour();
                }
            }, delay);
            delay += period;
        }
        */

        // request device slope, if not exist;
        if (sharedPref.getFloat(MainActivity.DEVICE_SLOPE, 0f) == 0f) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestDeviceSlope();
                }
            }, delay);
            delay += period;
        }

        // request device offset, if not exist;
        if (sharedPref.getFloat(MainActivity.DEVICE_OFFSET, 0f) == 0f) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestDeviceOffset();
                }
            }, delay);
            delay += period;
        }

        /*
        if (sharedPref.getInt(MainActivity.DEVICE_SAMPLING_FREQUENCY, 0) == 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestSamplingFrequency();
                }
            }, delay);
            delay += period;
        }
        */

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestBaseInfo();
            }
        }, delay);
        delay += period;

        // request device enable date, if not exist;
        if (sharedPref.getString(MainActivity.DEVICE_START_USING_DATE, "").equals("")) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestDeviceEnableDate();
                }
            }, delay);
            delay += period;
        }

        // set sync time
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncTime();
            }
        }, delay);
        delay += period;

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startRealtime();
//            }
//        }, delay);
    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d) {
        if (D) Log.d(TAG, "data: " + d);

        String uid = sharedPref.getString(MainActivity.USER_ID, "");
        if (uid.equals("")) {
            mUserView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);
            if (D) Log.d(TAG, "protocol: " + protocol);
            byte[] buff = BoxRequestProtocol.convertHex2Bytes(protocol);
            c.setValue(buff);
            s.writeCharacteristic(c);
        } else {
            mUserView.showInfo(R.string.empty_data);
        }
    }

    @Override
    public BluetoothGattCharacteristic discoverCharacteristic(BluetoothLeService service) {

        List<BluetoothGattService> gattServices = service.getSupportedGattServices();

        String uuid;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            String LIST_NAME = "SERVICE_NAME";
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            String LIST_UUID = "SERVICE_UUID";
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        if (D) Log.e(TAG, "gattServiceData: " + gattServiceData.toString());
        if (D) Log.e(TAG, "gattCharacteristicData: " + gattCharacteristicData.toString());

        for (ArrayList<BluetoothGattCharacteristic> bluetoothGattCharacteristics : mGattCharacteristics) {
            for (BluetoothGattCharacteristic charas : bluetoothGattCharacteristics) {
                if (charas.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                    return charas;
                }
            }
        }
        if (mUserView.isActive())
            mUserView.showNoAvailableService();
        return null;
    }

    @Override
    public void analyzeData(String data) {
        if (D) Log.d(TAG, "receive data: " + data);

        // 请求历史数据的确认报文
        Pattern pHistoryBegin = Pattern.compile("FF2403");
        // 历史数据传输完成的确认报文
        Pattern pHistoryEnd = Pattern.compile("2303");

        // 开始接收历史数据
        if (pHistoryBegin.matcher(data.toUpperCase()).find()) {
            realtimeOrHistoryDataFlag = 2;
            historyStringBuilder = new StringBuilder();
        }

        // 历史数据接收完毕
        if (realtimeOrHistoryDataFlag == 2 &&
                (pHistoryEnd.matcher(data.toUpperCase()).find() || data.length() < 34)) {
            realtimeOrHistoryDataFlag = 1;
            // 该天的历史数据传输完成，下一步：
            // 保存到本地数据库 data.tb
            // 并更新 datastatus.tb
            mUserView.saveHistoryData();
        }

        if (realtimeOrHistoryDataFlag == 2) {
            historyStringBuilder.append(data);
            return;
        }

        if (realtimeOrHistoryDataFlag == 1 && data.length() < 34) return;

        UnboxResponseProtocol protocol = new UnboxResponseProtocol(data);
        switch (protocol.getType()) {
            case ProtocolMsg.RS_DATA:
                // 实时数据
                onRealTimeDataResponse(protocol);
                break;
            case ProtocolMsg.RS_STATUS_OR_PARAM:
                // 设备状态／参数响应包
                onStatusOrParamResponse(protocol);
                break;
            case RS_EXECUTE_STATUS:
                // 处理指令执行结果
                onExecutedResponse(protocol);
                break;
            case RS_ACK:
                onACKResponse(protocol);
                break;
        }
    }

    @Override
    public void doSaveHistoryData(HealthDbHelper dbHelper, String historyDate) {
        List<DataBean> dataBeans = new ArrayList<>();
        String c_mid = sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "");

        Pattern pHistory = Pattern.compile("FF01[\\dA-F]{24}FF0D0A");

        if (historyStringBuilder == null) historyStringBuilder = new StringBuilder();
        Matcher matcher = pHistory.matcher(historyStringBuilder.toString());

        while (matcher.find()) {
            String item = historyStringBuilder.toString().substring(
                    matcher.start(),
                    matcher.end()
            );

            if (D) Log.d(TAG, "history data: " + item);

            UnboxResponseProtocol unboxResponseProtocol = new UnboxResponseProtocol(item);
            if (unboxResponseProtocol.getIsValidate() && !historyDate.equals("")
                    && !unboxResponseProtocol.getType().equals(RS_EXECUTE_STATUS)) {
                String time = unboxResponseProtocol.getDataTime();

                String sequenceNum = unboxResponseProtocol.getSequenceNum();
                String[] data = unboxResponseProtocol.getData();

                DataBean bean = new DataBean(c_mid,
                        Integer.valueOf(sequenceNum),
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        historyDate + " " + time
                );
                bean.setId(UUID.randomUUID().toString());
                dataBeans.add(bean);
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (DataBean bean : dataBeans) {
            ContentValues values = new ContentValues();

            values.put(DataEntry.COLUMN_NAME_ID, bean.getId());
            values.put(DataEntry.COLUMN_NAME_MID, bean.getMid());
            values.put(DataEntry.COLUMN_NAME_SEQUENCE, bean.getSequence());
            values.put(DataEntry.COLUMN_NAME_AA, bean.getAa());
            values.put(DataEntry.COLUMN_NAME_BB, bean.getBb());
            values.put(DataEntry.COLUMN_NAME_CC, bean.getCc());
            values.put(DataEntry.COLUMN_NAME_DD, bean.getDd());
            values.put(DataEntry.COLUMN_NAME_TIME, bean.getTime());

            db.insert(DataEntry.TABLE_NAME, null, values);
        }

        String sql;
        if (historyStringBuilder.length() == 0) {
            // 已请求，设备无数据
            sql = "UPDATE " + DataStatusEntry.TABLE_NAME + " SET " +
                    DataStatusEntry.COLUMN_NAME_STATUS + " = 2 WHERE " +
                    DataStatusEntry.COLUMN_NAME_TIME + " = '" + historyDate + "'";
        } else {
            // 已请求，本地保存
            sql = "UPDATE " + DataStatusEntry.TABLE_NAME + " SET " +
                    DataStatusEntry.COLUMN_NAME_STATUS + " = 1 WHERE " +
                    DataStatusEntry.COLUMN_NAME_TIME + " = '" + historyDate + "'";
        }

        if (D) Log.e(TAG, "update datastatus sql is " + sql);

        db.execSQL(sql);

        //继续请求历史数据
        mUserView.updateHistoryData();
    }

    @Override
    public void loadWeekBarChartData(HealthDbHelper dbHelper) {
        boolean visible = false;
        String mid = sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "");
        // 采集率为每30秒一次，故一天的数据总量为：24 * 60 * 2 = 2880
        Float dataNumOneDay = 24 * 60 * 2f;

        ArrayList<BarEntry> yVals = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 获取近7天的数据(不包括今天)
        DateFormat yMd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar day = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            day.add(Calendar.DATE, -1);
            String dayStr = yMd.format(day.getTime());

            String sql = "SELECT * FROM " + DataEntry.TABLE_NAME +
                    " WHERE " + DataEntry.COLUMN_NAME_MID + " = '" + mid +
                    "' AND " + DataEntry.COLUMN_NAME_TIME + " LIKE '" + dayStr + "%'";
            Cursor c = db.rawQuery(sql, null);
            if (D) Log.d(TAG, dayStr + " data count: " + c.getCount());

            // 近7天，最少有一天有数据
            if (c.getCount() != 0 && !visible) visible = true;
            // y轴单位：小时(h)
            yVals.add(new BarEntry(i, (c.getCount() / dataNumOneDay) * 24));
            c.close();
        }

        mUserView.updateWeekBarChart(yVals, visible);
    }

    @Override
    public void setCurrentFixIndex(int index) {
        this.currentFixIndex = index;
    }

    /**
     * 收到"0x21"类型报文，处理实时数据
     *
     * @param unboxResponseProtocol 解析报文
     */
    private void onRealTimeDataResponse(UnboxResponseProtocol unboxResponseProtocol) {
        float[] voltages = new float[4];
        String[] voltages_status = new String[4];

        // 数据响应包／实时数据
        if (D) {
            Log.d(TAG, "实时数据时间 = " + unboxResponseProtocol.getDataTime());
            Log.d(TAG, "实时数据序号 = " + unboxResponseProtocol.getSequenceNum());
        }

        if (unboxResponseProtocol.getSequenceNum().equals("FE")) {
            mUserView.showInfo("存储错误: " + unboxResponseProtocol.getMsg());
            return;
        }

        Float slope = sharedPref.getFloat(MainActivity.DEVICE_SLOPE, 27.5f);
        Float offset = sharedPref.getFloat(MainActivity.DEVICE_OFFSET, 89.5f);

        int indexTemp = 0;
        for (String item : unboxResponseProtocol.getData()) {

            voltages[indexTemp] = (Integer.valueOf(item.substring(1, 4), 16) - offset) / slope;

            if (voltages[indexTemp] < 0) voltages[indexTemp] = 0;
            voltages_status[indexTemp++] = item.substring(0, 1); // 0,1,2,3,4
        }

        mUserView.updateLineChartRT(voltages[0], voltages_status[0], 0);
        mUserView.updateLineChartRT(voltages[1], voltages_status[1], 1);
        mUserView.updateLineChartRT(voltages[2], voltages_status[2], 2);
        mUserView.updateLineChartRT(voltages[3], voltages_status[3], 3);

        if (D) Log.d(TAG, "realtime lineChart updated --- " + "AA: " + voltages[0] + "\nBB: " +
                voltages[1] + "\nCC: " + voltages[2] + "\nDD: " + voltages[3]);

        if (D)
            Log.d(TAG, "realtime lineChart updated --- " + "AA: " + voltages_status[0] + "\nBB: " +
                    voltages_status[1] + "\nCC: " + voltages_status[2] + "\nDD: " + voltages_status[3]);
    }

    /**
     * 收到"0x22"类型报文，处理（更新界面的电池余量，SD剩余容量，系统时间）
     *
     * @param unboxResponseProtocol 解析报文
     */
    private void onStatusOrParamResponse(UnboxResponseProtocol unboxResponseProtocol) {
        if (unboxResponseProtocol.isStatus()) {
            String powerLeft = Integer.valueOf(unboxResponseProtocol.getPowerLeft(), 16) + "";
            String storageLeft = Integer.valueOf(unboxResponseProtocol.getStorageLeft(), 16) + "";
            String systemTime = unboxResponseProtocol.getSystemTime();

            String year = Integer.valueOf(systemTime.substring(0, 4), 16) + "";
            String month = Integer.valueOf(systemTime.substring(4, 6), 16) + "";
            String day = Integer.valueOf(systemTime.substring(6, 8), 16) + "";

            systemTime = year + "-" + month + "-" + day;

            sharedPref.edit().putString(MainActivity.DEVICE_POWER, powerLeft).apply();
            sharedPref.edit().putString(MainActivity.DEVICE_STORAGE, storageLeft).apply();
            sharedPref.edit().putString(MainActivity.DEVICE_TIME, systemTime).apply();

            mUserView.updateBattery(powerLeft + "%");
            mUserView.updateStorage(storageLeft + "MB");
            mUserView.updateTime(systemTime);

            if (Integer.valueOf(powerLeft) < 10) mUserView.showInfo("电量不足10%");
            if (Integer.valueOf(storageLeft) < 100) mUserView.showInfo("SD卡即将满容，请转移数据");
        }

        if (unboxResponseProtocol.isParam()) {
            String data = unboxResponseProtocol.getParamContent();

            // 请求设备设置参数的响应
            switch (unboxResponseProtocol.getParamType()) {
                // 获取向设备请求启用参数，如果存在则保存至本地 shared preference
                // 如果没有则需要进行定标操作
                case ProtocolMsg.DEVICE_PARAM_ENABLE_DATE:
                    if (D) Log.e(TAG, "DEVICE_PARAM_ENABLE_DATE: " + data);

                    int year = Integer.valueOf(data.substring(0, 4), 16);
                    int month = Integer.valueOf(data.substring(4, 6), 16);
                    int day = Integer.valueOf(data.substring(6, 8), 16);

                    String enableDate = year + "-" +
                            Fun.leftPad(month + "", 2) + "-" +
                            Fun.leftPad(day + "", 2);
                    Log.e(TAG, "DEVICE_PARAM_ENABLE_DATE: " + enableDate);


                    sharedPref.edit().putString(MainActivity.DEVICE_START_USING_DATE, enableDate).apply();
                    //mUserView.updateDataStatus();

                    uploadDeviceEnableDate();
                    break;
                case ProtocolMsg.DEVICE_PARAM_CHANNEL_NUM:
                    int channelNum = Integer.valueOf(data.substring(0, 2), 16);
                    if (channelNum == 0)
                        sharedPref.edit().putInt(MainActivity.DEVICE_CHANNEL_NUM, 4).apply();
                    else
                        sharedPref.edit().putInt(MainActivity.DEVICE_CHANNEL_NUM, channelNum).apply();

                    break;
                case ProtocolMsg.DEVICE_PARAM_COMFORT_ONE:
                    Log.e(TAG, "fix criterion data:（废弃） " + data);
//                    int comA = Integer.valueOf(data.substring(0, 4), 16);
//                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_A, comA + "").apply();
//
//                    uploadFixResultItem(comA + "", "A");
//                    // mUserView.showInfo("定标成功");
                    break;
                case ProtocolMsg.DEVICE_PARAM_COMFORT_TWO:
                    Log.e(TAG, "fix criterion data: （废弃）" + data);
//                    int comB = Integer.valueOf(data.substring(0, 4), 16);
//                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_B, comB + "").apply();
//
//                    uploadFixResultItem(comB + "", "B");
//                    //mUserView.showInfo("定标成功");
//
//                    mUserView.hideProgressDialog();
                    break;
                case ProtocolMsg.DEVICE_PARAM_COMFORT_THREE:
                    Log.e(TAG, "fix criterion data:（废弃） " + data);
//                    int comC = Integer.valueOf(data.substring(0, 4), 16);
//                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_C, comC + "").apply();
//
//                    uploadFixResultItem(comC + "", "C");
//                    //mUserView.showInfo("定标成功");
//
//                    mUserView.hideProgressDialog();
                    break;
                case ProtocolMsg.DEVICE_PARAM_COMFORT_FOUR:
                    Log.e(TAG, "fix criterion data: （废弃）" + data);
//                    int comD = Integer.valueOf(data.substring(0, 4), 16);
//                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_D, comD + "").apply();

//                    uploadFixResultItem(comD + "", "D");
//                    //mUserView.showInfo("定标成功");
//
//                    mUserView.hideProgressDialog();
                    break;
                case ProtocolMsg.DEVICE_PARAM_SLOPE:
                    float slope = Integer.valueOf(data.substring(0, 4), 16) / 1000f;
                    sharedPref.edit().putFloat(MainActivity.DEVICE_SLOPE, slope).apply();
                    break;
                case ProtocolMsg.DEVICE_PARAM_OFFSET:
                    float offset = Integer.valueOf(data.substring(0, 4), 16) / 1000f;
                    sharedPref.edit().putFloat(MainActivity.DEVICE_OFFSET, offset).apply();
                    break;
                case ProtocolMsg.DEVICE_PARAM_SAMPLING_RATE:
                    int frequency = Integer.valueOf(data.substring(0, 4), 16);
                    sharedPref.edit().putInt(MainActivity.DEVICE_SAMPLING_FREQUENCY, frequency).apply();
                    break;
                case ProtocolMsg.DEVICE_PARAM_CHANNEL_ONE:
                    break;
                case ProtocolMsg.DEVICE_PARAM_CHANNEL_TWO:
                    break;
                case ProtocolMsg.DEVICE_PARAM_CHANNEL_THREE:
                    break;
                case ProtocolMsg.DEVICE_PARAM_CHANNEL_FOUR:
                    break;
                case ProtocolMsg.DEVICE_PARAM_HIGH_MID:
                    highMid = data.substring(0, 12);
                    if (!lowMid.equals("")) {
                        sharedPref.edit().putString(MainActivity.DEVICE_COMPLETED_MID, highMid + lowMid).apply();
                        mUserView.showInfo("设备完整MID已获取");

                        String uuid = sharedPref.getString(MainActivity.USER_UID, "");
                        String mid = sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "");

                        if (uuid.equals("") || mid.equals("")) return;
                        bindUserMid(uuid, mid);
                    }
                    break;
                case ProtocolMsg.DEVICE_PARAM_LOW_MID:
                    lowMid = unboxResponseProtocol.getParamContent().substring(0, 12);
                    if (!highMid.equals("")) {
                        sharedPref.edit().putString(MainActivity.DEVICE_COMPLETED_MID, highMid + lowMid).apply();
                        mUserView.showInfo("设备完整MID已获取");

                        String uuid = sharedPref.getString(MainActivity.USER_UID, "");
                        String mid = sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "");

                        if (uuid.equals("") || mid.equals("")) return;
                        bindUserMid(uuid, mid);
                    }
                    break;
                case ProtocolMsg.DEVICE_PARAM_DOCTOR_ID:
                    break;
            }
        }
    }

    // 更新用户完整的mid
    private void bindUserMid(String uuid, String machineId) {
        final String id = checkNotNull(uuid);
        final String mid = checkNotNull(machineId);

        Request bindRequest = BaseActivity.buildHttpGetRequest("/user/bindMachine?" +
                "userUid=" + id + "&" +
                "userMid=" + mid);

        if (bindRequest == null) return;
        if (D) Log.e(TAG, bindRequest.url().toString());

        new OkHttpClient().newCall(bindRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserView.showInfo("bind machine interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                if (D) Log.e(TAG, json);
                MiniResponse miniResponse = new Gson().fromJson(json, MiniResponse.class);

                if (miniResponse.getStatus().equals("200")) {
                    sharedPref.edit().putString(MainActivity.DEVICE_COMPLETED_MID, mid).apply();
                    mUserView.showInfo("设备完整MID已保存至服务器");
                } else {
                    if (D) Log.e(TAG, "can not upload bind info to remote database");
                }
            }
        });
    }

    /**
     * 收到"0x23"类型的报文，处理
     *
     * @param unboxResponseProtocol 解析报文
     */
    private void onExecutedResponse(UnboxResponseProtocol unboxResponseProtocol) {
        // 报文类型
        String executeResultType = unboxResponseProtocol.getExecuteResultType();
        // 执行结果
        String executeResult = unboxResponseProtocol.getExecuteResult();
        // 子指令类型
        String subType = unboxResponseProtocol.getChildExecuteResultType();
        // 内容
        String contentData = unboxResponseProtocol.getExecuteData();

        if (executeResultType.equals(ProtocolMsg.RE_SYNC_TIME)) {
            if (executeResult.equals(ProtocolMsg.EXECUTE_SUCCESS)) {
                mUserView.updateTime(null);
                mUserView.showInfo("时间已同步");
            }
        }

        if (executeResultType.equals(ProtocolMsg.RE_DEVICE_PARAM) &&
                executeResult.equals(ProtocolMsg.EXECUTE_SUCCESS)) {
            // 定标结果
            switch (currentFixIndex) {
                case 0:// 空载定标
                    String blank1 = contentData.substring(0, 4);
                    String blank2 = contentData.substring(4, 8);
                    String blank3 = contentData.substring(8, 12);
                    String blank4 = contentData.substring(12, 16);
                    int[] valuesA = {
                            Integer.valueOf(blank1, 16),
                            Integer.valueOf(blank2, 16),
                            Integer.valueOf(blank3, 16),
                            Integer.valueOf(blank4, 16)
                    };
                    uploadFixResultItem(valuesA, "A");
                    break;
                case 1:// 松
                    String loose1 = contentData.substring(0, 4);
                    String loose2 = contentData.substring(4, 8);
                    String loose3 = contentData.substring(8, 12);
                    String loose4 = contentData.substring(12, 16);
                    int[] valuesB = {
                            Integer.valueOf(loose1, 16),
                            Integer.valueOf(loose2, 16),
                            Integer.valueOf(loose3, 16),
                            Integer.valueOf(loose4, 16)
                    };
                    uploadFixResultItem(valuesB, "B");
                    break;
                case 2:// 合适
                    String comfort1 = contentData.substring(0, 4);
                    String comfort2 = contentData.substring(4, 8);
                    String comfort3 = contentData.substring(8, 12);
                    String comfort4 = contentData.substring(12, 16);
                    int[] valuesC = {
                            Integer.valueOf(comfort1, 16),
                            Integer.valueOf(comfort2, 16),
                            Integer.valueOf(comfort3, 16),
                            Integer.valueOf(comfort4, 16)
                    };
                    uploadFixResultItem(valuesC, "C");
                    break;
                case 3:// 紧
                    String tight1 = contentData.substring(0, 4);
                    String tight2 = contentData.substring(4, 8);
                    String tight3 = contentData.substring(8, 12);
                    String tight4 = contentData.substring(12, 16);
                    int[] valuesD = {
                            Integer.valueOf(tight1, 16),
                            Integer.valueOf(tight2, 16),
                            Integer.valueOf(tight3, 16),
                            Integer.valueOf(tight4, 16)
                    };
                    uploadFixResultItem(valuesD, "D");
                    break;
                default:
                    break;
            }
        }
    }

    private void onACKResponse(UnboxResponseProtocol unboxResponseProtocol) {
        String executeResultType = unboxResponseProtocol.getExecuteResultType();
        String executeResult = unboxResponseProtocol.getExecuteResult();

        if (executeResultType.equals(ProtocolMsg.RE_SYNC_TIME) &&
                executeResult.equals(ProtocolMsg.EXECUTE_SUCCESS)) {
            mUserView.updateTime(null);
            mUserView.showInfo("时间已同步");
        }

        if (executeResultType.equals(ProtocolMsg.RE_RT_DATA_START) &&
                executeResult.equals(ProtocolMsg.EXECUTE_SUCCESS)) {
            mUserView.showInfo("开始接收实时数据，请稍后...");
        }

        if (executeResultType.equals(ProtocolMsg.RE_RT_DATA_STOP) &&
                executeResult.equals(ProtocolMsg.EXECUTE_SUCCESS)) {
            mUserView.showInfo("停止接收实时数据");
        }
    }

    private void uploadDeviceEnableDate() {
        String uuid = sharedPref.getString(MainActivity.USER_UID, "");
        String enableDate = sharedPref.getString(MainActivity.DEVICE_START_USING_DATE, "");

        if (uuid.equals("")) mUserView.gotoLogin();
        if (enableDate.equals("")) requestDeviceEnableDate();

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = BaseActivity.buildHttpGetRequest("/user/firstCalibTime?" +
                "userUid=" + uuid + "&" +
                "firstCalibTime=" + enableDate);

        if (request == null) return;

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserView.showInfo("upload device enable date interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.e(TAG, json);
                MiniResponse mini = new Gson().fromJson(json, MiniResponse.class);

                if (mini.getStatus().equals("200")) {
                    mUserView.showInfo("设备启用日期保存成功");
                } else {
                    mUserView.showInfo("设备启用日期保存失败");
                }
            }
        });
    }

    private void uploadFixResultItem(final int[] values, final String type) {
        String uuid = sharedPref.getString(MainActivity.USER_UID, "");

        /*
        http://localhost:8080/user/upLoadComfortA?userUid=testUid
        &userComfortA1=239.00
        &userComfortA2=229.00
        &userComfortA3=219.00
        &userComfortA4=209.00
         */

        Request request = BaseActivity.buildHttpGetRequest("/user/upLoadComfort" + type + "?" +
                "userUid=" + uuid + "&" +
                "userComfort" + type + "1=" + values[0] + "&" +
                "userComfort" + type + "2=" + values[1] + "&" +
                "userComfort" + type + "3=" + values[2] + "&" +
                "userComfort" + type + "4=" + values[3]);

        if (request == null) return;

        if (D) Log.d(TAG, "request url is: " + request.url());

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserView.hideProgressDialog();
                mUserView.showInfo("upload comfort interface error");

                // 定标失败后将该次定标清空
                saveFixResultWithSharedPref(type, null, false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mUserView.hideProgressDialog();
                String json = response.body().string();
                if (D) Log.e(TAG, "response: " + json);

                MiniResponse mini = new Gson().fromJson(json, MiniResponse.class);
                if (mini.getStatus().equals("200")) {
                    mUserView.showInfo("定标成功");

                    String[] valuesStr = new String[4];
                    valuesStr[0] = String.valueOf(values[0]);
                    valuesStr[1] = String.valueOf(values[1]);
                    valuesStr[2] = String.valueOf(values[2]);
                    valuesStr[3] = String.valueOf(values[3]);

                    saveFixResultWithSharedPref(type, valuesStr, true);
                } else {
                    mUserView.showInfo("定标数据保存失败");
                    // 定标失败后将该次定标清空
                    saveFixResultWithSharedPref(type, null, false);
                }
            }
        });
    }

    private void saveFixResultWithSharedPref(String type, String[] values, boolean isSuccess) {
        if (!isSuccess) {
            //定标结果失败，这时覆盖 values 为默认值
            values = new String[4];
            values[0] = "0.0";
            values[1] = "0.0";
            values[2] = "0.0";
            values[3] = "0.0";
        }

        switch (type) {
            case "A":
                sharedPref.edit().putBoolean(MainActivity.DEVICE_COMFORT_A, isSuccess).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_A1, values[0]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_A2, values[1]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_A3, values[2]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_A4, values[3]).apply();
                break;
            case "B":
                sharedPref.edit().putBoolean(MainActivity.DEVICE_COMFORT_B, isSuccess).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_B1, values[0]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_B2, values[1]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_B3, values[2]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_B4, values[3]).apply();
                break;
            case "C":
                sharedPref.edit().putBoolean(MainActivity.DEVICE_COMFORT_C, isSuccess).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_C1, values[0]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_C2, values[1]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_C3, values[2]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_C4, values[3]).apply();
                break;
            case "D":
                sharedPref.edit().putBoolean(MainActivity.DEVICE_COMFORT_D, isSuccess).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_D1, values[0]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_D2, values[1]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_D3, values[2]).apply();
                sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_D4, values[3]).apply();
                break;
        }
    }
}
