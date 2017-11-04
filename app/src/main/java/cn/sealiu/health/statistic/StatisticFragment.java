package cn.sealiu.health.statistic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.sealiu.health.R;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.main.MyMarkerView;
import cn.sealiu.health.util.MyAxisValueFormatter;
import cn.sealiu.health.util.WeekDayAxisValueFormatter;

import static android.content.ContentValues.TAG;
import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public class StatisticFragment extends Fragment implements
        StatisticContract.View,
        View.OnClickListener, DatePickerDialog.OnDateSetListener {

    public final static int TYPE_DAY = 1;
    public final static int TYPE_WEEK = 2;
    public final static int TYPE_MONTH = 3;
    public final static int TYPE_YEAR = 4;

    private StatisticContract.Presenter mPresenter;
    private AppCompatButton chooseStatisticBtn, chooseDateBtn;
    private TextView selectedDateRange;

    private BarChart mBarChart;
    private BarChart mBarChartA, mBarChartB, mBarChartC, mBarChartD;
    private View noData, noDataA, noDataB, noDataC, noDataD;
    private List<BarChart> mBarCharts = new ArrayList<>();
    private List<View> noDataChannels = new ArrayList<>();
    private String[] mxAxis = new String[]{"空载", "松", "合适", "紧"};
    private String[] mMonths = new String[]{
            "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"
    };
    private String chooseDayStr = "";
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int chosenMonth;

    public StatisticFragment() {
    }

    public static StatisticFragment newInstance() {
        return new StatisticFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.statistic_frag, container, false);
        chooseDateBtn = root.findViewById(R.id.choose_date);
        chooseStatisticBtn = root.findViewById(R.id.choose_statistic);
        mBarChart = root.findViewById(R.id.statistic_chart);

        selectedDateRange = root.findViewById(R.id.comfort_statistic_range);
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        chooseDayStr = df.format(yesterday.getTime());
        selectedDateRange.setText(chooseDayStr);

        noData = root.findViewById(R.id.no_data);
        noDataA = root.findViewById(R.id.no_data_A);
        noDataB = root.findViewById(R.id.no_data_B);
        noDataC = root.findViewById(R.id.no_data_C);
        noDataD = root.findViewById(R.id.no_data_D);

        mBarChartA = root.findViewById(R.id.channel_A);
        mBarChartB = root.findViewById(R.id.channel_B);
        mBarChartC = root.findViewById(R.id.channel_C);
        mBarChartD = root.findViewById(R.id.channel_D);

        mBarCharts.add(mBarChartA);
        mBarCharts.add(mBarChartB);
        mBarCharts.add(mBarChartC);
        mBarCharts.add(mBarChartD);

        noDataChannels.add(noDataA);
        noDataChannels.add(noDataB);
        noDataChannels.add(noDataC);
        noDataChannels.add(noDataD);

        chooseStatisticBtn.setOnClickListener(this);
        chooseDateBtn.setOnClickListener(this);

        ((TextView) root.findViewById(R.id.channel_A_title))
                .setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_ONE, "通道1"));
        ((TextView) root.findViewById(R.id.channel_B_title))
                .setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_TWO, "通道2"));
        ((TextView) root.findViewById(R.id.channel_C_title))
                .setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_THREE, "通道3"));
        ((TextView) root.findViewById(R.id.channel_D_title))
                .setText(sharedPref.getString(MainActivity.DEVICE_CHANNEL_FOUR, "通道4"));

        setupStatisticChart(TYPE_DAY);
        setupBarChart(mBarChartA, TYPE_WEEK);
        setupBarChart(mBarChartB, TYPE_WEEK);
        setupBarChart(mBarChartC, TYPE_WEEK);
        setupBarChart(mBarChartD, TYPE_WEEK);

        List<CardView> cards = new ArrayList<>();
        cards.add((CardView) root.findViewById(R.id.channel_A_holder));
        cards.add((CardView) root.findViewById(R.id.channel_B_holder));
        cards.add((CardView) root.findViewById(R.id.channel_C_holder));
        cards.add((CardView) root.findViewById(R.id.channel_D_holder));

        int channelNum = sharedPref.getInt(MainActivity.DEVICE_CHANNEL_NUM, 4);
        for (int i = 0; i < channelNum; i++) {
            cards.get(i).setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void setPresenter(StatisticContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showInfo(String msg) {
        if (getView() != null)
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int strId) {
        showInfo(getString(strId));
    }

    @Override
    public void updateBarChartStatistic(ArrayList<BarEntry> yVals, boolean visible, int type) {
        mBarChart.clear();
        if (!visible) {
            noData.setVisibility(View.VISIBLE);
            return;
        } else {
            noData.setVisibility(View.GONE);
        }

        BarDataSet set1;
        if (mBarChart.getData() != null && mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals, "佩戴时间");
            set1.setDrawValues(false);
            set1.setColor(ActivityCompat.getColor(getActivity(), R.color.banana));
            set1.setValueTextColor(ActivityCompat.getColor(getActivity(), R.color.textOrIcons));

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            mBarChart.setData(data);
        }

        switch (type) {
            case TYPE_DAY:
                mBarChart.getXAxis().setAxisMaximum(24);
                break;
            case TYPE_WEEK:
                mBarChart.getXAxis().setAxisMaximum(7);
                break;
            case TYPE_MONTH:
                mBarChart.getXAxis().setAxisMaximum(31);
                break;
            case TYPE_YEAR:
                mBarChart.getXAxis().setAxisMaximum(12);
                break;
        }
        mBarChart.invalidate();
        mBarChart.animateXY(1000, 1500);
    }

    @Override
    public void updateComfortStatistic(ArrayList<BarEntry> yVals,
                                       boolean visible,
                                       int position) {
        mBarCharts.get(position).clear();
        if (!visible) {
            noDataChannels.get(position).setVisibility(View.VISIBLE);
            return;
        } else {
            noDataChannels.get(position).setVisibility(View.GONE);
        }

        BarDataSet set1;
        if (mBarCharts.get(position).getData() != null
                && mBarCharts.get(position).getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarCharts.get(position).getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            mBarCharts.get(position).getData().notifyDataChanged();
            mBarCharts.get(position).notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals, "佩戴时间");
            set1.setDrawValues(false);
            set1.setColor(ActivityCompat.getColor(getActivity(), R.color.blueSky));
            set1.setValueTextColor(ActivityCompat.getColor(getActivity(), R.color.thinkDark));

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            mBarCharts.get(position).setData(data);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_statistic:
                PopupMenu popupMenu = new PopupMenu(getActivity(), chooseStatisticBtn);
                popupMenu.getMenuInflater().inflate(R.menu.statistic_popup_menu, popupMenu.getMenu());

                final Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DATE, -1);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.day_statistic:
                                setupStatisticChart(TYPE_DAY);
                                setupBarChart(mBarChartA, TYPE_WEEK);
                                setupBarChart(mBarChartB, TYPE_WEEK);
                                setupBarChart(mBarChartC, TYPE_WEEK);
                                setupBarChart(mBarChartD, TYPE_WEEK);

                                final String[] dateArray = chooseDayStr.split("-");
                                final Calendar chooseDay = Calendar.getInstance();
                                chooseDay.set(Calendar.YEAR, Integer.valueOf(dateArray[0]));
                                chooseDay.set(Calendar.MONTH, Integer.valueOf(dateArray[1]) - 1);
                                chooseDay.set(Calendar.DATE, Integer.valueOf(dateArray[2]));

                                mPresenter.loadDayStatistic(chooseDay);
                                chooseStatisticBtn.setText(R.string.by_day);

                                selectedDateRange.setText(chooseDayStr);

                                chooseDateBtn.setVisibility(View.VISIBLE);
                                chooseDateBtn.performClick();
                                return true;
                            case R.id.week_statistic:
                                setupStatisticChart(TYPE_WEEK);
                                setupBarChart(mBarChartA, TYPE_WEEK);
                                setupBarChart(mBarChartB, TYPE_WEEK);
                                setupBarChart(mBarChartC, TYPE_WEEK);
                                setupBarChart(mBarChartD, TYPE_WEEK);
                                mPresenter.loadWeekStatistic(null);
                                chooseStatisticBtn.setText(R.string.by_week);

                                String yesterdayStr = df.format(yesterday.getTime());

                                yesterday.add(Calendar.DATE, -7);
                                String sevenDaysBeforeStr = df.format(yesterday.getTime());

                                selectedDateRange.setText(String.format("%s至%s", sevenDaysBeforeStr, yesterdayStr));
                                chooseDateBtn.setVisibility(View.GONE);
                                return true;
                            case R.id.month_statistic:
                                setupStatisticChart(TYPE_MONTH);
                                setupBarChart(mBarChartA, TYPE_YEAR);
                                setupBarChart(mBarChartB, TYPE_YEAR);
                                setupBarChart(mBarChartC, TYPE_YEAR);
                                setupBarChart(mBarChartD, TYPE_YEAR);
                                chooseStatisticBtn.setText(R.string.by_month);

                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.choose_month_dia, null);
                                final AppCompatSpinner spinner = dialogView.findViewById(R.id.month_list);
                                TextView yearText = dialogView.findViewById(R.id.this_year);
                                Calendar thisYear = Calendar.getInstance();
                                yearText.setText(thisYear.get(Calendar.YEAR) + "");

                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        chosenMonth = position;
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                        chosenMonth = -1;
                                    }
                                });

                                new AlertDialog.Builder(getActivity())
                                        .setView(dialogView)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (D)
                                                    Log.e(TAG, "chosen Month index: " + chosenMonth);
                                                if (chosenMonth != -1) {
                                                    mPresenter.loadMonthStatistic((chosenMonth + 1) + "");
                                                    selectedDateRange.setText(mMonths[chosenMonth]);
                                                } else {
                                                    showInfo("未选择月份");
                                                }
                                            }
                                        })
                                        .show();

                                chooseDateBtn.setVisibility(View.GONE);
                                return true;
                            case R.id.year_statistic:
                                setupStatisticChart(TYPE_YEAR);
                                setupBarChart(mBarChartA, TYPE_YEAR);
                                setupBarChart(mBarChartB, TYPE_YEAR);
                                setupBarChart(mBarChartC, TYPE_YEAR);
                                setupBarChart(mBarChartD, TYPE_YEAR);
                                chooseStatisticBtn.setText(R.string.by_year);

                                Calendar today = Calendar.getInstance();
                                int year, month, day;
                                year = today.get(Calendar.YEAR);
                                month = today.get(Calendar.MONTH);
                                day = today.get(Calendar.DATE);

                                YearOnlyPicker yp = YearOnlyPicker.newInstance(new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
                                        if (D) Log.e(TAG, "YearOnlyPicker: " + i);
                                        mPresenter.loadYearStatistic(i + "");
                                        selectedDateRange.setText(String.format("%s年", i + ""));
                                    }
                                }, year, month, day);
                                yp.setAccentColor(getResources().getColor(R.color.colorPrimary));
                                yp.show(getActivity().getFragmentManager(), "YearPicker");

                                chooseDateBtn.setVisibility(View.GONE);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                break;
            case R.id.choose_date:
                Calendar yesterday1 = Calendar.getInstance();
                yesterday1.add(Calendar.DATE, -1);

                int year, month, day;
                if (chooseDayStr.equals("")) {
                    year = yesterday1.get(Calendar.YEAR);
                    month = yesterday1.get(Calendar.MONTH);
                    day = yesterday1.get(Calendar.DAY_OF_MONTH);
                } else {
                    final String[] dateArray = chooseDayStr.split("-");

                    year = Integer.valueOf(dateArray[0]);
                    month = Integer.valueOf(dateArray[1]) - 1;
                    day = Integer.valueOf(dateArray[2]);
                }

                DatePickerDialog dpd = DatePickerDialog.newInstance(this, year, month, day);
                dpd.setAccentColor(getResources().getColor(R.color.colorPrimary));

                String startUseStr = sharedPref.getString(MainActivity.DEVICE_START_USING_DATE, "");
                Date startUseDate = null;
                try {
                    startUseDate = df.parse(startUseStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (startUseDate != null) {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.setTime(startUseDate);

                    dpd.setMinDate(startCalendar);
                    dpd.setMaxDate(yesterday1);
                    dpd.show(getActivity().getFragmentManager(), "DatePicker");
                }
                break;
        }
    }

    /**
     * @param type 统计类型 1:按天，2:按周，3:按月，4:按年
     */
    private void setupStatisticChart(int type) {
        int white = ActivityCompat.getColor(getActivity(), R.color.textOrIcons);

        mBarChart.setDrawGridBackground(false);
        mBarChart.setBorderColor(white);
        mBarChart.getDescription().setEnabled(false);

        mBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "entry: X = " + e.getX() + "// Y = " + e.getY());
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mBarChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(white);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);

        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setTextColor(white);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setValueFormatter(new MyAxisValueFormatter(type));
        mBarChart.getAxisRight().setEnabled(false);

        switch (type) {
            case TYPE_DAY:
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return String.valueOf((int) value) + "时";
                    }
                });
                leftAxis.setAxisMinimum(1f);
                break;
            case TYPE_MONTH:
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return String.valueOf((int) value + 1) + "日";
                    }
                });
                leftAxis.setAxisMinimum(1f);
                break;
            case TYPE_WEEK:
                xAxis.setValueFormatter(new WeekDayAxisValueFormatter());
                leftAxis.setAxisMinimum(1f);
                break;
            case TYPE_YEAR:
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        if (value < 0 || value > 11) return "";
                        return mMonths[(int) value];
                    }
                });
                leftAxis.setAxisMinimum(0.1f);
                break;
        }

        Legend l = mBarChart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextColor(Color.WHITE);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view, type);
        mv.setChartView(mBarChart);
        mBarChart.setMarker(mv);
    }

    private void setupBarChart(BarChart barChart, int type) {
        int thinkDark = ActivityCompat.getColor(getActivity(), R.color.thinkDark);

        barChart.setDrawGridBackground(false);
        barChart.setBorderColor(thinkDark);
        barChart.getDescription().setEnabled(false);

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "entry: X = " + e.getX() + "// Y = " + e.getY());
            }

            @Override
            public void onNothingSelected() {

            }
        });

        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setClickable(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(thinkDark);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mxAxis[(int) value];
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0.1f);
        leftAxis.setTextColor(thinkDark);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setValueFormatter(new MyAxisValueFormatter(type));

        barChart.getAxisRight().setEnabled(false);

        Legend l = barChart.getLegend();
        l.setEnabled(false);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view, type);
        mv.setChartView(barChart);
        barChart.setMarker(mv);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        chooseDayStr = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        selectedDateRange.setText(chooseDayStr);
        datePickerDialog.dismiss();

        Calendar chooseDay = Calendar.getInstance();
        chooseDay.set(Calendar.YEAR, year);
        chooseDay.set(Calendar.MONTH, monthOfYear);
        chooseDay.set(Calendar.DATE, dayOfMonth);

        mPresenter.loadDayStatistic(chooseDay);
    }

    public static class YearOnlyPicker extends DatePickerDialog {

        public static YearOnlyPicker newInstance(OnDateSetListener callBack, int year,
                                                 int monthOfYear,
                                                 int dayOfMonth) {
            YearOnlyPicker ret = new YearOnlyPicker();
            ret.showYearPickerFirst(true);
            ret.initialize(callBack, year, monthOfYear, dayOfMonth);
            return ret;
        }

        @Override
        public void onYearSelected(int year) {
            super.onYearSelected(year);
            notifyOnDateListener();
            dismiss();

        }
    }
}
