package cn.sealiu.health.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

import cn.sealiu.health.statistic.StatisticFragment;

/**
 * Created by liuyang
 * on 2017/10/6.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter {
    private DecimalFormat mFormat;
    private int type;

    public MyAxisValueFormatter(int type) {
        this.type = type;
        switch (type) {
            case StatisticFragment.TYPE_WEEK: //week
            case StatisticFragment.TYPE_MONTH: //month
                mFormat = new DecimalFormat("##0.0");
                break;
            case StatisticFragment.TYPE_DAY: //day
                mFormat = new DecimalFormat("##");
                break;
            case StatisticFragment.TYPE_YEAR: //year
                mFormat = new DecimalFormat("##0.0");
                break;
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        switch (type) {
            case StatisticFragment.TYPE_DAY: //day
                return mFormat.format(value) + " min";
            case StatisticFragment.TYPE_WEEK: //week
            case StatisticFragment.TYPE_MONTH: //month
                return mFormat.format(value) + " h";
            case StatisticFragment.TYPE_YEAR: //year
                return mFormat.format(value) + " d";
            default:
                return value + "";
        }
    }
}
