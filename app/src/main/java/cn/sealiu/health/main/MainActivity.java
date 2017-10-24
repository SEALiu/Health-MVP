package cn.sealiu.health.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.bluetooth.FindBluetoothActivity;
import cn.sealiu.health.forum.ForumActivity;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.message.MessageActivity;
import cn.sealiu.health.profile.ProfileActivity;
import cn.sealiu.health.setting.SettingActivity;
import cn.sealiu.health.util.ActivityUtils;

public class MainActivity extends BaseActivity {
    public static final String USER_LOGIN = "user_login";
    public static final String USER_UID = "user_uid";
    public static final String USER_ID = "user_id";
    public static final String USER_TYPE = "user_type";

    public static final String FIX_CRITERION_BLANK = "fix_blank";
    public static final String FIX_CRITERION_LOOSE = "fix_loose";
    public static final String FIX_CRITERION_COMFORT = "fix_comfort";
    public static final String FIX_CRITERION_TIGHT = "fix_tight";

    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DEVICE_ENABLE_DATE = "device_enable_date";
    public static final String DEVICE_SLOPE = "device_slope";
    public static final String DEVICE_OFFSET = "device_offset";
    public static final String DEVICE_CHANNEL_NUM = "device_channel_num";
    public static final String DEVICE_CHANNEL_ONE = "device_channel_one";
    public static final String DEVICE_CHANNEL_TWO = "device_channel_two";
    public static final String DEVICE_CHANNEL_THREE = "device_channel_three";
    public static final String DEVICE_CHANNEL_FOUR = "device_channel_four";
    public static final String DEVICE_SAMPLING_FREQUENCY = "device_sampling_frequency";
    public static final String DEVICE_MID = "device_mid";
    public static final String DEVICE_COMPLETED_MID = "device_completed_mid";
    public static final String DEVICE_POWER = "device_power";
    public static final String DEVICE_STORAGE = "device_storage";
    public static final String DEVICE_TIME = "device_time";
    public static final String DEVICE_START_USING_DATE = "device_start_using_date";
    public static final String DEVICE_COMFORT_A = "COMFORT_A";
    public static final String DEVICE_COMFORT_B = "COMFORT_B";
    public static final String DEVICE_COMFORT_C = "COMFORT_C";
    public static final String DEVICE_COMFORT_D = "COMFORT_D";

    public static final String NETWORK_ONLY_WIFI = "network_only_wifi";

    public static final String HISTORY_DATA_SYNC_DATE = "history_data_sync_date";

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_act);

        // set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        if (!sharedPref.getBoolean(USER_LOGIN, false)) {
            if (D) Log.e(TAG, "user not logged in");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
//        else if (!(sharedPref.getBoolean(MainActivity.FIX_CRITERION_BLANK, false) ||
//                sharedPref.getBoolean(MainActivity.FIX_CRITERION_LOOSE, false) ||
//                sharedPref.getBoolean(MainActivity.FIX_CRITERION_COMFORT, false) ||
//                sharedPref.getBoolean(MainActivity.FIX_CRITERION_TIGHT, false))) {
//
//            if (D) Log.d(TAG, "user never fix criterion");
//            startActivity(new Intent(this, FixCriterionActivity.class));
//            finish();
//        }

        // check logged user's identity and
        // create the presenter
        String identity = sharedPref.getString(USER_TYPE, "-1");

        if (D) Log.d(TAG, identity);

        switch (identity) {
            case IDENTITY_DOCTOR:
                HomeDoctorFragment doctorFragment =
                        (HomeDoctorFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                if (doctorFragment == null) {
                    // create the fragment
                    doctorFragment = HomeDoctorFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(), doctorFragment, R.id.contentFrame);
                }
                new DoctorPresenter(doctorFragment);
                break;
            case IDENTITY_USER:

                if (sharedPref.getString(DEVICE_MID, "").equals("") ||
                        sharedPref.getString(DEVICE_ADDRESS, "").equals("")) {
                    startActivity(new Intent(this, FindBluetoothActivity.class));
                    finish();
                }

                HomeUserFragment userFragment =
                        (HomeUserFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                if (userFragment == null) {
                    // create the fragment
                    userFragment = HomeUserFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(), userFragment, R.id.contentFrame);
                }
                new UserPresenter(userFragment);
                break;
            default:
                if (D) Log.e(TAG, "user identity error");
                sharedPref.edit().putBoolean(USER_LOGIN, false).apply();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.home_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.forum_menu_item:
                                Intent forumIntent =
                                        new Intent(MainActivity.this, ForumActivity.class);
                                startActivity(forumIntent);
                                break;
                            case R.id.myself_menu_item:
                                Intent profileIntent =
                                        new Intent(MainActivity.this, ProfileActivity.class);
                                startActivity(profileIntent);
                                break;
                            case R.id.message_menu_item:
                                Intent messageIntent =
                                        new Intent(MainActivity.this, MessageActivity.class);
                                startActivity(messageIntent);
                                break;
                            case R.id.setting_menu_item:
                                Intent settingIntent =
                                        new Intent(MainActivity.this, SettingActivity.class);
                                startActivity(settingIntent);
                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
}
