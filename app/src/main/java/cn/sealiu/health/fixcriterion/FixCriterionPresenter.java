package cn.sealiu.health.fixcriterion;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.NonNull;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.util.BoxRequestProtocol;

import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class FixCriterionPresenter implements FixCriterionContract.Presenter {

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
        String uid = sharedPref.getString("user-id", "");
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
            mFixCriterionView.showError(R.string.empty_data);
        }

        // TODO: 2017/9/19 remove one line code below
        int[] flags = {1, 0, 0, 0};
        mFixCriterionView.updateUI(flags);
    }
}
