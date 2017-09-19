package cn.sealiu.health.main;

import android.bluetooth.BluetoothGattCharacteristic;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.BluetoothLeService;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface UserContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showError(String msg);

        void showError(int strId);

        void gotoLogin();

        void setLoadingIndicator(boolean active);

        void updateBattery(String batteryLeft);

        void updateStorage(String storageLeft);

        void updateSync(String syncTime);
    }

    interface Presenter extends BasePresenter {
        // request battery left, storage left, last sync time
        void requestBaseInfo();

        void startRealtime();

        void stopRealtime();

        void syncTime();

        void syncLocalData();

        void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d);
    }
}
