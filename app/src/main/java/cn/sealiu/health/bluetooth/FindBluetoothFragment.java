package cn.sealiu.health.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.sealiu.health.BluetoothLeService;
import cn.sealiu.health.R;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.util.BoxRequestProtocol;
import cn.sealiu.health.util.ProtocolMsg;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;
import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * functions:
 * 1. find bluetooth device，and connection.
 * 2. verify user.
 *
 * Created by liuyang
 * on 2017/9/14.
 */

public class FindBluetoothFragment extends Fragment
        implements FindBluetoothContract.View,
        View.OnClickListener {
    private static final int LOCATION_PERM = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final long SCAN_PERIOD = 8000;

    private FindBluetoothContract.Presenter mPresenter;

    private ListView mBluetoothListView;
    private EditText mMachineIdET;
    private AppCompatButton mVerifyBtn;
    private TextView mSearchBTHelpTV;
    private Handler mHandler;

    private BluetoothLeService mBluetoothLeService;
    private ServiceConnection mServiceConnection;
    BluetoothGattCharacteristic mWantedCharacteristic;
    private BTAdapter mBTAdapter;

    /**
     * -1: bluetooth isn't open
     * 0: disconnected
     * 1: searching
     * 2: connected
     */
    private int mConnected = BluetoothLeService.STATE_DISCONNECTED;

    private BluetoothAdapter mBluetoothAdapter;
    private String mChosenBTName, mChosenBTAddress;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int i, byte[] bytes) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBTAdapter.addDevice(device);
                    mBTAdapter.notifyDataSetChanged();
                }
            });
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

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = BluetoothLeService.STATE_CONNECTED;
                showMessage(getString(R.string.device_connected));
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = BluetoothLeService.STATE_DISCONNECTED;
                showMessage(getString(R.string.device_disconnected), R.string.reconnect,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                manualConnect();
                            }
                        });
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

                    mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                            BoxRequestProtocol.boxRequestCertification(""));
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                mPresenter.analyzeData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private BluetoothDeviceListener mBluetoothListener = new BluetoothDeviceListener() {
        @Override
        public void onDeviceClick(BluetoothDevice device) {
            scanLeDevice(false);

            mChosenBTName = device.getName();
            mChosenBTAddress = device.getAddress();

            if (D) Log.d(TAG, mChosenBTName + "||" + mChosenBTAddress);

            if (mConnected == BluetoothLeService.STATE_CONNECTED) {
                if (D) Log.d(TAG, "connected");
                if (mWantedCharacteristic != null) {
                    if (D) Log.d(TAG, "wanted characteristic is not null");
                    mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                            BoxRequestProtocol.boxRequestCertification(""));
                }
            } else {
                if (D) Log.d(TAG, "disconnected");
                if (mChosenBTAddress != null && !mChosenBTAddress.isEmpty()) {
                    mServiceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
                            if (!mBluetoothLeService.initialize()) {
                                if (D) Log.e(TAG, "Unable to initialize Bluetooth");
                                getActivity().finish();
                            }
                            // Automatically connects to the device upon successful start-up initialization.
                            mBluetoothLeService.connect(mChosenBTAddress);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {
                            mBluetoothLeService = null;
                        }
                    };

                    Intent gattServiceIntent = new Intent(getActivity(), BluetoothLeService.class);
                    getActivity()
                            .bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                    //getActivity().startService(gattServiceIntent);

                    getActivity().registerReceiver(mGattUpdateReceiver, gattUpdateIntentFilter());
                    if (mBluetoothLeService != null) {
                        final boolean result = mBluetoothLeService.connect(mChosenBTAddress);
                        if (D) Log.d(TAG, "Connect request result=" + result);
                    }
                }
            }
        }
    };

    public FindBluetoothFragment() {
    }

    public static FindBluetoothFragment newInstance() {
        return new FindBluetoothFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBTAdapter = new BTAdapter(mBluetoothListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.find_bluetooth_frag, container, false);
        mHandler = new Handler();
        setHasOptionsMenu(true);
        mVerifyBtn = root.findViewById(R.id.send_btn);
        mMachineIdET = root.findViewById(R.id.machine_id);
        mSearchBTHelpTV = root.findViewById(R.id.bluetooth_search_helps);
        mBluetoothListView = root.findViewById(R.id.bluetooth_list);

        mBluetoothListView.setAdapter(mBTAdapter);
        mMachineIdET.setVisibility(View.GONE);
        mVerifyBtn.setVisibility(View.GONE);
        mSearchBTHelpTV.setVisibility(View.GONE);

        mVerifyBtn.setOnClickListener(this);

        mBluetoothAdapter = ((BluetoothManager) getActivity()
                .getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        // check is support bluetooth AND bluetooth low energy AND bluetooth is open
        // if every below is ok, then open bluetooth and start scan ble device
        mPresenter.checkBluetoothSupport(getActivity());

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null) {
            getActivity().unbindService(mServiceConnection);
        }

        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            getActivity().unregisterReceiver(mGattUpdateReceiver);
        }
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showNoAvailableService() {
        showMessage(getString(R.string.bt_device_wrong));
    }

    @Override
    public void showInfo(int stringId) {
        showMessage(getString(stringId));
    }

    @Override
    public void showInfo(String errorMsg) {
        showMessage(errorMsg);
    }

    @Override
    public void setPresenter(FindBluetoothContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_btn:
                String mid = mMachineIdET.getText().toString();
                if (mid.length() != 8) {
                    showMessage(getString(R.string.mid_length_error));
                } else {
                    mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                            BoxRequestProtocol.boxRequestCertification(mid));
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.findbluetooth_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_bt:
                scanLeDevice(true);
                break;
        }
        return false;
    }

    // Location Permission is needed on Android M and above
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERM &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanLeDevice(true);
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
                    scanLeDevice(true);
                }
                break;
        }
    }

    @Override
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // no location permission
                    ActivityCompat.requestPermissions(
                            getActivity(),
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERM
                    );
                    return;
                } else {
                    // already have permission
                    if (!mPresenter.checkLocation(getActivity())) {
                        showMessage(getString(R.string.open_locating_info));
                        return;
                    }
                }
            }

            mBTAdapter.clear();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mSearchBTHelpTV.setText(R.string.searching);
            mSearchBTHelpTV.setVisibility(View.VISIBLE);

            // Stops scanning after SCAN_PERIOD ms.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(false);
                }
            }, SCAN_PERIOD);

        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mSearchBTHelpTV.setVisibility(View.GONE);

            if (mBTAdapter.getCount() == 0) {
                mSearchBTHelpTV.setText(R.string.none_device);
                mSearchBTHelpTV.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void bindWithMid() {
        mBluetoothListView.setVisibility(View.GONE);
        mMachineIdET.setVisibility(View.VISIBLE);
        mVerifyBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void requestCompleteMid() {
        mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                BoxRequestProtocol.boxRequestDeviceParam(ProtocolMsg.DEVICE_PARAM_HIGH_MID, null));
        mPresenter.doSentRequest(mWantedCharacteristic, mBluetoothLeService,
                BoxRequestProtocol.boxRequestDeviceParam(ProtocolMsg.DEVICE_PARAM_LOW_MID, null));
    }

    @Override
    public void gotoHome() {
        sharedPref.edit().putString(MainActivity.DEVICE_NAME, mChosenBTName).apply();
        sharedPref.edit().putString(MainActivity.DEVICE_ADDRESS, mChosenBTAddress).apply();

        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    @Override
    public void gotoLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
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

    public class BTAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private BluetoothDeviceListener mDeviceListener;

        BTAdapter(BluetoothDeviceListener listener) {
            super();
            mLeDevices = new ArrayList<>();
            mDeviceListener = listener;
        }

        void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public BluetoothDevice getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.bluetooth_item, viewGroup, false);
            }

            final BluetoothDevice device = getItem(i);
            TextView name = rowView.findViewById(R.id.device_name);
            TextView address = rowView.findViewById(R.id.device_address);

            name.setText(device.getName() == null ? getString(R.string.unknown_device) : device.getName());
            address.setText(device.getAddress());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDeviceListener.onDeviceClick(device);
                }
            });

            return rowView;
        }
    }

    interface BluetoothDeviceListener {
        void onDeviceClick(BluetoothDevice device);
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
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mChosenBTAddress);
            mConnected = BluetoothLeService.STATE_CONNECTING;
        }
    }
}