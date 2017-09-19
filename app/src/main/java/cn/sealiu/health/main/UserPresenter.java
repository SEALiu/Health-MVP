package cn.sealiu.health.main;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;

import java.util.Timer;
import java.util.TimerTask;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.util.BoxRequestProtocol;

import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;
/**
 * Created by liuyang
 * on 2017/9/16.
 */

public class UserPresenter implements UserContract.Presenter {

    private final UserContract.View mUserView;

    @Override
    public void start() {

    }

    public UserPresenter(UserContract.View view) {
        mUserView = checkNotNull(view);
        mUserView.setPresenter(this);
    }

    @Override
    public void requestBaseInfo() {
        mUserView.setLoadingIndicator(true);

        // TODO: 2017/9/19 remove code below
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mUserView.setLoadingIndicator(false);
            }
        }, 5000);
    }

    @Override
    public void startRealtime() {
        mUserView.showError("start realtime");
    }

    @Override
    public void stopRealtime() {
        mUserView.showError("stop realtime");
    }

    @Override
    public void syncTime() {

    }

    @Override
    public void syncLocalData() {

    }

    @Override
    public void doSentRequest(BluetoothGattCharacteristic c, BluetoothLeService s, String d) {
        String uid = sharedPref.getString("user-id", "");
        if (uid.equals("")) {
            mUserView.gotoLogin();
            return;
        }

        if (d.length() > 0) {
            String protocol = BoxRequestProtocol.boxProtocol(d, uid);
            byte[] buff = BoxRequestProtocol.convertHex2Bytes(protocol);
            c.setValue(buff);
            s.writeCharacteristic(c);
        } else {
            mUserView.showError(R.string.empty_data);
        }
    }
}
