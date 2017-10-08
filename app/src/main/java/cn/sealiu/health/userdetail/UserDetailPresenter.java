package cn.sealiu.health.userdetail;

import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.DataBean;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.data.response.HistoryDataResponse;
import cn.sealiu.health.data.response.MiniResponse;
import cn.sealiu.health.data.response.ProfileResponse;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.statistic.StatisticFragment;
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
    private DateFormat yMd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private DateFormat yMdHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private String userMid;

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
                    userMid = mid;

                    User user = new User(null, username, gender, age, null, userId,
                            true, phone, email, null, mid);

                    mUserDetailView.showUserDetail(user);

                    // 获取到userMid，立即获取该用户昨日统计数据
                    loadDayStatistic(null);
                } else {
                    mUserDetailView.showInfo(R.string.load_userinfo_failed);
                }
            }
        });
    }

    @Override
    public void start() {
        loadUserDetail(mUserId);
    }

    @Override
    public void doSentMsg(String toId, String content) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String fromId = sharedPref.getString(MainActivity.USER_ID, "");
        if (fromId.equals("")) {
            mUserDetailView.gotoLogin();
        }

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
    public void loadDayStatistic(Calendar day) {
        // 采集率为每30秒一次，故一小时的数据总量为：1 * 60 * 2 = 120
        final Float dataNumOneHour = 1 * 60 * 2f;

        // 佩戴时间
        final int[] wearTimes = new int[24];
        for (int i = 0; i < 24; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        final int[] channelA = new int[4];
        final int[] channelB = new int[4];
        final int[] channelC = new int[4];
        final int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        if (day == null) {
            day = Calendar.getInstance();
            day.add(Calendar.DATE, -1);
        }

        String start = yMd.format(day.getTime()) + " 00:00:00.0";
        String end = yMd.format(day.getTime()) + " 23:59:59.0";

        final OkHttpClient okHttpClient = new OkHttpClient();
        Request request = BaseActivity.buildHttpGetRequest("/data/queryData?" +
                "mid=" + userMid + "&" +
                "startTime=" + start + "&" +
                "endTime=" + end);

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserDetailView.showInfo("query data interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                if (D) Log.d(TAG, "history data: " + json);

                HistoryDataResponse historyDataResponse = new Gson().fromJson(json,
                        HistoryDataResponse.class);

                DataBean[] dataBeans = historyDataResponse.getHistoryData();
                for (DataBean data : dataBeans) {
                    String aa = data.getAa();
                    String bb = data.getBb();
                    String cc = data.getCc();
                    String dd = data.getDd();
                    String time = data.getTime();

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;

                    try {
                        Date date = yMdHms.parse(time);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        wearTimes[calendar.get(Calendar.HOUR_OF_DAY)] += 1;

                    } catch (ParseException e) {
                        if (D) Log.e(TAG, e.getLocalizedMessage());
                    }
                }

                boolean visible = historyDataResponse.getCount() != 0;

                ArrayList<BarEntry> yVals = new ArrayList<>();
                for (int i = 0; i < 24; i++)
                    yVals.add(new BarEntry(i, (wearTimes[i] / dataNumOneHour) * 60));
                mUserDetailView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_DAY);

                updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneHour);
            }
        });
    }

    @Override
    public void loadWeekStatistic(Calendar day) {
        // 采集率为每30秒一次，故一天的数据总量为：24 * 60 * 2 = 2880
        final Float dataNumOneDay = 24 * 60 * 2f;

        // 佩戴时间(7天)
        final int[] wearTimes = new int[7];
        for (int i = 0; i < 7; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        final int[] channelA = new int[4];
        final int[] channelB = new int[4];
        final int[] channelC = new int[4];
        final int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        if (day == null) {
            day = Calendar.getInstance();
        }

        // 获取近7天的数据(不包括今天)
        final String[] targetDates = new String[7];
        String start = "", end = "";

        for (int i = 0; i < 7; i++) {
            day.add(Calendar.DATE, -1);
            targetDates[i] = yMd.format(day.getTime());

            if (i == 0) end = yMd.format(day.getTime()) + " 23:59:59.0";
            if (i == 6) start = yMd.format(day.getTime()) + " 00:00:00.0";
        }

        final OkHttpClient okHttpClient = new OkHttpClient();
        Request request = BaseActivity.buildHttpGetRequest("/data/queryData?" +
                "mid=" + userMid + "&" +
                "startTime=" + start + "&" +
                "endTime=" + end);

        if (D) Log.d(TAG, "request url: " + request.url());

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserDetailView.showInfo("query data interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                if (D) Log.d(TAG, "history data: " + json);

                HistoryDataResponse historyDataResponse = new Gson().fromJson(json,
                        HistoryDataResponse.class);

                DataBean[] dataBeans = historyDataResponse.getHistoryData();
                for (DataBean data : dataBeans) {
                    String aa = data.getAa();
                    String bb = data.getBb();
                    String cc = data.getCc();
                    String dd = data.getDd();
                    String time = data.getTime();

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;

                    for (int i = 0; i < 7; i++) {
                        if (time.substring(0, 10).equals(targetDates[i])) {
                            wearTimes[i]++;
                            break;
                        }
                    }
                }

                boolean visible = historyDataResponse.getCount() != 0;

                ArrayList<BarEntry> yVals = new ArrayList<>();
                for (int i = 0; i < 7; i++)
                    yVals.add(new BarEntry(i, (wearTimes[i] / dataNumOneDay) * 24));

                mUserDetailView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_WEEK);

                updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneDay);
            }
        });
    }

    @Override
    public void loadMonthStatistic(String MM) {
        // 采集率为每30秒一次，故一天的数据总量为：24 * 60 * 2 = 2880
        final Float dataNumOneDay = 24 * 60 * 2f;

        // 佩戴时间
        final int[] wearTimes = new int[31];
        for (int i = 0; i < 31; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        final int[] channelA = new int[4];
        final int[] channelB = new int[4];
        final int[] channelC = new int[4];
        final int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        DateFormat yM = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        DateFormat y = new SimpleDateFormat("yyyy", Locale.getDefault());
        DateFormat d = new SimpleDateFormat("dd", Locale.getDefault());

        String yMStr;
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DATE, -1);
        if (MM == null) {
            yMStr = yM.format(day.getTime());
        } else {
            yMStr = y.format(day.getTime()) + "-" + MM;
        }

        // 获取MM月的最后一天的日期
        Calendar temp = Calendar.getInstance();
        temp.add(Calendar.DATE, -1);
        // 设置 MM + 1月 的第一天
        temp.set(Calendar.MONTH, Integer.valueOf(yMStr.substring(5, 7)) + 1);
        temp.set(Calendar.DATE, 1);
        // 往前减一天，就是 MM 月的最后一天的日期
        String lastDayOfMonth = d.format(temp.getTime());

        String start = yMStr + "-01 00:00:00.0";
        String end = yMd.format(day.getTime()) + "-" + lastDayOfMonth + " 23:59:59.0";

        final OkHttpClient okHttpClient = new OkHttpClient();
        Request request = BaseActivity.buildHttpGetRequest("/data/queryData?" +
                "mid=" + userMid + "&" +
                "startTime=" + start + "&" +
                "endTime=" + end);

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserDetailView.showInfo("query data interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                if (D) Log.d(TAG, "history data: " + json);

                HistoryDataResponse historyDataResponse = new Gson().fromJson(json,
                        HistoryDataResponse.class);

                DataBean[] dataBeans = historyDataResponse.getHistoryData();
                for (DataBean data : dataBeans) {
                    String aa = data.getAa();
                    String bb = data.getBb();
                    String cc = data.getCc();
                    String dd = data.getDd();
                    String time = data.getTime();

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;

                    try {
                        Date date = yMdHms.parse(time);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        wearTimes[calendar.get(Calendar.DATE) - 1]++;

                    } catch (ParseException e) {
                        if (D) Log.e(TAG, e.getLocalizedMessage());
                    }
                }

                boolean visible = historyDataResponse.getCount() != 0;

                ArrayList<BarEntry> yVals = new ArrayList<>();
                for (int i = 0; i < 31; i++)
                    yVals.add(new BarEntry(i, (wearTimes[i] / dataNumOneDay) * 24));

                mUserDetailView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_MONTH);
                updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneDay);
            }
        });
    }

    @Override
    public void loadYearStatistic(String yyyy) {
        //如果 yyyy 为空，则默认为 "今年"
        DateFormat y = new SimpleDateFormat("yyyy", Locale.getDefault());
        if (yyyy == null || yyyy.equals("")) {
            yyyy = y.format(new Date());
        }

        boolean visible = false;
        // 采集率为每30秒一次，故一天的数据总量为：24 * 60 * 2 = 2880
        final Float dataNumOneDay = 24 * 60 * 2f;

        // 佩戴时间(12月)
        final int[] wearTimes = new int[12];
        for (int i = 0; i < 12; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        final int[] channelA = new int[4];
        final int[] channelB = new int[4];
        final int[] channelC = new int[4];
        final int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        String start = yyyy + "-01-01 00:00:00.0";
        String end = yyyy + "-12-31 23:59:59.0";

        final OkHttpClient okHttpClient = new OkHttpClient();
        Request request = BaseActivity.buildHttpGetRequest("/data/queryData?" +
                "mid=" + userMid + "&" +
                "startTime=" + start + "&" +
                "endTime=" + end);

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mUserDetailView.showInfo("query data interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                if (D) Log.d(TAG, "history data: " + json);

                HistoryDataResponse historyDataResponse = new Gson().fromJson(json,
                        HistoryDataResponse.class);

                DataBean[] dataBeans = historyDataResponse.getHistoryData();
                for (DataBean data : dataBeans) {
                    String aa = data.getAa();
                    String bb = data.getBb();
                    String cc = data.getCc();
                    String dd = data.getDd();
                    String time = data.getTime();

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;

                    try {
                        Date date = yMdHms.parse(time);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        wearTimes[calendar.get(Calendar.MONTH)]++;

                    } catch (ParseException e) {
                        if (D) Log.e(TAG, e.getLocalizedMessage());
                    }
                }

                boolean visible = historyDataResponse.getCount() != 0;

                ArrayList<BarEntry> yVals = new ArrayList<>();
                for (int i = 0; i < 12; i++) {
                    yVals.add(new BarEntry(i, wearTimes[i] / dataNumOneDay));
                }

                mUserDetailView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_YEAR);
                updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneDay);
            }
        });

    }

    private void updateComfortStatistic(int[] channelA, int[] channelB, int[] channelC, int[] channelD, float criterion) {
        ArrayList<BarEntry> yValsA = new ArrayList<>();
        ArrayList<BarEntry> yValsB = new ArrayList<>();
        ArrayList<BarEntry> yValsC = new ArrayList<>();
        ArrayList<BarEntry> yValsD = new ArrayList<>();

        boolean visibleA = false, visibleB = false, visibleC = false, visibleD = false;
        for (int j = 0; j < 4; j++) {
            if (channelA[j] != 0 && !visibleA) visibleA = true;
            if (channelB[j] != 0 && !visibleB) visibleB = true;
            if (channelC[j] != 0 && !visibleC) visibleC = true;
            if (channelD[j] != 0 && !visibleD) visibleD = true;
            yValsA.add(new BarEntry(j, channelA[j] / criterion));
            yValsB.add(new BarEntry(j, channelB[j] / criterion));
            yValsC.add(new BarEntry(j, channelC[j] / criterion));
            yValsD.add(new BarEntry(j, channelD[j] / criterion));
        }

        // 一天舒适度的统计，单位应该是小时，所以用 "TYPE_WEEK"
        mUserDetailView.updateComfortStatistic(yValsA, visibleA, 0);
        mUserDetailView.updateComfortStatistic(yValsB, visibleB, 1);
        mUserDetailView.updateComfortStatistic(yValsC, visibleC, 2);
        mUserDetailView.updateComfortStatistic(yValsD, visibleD, 3);
    }
}
