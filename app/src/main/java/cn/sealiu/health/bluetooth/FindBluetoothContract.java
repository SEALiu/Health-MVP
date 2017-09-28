package cn.sealiu.health.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.BluetoothLeService;

/**
 * Created by liuyang
 * on 2017/9/14.
 */

public interface FindBluetoothContract {

    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showNoAvailableService();

        void showInfo(int stringId);

        void showInfo(String errorMsg);

        void delayExit();

        void openBluetooth();

        void scanLeDevice(boolean enable);

        void gotoHome();

        void gotoLogin();

        void bindWithMid();

        void requestCompleteMid();
    }

    interface Presenter extends BasePresenter {
        BluetoothGattCharacteristic discoverCharacteristic(BluetoothLeService service);

        boolean checkLocation(Context context);

        void checkBluetoothSupport(Context context);

        void bindDevice2User(String uid, String mid);

        void analyzeData(String data);

        void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d);
    }
}
