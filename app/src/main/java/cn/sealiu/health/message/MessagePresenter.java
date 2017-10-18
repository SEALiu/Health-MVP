package cn.sealiu.health.message;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.bean.Message;
import cn.sealiu.health.data.bean.Responsible;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.data.response.MessageResponse;
import cn.sealiu.health.data.response.MiniResponse;
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

    @Override
    public void doSentMsg(String[] ids, String content) {
        final String userId = sharedPref.getString(MainActivity.USER_ID, "");
        if (userId.equals("")) {
            mMessageView.gotoLogin();
            return;
        }

        Map<String, Object> groupMessage = new HashMap<>();
        List<Msg> msgs = new ArrayList<>();
        for (String id : ids) {
            Msg msg = new Msg(userId, id, content);
            msgs.add(msg);
        }

        groupMessage.put("GroupMessage", msgs);
        String messageJson = new Gson().toJson(groupMessage);

        if (D) Log.e(TAG, messageJson);

        Request sendMsgsRequest = BaseActivity.buildHttpGetRequest("/message/sendGroupMessage?" +
                "groupMessage=" + URLEncoder.encode(messageJson));

        if (sendMsgsRequest == null) return;

        new OkHttpClient().newCall(sendMsgsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mMessageView.showInfo("send group message interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                MiniResponse miniResponse = new Gson().fromJson(json, MiniResponse.class);

                if (miniResponse.getStatus().equals("200")) {
                    mMessageView.showInfo("信息已发送");
                    loadMessages();
                } else {
                    mMessageView.showInfo("信息发送失败");
                }
            }
        });
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

        if (getBindDoctorRequest == null) return;

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

                    if (getMsgListRequest == null) return;

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

        if (getBindUserRequest == null) return;

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

                    if (getMsgListRequest == null) return;

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

    private class Msg {
        private String fromId;
        private String toId;
        private String content;

        public Msg(String fromId, String toId, String content) {
            this.fromId = fromId;
            this.toId = toId;
            this.content = content;
        }

        public String getFromId() {
            return fromId;
        }

        public void setFromId(String fromId) {
            this.fromId = fromId;
        }

        public String getToId() {
            return toId;
        }

        public void setToId(String toId) {
            this.toId = toId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
