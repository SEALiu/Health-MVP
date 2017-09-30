package cn.sealiu.health.setting;

import android.bluetooth.BluetoothGattCharacteristic;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.BluetoothLeService;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface SettingContract {

    interface View extends BaseView<Presenter> {
        boolean isActive();

        void setLoadingIndicator(boolean active);

        void gotoLogin();

        void showInfo(String msg);

        void showInfo(int strId);

        void updateChannelNum();
    }

    interface Presenter extends BasePresenter {

        void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d);
    }
}
