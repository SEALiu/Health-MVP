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

        void showError(String msg);

        void showError(int strId);

        void gotoLogin();

        void updateUI(int[] fixFlags);
    }

    interface Presenter extends BasePresenter {
        void loadFixInfo();

        void uploadFixResult();

        void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d);
    }
}