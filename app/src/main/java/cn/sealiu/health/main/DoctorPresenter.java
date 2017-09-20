package cn.sealiu.health.main;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.bean.ResponsibleBean;
import cn.sealiu.health.data.bean.ResponsibleResponse;
import cn.sealiu.health.data.bean.User;
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
 * on 2017/9/16.
 */

public class DoctorPresenter implements DoctorContract.Presenter {
    private static final String TAG = "DoctorPresenter";

    private final DoctorContract.View mDoctorView;

    public DoctorPresenter(@NonNull DoctorContract.View view) {
        mDoctorView = checkNotNull(view);
        mDoctorView.setPresenter(this);
    }

    @Override
    public void loadUsers() {
        mDoctorView.setLoadingIndicator(true);

        String doctorId = sharedPref.getString(MainActivity.USER_ID, "");
        if (doctorId.equals("")) {
            mDoctorView.gotoLogin();
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request getBindUserRequest = BaseActivity.buildHttpGetRequest("/res/getBoundUser?" +
                "docId=" + doctorId);

        okHttpClient.newCall(getBindUserRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mDoctorView.showInterfaceError();
                mDoctorView.setLoadingIndicator(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                ResponsibleResponse resResponse = new Gson().fromJson(json, ResponsibleResponse.class);

                if (resResponse.getBondUser().length == 0) {
                    if (!mDoctorView.isActive()){
                        return;
                    }
                    mDoctorView.showNoUsers();
                    return;
                }

                List<User> users = new ArrayList<>();

                for (ResponsibleBean bean : resResponse.getBondUser()) {
                    users.add(bean.getUser()[0]);
                }

                if (!mDoctorView.isActive()) {
                    return;
                }
                mDoctorView.setLoadingIndicator(false);
                mDoctorView.showUsers(users);
            }
        });
    }

    @Override
    public void start() {
        loadUsers();
    }
}
