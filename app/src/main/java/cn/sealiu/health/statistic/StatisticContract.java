package cn.sealiu.health.statistic;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public interface StatisticContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showInfo(String msg);

        void showInfo(int strId);

        void updateBarChartStatistic(ArrayList<BarEntry> yVals, boolean visible, int type);

        void updateComfortStatistic(ArrayList<BarEntry> yVals, boolean visible, int position);
    }

    interface Presenter extends BasePresenter {
        void loadDayStatistic(Calendar day);

        void loadWeekStatistic(Calendar day);

        void loadMonthStatistic(String MM);

        void loadYearStatistic(String yyyy);
    }
}
