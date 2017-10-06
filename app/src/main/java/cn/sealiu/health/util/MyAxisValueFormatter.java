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
    private int type;

    public MyAxisValueFormatter(int type) {
        this.type = type;
        switch (type) {
            case 2: //week
            case 3: //month
                mFormat = new DecimalFormat("##0.0");
                break;
            case 1: //day
            case 4: //year
                mFormat = new DecimalFormat("##");
                break;
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        switch (type) {
            case 1: //day
                return mFormat.format(value) + " min";
            case 2: //week
                return mFormat.format(value) + " h";
            case 3: //month
                return mFormat.format(value) + " h";
            case 4: //year
                return mFormat.format(value) + " d";
            default:
                return "";
        }
    }
}
