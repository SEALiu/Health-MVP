package cn.sealiu.health.login;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.UUID;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.response.LoginAndRegisterResponse;
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

public class LoginPresenter implements LoginContract.Presenter {
    private static final String TAG = "LoginPresenter";

    @NonNull
    private final LoginContract.View mLoginView;

    @Override
    public void start() {

    }

    public LoginPresenter(@NonNull LoginContract.View loginView) {
        mLoginView = checkNotNull(loginView);
        mLoginView.setPresenter(this);
    }

    @Override
    public void restore() {
        String phone = sharedPref.getString(LoginActivity.USER_PHONE, "");
        String pwd = sharedPref.getString(LoginActivity.USER_PASSWROD, "");

        mLoginView.restorePhone(phone);
        mLoginView.restorePwd(pwd);
    }

    @Override
    public void login(final String phone, final String pwd, final boolean isRemember) {
        if (phone.isEmpty()) {
            mLoginView.showPhoneEmptyError();
            return;
        }

        if (pwd.isEmpty()) {
            mLoginView.showPwdEmptyError();
            return;
        }

        // check network connection
        if (!ActivityUtils.isNetworkAvailable()) {
            mLoginView.showNoNetWorkError();
            return;
        }

        // do login
        OkHttpClient okHttpClient = new OkHttpClient();
        Request loginRequest = BaseActivity.buildHttpGetRequest("/user/doSignIn?" +
                "userPhone=" + phone + "&" +
                "userPwd=" + Fun.encode("MD5", pwd));


        if (loginRequest == null) return;
        if (D) Log.e(TAG, loginRequest.url().toString());

        okHttpClient.newCall(loginRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLoginView.showLoginError("login interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                LoginAndRegisterResponse result;
                try {
                    result = new Gson().fromJson(json, LoginAndRegisterResponse.class);
                } catch (Exception e) {
                    mLoginView.showLoginError("login interface error");
                    return;
                }

                if (result.getStatus().equals("200")) {
                    sharedPref.edit().putBoolean(MainActivity.USER_LOGIN, true).apply();
                    sharedPref.edit().putString(MainActivity.USER_UID, result.getUserUid()).apply();
                    sharedPref.edit().putString(MainActivity.USER_ID, result.getUserId()).apply();
                    sharedPref.edit().putString(MainActivity.USER_TYPE, result.getTypeId()).apply();
                    sharedPref.edit().putString(MainActivity.DEVICE_MID, result.getUserMid()).apply();

                    sharedPref.edit().putString(MainActivity.DEVICE_START_USING_DATE,
                            result.getFirst_calibtime()).apply();
                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_A, result.getComfort_A())
                            .apply();
                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_B, result.getComfort_B())
                            .apply();
                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_C, result.getComfort_C())
                            .apply();
                    sharedPref.edit().putString(MainActivity.DEVICE_COMFORT_D, result.getComfort_D())
                            .apply();

                    if (isRemember) {
                        sharedPref.edit().putString(LoginActivity.USER_PHONE, phone).apply();
                        sharedPref.edit().putString(LoginActivity.USER_PASSWROD, pwd).apply();
                    } else {
                        sharedPref.edit().putString(LoginActivity.USER_PHONE, "").apply();
                        sharedPref.edit().putString(LoginActivity.USER_PASSWROD, "").apply();
                    }

                    mLoginView.showLoginSuccess();

                    if (result.getTypeId().equals(IDENTITY_USER)) {
                        if (D) Log.d(TAG, sharedPref.getString(MainActivity.DEVICE_ADDRESS, ""));
                        if (D) Log.d(TAG, sharedPref.getString(MainActivity.DEVICE_MID, ""));

                        if (sharedPref.getString(MainActivity.DEVICE_ADDRESS, "").equals("") ||
                                sharedPref.getString(MainActivity.DEVICE_MID, "").equals("")) {
                            // there is no bluetooth mac address or
                            // device machine id in shardPref
                            mLoginView.gotoFindBluetooth();
                        } else {
                            mLoginView.gotoHome();
                        }

                    } else if (result.getTypeId().equals(IDENTITY_DOCTOR)) {
                        mLoginView.gotoHome();
                    }
                } else {
                    mLoginView.showLoginError(null);
                }
            }
        });
    }

    /**
     * 为程序提供一个固定用户（患者），可以使用离线功能
     */
    @Override
    public void offlineMode() {
        sharedPref.edit().putBoolean(MainActivity.USER_LOGIN, true).apply();
        if (sharedPref.getString(MainActivity.USER_UID, "").equals(""))
            sharedPref.edit().putString(MainActivity.USER_UID, UUID.randomUUID().toString().replace("-", "")).apply();

        sharedPref.edit().putString(MainActivity.USER_ID, "23").apply();
        sharedPref.edit().putString(MainActivity.USER_TYPE, IDENTITY_USER).apply();
        sharedPref.edit().putString(MainActivity.DEVICE_MID, "30303538").apply();
        sharedPref.edit().putString(BaseActivity.SERVER_IP, "").apply();

        if (sharedPref.getString(MainActivity.DEVICE_ADDRESS, "").equals("") ||
                sharedPref.getString(MainActivity.DEVICE_MID, "30303538").equals("30303538")) {
            // there is no bluetooth mac address or
            // device machine id in shardPref
            mLoginView.gotoFindBluetooth();
        } else {
            mLoginView.gotoHome();
        }
    }

    /**
     * 自定义服务器ip地址
     *
     * @param ip 服务器ip地址
     */
    @Override
    public void setCustomIp(String ip) {
        sharedPref.edit().putString(BaseActivity.SERVER_IP, "http://" + ip + ":8080").apply();
        mLoginView.showInfo("设置成功");
    }
}
