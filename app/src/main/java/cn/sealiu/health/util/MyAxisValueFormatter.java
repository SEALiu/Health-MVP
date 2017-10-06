package cn.sealiu.health.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by liuyang
 * on 2017/10/6.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter {
    private DecimalFormat mFormat;

    public MyAxisValueFormatter() {
        mFormat = new DecimalFormat("##0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value) + " h";
    }
}
