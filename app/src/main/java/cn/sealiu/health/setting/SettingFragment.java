package cn.sealiu.health.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import cn.sealiu.health.R;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.IDENTITY_DOCTOR;
import static cn.sealiu.health.BaseActivity.sharedPref;
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

        showChannelNumTV = root.findViewById(R.id.show_channel_num);
        updateChannelNum();

        root.findViewById(R.id.logout).setOnClickListener(this);

        View showChannel = root.findViewById(R.id.set_show_channel);
        View fixCriterion = root.findViewById(R.id.fix_criterion);
        View setChannelName = root.findViewById(R.id.set_channel_name);
        View setNetwork = root.findViewById(R.id.set_network);

        setChannelName.setOnClickListener(this);
        setNetwork.setOnClickListener(this);
        fixCriterion.setOnClickListener(this);
        showChannel.setOnClickListener(this);

        if (userType.equals(IDENTITY_DOCTOR)) {
            showChannel.setVisibility(View.GONE);
            fixCriterion.setVisibility(View.GONE);
            setChannelName.setVisibility(View.GONE);
            setNetwork.setVisibility(View.GONE);
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
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
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
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }).show();
                break;
            case R.id.fix_criterion:
//                Intent intent = new Intent(getActivity(), FixCriterionActivity.class);
//                startActivityForResult(intent, REQUEST_FIX);
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
            case R.id.set_network:
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
