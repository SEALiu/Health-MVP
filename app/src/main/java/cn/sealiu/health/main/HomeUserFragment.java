package cn.sealiu.health.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.statistic.StatisticActivity;
import cn.sealiu.health.util.BoxRequestProtocol;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

public class HomeUserFragment extends Fragment implements
        UserContract.View,
        View.OnClickListener {
    private static final int REQUEST_ENABLE_BT = 2;
    public static final String BIND_SUCCESS = "00000000";

    private UserContract.Presenter mPresenter;
    private TextView batteryLeftTV, storageLeftTV, timeTV;
    private BarChart weekBarChart;
    private LineChart realtimeLineChart;
    private SwitchCompat realtimeSwitch;
    Menu menu;

    private List<ImageView> dots = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    List<Drawable> comfortableDrawables = new ArrayList<>();

    /**
     * -1: bluetooth isn't open
     * 0: disconnected
     * 1: searching
     * 2: connected
     */
    public int mConnected = BluetoothLeService.STATE_DISCONNECTED;
    public BluetoothLeService mBluetoothLeService;
    public BluetoothGattCharacteristic mWantedCharacteristic;
    public String mChosenBTName, mChosenBTAddress;
    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
            if (!mBluetoothLeService.initialize()) {
                if (D) Log.e(TAG, "Unable to initialize Bluetooth");
                getActivity().finish();
            }

            if (mChosenBTAddress == null) {
                mChosenBTName = sharedPref.getString(MainActivity.DEVICE_NAME, "未知设备");
                mChosenBTAddress = sharedPref.getString(MainActivity.DEVICE_ADDRESS, "");
            }

            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mChosenBTAddress);
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
//        getActivity().startService(gattServiceIntent);

        getContext().registerReceiver(mGattUpdateReceiver, gattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mChosenBTAddress);
            if (D) Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        setupRealtimeLineChart();

        setupBaseInfo();

        setHasOptionsMenu(true);
        return root;
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
        mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                BoxRequestProtocol.boxRequestDeviceParam(paramName, null));
    }

    @Override
    public void setDeviceParam(String paramName, String value) {
        mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                BoxRequestProtocol.boxRequestDeviceParam(paramName, value));
    }

    @Override
    public void requestDeviceStatus() {
        mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                BoxRequestProtocol.boxRequestStatus());
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

            realtimeLineChart.setVisibleXRangeMaximum(10);

            // this automatically refreshes the chart (calls invalidate())
            realtimeLineChart.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);
        }

        int lightIndex = Integer.valueOf(status);
        if (lightIndex >= 0 && lightIndex < 4) {
            dots.get(index).setImageDrawable(comfortableDrawables.get(lightIndex));
        }
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