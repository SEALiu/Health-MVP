package cn.sealiu.health.main;

import android.content.Intent;
import android.os.Bundle;
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
import cn.sealiu.health.fixcriterion.FixCriterionActivity;
import cn.sealiu.health.forum.ForumActivity;
import cn.sealiu.health.forum.ForumFragment;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.message.MessageActivity;
import cn.sealiu.health.profile.ProfileActivity;
import cn.sealiu.health.setting.SettingActivity;
import cn.sealiu.health.util.ActivityUtils;

public class MainActivity extends BaseActivity {

    private DoctorContract.Presenter mDoctorPresenter;
    private UserContract.Presenter mUserPresenter;
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

        // TODO: 2017/9/19 remove below fake info
        sharedPref.edit().putBoolean("user-login", true).apply();
        sharedPref.edit().putString("user-id", "0").apply();
        sharedPref.edit().putString("user-type", "1").apply();
        sharedPref.edit().putString("user-mid", "fake-mid").apply();
        sharedPref.edit().putString("user-mac", "fake-mac").apply();

        // TODO: 2017/9/19 change below code
        //sharedPref.edit().putBoolean("user-fixed", false).apply();
        if (!sharedPref.getBoolean("user-fixed", false)) {
            if (D) Log.e(TAG, "user never fix criterion");
            startActivity(new Intent(this, FixCriterionActivity.class));
            finish();
        }

        // check is logged in
        if (!sharedPref.getBoolean("user-login", false)) {
            if (D) Log.e(TAG, "user not logged in");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // check mid is exist?
        if (sharedPref.getString("user-mid", "").equals("") ||
                sharedPref.getString("user-mac", "").equals("")) {
            startActivity(new Intent(this, FindBluetoothActivity.class));
            finish();
        }

        // check logged user's identity and
        // create the presenter
        String identity = sharedPref.getString("user-type", "-1");
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
                mDoctorPresenter = new DoctorPresenter(doctorFragment);
                break;
            case IDENTITY_USER:
                HomeUserFragment userFragment =
                        (HomeUserFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
                if (userFragment == null) {
                    // create the fragment
                    userFragment = HomeUserFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(
                            getSupportFragmentManager(), userFragment, R.id.contentFrame);
                }
                mUserPresenter = new UserPresenter(userFragment);
                break;
            default:
                if (D) Log.e(TAG, "user identity error");
                sharedPref.edit().putBoolean("user-login", false).apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
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
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
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
