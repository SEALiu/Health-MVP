package cn.sealiu.health.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.regex.Pattern;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.bluetooth.FindBluetoothActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.register.RegisterActivity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class LoginFragment extends Fragment implements
        LoginContract.View,
        View.OnClickListener {

    private LoginContract.Presenter mPresenter;
    private EditText mPhoneET, mPwdET, customIpET;
    private AppCompatCheckBox mRememberCB;

    public LoginFragment() {
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull LoginContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login_frag, container, false);
        mPhoneET = root.findViewById(R.id.phone_number);
        mPwdET = root.findViewById(R.id.password);
        mRememberCB = root.findViewById(R.id.remember_me);
        customIpET = root.findViewById(R.id.custom_ip);

        mRememberCB.setChecked(true);

        root.findViewById(R.id.login_btn).setOnClickListener(this);
        root.findViewById(R.id.register_btn).setOnClickListener(this);
        root.findViewById(R.id.set_ip).setOnClickListener(this);
        root.findViewById(R.id.offline_model).setOnClickListener(this);

        // restore phone and password if checkbox "remember me" is checked last time;
        mPresenter.restore();

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                BaseActivity.hideKeyboard();
                mPresenter.login(mPhoneET.getText().toString(), mPwdET.getText().toString(),
                        mRememberCB.isChecked());
                break;
            case R.id.register_btn:
                getActivity().startActivity(new Intent(getActivity(), RegisterActivity.class));
                break;
            case R.id.offline_model:
                mPresenter.offlineMode();
                break;
            case R.id.set_ip:
                String customIp = customIpET.getText().toString();

                String IP_PATTERN = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
                Pattern ipPattern = Pattern.compile(IP_PATTERN);

                if (customIp.equals(""))
                    Snackbar.make(mPwdET, "IP地址为空", Snackbar.LENGTH_LONG).show();
                else if (!ipPattern.matcher(customIp).matches())
                    Snackbar.make(mPwdET, "IP地址格式错误", Snackbar.LENGTH_LONG).show();
                else
                    mPresenter.setCustomIp(customIp);
                break;
        }
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showNoNetWorkError() {
        Snackbar.make(mPwdET, R.string.error_network, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPhoneEmptyError() {
        //mPhoneET.setError(getString(R.string.required));
        Snackbar.make(mPhoneET, R.string.empty_phone_number, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPwdEmptyError() {
        //mPwdET.setError(getString(R.string.required));
        Snackbar.make(mPwdET, R.string.empty_pwd, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoginError(String reason) {
        if (reason == null)
            Snackbar.make(mPwdET, R.string.login_error_help, Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(mPwdET, reason, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoginSuccess() {
        Snackbar.make(mPwdET, R.string.login_success, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void restorePhone(String phone) {
        mPhoneET.setText(phone);
    }

    @Override
    public void restorePwd(String pwd) {
        mPwdET.setText(pwd);
    }

    @Override
    public void gotoFindBluetooth() {
        getActivity().startActivity(new Intent(getActivity(), FindBluetoothActivity.class));
        getActivity().finish();
    }

    @Override
    public void gotoHome() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showInfo(String msg) {
        Snackbar.make(mPwdET, msg, Snackbar.LENGTH_LONG).show();
    }
}
