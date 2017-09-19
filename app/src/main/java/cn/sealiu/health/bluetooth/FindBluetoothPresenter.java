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
import cn.sealiu.health.data.bean.MiniResponse;
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
import static cn.sealiu.health.util.ProtocolMsg.RS_EXECUTE_STATUS;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/14.
 */

public class FindBluetoothPresenter implements FindBluetoothContract.Presenter {
    private static final String TAG = "FindBluetoothPresenter";

    private BluetoothAdapter mBluetoothAdapter;

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
            mFindBluetoothView.showError(R.string.bt_not_support);
            mFindBluetoothView.delayExit();
            return;
        }

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mFindBluetoothView.showError(R.string.ble_not_support);
            mFindBluetoothView.delayExit();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mFindBluetoothView.openBluetooth();
        } else {
            mFindBluetoothView.scanLeDevice(true);
        }
    }

    @Override
    public void bindDevice2User(@NonNull String userId, @NonNull String machineId) {
        String uid = checkNotNull(userId);
        final String mid = checkNotNull(machineId);

        Request bindRequest = BaseActivity.buildHttpGetRequest("/user/bindMachine?" +
                "userId=" + uid + "&" +
                "userMid=" + mid);

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(bindRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mFindBluetoothView.showError("bind machine interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MiniResponse miniResponse = new Gson().fromJson(
                        response.body().string(), MiniResponse.class);

                if (miniResponse.getStatus().equals("200")) {
                    sharedPref.edit().putString("mid", mid).apply();
                    mFindBluetoothView.gotoHome();
                } else {
                    if (D) Log.e(TAG, "can not upload bind info to remote database");
                }
            }
        });
    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d) {
        String uid = sharedPref.getString("user-id", "");
        if (uid.equals("")) {
            mFindBluetoothView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);
            byte[] buff = BoxRequestProtocol.convertHex2Bytes(protocol);
            c.setValue(buff);
            s.writeCharacteristic(c);
        } else {
            mFindBluetoothView.showError(R.string.empty_data);
        }
    }

    @Override
    public void analyzeData(String data) {
        if (D) Log.d(TAG, "receive data: " + data);

        UnboxResponseProtocol protocol = new UnboxResponseProtocol(data);
        if (data.length() >= 34 &&
                protocol.getIsValidate() &&
                protocol.getType().equals(RS_EXECUTE_STATUS)) {
            String resultType = protocol.getExecuteResultType();
            String result = protocol.getExecuteResult();

            if (resultType.equals(RE_CERTIFICATION)) {
                switch (result) {
                    case EXECUTE_SUCCESS:
                        String mid = protocol.getExecuteBindedData().substring(10, 18);
                        String userId = sharedPref.getString("user-id", "");
                        if (!userId.equals("")) {
                            bindDevice2User(userId, mid);
                        }
                        break;
                    case EXECUTE_FAILED_WRONG_UID:
                        // when uid = 0, user need to re-login
                        mFindBluetoothView.gotoLogin();
                        break;
                    case EXECUTE_FAILED_NUMBER_LIMIT:
                        mFindBluetoothView.showError(R.string.upto_bind_number_limit);
                        break;
                    case EXECUTE_FAILED_UID_EXIST:
                        mFindBluetoothView.bindWithMid();
                        break;
                    case EXECUTE_FAILED_WRONG_MID:
                        mFindBluetoothView.showError(R.string.input_mid_error);
                        break;
                }
            }
        }
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
