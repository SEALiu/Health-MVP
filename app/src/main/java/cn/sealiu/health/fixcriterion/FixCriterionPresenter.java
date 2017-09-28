package cn.sealiu.health.fixcriterion;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.SampleGattAttributes;
import cn.sealiu.health.util.UnboxResponseProtocol;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.util.ProtocolMsg.RS_ACK;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class FixCriterionPresenter implements FixCriterionContract.Presenter {
    private final static String TAG = "FixCriterionPresenter";

    private final FixCriterionContract.View mFixCriterionView;

    public FixCriterionPresenter(@NonNull FixCriterionContract.View view) {
        mFixCriterionView = checkNotNull(view);
        mFixCriterionView.setPresenter(this);
    }

    @Override
    public void start() {
        loadFixInfo();
    }

    @Override
    public void loadFixInfo() {
        // TODO: 2017/9/19 get user's fix info
    }

    @Override
    public void uploadFixResult() {
        // TODO: 2017/9/19 upload fix result
    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d) {
        String uid = sharedPref.getString(MainActivity.USER_ID, "");
        if (uid.equals("")) {
            mFixCriterionView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);
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
        UnboxResponseProtocol protocol = null;
        if (data.length() >= 34) {
            protocol = new UnboxResponseProtocol(data);
        }

        if (protocol != null && protocol.getType().equals(RS_ACK)) {
            String resultType = protocol.getExecuteResultType();
            String result = protocol.getExecuteResult();

            if (D) Log.d(TAG, "type: " + resultType);
            if (D) Log.d(TAG, "result: " + result);
        }
    }
}
