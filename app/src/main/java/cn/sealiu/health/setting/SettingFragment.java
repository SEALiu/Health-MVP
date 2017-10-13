package cn.sealiu.health.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.bluetooth.FindBluetoothActivity;
import cn.sealiu.health.fixcriterion.FixCriterionActivity;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.IDENTITY_DOCTOR;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.main.HomeUserFragment.mBluetoothLeService;
import static cn.sealiu.health.main.HomeUserFragment.mConnected;
import static cn.sealiu.health.main.HomeUserFragment.mWantedCharacteristic;
import static cn.sealiu.health.main.MainActivity.USER_LOGIN;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class SettingFragment extends Fragment implements SettingContract.View, View.OnClickListener {

    private static final String TAG = "SettingFragment";
    private static final int REQUEST_FIX = 1;
    private SettingContract.Presenter mPresenter;
    private TextView showChannelNumTV;
    private SwitchCompat setNetworkSwitch;
    private String userType;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userType = sharedPref.getString(MainActivity.USER_TYPE, "-1");
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_frag, container, false);

        setNetworkSwitch = root.findViewById(R.id.set_network_switch);
        if (sharedPref.getBoolean(MainActivity.NETWORK_ONLY_WIFI, false)) {
            //only use wifi
            setNetworkSwitch.setChecked(true);
        } else {
            setNetworkSwitch.setChecked(false);
        }

        showChannelNumTV = root.findViewById(R.id.show_channel_num);
        updateChannelNum();

        root.findViewById(R.id.logout).setOnClickListener(this);

        View showChannel = root.findViewById(R.id.set_show_channel);
        View fixCriterion = root.findViewById(R.id.fix_criterion);
        View setChannelName = root.findViewById(R.id.set_channel_name);
        View setNetwork = root.findViewById(R.id.set_network);
        View changeDevice = root.findViewById(R.id.change_device);

        //在设置里面取消定标操作
        fixCriterion.setVisibility(View.GONE);

        setChannelName.setOnClickListener(this);
        fixCriterion.setOnClickListener(this);
        showChannel.setOnClickListener(this);
        setNetworkSwitch.setOnClickListener(this);

        root.findViewById(R.id.about).setOnClickListener(this);
        changeDevice.setOnClickListener(this);

        if (userType.equals(IDENTITY_DOCTOR)) {
            showChannel.setVisibility(View.GONE);
            fixCriterion.setVisibility(View.GONE);
            setChannelName.setVisibility(View.GONE);
            setNetwork.setVisibility(View.GONE);
            changeDevice.setVisibility(View.GONE);
        }

        return root;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setLoadingIndicator(boolean active) {

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
    public void showInfo(String msg) {
        if (getView() != null)
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int strId) {
        showInfo(getString(strId));
    }

    @Override
    public void setPresenter(SettingContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_device:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.change_device)
                        .setMessage(R.string.change_device_confirm)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getActivity(), FindBluetoothActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }).show();
                break;
            case R.id.about:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.about)
                        .setMessage(R.string.about_content)
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                break;
            case R.id.logout:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.logout)
                        .setMessage(R.string.logout_confirm)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sharedPref.edit().putBoolean(USER_LOGIN, false).apply();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }).show();
                break;
            case R.id.fix_criterion:
                if (mConnected == BluetoothLeService.STATE_CONNECTED &&
                        mWantedCharacteristic != null &&
                        mBluetoothLeService != null) {
                    Intent intent = new Intent(getActivity(), FixCriterionActivity.class);
                    startActivityForResult(intent, REQUEST_FIX);
                } else {
                    showInfo("设备未连接");
                }
                break;
            case R.id.set_show_channel:
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.change_dia, null);
                final EditText content = dialogView.findViewById(R.id.content);
                content.setInputType(InputType.TYPE_CLASS_NUMBER);
                content.setHint(R.string.channel_number);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(getString(R.string.set_show_channel))
                        .setView(dialogView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String channelNum = content.getText().toString();
                                if (channelNum.isEmpty()) {
                                    showInfo(R.string.channel_number_unchanged);
                                    dialogInterface.dismiss();
                                } else if (Integer.valueOf(channelNum) < 1 ||
                                        Integer.valueOf(channelNum) > 4) {
                                    showInfo(R.string.channel_number_error);
                                    dialogInterface.dismiss();
                                } else {

                                    sharedPref.edit().putInt(MainActivity.DEVICE_CHANNEL_NUM, Integer.valueOf(channelNum)).apply();
                                    showInfo("显示通道数量已保存");

                                    updateChannelNum();
                                    // TODO: 2017/9/26 change code below; add first and second params;
//                                    mPresenter.doSentRequest(null, null,
//                                            BoxRequestProtocol.boxRequestDeviceParam(
//                                                    ProtocolMsg.DEVICE_PARAM_CHANNEL_NUM,
//                                                    channelNum
//                                            ));
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                builder.show();
                break;
            case R.id.set_channel_name:
                LayoutInflater inflater1 = getActivity().getLayoutInflater();
                View setChannelNameView = inflater1.inflate(R.layout.set_channel_name_dia, null);
                final EditText channelName = setChannelNameView.findViewById(R.id.content);
                final AppCompatRadioButton channelA = setChannelNameView.findViewById(R.id.channel_AA);
                final AppCompatRadioButton channelB = setChannelNameView.findViewById(R.id.channel_BB);
                final AppCompatRadioButton channelC = setChannelNameView.findViewById(R.id.channel_CC);
                final AppCompatRadioButton channelD = setChannelNameView.findViewById(R.id.channel_DD);

                channelA.setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_ONE, "通道一"));
                channelB.setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_TWO, "通道二"));
                channelC.setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_THREE, "通道三"));
                channelD.setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_FOUR, "通道四"));

                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

                builder1.setTitle(getString(R.string.set_channel_name))
                        .setView(setChannelNameView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = channelName.getText().toString();
                                if (!channelA.isChecked() && !channelB.isChecked() &&
                                        !channelC.isChecked() && !channelD.isChecked()) {
                                    showInfo("请先选择要命名的通道");
                                } else if (name.isEmpty()) {
                                    showInfo("请输入命名");
                                } else if (length(name) / 2 > 2) {
                                    showInfo(R.string.channel_name_toolong);
                                    dialogInterface.dismiss();
                                } else {
                                    if (channelA.isChecked()) {
                                        sharedPref.edit().putString(MainActivity.DEVICE_CHANNEL_ONE, name).apply();
                                    } else if (channelB.isChecked()) {
                                        sharedPref.edit().putString(MainActivity.DEVICE_CHANNEL_TWO, name).apply();
                                    } else if (channelC.isChecked()) {
                                        sharedPref.edit().putString(MainActivity.DEVICE_CHANNEL_THREE, name).apply();
                                    } else if (channelD.isChecked()) {
                                        sharedPref.edit().putString(MainActivity.DEVICE_CHANNEL_FOUR, name).apply();
                                    }

                                    showInfo("通道已命名");
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                builder1.show();
                break;
            case R.id.set_network_switch:
                sharedPref.edit().putBoolean(MainActivity.NETWORK_ONLY_WIFI, setNetworkSwitch.isChecked())
                        .apply();

                if (setNetworkSwitch.isChecked())
                    showInfo(R.string.only_wifi);
                else
                    showInfo(R.string.use_cell);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FIX) {
            // TODO: 2017/9/26 show fix result info
            if (D) Log.d(TAG, resultCode + "");
        }
    }

    @Override
    public void updateChannelNum() {
        showChannelNumTV.setText(String.valueOf(sharedPref.getInt(MainActivity.DEVICE_CHANNEL_NUM, 4)));
    }

    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }
}
