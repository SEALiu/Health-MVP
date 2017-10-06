package cn.sealiu.health.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.data.local.DataStatusPersistenceContract.DataStatusEntry;
import cn.sealiu.health.data.local.HealthDbHelper;
import cn.sealiu.health.fixcriterion.FixCriterionActivity;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.statistic.StatisticActivity;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.Fun;

import static android.app.Activity.RESULT_OK;
import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

public class HomeUserFragment extends Fragment implements
        UserContract.View,
        View.OnClickListener {

    private static final String TAG = "HomeUserFragment";
    private static final int REQUEST_FIX_CRITERION = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final String BIND_SUCCESS = "00000000";
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private UserContract.Presenter mPresenter;
    private TextView batteryLeftTV, storageLeftTV, timeTV;
    private BarChart weekBarChart;
    private LineChart realtimeLineChart;
    private SwitchCompat realtimeSwitch;
    Menu menu;

    private HealthDbHelper dbHelper;
    private SQLiteDatabase db;

    private List<ImageView> dots = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    List<Drawable> comfortableDrawables = new ArrayList<>();

    private String historyDate = "";
    /**
     * -1: bluetooth isn't open
     * 0: disconnected
     * 1: searching
     * 2: connected
     */
    public static int mConnected = BluetoothLeService.STATE_DISCONNECTED;
    public static BluetoothLeService mBluetoothLeService;
    public static BluetoothGattCharacteristic mWantedCharacteristic;
    public static String mChosenBTName, mChosenBTAddress;
    public static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
            if (!mBluetoothLeService.initialize()) {
                if (D) Log.e(TAG, "Unable to initialize Bluetooth");
            } else {
                if (mChosenBTAddress == null) {
                    mChosenBTName = sharedPref.getString(MainActivity.DEVICE_NAME, "未知设备");
                    mChosenBTAddress = sharedPref.getString(MainActivity.DEVICE_ADDRESS, "");
                }

                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(mChosenBTAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public static IntentFilter gattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = BluetoothLeService.STATE_CONNECTED;
                showMessage(getString(R.string.device_connected));
                changeMenuBluetoothIcon(R.drawable.ic_bluetooth_connected_black_24dp);
                if (realtimeSwitch != null) {
                    realtimeSwitch.setEnabled(true);
                    realtimeSwitch.setChecked(true);
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = BluetoothLeService.STATE_DISCONNECTED;
                showMessage(getString(R.string.device_disconnected), R.string.reconnect,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                manualConnect();
                            }
                        });
                changeMenuBluetoothIcon(R.drawable.ic_bluetooth_waiting_black_24dp);
                realtimeSwitch.setChecked(false);
                realtimeSwitch.setEnabled(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // You can get List<BluetoothGattService>
                // through function: mBluetoothLeService.getSupportedGattServices()
                if (D) Log.d(TAG, "BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED");

                mWantedCharacteristic = mPresenter.discoverCharacteristic(mBluetoothLeService);
                if (mWantedCharacteristic != null) {
                    final int charaProp = mWantedCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        mBluetoothLeService.readCharacteristic(mWantedCharacteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(
                                mWantedCharacteristic, true);
                    }

                    mPresenter.onGattServicesDiscovered();
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mPresenter.analyzeData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
        getContext().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        getContext().registerReceiver(mGattUpdateReceiver, gattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mChosenBTAddress);
            if (D) Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_user_frag, container, false);

        dots.add((ImageView) root.findViewById(R.id.dot_1));
        dots.add((ImageView) root.findViewById(R.id.dot_2));
        dots.add((ImageView) root.findViewById(R.id.dot_3));
        dots.add((ImageView) root.findViewById(R.id.dot_4));

        colors.add(ActivityCompat.getColor(getActivity(), R.color.tomato));
        colors.add(ActivityCompat.getColor(getActivity(), R.color.banana));
        colors.add(ActivityCompat.getColor(getActivity(), R.color.blueSky));
        colors.add(ActivityCompat.getColor(getActivity(), R.color.cucumber));

        comfortableDrawables.add(ActivityCompat.getDrawable(getActivity(), R.drawable.ic_dot_blank));
        comfortableDrawables.add(ActivityCompat.getDrawable(getActivity(), R.drawable.ic_dot_loose));
        comfortableDrawables.add(ActivityCompat.getDrawable(getActivity(), R.drawable.ic_dot_comfort));
        comfortableDrawables.add(ActivityCompat.getDrawable(getActivity(), R.drawable.ic_dot_tight));

        labels.add(sharedPref.getString(MainActivity.DEVICE_CHANNEL_ONE, "通道1"));
        labels.add(sharedPref.getString(MainActivity.DEVICE_CHANNEL_TWO, "通道2"));
        labels.add(sharedPref.getString(MainActivity.DEVICE_CHANNEL_THREE, "通道3"));
        labels.add(sharedPref.getString(MainActivity.DEVICE_CHANNEL_FOUR, "通道4"));

        // base info panel;
        batteryLeftTV = root.findViewById(R.id.battery_left);
        storageLeftTV = root.findViewById(R.id.storage_left);
        timeTV = root.findViewById(R.id.time);

        // chart
        weekBarChart = root.findViewById(R.id.week_barchart);
        realtimeLineChart = root.findViewById(R.id.realtime_linechart);

        realtimeSwitch = root.findViewById(R.id.switch_realtime);

        // set up realtime line chart with closing the switch button
        realtimeSwitch.setChecked(false);
        realtimeSwitch.setEnabled(false);

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
                mPresenter.checkBluetoothSupport(getContext());
                mPresenter.requestBaseInfo();
            }
        });

        mChosenBTName = sharedPref.getString(MainActivity.DEVICE_NAME, "未知设备");
        mChosenBTAddress = sharedPref.getString(MainActivity.DEVICE_ADDRESS, "");

        // check is support bluetooth AND bluetooth low energy AND bluetooth is open
        // if every below is ok, then open bluetooth
        mPresenter.checkBluetoothSupport(getActivity());

        setupWeekBarChart();

        setupRealtimeLineChart();

        if (dbHelper == null) dbHelper = new HealthDbHelper(getActivity());
        mPresenter.loadWeekBarChartData(dbHelper);

        setupBaseInfo();

        setHasOptionsMenu(true);
        return root;
    }

    /**
     * 维护本地数据状态表 datastatus.tb
     * 该表保存了设备启用日期到当前日期的历史数据的请求情况(已请求本地缓存有数据，已请求但设备无数据，未请求)
     */
    @Override
    public void updateDataStatus() {
        if (dbHelper == null) dbHelper = new HealthDbHelper(getActivity());
        db = dbHelper.getWritableDatabase();
        String startUseStr = sharedPref.getString(MainActivity.DEVICE_START_USING_DATE, "");

        if (startUseStr.equals("")) {
            if (D) Log.d(TAG, "start use date is empty");
            mPresenter.requestDeviceEnableDate();
        } else {
            if (D) Log.d(TAG, "start use date is not empty, " + startUseStr);

            Date startUseDate = null;
            Date now = null;
            try {
                startUseDate = df.parse(startUseStr);
                now = df.parse(df.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (startUseDate == null || now == null) return;

            // 启用日期 到 当前日期 之间的 日期List
            List<String> betweenDays = Fun.getDatesBetweenTwoDate(startUseDate, now);

            for (String dateStr : betweenDays) {
                String sql = "SELECT * FROM " + DataStatusEntry.TABLE_NAME +
                        " WHERE " + DataStatusEntry.COLUMN_NAME_TIME + " = '" + dateStr + "'";
                Cursor c = db.rawQuery(sql, null);

                // 如果该日期 datastatus.tb 中没有，则插入该条数据，status 默认为3，表示："未请求"
                if (!c.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(DataStatusEntry.COLUMN_NAME_ID, UUID.randomUUID().toString());
                    values.put(DataStatusEntry.COLUMN_NAME_TIME, dateStr);
                    values.put(DataStatusEntry.COLUMN_NAME_STATUS, 3); //未请求

                    db.insert(DataStatusEntry.TABLE_NAME, null, values);
                }
                c.close();
            }
            if (mConnected == BluetoothLeService.STATE_CONNECTED
                    && mWantedCharacteristic != null
                    && mBluetoothLeService != null) {
                mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                        BoxRequestProtocol.boxStopUpload());
                //向设备请求历史数据
                updateHistoryData();
            }
        }
    }

    /**
     * 向设备请求设备历史数据
     */
    @Override
    public void updateHistoryData() {

        // 按照本地数据库 datastatus.tb 请求历史数据
        String sql = "SELECT * FROM " + DataStatusEntry.TABLE_NAME +
                " WHERE " + DataStatusEntry.COLUMN_NAME_STATUS + " = 3";
        Cursor c = db.rawQuery(sql, null);

        if (c.moveToFirst()) {
            historyDate = c.getString(c.getColumnIndex(DataStatusEntry.COLUMN_NAME_TIME));
            if (D) Log.e(TAG, "request history: Date is " + historyDate);

            if (mConnected == BluetoothLeService.STATE_CONNECTED
                    && mWantedCharacteristic != null
                    && mBluetoothLeService != null) {
                mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                        BoxRequestProtocol.boxRequestHistoryData(historyDate));
            } else {
                if (D) Log.e(TAG, "device disconnected");
                showInfo("设备未连接");
            }
        } else {
            if (D) Log.e(TAG, "history data request over");
            showInfo("设备数据请求完毕");

            // 继续接收实时数据
            if (mConnected == BluetoothLeService.STATE_CONNECTED
                    && mWantedCharacteristic != null
                    && mBluetoothLeService != null) {
                mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                        BoxRequestProtocol.boxStartUpload());
            }
        }
        c.close();
    }

    @Override
    public void saveHistoryData() {
        if (dbHelper == null) dbHelper = new HealthDbHelper(getActivity());
        mPresenter.doSaveHistoryData(dbHelper, historyDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null && mBluetoothLeService != null) {
            getActivity().unbindService(mServiceConnection);
            mBluetoothLeService.disconnect();
            getActivity().unregisterReceiver(mGattUpdateReceiver);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK) {
                    showMessage(getString(R.string.open_bluetooth_info));
                } else {
                    manualConnect();
                }
                break;
            case REQUEST_FIX_CRITERION:
                // TODO: 2017/10/1 检查定标结果
                if (D) Log.d(TAG, "fix criterion result: " + resultCode);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.main_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth:
                final BluetoothManager bluetoothManager = (BluetoothManager)
                        getContext().getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

                if (!bluetoothAdapter.isEnabled()) {
                    openBluetooth();
                    break;
                }

                if (mConnected == BluetoothLeService.STATE_CONNECTED) {
                    String content = String.format(getString(R.string.connect_success), mChosenBTName);
                    showMessage(content);
                    break;
                }

                if (mConnected == BluetoothLeService.STATE_DISCONNECTED) {
                    manualConnect();
                }
                break;
            case R.id.sync:
                // TODO: 2017/10/1 检查本地数据表，更新，找到为上传的数据，检查网络，然后上传
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
    public void showInfo(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int strId) {
        showInfo(getString(strId));
    }

    @Override
    public void gotoLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void gotoFixCriterion() {
        getActivity().startActivityForResult(new Intent(getActivity(), FixCriterionActivity.class),
                REQUEST_FIX_CRITERION);
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
    public void updateTime(@Nullable String time) {
        if (time == null) {
            Calendar now = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateStr = df.format(now.getTime());
            timeTV.setText(dateStr);
        } else {
            timeTV.setText(time);
        }
    }

    @Override
    public void showNoAvailableService() {
        showMessage(getString(R.string.bt_device_wrong));
        // TODO: 2017/9/19 重新选择连接设备
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.see_all_statistic:
                Intent intent = new Intent(getActivity(), StatisticActivity.class);
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

    @Override
    public void delayExit() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().finish();
            }
        }, 1500);
    }

    @Override
    public void openBluetooth() {
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void changeMenuBluetoothIcon(final int resourceId) {
        if (menu != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
                    Drawable icon = ActivityCompat.getDrawable(getContext(), resourceId);
                    menu.getItem(0).setIcon(icon);
                }
            });
        }
    }

    @Override
    public void setSyncTime() {
        mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                BoxRequestProtocol.boxSyncTime());
    }

    @Override
    public void requestDeviceParam(String paramName) {
        if (mConnected == BluetoothLeService.STATE_CONNECTED &&
                mWantedCharacteristic != null &&
                mBluetoothLeService != null) {
            mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                    BoxRequestProtocol.boxRequestDeviceParam(paramName, null));
        }
    }

    @Override
    public void setDeviceParam(String paramName, String value) {
        if (mConnected == BluetoothLeService.STATE_CONNECTED &&
                mWantedCharacteristic != null &&
                mBluetoothLeService != null) {
            mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                    BoxRequestProtocol.boxRequestDeviceParam(paramName, value));
        }
    }

    @Override
    public void requestDeviceStatus() {
        if (mConnected == BluetoothLeService.STATE_CONNECTED &&
                mWantedCharacteristic != null &&
                mBluetoothLeService != null) {
            mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                    BoxRequestProtocol.boxRequestStatus());
        } else {
            showInfo("设备未连接");
        }
    }

    @Override
    public void requestRealtime(boolean active) {
        if (mConnected == BluetoothLeService.STATE_CONNECTED) {
            if (active)
                mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                        BoxRequestProtocol.boxStartUpload());
            else
                mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                        BoxRequestProtocol.boxStopUpload());
        } else {
            showInfo(R.string.bluetooth_disconnected);
        }

    }

    @Override
    public void updateLineChartRT(float value, String status, int index) {
        Log.d(TAG, "UserHomeFragment+++updateLineChartRT: " + value + " index: " + index);
        LineData data = realtimeLineChart.getData();

        ILineDataSet set = data.getDataSetByIndex(index);
        // set.addEntry(...); // can be called as well
        if (set != null) {
            data.addEntry(new Entry(data.getDataSetByIndex(index).getEntryCount(), value), index);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            realtimeLineChart.notifyDataSetChanged();

            realtimeLineChart.setVisibleXRangeMaximum(15);

            // this automatically refreshes the chart (calls invalidate())
            realtimeLineChart.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        }

        int lightIndex = Integer.valueOf(status);
        if (lightIndex >= 0 && lightIndex < 4) {
            dots.get(index).setImageDrawable(comfortableDrawables.get(lightIndex));
        }
    }

    @Override
    public void updateWeekBarChart(ArrayList<BarEntry> yVals) {
        BarDataSet set1;
        if (weekBarChart.getData() != null && weekBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) weekBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            weekBarChart.getData().notifyDataChanged();
            weekBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals, "佩戴时间");
            set1.setDrawValues(false);
            set1.setColor(ActivityCompat.getColor(getActivity(), R.color.blueSky));
            set1.setValueTextColor(ActivityCompat.getColor(getActivity(), R.color.textOrIcons));

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setValueFormatter(new LargeValueFormatter());
            weekBarChart.setData(data);
        }

        // restrict the x-axis range
        weekBarChart.getXAxis().setAxisMinimum(0);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        weekBarChart.getXAxis().setAxisMaximum(30);
        weekBarChart.invalidate();
        weekBarChart.animateXY(3000, 2000);
    }

    private void setupWeekBarChart() {
        weekBarChart.setDrawGridBackground(false);
        weekBarChart.getDescription().setEnabled(false);

        weekBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "entry: X = " + e.getX() + "// Y = " + e.getY());
            }

            @Override
            public void onNothingSelected() {

            }
        });

        weekBarChart.setDoubleTapToZoomEnabled(false);

        int white = ActivityCompat.getColor(getActivity(), R.color.textOrIcons);
        XAxis xAxis = weekBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setTextColor(white);
        xAxis.setCenterAxisLabels(false);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        YAxis leftAxis = weekBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(white);

        weekBarChart.getAxisRight().setEnabled(false);

        Legend l = weekBarChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(weekBarChart);
        weekBarChart.setMarker(mv);
    }

    private void setupRealtimeLineChart() {
        realtimeLineChart.setDrawGridBackground(false);
        realtimeLineChart.getDescription().setEnabled(false);

        realtimeLineChart.setDoubleTapToZoomEnabled(false);

        int blackColor = ActivityCompat.getColor(getContext(), R.color.primaryText);
        realtimeLineChart.getXAxis().setTextColor(blackColor);
        realtimeLineChart.getAxisLeft().setTextColor(blackColor);
        realtimeLineChart.getAxisRight().setTextColor(blackColor);

        Legend l = realtimeLineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(blackColor);

        // add an empty data object
        realtimeLineChart.setData(new LineData());
        realtimeLineChart.invalidate();

        LineData data = realtimeLineChart.getData();

        if (data != null) {
            int channelNum = sharedPref.getInt(MainActivity.DEVICE_CHANNEL_NUM, 4);

            for (int i = 0; i < channelNum; i++) {
                ArrayList<Entry> yVals = new ArrayList<>();
                yVals.add(new Entry(0, 0f));

                LineDataSet set = createSet(labels.get(i), colors.get(i), yVals);
                data.addDataSet(set);
                data.notifyDataChanged();
                realtimeLineChart.notifyDataSetChanged();
                realtimeLineChart.invalidate();
            }
        }
    }

    private void setupBaseInfo() {
        batteryLeftTV.setText(String.format("%s%%", sharedPref.getString(MainActivity.DEVICE_POWER, "")));
        storageLeftTV.setText(String.format("%sMB", sharedPref.getString(MainActivity.DEVICE_STORAGE, "")));
        timeTV.setText(sharedPref.getString(MainActivity.DEVICE_TIME, ""));
    }

    private void showMessage(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    private void showMessage(String msg, int actionId, View.OnClickListener clickListener) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).setAction(actionId, clickListener).show();
    }

    private void manualConnect() {
        if (D)
            Log.d(TAG, "mBluetoothLeService is " + (mBluetoothLeService == null ? "is" : "is not") + " null");
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mChosenBTAddress);
            mConnected = BluetoothLeService.STATE_CONNECTING;
            changeMenuBluetoothIcon(R.drawable.ic_bluetooth_searching_black_24dp);
        }
    }

    private LineDataSet createSet(String label, int color, ArrayList<Entry> yVals) {
        if (D) Log.d(TAG, "UserHomeFragment+++createSet");
        LineDataSet set = new LineDataSet(yVals, label);
        set.setDrawValues(false);
        set.setValueTextColor(ActivityCompat.getColor(getActivity(), R.color.textOrIcons));
        set.setLineWidth(2.5f);
        set.setCircleRadius(4.5f);
        set.setColor(color);
        set.setCircleColor(color);
        set.setHighLightColor(color);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }
}