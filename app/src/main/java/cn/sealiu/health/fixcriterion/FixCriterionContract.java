package cn.sealiu.health.fixcriterion;

import android.bluetooth.BluetoothGattCharacteristic;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.BluetoothLeService;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public interface FixCriterionContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoLogin();

        int getCurrentFix();

        void updateUI();
    }

    interface Presenter extends BasePresenter {

        void uploadFixResult();

        void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d, int currentFix);

        BluetoothGattCharacteristic discoverCharacteristic(BluetoothLeService service);

        void analyzeData(String data);
    }
}
