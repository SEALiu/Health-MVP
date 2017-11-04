package cn.sealiu.health.statistic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.sealiu.health.data.local.HealthDbHelper;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.Fun;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.data.local.DataPersistenceContract.DataEntry;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public class StatisticPresenter implements StatisticContract.Presenter {
    private final static String TAG = "StatisticPresenter";
    private final StatisticContract.View mStatisticView;
    private HealthDbHelper dbHelper;
    private DateFormat yMd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private DateFormat yMdHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private String mid = sharedPref.getString(MainActivity.DEVICE_COMPLETED_MID, "");


    @Override
    public void start() {
        loadDayStatistic(null);
    }

    public StatisticPresenter(@NonNull StatisticContract.View view, HealthDbHelper helper) {
        mStatisticView = checkNotNull(view);
        mStatisticView.setPresenter(this);
        dbHelper = helper;
    }

    @Override
    public void loadDayStatistic(@Nullable Calendar day) {
        boolean visible = false;
        // 采集率为每30秒一次，故一小时的数据总量为：1 * 60 * 2 = 120
        Float dataNumOneHour = 1 * 60 * 2f;

        // 佩戴时间
        int[] wearTimes = new int[24];
        for (int i = 0; i < 24; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        int[] channelA = new int[4];
        int[] channelB = new int[4];
        int[] channelC = new int[4];
        int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        if (day == null) {
            // TODO: 2017/10/6 修改为从 datastatus.tb 中查询，找到有数据的最近一天
            day = Calendar.getInstance();
            day.add(Calendar.DATE, -1);
        }

        String dateStr = yMd.format(day.getTime());

        String sql = "SELECT " + DataEntry.COLUMN_NAME_AA + ", " +
                DataEntry.COLUMN_NAME_BB + ", " +
                DataEntry.COLUMN_NAME_CC + ", " +
                DataEntry.COLUMN_NAME_DD + ", " +
                DataEntry.COLUMN_NAME_TIME + " FROM " + DataEntry.TABLE_NAME +
                " WHERE " + DataEntry.COLUMN_NAME_MID + " = '" + mid +
                "' AND " + DataEntry.COLUMN_NAME_TIME + " LIKE '" + dateStr + "%'";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        if (c.moveToFirst()) {
            visible = true;
            do {
                String aa = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_AA)));
                String bb = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_BB)));
                String cc = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_CC)));
                String dd = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_DD)));
                String time = c.getString(c.getColumnIndex(DataEntry.COLUMN_NAME_TIME));

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
            } while (c.moveToNext());
        }

        c.close();

        ArrayList<BarEntry> yVals = new ArrayList<>();
        for (int i = 0; i < 24; i++)
            yVals.add(new BarEntry(i, (wearTimes[i] / dataNumOneHour) * 60));
        mStatisticView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_DAY);

        updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneHour);
    }

    @Override
    public void loadWeekStatistic(@Nullable Calendar day) {
        boolean visible = false;
        // 采集率为每30秒一次，故一天的数据总量为：24 * 60 * 2 = 2880
        Float dataNumOneDay = 24 * 60 * 2f;

        // 佩戴时间(7天)
        int[] wearTimes = new int[7];
        for (int i = 0; i < 7; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        int[] channelA = new int[4];
        int[] channelB = new int[4];
        int[] channelC = new int[4];
        int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        DateFormat yMd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (day == null) {
            day = Calendar.getInstance();
        }

        // 获取近7天的数据(不包括今天)
        for (int i = 0; i < 7; i++) {
            day.add(Calendar.DATE, -1);
            String dayStr = yMd.format(day.getTime());

            String sql = "SELECT " + DataEntry.COLUMN_NAME_AA + ", " +
                    DataEntry.COLUMN_NAME_BB + ", " +
                    DataEntry.COLUMN_NAME_CC + ", " +
                    DataEntry.COLUMN_NAME_DD + " FROM " + DataEntry.TABLE_NAME +
                    " WHERE " + DataEntry.COLUMN_NAME_MID + " = '" + mid +
                    "' AND " + DataEntry.COLUMN_NAME_TIME + " LIKE '" + dayStr + "%'";

            Cursor c = db.rawQuery(sql, null);

            if (c.moveToFirst()) {
                visible = true;
                do {
                    String aa = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_AA)));
                    String bb = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_BB)));
                    String cc = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_CC)));
                    String dd = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_DD)));

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;
                    wearTimes[i]++;

                } while (c.moveToNext());
            }
            c.close();
        }

        ArrayList<BarEntry> yVals = new ArrayList<>();
        for (int i = 0; i < 7; i++)
            yVals.add(new BarEntry(i, (wearTimes[i] / dataNumOneDay) * 24));

        mStatisticView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_WEEK);

        updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneDay);
    }

    @Override
    public void loadMonthStatistic(String MM) {
        boolean visible = false;
        // 采集率为每30秒一次，故一天的数据总量为：24 * 60 * 2 = 2880
        Float dataNumOneDay = 24 * 60 * 2f;

        // 佩戴时间(31天)
        int[] wearTimes = new int[31];
        for (int i = 0; i < 31; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        int[] channelA = new int[4];
        int[] channelB = new int[4];
        int[] channelC = new int[4];
        int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        DateFormat M = new SimpleDateFormat("MM", Locale.getDefault());
        if (MM == null || MM.equals("")) {
            MM = M.format(new Date());
        }

        String yyyyMM = Calendar.getInstance().get(Calendar.YEAR) + "-" + Fun.leftPad(MM, 2);

        for (int i = 0; i < 31; i++) {

            String dayStr = yyyyMM + "-" + Fun.leftPad(i + 1 + "", 2);

            String sql = "SELECT " + DataEntry.COLUMN_NAME_AA + ", " +
                    DataEntry.COLUMN_NAME_BB + ", " +
                    DataEntry.COLUMN_NAME_CC + ", " +
                    DataEntry.COLUMN_NAME_DD + " FROM " + DataEntry.TABLE_NAME +
                    " WHERE " + DataEntry.COLUMN_NAME_MID + " = '" + mid +
                    "' AND " + DataEntry.COLUMN_NAME_TIME + " LIKE '%" + dayStr + "%'";

            Cursor c = db.rawQuery(sql, null);

            if (D) Log.e(TAG, "sql : " + sql);
            if (c.moveToFirst()) {
                visible = true;
                do {
                    String aa = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_AA)));
                    String bb = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_BB)));
                    String cc = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_CC)));
                    String dd = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_DD)));

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;
                } while (c.moveToNext());

                wearTimes[i] = c.getCount();
            }

            c.close();
        }

        ArrayList<BarEntry> yVals = new ArrayList<>();
        for (int i = 0; i < 31; i++)
            yVals.add(new BarEntry(i, (wearTimes[i] / dataNumOneDay) * 24));

        mStatisticView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_MONTH);
        updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneDay);
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
        Float dataNumOneDay = 24 * 60 * 2f;

        /*
        // 采集率为每30秒一次，故一个月的数据总量为：天数 * 24 * 60 * 2
        Float[] dataNumOneMonth = new Float[12];
        for (int i = 0; i < 12; i++) {
            Calendar mycal = new GregorianCalendar(Integer.valueOf(yyyy), i, 1);
            int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
            dataNumOneMonth[i] = daysInMonth * 24 * 60 * 2f;
        }
        */

        // 佩戴时间(12月)
        int[] wearTimes = new int[12];
        for (int i = 0; i < 12; i++) wearTimes[i] = 0;

        // 通道舒适度
        // 舒适度0:空载 1:松 2:合适 3:紧
        int[] channelA = new int[4];
        int[] channelB = new int[4];
        int[] channelC = new int[4];
        int[] channelD = new int[4];
        for (int i = 0; i < 4; i++) {
            channelA[i] = 0;
            channelB[i] = 0;
            channelC[i] = 0;
            channelD[i] = 0;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for (int i = 0; i < 12; i++) {

            String monthStr = yyyy + "-" + Fun.leftPad(i + 1 + "", 2);

            String sql = "SELECT " + DataEntry.COLUMN_NAME_AA + ", " +
                    DataEntry.COLUMN_NAME_BB + ", " +
                    DataEntry.COLUMN_NAME_CC + ", " +
                    DataEntry.COLUMN_NAME_DD + " FROM " + DataEntry.TABLE_NAME +
                    " WHERE " + DataEntry.COLUMN_NAME_MID + " = '" + mid +
                    "' AND " + DataEntry.COLUMN_NAME_TIME + " LIKE '" + monthStr + "%'";

            Cursor c = db.rawQuery(sql, null);

            if (D) Log.e(TAG, "sql : " + sql);
            if (c.moveToFirst()) {
                visible = true;
                do {
                    String aa = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_AA)));
                    String bb = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_BB)));
                    String cc = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_CC)));
                    String dd = String.valueOf(c.getDouble(c.getColumnIndex(DataEntry.COLUMN_NAME_DD)));

                    int ai = Integer.valueOf(aa.substring(0, 1)) - 1;
                    int bi = Integer.valueOf(bb.substring(0, 1)) - 1;
                    int ci = Integer.valueOf(cc.substring(0, 1)) - 1;
                    int di = Integer.valueOf(dd.substring(0, 1)) - 1;

                    if (ai >= 0 && ai < 4) channelA[ai]++;
                    if (bi >= 0 && bi < 4) channelB[bi]++;
                    if (ci >= 0 && ci < 4) channelC[ci]++;
                    if (di >= 0 && di < 4) channelD[di]++;
                } while (c.moveToNext());

                wearTimes[i] = c.getCount();
                if (D) Log.e(TAG, monthStr + " data count: " + c.getCount());
            }
            c.close();
        }

        ArrayList<BarEntry> yVals = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            yVals.add(new BarEntry(i, wearTimes[i] / dataNumOneDay));
            if (D) Log.d(TAG, "y: " + wearTimes[i] / dataNumOneDay);
        }

        mStatisticView.updateBarChartStatistic(yVals, visible, StatisticFragment.TYPE_YEAR);
        updateComfortStatistic(channelA, channelB, channelC, channelD, dataNumOneDay);
    }

    private void updateComfortStatistic(int[] channelA, int[] channelB, int[] channelC, int[] channelD, float criterion) {
        ArrayList<BarEntry> yValsA = new ArrayList<>();
        ArrayList<BarEntry> yValsB = new ArrayList<>();
        ArrayList<BarEntry> yValsC = new ArrayList<>();
        ArrayList<BarEntry> yValsD = new ArrayList<>();

        //boolean visibleA = false, visibleB = false, visibleC = false, visibleD = false;
        for (int j = 0; j < 4; j++) {
            /*
            if (channelA[j] != 0 && !visibleA) visibleA = true;
            if (channelB[j] != 0 && !visibleB) visibleB = true;
            if (channelC[j] != 0 && !visibleC) visibleC = true;
            if (channelD[j] != 0 && !visibleD) visibleD = true;
            */
            yValsA.add(new BarEntry(j, channelA[j] / criterion));
            yValsB.add(new BarEntry(j, channelB[j] / criterion));
            yValsC.add(new BarEntry(j, channelC[j] / criterion));
            yValsD.add(new BarEntry(j, channelD[j] / criterion));
        }

        /*
        mStatisticView.updateComfortStatistic(yValsA, visibleA, 0);
        mStatisticView.updateComfortStatistic(yValsB, visibleB, 1);
        mStatisticView.updateComfortStatistic(yValsC, visibleC, 2);
        mStatisticView.updateComfortStatistic(yValsD, visibleD, 3);
        */

        mStatisticView.updateComfortStatistic(yValsA, true, 0);
        mStatisticView.updateComfortStatistic(yValsB, true, 1);
        mStatisticView.updateComfortStatistic(yValsC, true, 2);
        mStatisticView.updateComfortStatistic(yValsD, true, 3);
    }
}
