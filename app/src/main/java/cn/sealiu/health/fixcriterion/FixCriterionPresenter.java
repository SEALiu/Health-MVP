package cn.sealiu.health.fixcriterion;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import static cn.sealiu.health.util.ProtocolMsg.RE_FIX_NORM;
import static cn.sealiu.health.util.ProtocolMsg.RS_EXECUTE_STATUS;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class FixCriterionPresenter implements FixCriterionContract.Presenter {
    private final static String TAG = "FixCriterionPresenter";
    private int currentFix = 0;
    private boolean[] fixResult = new boolean[4];
    private Double[] fixValue = new Double[4];
    private String[] comforts = new String[4];

    private final FixCriterionContract.View mFixCriterionView;

    public FixCriterionPresenter(@NonNull FixCriterionContract.View view) {
        mFixCriterionView = checkNotNull(view);
        mFixCriterionView.setPresenter(this);

        fixResult[0] = sharedPref.getString(MainActivity.DEVICE_COMFORT_A, "").equals("");
        fixResult[1] = sharedPref.getString(MainActivity.DEVICE_COMFORT_B, "").equals("");
        fixResult[2] = sharedPref.getString(MainActivity.DEVICE_COMFORT_C, "").equals("");
        fixResult[3] = sharedPref.getString(MainActivity.DEVICE_COMFORT_D, "").equals("");

        comforts[0] = MainActivity.DEVICE_COMFORT_A;
        comforts[1] = MainActivity.DEVICE_COMFORT_B;
        comforts[2] = MainActivity.DEVICE_COMFORT_C;
        comforts[3] = MainActivity.DEVICE_COMFORT_D;
    }

    @Override
    public void start() {
    }

    @Override
    public void uploadFixResult() {
        if (fixValue[currentFix] == null) {
            mFixCriterionView.showInfo("uploadFixResult error: fixValue[currentFix] is null");
            return;
        }

        if (currentFix == 0) uploadFixResultItem(fixValue[currentFix], "A");
        if (currentFix == 1) uploadFixResultItem(fixValue[currentFix], "B");
        if (currentFix == 2) uploadFixResultItem(fixValue[currentFix], "C");
        if (currentFix == 3) uploadFixResultItem(fixValue[currentFix], "D");
    }

    private void uploadFixResultItem(final Double value, String type) {
        String uuid = sharedPref.getString(MainActivity.USER_UID, "");

        /*
        http://localhost:8080/user/upLoadComfortA?userUid=testUid&userComfortA=239.00
         */
        Request request = BaseActivity.buildHttpGetRequest("/user/upLoadComfort" + type + "?" +
                "userUid=" + uuid + "&" +
                "userComfort" + type + "=" + value);

        if (request == null) return;

        if (D) Log.d(TAG, "request url is: " + request.url());

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mFixCriterionView.showInfo("upload comfort interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.e(TAG, "response: " + json);

                MiniResponse mini = new Gson().fromJson(json, MiniResponse.class);
                if (mini.getStatus().equals("200")) {
                    // 更新本地定标状态，并刷新界面
                    fixResult[currentFix] = true;
                    Log.e(TAG, "fix success: " + comforts[currentFix]);
                    sharedPref.edit().putString(comforts[currentFix], value + "").apply();
                    mFixCriterionView.showInfo("定标成功");
                    mFixCriterionView.updateUI();
                } else {
                    mFixCriterionView.showInfo("定标数据保存失败");
                }
            }
        });
    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d, int cf) {
        currentFix = cf;
        String uid = sharedPref.getString(MainActivity.USER_ID, "");
        if (uid.equals("")) {
            mFixCriterionView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);
            if (D) Log.e(TAG, "fix criterion protocol: " + protocol);

            byte[] buff = BoxRequestProtocol.convertHex2Bytes(protocol);
            c.setValue(buff);
            s.writeCharacteristic(c);
        } else {
            mFixCriterionView.showInfo(R.string.empty_data);
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

        if (D) Log.e(TAG, "discoverCharacteristic failed");
        return null;
    }

    @Override
    public void analyzeData(String data) {
        if (D) Log.d(TAG, "data: " + data);
        UnboxResponseProtocol protocol = null;
        if (data.length() >= 34) {
            protocol = new UnboxResponseProtocol(data);
        }

        if (protocol != null && protocol.getType().equals(RS_EXECUTE_STATUS)) {
            String resultType = protocol.getExecuteResultType();
            String result = protocol.getExecuteResult();

            if (D) Log.d(TAG, "type: " + resultType);
            if (D) Log.d(TAG, "result: " + result);

            if (resultType.equals(RE_FIX_NORM) && result.equals("00")) {
                // todo 解析出报文中的数据，保存到 fixValue 中
                uploadFixResult();
            }
        }

        // TODO: 2017/10/9 remove code below
        // test interface
//        fixValue[currentFix] = 23.5;
//        uploadFixResult();
    }
}
