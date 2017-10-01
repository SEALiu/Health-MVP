package cn.sealiu.health.main;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.Nullable;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.data.local.HealthDbHelper;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface UserContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoLogin();

        void gotoFixCriterion();

        void setLoadingIndicator(boolean active);

        void updateBattery(String batteryLeft);

        void updateStorage(String storageLeft);

        void updateTime(@Nullable String time);

        void delayExit();

        void openBluetooth();

        void showNoAvailableService();

        void changeMenuBluetoothIcon(int resourceId);

        void setSyncTime();

        void requestDeviceParam(String paramName);

        void setDeviceParam(String paramName, String value);

        void requestDeviceStatus();

        void requestRealtime(boolean active);

        void updateLineChartRT(float value, String comfort, int sequence);

        void updateDataStatus();
    }

    interface Presenter extends BasePresenter {
        void checkBluetoothSupport(Context context);

        // request battery left, storage left, last sync time
        void requestBaseInfo();

        void requestDeviceHighMID();

        void requestDeviceLowMID();

        void requestDeviceEnableDate();

        void requestChannelNum();

        void requestChannelOne();

        void requestChannelTwo();

        void requestChannelThree();

        void requestChannelFour();

        void requestDeviceSlope();

        void requestDeviceOffset();

        void requestSamplingFrequency();

        void startRealtime();

        void stopRealtime();

        void syncTime();

        void syncLocalData();

        void onGattServicesDiscovered();

        void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d);

        BluetoothGattCharacteristic discoverCharacteristic(BluetoothLeService service);

        void analyzeData(String data);

        void updateDatastatusTb(HealthDbHelper dbHelper);
    }
}
