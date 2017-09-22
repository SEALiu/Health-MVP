package cn.sealiu.health.postdetail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.util.ActivityUtils;

public class PostDetailActivity extends BaseActivity {

    public static final String EXTRA_POST_ID = "POST_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postdetail_act);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.post_detail);
        }

        // Get the requested post id
        Intent intent = getIntent();
        String postId = intent.getStringExtra(EXTRA_POST_ID);
        Log.d(TAG, "post id: " + postId);

        PostDetailFragment postDetailFragment =
                (PostDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (postDetailFragment == null) {
            postDetailFragment = PostDetailFragment.newInstance(postId);
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), postDetailFragment, R.id.contentFrame);
        }

        new PostDetailPresenter(postDetailFragment, postId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavUtils.navigateUpFromSameTask(PostDetailActivity.this);
        return true;
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(PostDetailActivity.this);
    }
}
