package cn.sealiu.health.userdetail;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.data.response.MiniResponse;
import cn.sealiu.health.data.response.ProfileResponse;
import cn.sealiu.health.main.MainActivity;
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
 * on 2017/9/30.
 */

public class UserDetailPresenter implements UserDetailContract.Presenter {
    private static final String TAG = "UserDetailPresenter";

    private UserDetailContract.View mUserDetailView;
    private String mUserId;

    public UserDetailPresenter(@NonNull UserDetailContract.View view, @NonNull String userId) {
        mUserDetailView = checkNotNull(view);
        mUserId = checkNotNull(userId);
        mUserDetailView.setPresenter(this);
    }

    @Override
    public void loadUserDetail(final String userId) {

        final OkHttpClient okHttpClient = new OkHttpClient();
        final Request getProfileRequest = BaseActivity.buildHttpGetRequest("/user/getProfile?" +
                "id=" + userId);

        okHttpClient.newCall(getProfileRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getMessage());
                mUserDetailView.showInfo("get profile interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();
                if (D) Log.d(TAG, json);

                ProfileResponse result = new Gson().fromJson(json, ProfileResponse.class);

                if (result.getStatus().equals("200")) {
                    String username = result.getUserName();
                    int gender = Integer.parseInt(result.getUserGender());
                    String age = result.getUserAge();
                    String phone = result.getUserPhone();
                    String email = result.getUserEmail();
                    String mid = result.getUserMid();

                    User user = new User(null, username, gender, age, null, userId,
                            true, phone, email, null, mid);

                    mUserDetailView.showUserDetail(user);
                } else {
                    mUserDetailView.showInfo(R.string.load_userinfo_failed);
                }
            }
        });
    }

    @Override
    public void doSentMsg(String toId, String content) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String fromId = sharedPref.getString(MainActivity.USER_ID, "");
        if (fromId.equals("")) {
            mUserDetailView.gotoLogin();
        }

        /*
        http://localhost:8080/message/sendMessage?fromId=3&toId=2&content=Test4
         */
        Request getMsgListRequest =
                BaseActivity.buildHttpGetRequest("/message/sendMessage?" +
                        "fromId=" + fromId + "&" +
                        "toId=" + toId + "&" +
                        "content=" + content);
        if (D) Log.e(TAG, getMsgListRequest.url().toString());

        okHttpClient.newCall(getMsgListRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserDetailView.showInfo("send message interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultJson = response.body().string();
                if (D) Log.d(TAG, resultJson);

                MiniResponse mini = new Gson().fromJson(resultJson, MiniResponse.class);
                if (mini.getStatus().equals("200")) {
                    mUserDetailView.showInfo("信息已发送");
                } else {
                    mUserDetailView.showInfo("信息发送失败");
                }
            }
        });
    }

    @Override
    public void start() {
        loadUserDetail(mUserId);
    }
}
