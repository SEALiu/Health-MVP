package cn.sealiu.health.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

import cn.sealiu.health.R;
import cn.sealiu.health.bluetooth.FindBluetoothActivity;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;

import static cn.sealiu.health.BaseActivity.D;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class RegisterFragment extends Fragment implements
        RegisterContract.View,
        View.OnClickListener {

    private final static String TAG = "RegisterFragment";
    private RegisterContract.Presenter mPresenter;
    private EditText mPhoneET, mPwdET, mRePwdET, mCodeET;
    private AppCompatCheckBox mDoctorCB;

    public RegisterFragment() {
    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.register_frag, container, false);

        mPhoneET = root.findViewById(R.id.phone_number);
        mPwdET = root.findViewById(R.id.password);
        mRePwdET = root.findViewById(R.id.re_password);
        mCodeET = root.findViewById(R.id.code);
        mDoctorCB = root.findViewById(R.id.is_doctor);

        // init view: not doctor
        mDoctorCB.setChecked(false);
        mCodeET.setVisibility(View.GONE);

        root.findViewById(R.id.register_btn).setOnClickListener(this);
        root.findViewById(R.id.login_btn).setOnClickListener(this);
        mDoctorCB.setOnClickListener(this);
        return root;
    }

    @Override
    public void showNoNetWorkError() {
        Snackbar.make(mRePwdET, R.string.error_network, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPhoneEmptyError() {
//        mPhoneET.setError(getString(R.string.required));
        Snackbar.make(mPwdET, R.string.empty_phone_number, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPwdEmptyError() {
//        mPwdET.setError(getString(R.string.required));
        Snackbar.make(mPwdET, R.string.empty_pwd, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showRePwdEmptyError() {
//        mRePwdET.setError(getString(R.string.required));
        Snackbar.make(mRePwdET, R.string.empty_re_pwd, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showCodeEmptyError() {
//        mCodeET.setError(getString(R.string.required));
        Snackbar.make(mCodeET, R.string.empty_code, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPhoneFormatError() {
//        mPhoneET.setError(getString(R.string.phone_format_error));
        Snackbar.make(mPwdET, R.string.phone_format_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPwdFormatError() {
//        mPwdET.setError(getString(R.string.pwd_format_error));
        Snackbar.make(mPwdET, R.string.pwd_format_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showCodeFormatError() {
//        mCodeET.setError(getString(R.string.format_error));
        Snackbar.make(mCodeET, R.string.code_format_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showRePwdError() {
//        mRePwdET.setError(getString(R.string.re_pwd_error));
        Snackbar.make(mRePwdET, R.string.re_pwd_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showPhoneRegistered() {
        mPhoneET.setText("");
        Snackbar.make(mPwdET, R.string.phone_registered_error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showRegisterError(String reason) {
        Snackbar.make(mRePwdET, reason, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showRegisterSuccess() {
        Snackbar.make(mRePwdET, R.string.register_success, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void gotoHome() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().startActivity(intent);

                getActivity().finish();
            }
        }, 1000);
    }

    @Override
    public void gotoFindBluetooth() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().startActivity(new Intent(getActivity(), FindBluetoothActivity.class));
                getActivity().finish();
            }
        }, 1000);
    }

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_btn:
                if (D) Log.e(TAG, mPhoneET.getText().toString() + "\n" +
                        mPwdET.getText().toString() + "\n" +
                        mDoctorCB.isChecked() + "\n" +
                        mCodeET.getText().toString()
                );

                mPresenter.register(mPhoneET.getText().toString(),
                        mPwdET.getText().toString(),
                        mRePwdET.getText().toString(),
                        mDoctorCB.isChecked(),
                        mCodeET.getText().toString());

                break;
            case R.id.login_btn:
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                break;
            case R.id.is_doctor:
                if (mDoctorCB.isChecked()) {
                    mCodeET.setVisibility(View.VISIBLE);
                } else {
                    mCodeET.setVisibility(View.GONE);
                }
                break;
        }
    }
}
