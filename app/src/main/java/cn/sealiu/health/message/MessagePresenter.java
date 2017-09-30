package cn.sealiu.health.message;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.bean.Message;
import cn.sealiu.health.data.bean.Responsible;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.data.response.MessageResponse;
import cn.sealiu.health.data.response.ResponsibleResponse;
import cn.sealiu.health.main.MainActivity;
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
 * on 2017/9/19.
 */

public class MessagePresenter implements MessageContract.Presenter {
    private static final String TAG = "MessagePresenter";
    private final MessageContract.View mMessageView;

    private String bindDoctorId = null;

    @Override
    public void start() {
        loadMessages();
    }

    public MessagePresenter(@NonNull MessageContract.View messageView) {
        mMessageView = checkNotNull(messageView);
        mMessageView.setPresenter(this);
    }

    @Override
    public void loadMessages() {
        mMessageView.setLoadingIndicator(true);

        switch (sharedPref.getString(MainActivity.USER_TYPE, "-1")) {
            case IDENTITY_USER:
                userGetMsgList();
                break;
            case IDENTITY_DOCTOR:
                doctorGetMsgList();
                break;
            default:
                mMessageView.gotoLogin();
                break;
        }
        mMessageView.setLoadingIndicator(false);
    }

    private void userGetMsgList() {
        final String userId = sharedPref.getString(MainActivity.USER_ID, "");
        if (userId.equals("")) {
            mMessageView.gotoLogin();
            return;
        }

        final OkHttpClient okHttpClient = new OkHttpClient();
        Request getBindDoctorRequest = BaseActivity.buildHttpGetRequest("/res/getBoundDoctor?" +
                "patId=" + userId);

        okHttpClient.newCall(getBindDoctorRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mMessageView.showInfo("get bound doctor interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                BindDoctorResponse bindDoctorResponse = new Gson().fromJson(
                        json, BindDoctorResponse.class);

                bindDoctorId = bindDoctorResponse.getDoctorId();

                if (bindDoctorId == null) {
                    mMessageView.showNoMessage();
                } else {
                    final List<Message> messages = new ArrayList<>();

                    Request getMsgListRequest =
                            BaseActivity.buildHttpGetRequest("/message/getMessageList?" +
                                    "PatId=" + userId + "&DocId=" + bindDoctorId);

                    okHttpClient.newCall(getMsgListRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (D) Log.e(TAG, e.getLocalizedMessage());
                            mMessageView.showInfo("get message list interface error");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String resultJson = response.body().string();

                            if (D) Log.e(TAG, resultJson);
                            MessageResponse messageResponse = new Gson().fromJson(resultJson, MessageResponse.class);

                            for (Message msg : messageResponse.getPatientList()) {
                                msg.setFromWho("医生");
                                messages.add(msg);
                            }

                            if (messages.size() == 0) {
                                mMessageView.showNoMessage();
                            } else {
                                mMessageView.showMessages(messages);
                            }
                        }
                    });

                }
            }
        });

    }

    private void doctorGetMsgList() {

        final String doctorId = sharedPref.getString(MainActivity.USER_ID, "");
        if (doctorId.equals("")) {
            mMessageView.gotoLogin();
            return;
        }

        final OkHttpClient okHttpClient = new OkHttpClient();
        Request getBindUserRequest = BaseActivity.buildHttpGetRequest("/res/getBoundUser?" +
                "docId=" + doctorId);

        okHttpClient.newCall(getBindUserRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mMessageView.showInfo("get bound user interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.d(TAG, json);

                ResponsibleResponse resResponse = new Gson().fromJson(json, ResponsibleResponse.class);

                if (resResponse.getBondUser().length == 0) {
                    mMessageView.showNoMessage();
                    return;
                }

                List<User> users = new ArrayList<>();

                for (Responsible bean : resResponse.getBondUser()) {
                    users.add(bean.getUser()[0]);
                }

                final List<Message> messages = new ArrayList<>();

                for (final User user : users) {
                    Request getMsgListRequest =
                            BaseActivity.buildHttpGetRequest("/message/getMessageList?" +
                                    "PatId=" + user.getId() + "&DocId=" + doctorId);

                    okHttpClient.newCall(getMsgListRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (D) Log.e(TAG, e.getLocalizedMessage());
                            mMessageView.showInfo("get message list interface error");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String resultJson = response.body().string();

                            if (D) Log.e(TAG, resultJson);
                            MessageResponse messageResponse = new Gson().fromJson(resultJson, MessageResponse.class);

                            for (Message msg : messageResponse.getPatientList()) {
                                msg.setFromWho(user.getUsername());
                                messages.add(msg);
                            }

                            if (messages.size() == 0) {
                                mMessageView.showNoMessage();
                            } else {
                                mMessageView.showMessages(messages);
                            }
                        }
                    });
                }
            }
        });
    }

    public class BindDoctorResponse {
        String DoctorId;

        public String getDoctorId() {
            return DoctorId;
        }

        public void setDoctorId(String doctorId) {
            DoctorId = doctorId;
        }
    }
}
