package cn.sealiu.health.fixcriterion;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.ProtocolMsg;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.main.HomeUserFragment.mBluetoothLeService;
import static cn.sealiu.health.main.HomeUserFragment.mChosenBTAddress;
import static cn.sealiu.health.main.HomeUserFragment.mConnected;
import static cn.sealiu.health.main.HomeUserFragment.mWantedCharacteristic;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class FixCriterionFragment extends Fragment implements FixCriterionContract.View, View.OnClickListener {
    private static final String TAG = "FixCriterionFragment";

    private FixCriterionContract.Presenter mPresenter;
    private RadioButton fixBlankRB, fixLooseRB, fixComfortRB, fixTightRB;
    private AppCompatButton skipButton;

    private List<RadioButton> fixRBs = new ArrayList<>();
    private List<TextView> fixResultTVs = new ArrayList<>();

    private int currentFix = -1;
    private boolean[] flags = new boolean[4];

    private IntentFilter gattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = BluetoothLeService.STATE_CONNECTED;
                showMessage(getString(R.string.device_connected));
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = BluetoothLeService.STATE_DISCONNECTED;

                new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setTitle(R.string.device_disconnected)
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.reconnect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                manualConnect();
                            }
                        }).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // You can get List<BluetoothGattService>
                // through function: mBluetoothLeService.getSupportedGattServices()
                if (D) Log.d(TAG, "BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED");

                mWantedCharacteristic = mPresenter.discoverCharacteristic(mBluetoothLeService);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mPresenter.analyzeData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    public FixCriterionFragment() {
    }

    public static FixCriterionFragment newInstance() {
        return new FixCriterionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().registerReceiver(mGattUpdateReceiver, gattUpdateIntentFilter());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fixcriterion_frag, container, false);

        fixBlankRB = root.findViewById(R.id.fix_blank);
        fixLooseRB = root.findViewById(R.id.fix_loose);
        fixComfortRB = root.findViewById(R.id.fix_comfort);
        fixTightRB = root.findViewById(R.id.fix_tight);

        fixRBs.add(fixBlankRB);
        fixRBs.add(fixLooseRB);
        fixRBs.add(fixComfortRB);
        fixRBs.add(fixTightRB);

        TextView fixBlankResultTV = root.findViewById(R.id.fix_blank_result);
        TextView fixLooseResultTV = root.findViewById(R.id.fix_loose_result);
        TextView fixComfortResultTV = root.findViewById(R.id.fix_comfort_result);
        TextView fixTightResultTV = root.findViewById(R.id.fix_tight_result);

        fixResultTVs.add(fixBlankResultTV);
        fixResultTVs.add(fixLooseResultTV);
        fixResultTVs.add(fixComfortResultTV);
        fixResultTVs.add(fixTightResultTV);

        skipButton = root.findViewById(R.id.skip);
        AppCompatButton fixButton = root.findViewById(R.id.fix_criterion_btn);

        fixBlankRB.setOnClickListener(this);
        fixLooseRB.setOnClickListener(this);
        fixComfortRB.setOnClickListener(this);
        fixTightRB.setOnClickListener(this);
        fixButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);

        // init UI
        skipButton.setVisibility(View.GONE);
        updateUI();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showInfo(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int strId) {
        showInfo(getString(strId));
    }

    @Override
    public void gotoLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public int getCurrentFix() {
        return currentFix;
    }

    @Override
    public void updateUI() {

        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                boolean isSomeoneFixed = false;

                String comfortA = sharedPref.getString(MainActivity.DEVICE_COMFORT_A, "");
                String comfortB = sharedPref.getString(MainActivity.DEVICE_COMFORT_B, "");
                String comfortC = sharedPref.getString(MainActivity.DEVICE_COMFORT_C, "");
                String comfortD = sharedPref.getString(MainActivity.DEVICE_COMFORT_D, "");

                flags[0] = comfortA.equals("") || comfortA.equals("0.0");
                flags[1] = comfortB.equals("") || comfortB.equals("0.0");
                flags[2] = comfortC.equals("") || comfortC.equals("0.0");
                flags[3] = comfortD.equals("") || comfortD.equals("0.0");

                for (boolean flag : flags) {
                    if (flag) { //unfixed
                        fixResultTVs.get(index).setText(R.string.unfixed);
                        fixRBs.get(index).setEnabled(true);
                    } else { //fixed
                        fixResultTVs.get(index).setText(R.string.fixed);
                        fixRBs.get(index).setEnabled(false);
                        isSomeoneFixed = true;
                    }

                    index++;
                }

                if (isSomeoneFixed)
                    skipButton.setVisibility(View.VISIBLE);
                else
                    skipButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void setPresenter(@NonNull FixCriterionContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fix_blank:
                resetAllRadioButton();
                fixBlankRB.setChecked(true);
                currentFix = 0;
                break;
            case R.id.fix_loose:
                resetAllRadioButton();
                fixLooseRB.setChecked(true);
                currentFix = 1;
                break;
            case R.id.fix_comfort:
                resetAllRadioButton();
                fixComfortRB.setChecked(true);
                currentFix = 2;
                break;
            case R.id.fix_tight:
                resetAllRadioButton();
                fixTightRB.setChecked(true);
                currentFix = 3;
                break;
            case R.id.skip:
//                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
//                getActivity().finish();
                break;
            case R.id.fix_criterion_btn:
                if (!fixBlankRB.isChecked() && !fixLooseRB.isChecked() && !fixComfortRB.isChecked() &&
                        !fixTightRB.isChecked()) {
                    showInfo(R.string.no_fix_type_selected);
                    return;
                }
                if (mWantedCharacteristic != null) {
                    final int charaProp = mWantedCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        mBluetoothLeService.readCharacteristic(mWantedCharacteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(
                                mWantedCharacteristic, true);
                    }

//                    mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
//                            BoxRequestProtocol.boxRequestFixNorm(), currentFix);

                    mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                            BoxRequestProtocol.boxRequestDeviceParam(
                                    ProtocolMsg.DEVICE_PARAM_COMFORT_ONE, null), currentFix);
                }
                break;
        }
    }

    private void resetAllRadioButton() {
        fixBlankRB.setChecked(false);
        fixLooseRB.setChecked(false);
        fixComfortRB.setChecked(false);
        fixTightRB.setChecked(false);
    }

    private void showMessage(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    private void manualConnect() {
        if (D)
            Log.d(TAG, "mBluetoothLeService is " + (mBluetoothLeService == null ? "is" : "is not") + " null");
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mChosenBTAddress);
            mConnected = BluetoothLeService.STATE_CONNECTING;
        }
    }
}
