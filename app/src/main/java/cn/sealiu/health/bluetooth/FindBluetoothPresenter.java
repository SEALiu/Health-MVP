package cn.sealiu.health.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.data.response.MiniResponse;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.SampleGattAttributes;
import cn.sealiu.health.util.UnboxResponseProtocol;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.util.ProtocolMsg.EXECUTE_FAILED_NUMBER_LIMIT;
import static cn.sealiu.health.util.ProtocolMsg.EXECUTE_FAILED_UID_EXIST;
import static cn.sealiu.health.util.ProtocolMsg.EXECUTE_FAILED_WRONG_MID;
import static cn.sealiu.health.util.ProtocolMsg.EXECUTE_FAILED_WRONG_UID;
import static cn.sealiu.health.util.ProtocolMsg.EXECUTE_SUCCESS;
import static cn.sealiu.health.util.ProtocolMsg.RE_CERTIFICATION;
import static cn.sealiu.health.util.ProtocolMsg.RS_ACK;
import static cn.sealiu.health.util.ProtocolMsg.RS_EXECUTE_STATUS;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/14.
 */

public class FindBluetoothPresenter implements FindBluetoothContract.Presenter {
    private static final String TAG = "FindBluetoothPresenter";

    private BluetoothAdapter mBluetoothAdapter;
    private String dataCache = "";

    @NonNull
    private final FindBluetoothContract.View mFindBluetoothView;

    @Override
    public void start() {

    }

    public FindBluetoothPresenter(@NonNull FindBluetoothContract.View view) {
        mFindBluetoothView = checkNotNull(view);
        mFindBluetoothView.setPresenter(this);
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
        if (mFindBluetoothView.isActive())
            mFindBluetoothView.showNoAvailableService();
        return null;
    }

    @Override
    public void checkBluetoothSupport(Context context) {
        BluetoothManager bm = (BluetoothManager)
                context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bm.getAdapter();
        if (mBluetoothAdapter == null) {
            mFindBluetoothView.showInfo(R.string.bt_not_support);
            mFindBluetoothView.delayExit();
            return;
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mFindBluetoothView.showInfo(R.string.ble_not_support);
            mFindBluetoothView.delayExit();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mFindBluetoothView.openBluetooth();
        } else {
            mFindBluetoothView.scanLeDevice(true);
        }
    }

