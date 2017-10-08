package cn.sealiu.health.register;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.UUID;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.response.LoginAndRegisterResponse;
import cn.sealiu.health.data.response.MiniResponse;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.ActivityUtils;
import cn.sealiu.health.util.Fun;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.IDENTITY_DOCTOR;
import static cn.sealiu.health.BaseActivity.IDENTITY_USER;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class RegisterPresenter implements RegisterContract.Presenter {

    private static final String TAG = "RegisterPresenter";

    @NonNull
    private final RegisterContract.View mRegisterView;

    @Override
    public void start() {

    }

    public RegisterPresenter(@NonNull RegisterContract.View registerView) {
        mRegisterView = checkNotNull(registerView);
        mRegisterView.setPresenter(this);
    }

    @Override
    public void register(@NonNull String phone, @NonNull String pwd, @NonNull String rePwd,
                         boolean isDoctor, String code) {
        // to upper case and remove blank space
        code = code.toUpperCase().trim();

        // check register info
        if (!checkInfo(phone, pwd, rePwd, isDoctor, code)) {
            return;
        }

        // check network connection
        if (!ActivityUtils.isNetworkAvailable()) {
            mRegisterView.showNoNetWorkError();
            return;
        }

        // do register after checking phone number and code;
        OkHttpClient okHttpClient = new OkHttpClient();

        checkPhoneRegistered(okHttpClient, phone, pwd, isDoctor, code);
    }

    // check phone number, password, confirm-password, role and verify code
    // return a boolean value showing whether these info through the checking.
    private boolean checkInfo(String phone, String pwd, String rePwd, boolean isDoctor, String code) {
        if (phone.isEmpty()) {
            mRegisterView.showPhoneEmptyError();
            return false;
        }

        if (pwd.isEmpty()) {
            mRegisterView.showPwdEmptyError();
            return false;
        }

        if (rePwd.isEmpty()) {
            mRegisterView.showRePwdEmptyError();
            return false;
        }

        if (isDoctor && code.isEmpty()) {
            mRegisterView.showCodeEmptyError();
            return false;
        }

        if (isDoctor && code.length() != 6) {
            mRegisterView.showCodeFormatError();
            return false;
        }

        if (phone.length() != 11) {
            mRegisterView.showPhoneFormatError();
            return false;
        }

        if (pwd.length() < 6) {
            mRegisterView.showPwdFormatError();
            return false;
        }

        if (!rePwd.equals(pwd)) {
            mRegisterView.showRePwdError();
            return false;
        }

        return true;
    }

    // check phone number is registered
    private void checkPhoneRegistered(final OkHttpClient okHttp, final String phone, final String pwd,
                                      final boolean isDoctor, final String code) {
        Request phoneCheckRequest = BaseActivity.buildHttpGetRequest("/user/isRepeatLoginName?" +
                "userPhone=" + phone);
        okHttp.newCall(phoneCheckRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getMessage());
                mRegisterView.showRegisterError("check phone interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                MiniResponse result = new Gson().fromJson(json, MiniResponse.class);
                if (result.getResult().equals("true")) {
                    mRegisterView.showPhoneRegistered();
                } else {
                    // 手机号未注册过
                    if (isDoctor)
                        checkDoctorCode(okHttp, phone, pwd, code);
                    else
                        doRegister(okHttp, phone, pwd, false);
                }
            }
        });
    }

    // check doctor code is correct
    private void checkDoctorCode(final OkHttpClient okHttp, final String phone, final String pwd, String code) {
        Request checkCodeRequest = BaseActivity.buildHttpGetRequest("/sys/isCorrectCode?" +
                "svalue=" + code);

        okHttp.newCall(checkCodeRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getMessage());
                mRegisterView.showRegisterError("check code interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, "checkDoctorCode: " + json);

                MiniResponse result = new Gson().fromJson(json, MiniResponse.class);
                if (result.getStatus().equals("200")) {
                    doRegister(okHttp, phone, pwd, true);
                } else {
                    mRegisterView.showRegisterError("身份验证码错误");
                }
            }
        });
    }

    // through all checking
    // do register operation
    private void doRegister(OkHttpClient okHttp, final String phone, final String pwd,
                            final boolean isDoctor) {
        // generate a random uuid for the user;
        final String uuid = UUID.randomUUID().toString().replace("-", "");

        final String identity = isDoctor ? IDENTITY_DOCTOR : IDENTITY_USER;

        Request registerRequest = BaseActivity.buildHttpGetRequest("/user/doSignUp?" +
                "userPhone=" + phone + "&" +
                "userPwd=" + Fun.encode("MD5", pwd) + "&" +
                "t_id=" + identity + "&" +
                "userUid=" + uuid);
        if (D) Log.d(TAG, registerRequest.url().toString());

        okHttp.newCall(registerRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getMessage());
                mRegisterView.showRegisterError("register interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                LoginAndRegisterResponse result;
                try {
                    result = new Gson().fromJson(json, LoginAndRegisterResponse.class);
                } catch (Exception e) {
                    mRegisterView.showRegisterError("register interface error");
                    return;
                }

                if (result.getStatus().equals("200")) {
                    sharedPref.edit().putString(MainActivity.USER_UID, uuid).apply();
                    sharedPref.edit().putString(MainActivity.USER_ID, result.getUserId()).apply();
                    sharedPref.edit().putString(MainActivity.USER_TYPE, identity).apply();
                    sharedPref.edit().putBoolean(MainActivity.USER_LOGIN, true).apply();

                    sharedPref.edit().putString(LoginActivity.USER_PHONE, phone).apply();
                    sharedPref.edit().putString(LoginActivity.USER_PASSWROD, pwd).apply();

                    mRegisterView.showRegisterSuccess();

                    if (isDoctor) {
                        mRegisterView.showRegisterSuccess();
                        // doctor can access home directly;
                        mRegisterView.gotoHome();
                    } else {
                        mRegisterView.showRegisterSuccess();
                        // normal user must paired with a bluetooth before enter home page;
                        mRegisterView.gotoFindBluetooth();
                    }
                }
            }
        });
    }
}
