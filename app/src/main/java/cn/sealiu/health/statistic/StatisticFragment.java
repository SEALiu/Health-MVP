package cn.sealiu.health.statistic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;

import cn.sealiu.health.R;
import cn.sealiu.health.main.MyMarkerView;
import cn.sealiu.health.util.DayAxisValueFormatter;
import cn.sealiu.health.util.MyAxisValueFormatter;
import cn.sealiu.health.util.WeekDayAxisValueFormatter;

import static android.content.ContentValues.TAG;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public class StatisticFragment extends Fragment implements
        StatisticContract.View,
        View.OnClickListener {

    public final static int TYPE_DAY = 1;
    public final static int TYPE_WEEK = 2;
    public final static int TYPE_MONTH = 3;
    public final static int TYPE_YEAR = 4;
    private StatisticContract.Presenter mPresenter;
    private AppCompatButton chooseStatisticBtn, chooseDateBtn;
    private BarChart mBarChart;
    private View noData;

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
        noData = root.findViewById(R.id.no_data);

        chooseStatisticBtn.setOnClickListener(this);
        chooseDateBtn.setOnClickListener(this);

        setupStatisticChart(TYPE_DAY);
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
    public void updateDayStatistic(ArrayList<BarEntry> yVals, boolean visible) {
        if (!visible) {
            noData.setVisibility(View.VISIBLE);
            return;
        } else {
            noData.setVisibility(View.GONE);
        }

        mBarChart.clear();

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

        mBarChart.getXAxis().setAxisMaximum(24);
        mBarChart.invalidate();
        mBarChart.animateXY(1000, 1500);
    }

    @Override
    public void updateWeekStatistic(ArrayList<BarEntry> yVals, boolean visible) {
        if (!visible) {
            noData.setVisibility(View.VISIBLE);
            return;
        } else {
            noData.setVisibility(View.GONE);
        }

        mBarChart.clear();

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

        mBarChart.getXAxis().setAxisMaximum(7);
        mBarChart.invalidate();
        mBarChart.animateXY(1000, 1500);
    }

    @Override
    public void updateMonthStatistic(ArrayList<BarEntry> yVals, boolean visible) {
        if (!visible) {
            noData.setVisibility(View.VISIBLE);
            return;
        } else {
            noData.setVisibility(View.GONE);
        }

        mBarChart.clear();
    }

    @Override
    public void updateYearStatistic(ArrayList<BarEntry> yVals, boolean visible) {
        if (!visible) {
            noData.setVisibility(View.VISIBLE);
            return;
        } else {
            noData.setVisibility(View.GONE);
        }

        mBarChart.clear();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_statistic:
                PopupMenu popupMenu = new PopupMenu(getActivity(), chooseStatisticBtn);
                popupMenu.getMenuInflater().inflate(R.menu.statistic_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.day_statistic:
                                setupStatisticChart(TYPE_DAY);
                                mPresenter.loadDayStatistic(null);
                                chooseStatisticBtn.setText(R.string.by_day);
                                return true;
                            case R.id.week_statistic:
                                setupStatisticChart(TYPE_WEEK);
                                mPresenter.loadWeekStatistic(null);
                                chooseStatisticBtn.setText(R.string.by_week);
                                return true;
                            case R.id.month_statistic:
                                setupStatisticChart(TYPE_MONTH);
                                mPresenter.loadMonthStatistic(null);
                                chooseStatisticBtn.setText(R.string.by_month);
                                return true;
                            case R.id.year_statistic:
                                setupStatisticChart(TYPE_YEAR);
                                mPresenter.loadYearStatistic(null);
                                chooseStatisticBtn.setText(R.string.by_year);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                break;
            case R.id.choose_date:
                // TODO: 2017/10/6 choose date
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

        switch (type) {
            case TYPE_DAY:
            case TYPE_MONTH:
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return String.valueOf((int) value);
                    }
                });
                break;
            case TYPE_WEEK:
                xAxis.setValueFormatter(new WeekDayAxisValueFormatter());
                break;
            case TYPE_YEAR:
                xAxis.setValueFormatter(new DayAxisValueFormatter(mBarChart));
                break;
        }

        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(1f);
        leftAxis.setTextColor(white);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setValueFormatter(new MyAxisValueFormatter(type));

        mBarChart.getAxisRight().setEnabled(false);

        Legend l = mBarChart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextColor(Color.WHITE);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view, type);
        mv.setChartView(mBarChart);
        mBarChart.setMarker(mv);
    }
}
