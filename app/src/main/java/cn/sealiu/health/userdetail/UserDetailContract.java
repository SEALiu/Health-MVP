package cn.sealiu.health.userdetail;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.User;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface UserDetailContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showUserDetail(User user);

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoLogin();

        void updateBarChartStatistic(ArrayList<BarEntry> yVals, boolean visible, int type);

        void updateComfortStatistic(ArrayList<BarEntry> yVals, boolean visible, int position);
    }

    interface Presenter extends BasePresenter {
        void loadUserDetail(String userId);

        void doSentMsg(String toId, String content);

        void loadDayStatistic(Calendar day);

        void loadWeekStatistic(Calendar day);

        void loadMonthStatistic(String MM);

        void loadYearStatistic(String yyyy);
    }
}
