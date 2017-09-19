package cn.sealiu.health.forum;

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
import cn.sealiu.health.message.MessageActivity;
import cn.sealiu.health.profile.ProfileActivity;
import cn.sealiu.health.setting.SettingActivity;
import cn.sealiu.health.util.ActivityUtils;

public class ForumActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.title_forum);
        }

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        ForumFragment forumFragment =
                (ForumFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (forumFragment == null) {
            // Create the fragment
            forumFragment = ForumFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), forumFragment, R.id.contentFrame);
        }

        // Create the presenter
        new ForumPresenter(forumFragment);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.home_menu_item:
                                NavUtils.navigateUpFromSameTask(ForumActivity.this);
                                break;
                            case R.id.forum_menu_item:
                                // Do nothing, we're already on that screen
                                break;
                            case R.id.myself_menu_item:
                                Intent profileIntent =
                                        new Intent(ForumActivity.this, ProfileActivity.class);
                                startActivity(profileIntent);
                                finish();
                                break;
                            case R.id.message_menu_item:
                                Intent messageIntent =
                                        new Intent(ForumActivity.this, MessageActivity.class);
                                startActivity(messageIntent);
                                finish();
                                break;
                            case R.id.setting_menu_item:
                                Intent settingIntent =
                                        new Intent(ForumActivity.this, SettingActivity.class);
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
}
