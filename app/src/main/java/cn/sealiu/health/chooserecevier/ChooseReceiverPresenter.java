package cn.sealiu.health.chooserecevier;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.bean.Responsible;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.data.response.ResponsibleResponse;
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
 * on 2017/10/9.
 */

public class ChooseReceiverPresenter implements ChooseReceiverContract.Presenter {
    private static final String TAG = "ChooseReceiverPresenter";

    private final ChooseReceiverContract.View mChooseReceiverView;

    public ChooseReceiverPresenter(ChooseReceiverContract.View view) {
        mChooseReceiverView = checkNotNull(view);
        mChooseReceiverView.setPresenter(this);
    }

    @Override
    public void loadUsers() {
        mChooseReceiverView.setLoadingIndicator(true);

        String doctorId = sharedPref.getString(MainActivity.USER_ID, "");
        if (doctorId.equals("")) {
            mChooseReceiverView.gotoLogin();
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request getBindUserRequest = BaseActivity.buildHttpGetRequest("/res/getBoundUser?" +
                "docId=" + doctorId);

        if (getBindUserRequest == null) return;

        okHttpClient.newCall(getBindUserRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mChooseReceiverView.showInfo("get bound user interface error");
                mChooseReceiverView.setLoadingIndicator(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                ResponsibleResponse resResponse = new Gson().fromJson(json, ResponsibleResponse.class);

                if (resResponse.getBondUser().length == 0) {
                    if (!mChooseReceiverView.isActive()) {
                        return;
                    }
                    mChooseReceiverView.showNoUsers();
                    return;
                }

                List<User> users = new ArrayList<>();

                for (Responsible bean : resResponse.getBondUser()) {
                    users.add(bean.getUser()[0]);
                }

                if (!mChooseReceiverView.isActive()) {
                    return;
                }
                mChooseReceiverView.setLoadingIndicator(false);
                mChooseReceiverView.showUsers(users);
            }
        });

        mChooseReceiverView.setLoadingIndicator(false);
    }

    @Override
    public void start() {
        loadUsers();
    }
}
