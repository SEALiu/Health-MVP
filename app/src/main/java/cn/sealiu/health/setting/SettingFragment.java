package cn.sealiu.health.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import cn.sealiu.health.R;
import cn.sealiu.health.fixcriterion.FixCriterionActivity;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.ProtocolMsg;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class SettingFragment extends Fragment implements SettingContract.View, View.OnClickListener {

    private static final String TAG = "SettingFragment";
    private static final int REQUEST_FIX = 1;
    private SettingContract.Presenter mPresenter;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        //setHasOptionsMenu(true);

        root.findViewById(R.id.fix_criterion).setOnClickListener(this);
        root.findViewById(R.id.set_show_channel).setOnClickListener(this);
        root.findViewById(R.id.logout).setOnClickListener(this);

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
                                sharedPref.edit().putBoolean(MainActivity.USER_LOGIN, false).apply();
                                getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
                                getActivity().finish();
                            }
                        }).show();
                break;
            case R.id.fix_criterion:
                Intent intent = new Intent(getActivity(), FixCriterionActivity.class);
                startActivityForResult(intent, REQUEST_FIX);
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

                                    // TODO: 2017/9/26 change code below; add first and second params;
                                    mPresenter.doSentRequest(null, null,
                                            BoxRequestProtocol.boxRequestDeviceParam(
                                                    ProtocolMsg.DEVICE_PARAM_CHANNEL_NUM,
                                                    channelNum
                                            ));
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                builder.show();
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
}
