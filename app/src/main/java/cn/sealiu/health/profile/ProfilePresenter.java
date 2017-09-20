package cn.sealiu.health.profile;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.BaseResponse;
import cn.sealiu.health.data.bean.ProfileResponse;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.Fun;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class ProfilePresenter implements ProfileContract.Presenter {
    private static final String TAG = "ProfilePresenter";

    private final ProfileContract.View mProfileView;

    @Override
    public void start() {
        loadUserInfo();
    }

    public ProfilePresenter(@NonNull ProfileContract.View profileView) {
        mProfileView = checkNotNull(profileView);
        mProfileView.setPresenter(this);
    }

    @Override
    public void loadUserInfo() {
        mProfileView.setLoadingIndicator(true);
        final String uid = sharedPref.getString(MainActivity.USER_ID, "");

        final OkHttpClient okHttpClient = new OkHttpClient();
        final Request getProfileRequest = BaseActivity.buildHttpGetRequest("/user/getProfile?" +
                "id=" + uid);

        okHttpClient.newCall(getProfileRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getMessage());
                mProfileView.showInfo("login interface error");
                mProfileView.setLoadingIndicator(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();
                if (D) Log.d(TAG, json);

                ProfileResponse result = new Gson().fromJson(json, ProfileResponse.class);

                if (result.getStatus().equals("200")) {
                    String username = result.getUserName();
                    int gender = Integer.parseInt(result.getUserGender());
                    int age = result.getAge();
                    String phone = result.getUserPhone();
                    String email = result.getUserEmail();
                    String mid = result.getMid();

                    // 给用户资料做一个本地存储，避免每次都从服务器请求数据
                    BaseActivity.sharedPref.edit().putBoolean(ProfileActivity.PROFILE_SAVED, true).apply();
                    BaseActivity.sharedPref.edit().putString(ProfileActivity.PROFILE_USERNAME, username).apply();
                    BaseActivity.sharedPref.edit().putInt(ProfileActivity.PROFILE_GENDER, gender).apply();
                    BaseActivity.sharedPref.edit().putInt(ProfileActivity.PROFILE_AGE, age).apply();
                    BaseActivity.sharedPref.edit().putString(ProfileActivity.PROFILE_PHONE, phone).apply();
                    BaseActivity.sharedPref.edit().putString(ProfileActivity.PROFILE_EMAIL, email).apply();
                    BaseActivity.sharedPref.edit().putString(ProfileActivity.PROFILE_MID, mid).apply();

                    User user = new User(null, username, gender, age, null, uid,
                            true, phone, email, null, mid);

                    mProfileView.updateUserInfo(user);
                } else {
                    mProfileView.showInfo(R.string.load_userinfo_failed);
                }

                mProfileView.setLoadingIndicator(false);
            }
        });
    }

    @Override
    public void changeUsername(String name) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request changeNameRequest = BaseActivity.buildHttpGetRequest(
                "/user/changeProfileName?" +
                        "userId=" + userId +
                        "&userName=" + name
        );

        okHttpClient.newCall(changeNameRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mProfileView.showInfo("change username interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onInterfaceSuccess(response);
            }
        });
    }

    @Override
    public void changeGender(int gender) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request changeGenderRequest = BaseActivity.buildHttpGetRequest(
                "/user/changeProfileGender?" +
                        "userId=" + userId +
                        "&userGender=" + gender
        );

        okHttpClient.newCall(changeGenderRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mProfileView.showInfo("change gender interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onInterfaceSuccess(response);
            }
        });
    }

    @Override
    public void changeAge(int age) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request changeAgeRequest = BaseActivity.buildHttpGetRequest(
                "/user/changeProfileAge?" +
                        "userId=" + userId +
                        "&Age=" + age
        );

        okHttpClient.newCall(changeAgeRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mProfileView.showInfo("change age interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mProfileView.showInfo("unfinished interface");
                //onInterfaceSuccess(response);
            }
        });
    }

    @Override
    public void changePhone(String phone) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request changePhoneRequest = BaseActivity.buildHttpGetRequest(
                "/user/changeProfilePhone?" +
                        "userId=" + userId +
                        "&userPhone=" + phone
        );

        okHttpClient.newCall(changePhoneRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mProfileView.showInfo("change phone interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onInterfaceSuccess(response);
            }
        });
    }

    @Override
    public void changeEmail(String email) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request changeEmailRequest = BaseActivity.buildHttpGetRequest(
                "/user/changeProfileEmail?" +
                        "userId=" + userId +
                        "&userEmail=" + email
        );

        okHttpClient.newCall(changeEmailRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mProfileView.showInfo("change email interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onInterfaceSuccess(response);
            }
        });
    }

    @Override
    public void changePassword(String oldPwd, String newPwd) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request changePasswordRequest = BaseActivity.buildHttpGetRequest(
                "/user/changeProfilePassword?" +
                        "userId=" + userId +
                        "&oldPassWord=" + Fun.encode("MD5", oldPwd) +
                        "&newPassWord=" + Fun.encode("MD5", newPwd)
        );

        okHttpClient.newCall(changePasswordRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mProfileView.showInfo("change password interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onInterfaceSuccess(response);
            }
        });
    }

    private void onInterfaceSuccess(Response response) throws IOException {
        String resultJson = response.body().string();
        if (D) Log.d(TAG, resultJson);
        BaseResponse result = new Gson().fromJson(resultJson, BaseResponse.class);

        if (result.getStatus().equals("200")) {
            loadUserInfo();
            mProfileView.showInfo(R.string.profile_changed);
        } else {
            mProfileView.showInfo(R.string.profile_unchanged);
        }
    }
}
