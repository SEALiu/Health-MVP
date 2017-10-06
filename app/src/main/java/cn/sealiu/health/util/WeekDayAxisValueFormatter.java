package cn.sealiu.health.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by liuyang
 * on 2017/10/6.
 */

public class WeekDayAxisValueFormatter implements IAxisValueFormatter {

    private String[] weekDays = new String[7];

    public WeekDayAxisValueFormatter() {
        DateFormat Md = new SimpleDateFormat("MM-dd", Locale.getDefault());

        Calendar day = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            day.add(Calendar.DATE, -1);
            String dayStr = Md.format(day.getTime());
            weekDays[i] = dayStr;
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;
        if (index < 0 || index > 6) return "";
        return weekDays[index];
    }
}
