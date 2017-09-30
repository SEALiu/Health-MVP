package cn.sealiu.health.userdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.util.ActivityUtils;

public class UserDetailActivity extends BaseActivity {

    public static final String EXTRA_USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.patient_detail);
        }

        // Get the requested post id
        Intent intent = getIntent();
        String userId = intent.getStringExtra(EXTRA_USER_ID);
        Log.d(TAG, "user id: " + userId);

        UserDetailFragment userDetailFragment =
                (UserDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (userDetailFragment == null) {
            userDetailFragment = UserDetailFragment.newInstance(userId);
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), userDetailFragment, R.id.contentFrame);
        }

        new UserDetailPresenter(userDetailFragment, userId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavUtils.navigateUpFromSameTask(UserDetailActivity.this);
        return true;
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(UserDetailActivity.this);
    }
}