    /**
     * 涉及到换绑情况，所以需要判断，当前用户是否已有绑定的设备
     *
     * @param uuid      user uuid
     * @param machineId 要绑定的device id
     */
    @Override
    public void bindDevice2User(@NonNull String uuid, @NonNull String machineId) {
        final String id = checkNotNull(uuid);
        final String mid = checkNotNull(machineId);

        // 检测当前登录的用户是否已有绑定的设备
        String currentMid = sharedPref.getString(MainActivity.DEVICE_MID, "");

        final OkHttpClient okHttpClient = new OkHttpClient();

        if (currentMid.equals("")) {
            // 当前用户没有已绑定的设备，直接绑定当前设备
            Request bindRequest = BaseActivity.buildHttpGetRequest("/user/bindMachine?" +
                    "userUid=" + id + "&" +
                    "userMid=" + mid);

            okHttpClient.newCall(bindRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (D) Log.e(TAG, e.getLocalizedMessage());
                    mFindBluetoothView.showInfo("bind machine interface error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    MiniResponse miniResponse = new Gson().fromJson(
                            response.body().string(), MiniResponse.class);

                    if (D) Log.e(TAG, response.body().string());
                    if (miniResponse.getStatus().equals("200")) {
                        sharedPref.edit().putString(MainActivity.DEVICE_MID, mid).apply();
                        mFindBluetoothView.gotoHome();
                    } else {
                        if (D) Log.e(TAG, "can not upload bind info to remote database");
                    }
                }
            });
        } else if (!currentMid.equals(machineId)) {
            // 当前用户已有绑定的设备，且不是现在欲绑定的设备
            // 保存历史设备
            Request saveDeviceRequest = BaseActivity.buildHttpGetRequest("/HistoryMid/memoryHistMid?" +
                    "userUid=" + id);
            okHttpClient.newCall(saveDeviceRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (D) Log.e(TAG, e.getLocalizedMessage());
                    mFindBluetoothView.showInfo("save history device interface error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    MiniResponse mini = new Gson().fromJson(
                            response.body().string(), MiniResponse.class);

                    // 保存历史设备保存成功，保存新设备
                    if (mini.getStatus().equals("200")) {
                        Request bindRequest = BaseActivity.buildHttpGetRequest("/user/bindMachine?" +
                                "userUid=" + id + "&" +
                                "userMid=" + mid);

                        okHttpClient.newCall(bindRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                if (D) Log.e(TAG, e.getLocalizedMessage());
                                mFindBluetoothView.showInfo("bind machine interface error");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                MiniResponse miniResponse = new Gson().fromJson(
                                        response.body().string(), MiniResponse.class);

                                if (miniResponse.getStatus().equals("200")) {
                                    sharedPref.edit().putString(MainActivity.DEVICE_MID, mid).apply();
                                    mFindBluetoothView.gotoHome();
                                } else {
                                    if (D)
                                        Log.e(TAG, "can not upload bind info to remote database");
                                }
                            }
                        });
                    } else {
                        mFindBluetoothView.showInfo("保存旧设备信息失败，请重试");
                    }
                }
            });
        } else {
            mFindBluetoothView.gotoHome();
        }
    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d) {
        if (D) Log.e(TAG, "data: " + d);
        mFindBluetoothView.showInfo(R.string.verifing);

        String uid = sharedPref.getString(MainActivity.USER_ID, "");
        if (uid.equals("")) {
            mFindBluetoothView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);

            if (D) Log.e(TAG, "protocol: " + protocol);

            byte[] buff = BoxRequestProtocol.convertHex2Bytes(protocol);
            c.setValue(buff);
            s.writeCharacteristic(c);
        } else {
            mFindBluetoothView.showInfo(R.string.empty_data);
        }
    }

    @Override
    public void analyzeData(String data) {
        if (D) Log.e(TAG, "find data: " + data);

        if (data.equals("00")) {
            mFindBluetoothView.showInfo("认证失败，请重新点击设备");
            return;
        }


        if (data.length() >= 34) {
            UnboxResponseProtocol protocol = new UnboxResponseProtocol(data.substring(0, 34));

            String resultType = protocol.getExecuteResultType();
            String result = protocol.getExecuteResult();
            if (resultType.equals(RE_CERTIFICATION)) {
                switch (protocol.getType()) {
                    case RS_ACK:
                        if (D) Log.e(TAG, "0x24" + resultType + result);
                        mFindBluetoothView.showInfo("收到认证请求，正在处理...");
                        break;
                    case RS_EXECUTE_STATUS:
                        switch (result) {
                            case EXECUTE_SUCCESS:
                                Log.e(TAG, "bind success");

                                String mid = protocol.getExecuteBindedData().substring(0, 16);
                                String uuid = sharedPref.getString(MainActivity.USER_UID, "");

                                //3330333033353338 to 30303538
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < mid.length() - 1; i += 2) {
                                    int value = Integer.valueOf(mid.substring(i, i + 2), 16);
                                    builder.append((char) value);
                                }
                                mid = builder.toString();

                                if (!uuid.equals("")) {
                                    bindDevice2User(uuid, mid);
                                } else {
                                    if (D) Log.e(TAG, "user id is empty");
                                }
                                break;
                            case EXECUTE_FAILED_WRONG_UID:
                                // when uid = 0, user need to re-login
                                mFindBluetoothView.gotoLogin();
                                break;
                            case EXECUTE_FAILED_NUMBER_LIMIT:
                                mFindBluetoothView.showInfo(R.string.upto_bind_number_limit);
                                break;
                            case EXECUTE_FAILED_UID_EXIST://用户ID已存在
                                mFindBluetoothView.showInfo("用户已通过认证");
                                mFindBluetoothView.gotoHome();
                                break;
                            case EXECUTE_FAILED_WRONG_MID:
                                Log.e(TAG, "wrong mid");
                                mFindBluetoothView.showInfo(R.string.input_mid_error);
                                mFindBluetoothView.bindWithMid();
                                break;
                        }
                        break;
                }
            }
        }

        /*
        Pattern p24 = Pattern.compile("^FF2408[\\dA-F]{22}FF0D0AFF2308");
        Pattern p = Pattern.compile("[\\dA-F]{22}FF0D0A$");

        if (p24.matcher(data.toUpperCase()).find()) {
            dataCache = data;
        } else if (p.matcher(data.toUpperCase()).find() && !dataCache.equals("")) {
            data = dataCache + data;
            dataCache = "";

            if (D) Log.e(TAG, "find data: " + data.substring(34, 68));
            UnboxResponseProtocol protocol = new UnboxResponseProtocol(data.substring(34, 68));

            String resultType = protocol.getExecuteResultType();
            String result = protocol.getExecuteResult();

            if (D) Log.e(TAG, "type: " + resultType);
            if (D) Log.e(TAG, "result: " + result);

            String mid = protocol.getExecuteBindedData().substring(10, 18);
            String uuid = sharedPref.getString(MainActivity.USER_UID, "");

            if (resultType.equals(RE_CERTIFICATION)) {
                switch (result) {
                    case EXECUTE_SUCCESS:
                        Log.e(TAG, "bind success");
                        if (!uuid.equals("")) {
                            bindDevice2User(uuid, mid);
                        } else {
                            if (D) Log.e(TAG, "user id is empty");
                        }
                        break;
                    case EXECUTE_FAILED_WRONG_UID:
                        // when uid = 0, user need to re-login
                        mFindBluetoothView.gotoLogin();
                        break;
                    case EXECUTE_FAILED_NUMBER_LIMIT:
                        mFindBluetoothView.showInfo(R.string.upto_bind_number_limit);
                        break;
                    case EXECUTE_FAILED_UID_EXIST://用户ID已存在
                        mFindBluetoothView.showInfo("用户已通过认证");
                        mFindBluetoothView.gotoHome();
                        break;
                    case EXECUTE_FAILED_WRONG_MID:
                        // TODO: 2017/9/28 代码完成后需要恢复
                        Log.e(TAG, "wrong mid");
                        mFindBluetoothView.showInfo(R.string.input_mid_error);
                        mFindBluetoothView.bindWithMid();

                        // TODO: 2017/9/28 移除下面的代码
//                        if (!uuid.equals("")) {
//                            bindDevice2User(uuid, mid);
//                        }
                        break;
                }
            }

        }
        */


        // TODO: 2017/10/12 移除下面的代码
