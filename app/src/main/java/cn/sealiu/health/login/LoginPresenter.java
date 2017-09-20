package cn.sealiu.health.login;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.bean.BaseResponse;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.ActivityUtils;
import cn.sealiu.health.util.Fun;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
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

        okHttpClient.newCall(loginRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLoginView.showLoginError("login interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                BaseResponse result;
                try {
                    result = new Gson().fromJson(json, BaseResponse.class);
                } catch (Exception e) {
                    mLoginView.showLoginError("login interface error");
                    return;
                }

                if (result.getStatus().equals("200")) {
                    //sharedPref.edit().putString("user-uuid", result.getUserUid()).apply();
                    sharedPref.edit().putString(MainActivity.USER_ID, result.getUserId()).apply();
                    sharedPref.edit().putString(MainActivity.USER_TYPE, result.getTypeId()).apply();
                    sharedPref.edit().putBoolean(MainActivity.USER_LOGIN, true).apply();
                    // TODO: 2017/9/15 登录接口返回用户的 mid
                    sharedPref.edit().putString(MainActivity.DEVICE_MID, result.getMid()).apply();

                    if (isRemember) {
                        sharedPref.edit().putString(LoginActivity.USER_PHONE, phone).apply();
                        sharedPref.edit().putString(LoginActivity.USER_PASSWROD, pwd).apply();
                    } else {
                        sharedPref.edit().putString(LoginActivity.USER_PHONE, "").apply();
                        sharedPref.edit().putString(LoginActivity.USER_PASSWROD, "").apply();
                    }

                    mLoginView.showLoginSuccess();

                    if (result.getTypeId().equals(IDENTITY_USER) &&
                            sharedPref.getString(MainActivity.DEVICE_MID, "").equals("")) {
                        // logged user's role is patient
                        // AND
                        // there is no bluetooth mac address in shardPref
                        mLoginView.gotoFindBluetooth();
                    } else {
                        mLoginView.gotoHome();
                    }
                } else {
                    mLoginView.showLoginError(null);
                }
            }
        });
    }
}
