package cn.sealiu.health.setting;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.BoxRequestProtocol;

import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class SettingPresenter implements SettingContract.Presenter {

    private final SettingContract.View mSettingView;

    @Override
    public void start() {

    }

    public SettingPresenter(@NonNull SettingContract.View settingView) {
        mSettingView = checkNotNull(settingView);
        mSettingView.setPresenter(this);
    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d) {
        String uid = sharedPref.getString(MainActivity.USER_ID, "");
        if (uid.equals("")) {
            mSettingView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);
            byte[] buff = BoxRequestProtocol.convertHex2Bytes(protocol);
            c.setValue(buff);
            s.writeCharacteristic(c);
        } else {
            mSettingView.showInfo(R.string.empty_data);
        }

        // TODO: 2017/9/26 还要实现获取数据的接口，通过返回的数据进行提示成功或失败
//        int[] flags = {1, 0, 0, 0};
//        mFixCriterionView.updateUI(flags);
    }
}
