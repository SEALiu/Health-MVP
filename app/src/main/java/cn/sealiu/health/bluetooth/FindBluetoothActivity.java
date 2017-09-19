package cn.sealiu.health.bluetooth;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.util.ActivityUtils;

public class FindBluetoothActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_bluetooth_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.user_verification);
        }

        // create the fragment
        FindBluetoothFragment findBluetoothFragment = (FindBluetoothFragment)
                getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (findBluetoothFragment == null) {
            findBluetoothFragment = FindBluetoothFragment.newInstance();

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    findBluetoothFragment, R.id.contentFrame);
        }

        // create the presenter
        new FindBluetoothPresenter(findBluetoothFragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
