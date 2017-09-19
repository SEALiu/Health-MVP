package cn.sealiu.health.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.R;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.statistic.StatisticActivity;

import static com.google.common.base.Preconditions.checkNotNull;

public class HomeUserFragment extends Fragment implements UserContract.View, View.OnClickListener {

    private UserContract.Presenter mPresenter;
    private TextView batteryLeftTV, storageLeftTV, syncTimeTV;
    private BarChart weekBarChart;
    private LineChart realtimeLineChart;
    private SwitchCompat realtimeSwitch;
    private View noRealTimeView;
    private View dotsHolder;

    private List<ImageView> dots = new ArrayList<>();

    public HomeUserFragment() {
    }

    public static HomeUserFragment newInstance() {
        return new HomeUserFragment();
    }

    @Override
    public void setPresenter(@NonNull UserContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_user_frag, container, false);

        // base info panel;
        batteryLeftTV = root.findViewById(R.id.battery_left);
        storageLeftTV = root.findViewById(R.id.storage_left);
        syncTimeTV = root.findViewById(R.id.sync_time);

        dotsHolder = root.findViewById(R.id.dots_holder);

        // chart
        weekBarChart = root.findViewById(R.id.week_barchart);
        realtimeLineChart = root.findViewById(R.id.realtime_linechart);

        realtimeSwitch = root.findViewById(R.id.switch_realtime);
        noRealTimeView = root.findViewById(R.id.no_realtime);

        // set up realtime line chart with closing the switch button
        realtimeSwitch.setChecked(false);
        noRealTimeView.setVisibility(View.VISIBLE);
        realtimeLineChart.setVisibility(View.GONE);
        dotsHolder.setVisibility(View.GONE);

        root.findViewById(R.id.see_all_statistic).setOnClickListener(this);
        realtimeSwitch.setOnClickListener(this);

        ScrollChildSwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        //swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.requestBaseInfo();
            }
        });

        mPresenter.requestBaseInfo();

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth:
                break;
            case R.id.sync:
                mPresenter.syncLocalData();
                break;
        }

        return true;
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null)
            return;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.refresh_layout);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(active);
            }
        });
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void showError(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showError(int strId) {
        showError(getString(strId));
    }

    @Override
    public void gotoLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void updateBattery(String batteryLeft) {
        batteryLeftTV.setText(batteryLeft);
    }

    @Override
    public void updateStorage(String storageLeft) {
        storageLeftTV.setText(storageLeft);
    }

    @Override
    public void updateSync(String syncTime) {
        syncTimeTV.setText(syncTime);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.see_all_statistic:
                Intent intent  = new Intent(getActivity(), StatisticActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.switch_realtime:
                if (realtimeSwitch.isChecked()) {
                    mPresenter.startRealtime();
                } else {
                    mPresenter.stopRealtime();
                }
                break;
        }
    }
}