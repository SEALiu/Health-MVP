package cn.sealiu.health.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.forum.ForumActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.message.MessageActivity;
import cn.sealiu.health.setting.SettingActivity;
import cn.sealiu.health.util.ActivityUtils;

public class ProfileActivity extends BaseActivity {
    public static final String PROFILE_SAVED = "profile_saved";
    public static final String PROFILE_USERNAME = "profile_username";
    public static final String PROFILE_GENDER = "profile_gender";
    public static final String PROFILE_AGE = "profile_age";
    public static final String PROFILE_PHONE = "profile_phone";
    public static final String PROFILE_EMAIL = "profile_email";
    public static final String PROFILE_MID = "profile_mid";

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_act);

        // set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.title_myself);
        }

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        User user = null;
        if (sharedPref.getBoolean(PROFILE_SAVED, true)) {
            String uid = sharedPref.getString(MainActivity.USER_ID, "");
            String type = sharedPref.getString(MainActivity.USER_TYPE, "-1");
            String username = sharedPref.getString(PROFILE_USERNAME, "");
            int gender = sharedPref.getInt(PROFILE_GENDER, -1);
            int age = sharedPref.getInt(PROFILE_AGE, -1);
            String phone = sharedPref.getString(PROFILE_PHONE, "");
            String email = sharedPref.getString(PROFILE_EMAIL, "");
            String mid = sharedPref.getString(PROFILE_MID, "");

            user = new User(null, username, gender, age, type, uid, true, phone,
                    email, null, mid);
        }

        ProfileFragment profileFragment =
                (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (profileFragment == null) {
            // create the fragment
            profileFragment = ProfileFragment.newInstance(user);
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), profileFragment, R.id.contentFrame);
        }
        new ProfilePresenter(profileFragment);
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
                                NavUtils.navigateUpFromSameTask(ProfileActivity.this);
                                break;
                            case R.id.forum_menu_item:
                                Intent forumIntent =
                                        new Intent(ProfileActivity.this, ForumActivity.class);
                                startActivity(forumIntent);
                                finish();
                                break;
                            case R.id.myself_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.message_menu_item:
                                Intent messageIntent =
                                        new Intent(ProfileActivity.this, MessageActivity.class);
                                startActivity(messageIntent);
                                finish();
                                break;
                            case R.id.setting_menu_item:
                                Intent settingIntent =
                                        new Intent(ProfileActivity.this, SettingActivity.class);
                                startActivity(settingIntent);
                                finish();
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