//        if (data.equals("00")) {//认证成功返回00，然后再次点击返回的是：0x230804......
//            Log.e(TAG, data);
//
//            //mFindBluetoothView.requestCompleteMid();
//            String uuid = sharedPref.getString(MainActivity.USER_UID, "");
//
//            if (!uuid.equals("")) {
//                bindDevice2User(uuid, BIND_SUCCESS);
//            } else {
//                if (D) Log.e(TAG, "user id is empty");
//            }
//        }

//        switch (data) {
//            case "00":
//                mFindBluetoothView.requestCompleteMid();
//                sharedPref.edit().putString(MainActivity.DEVICE_MID, BIND_SUCCESS).apply();
//                mFindBluetoothView.gotoHome();
//                break;
//            case "01":
//                mFindBluetoothView.gotoLogin();
//                break;
//            case "02":
//                mFindBluetoothView.showInfo("该设备绑定用户已达到限制");
//                break;
//            case "03":
//                mFindBluetoothView.showInfo("请输入设备ID后8位进行验证");
//                mFindBluetoothView.bindWithMid();
//                break;
//            case "04":
//                mFindBluetoothView.showInfo("设备ID输入错误，请重新输入");
//                mFindBluetoothView.bindWithMid();
//                break;
//        }

    }

    @Override
    public boolean checkLocation(Context context) {
        LocationManager locationMgr = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        if (locationMgr == null) return false;

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        try {
            network_enabled = locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        return gps_enabled || network_enabled;
    }
}
