package cn.sealiu.health.postdetail;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;

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
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.post_detail);
        }

        // Get the requested post id
        String postId = getIntent().getStringExtra(EXTRA_POST_ID);

        PostDetailFragment postDetailFragment =
                (PostDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (postDetailFragment == null) {
            postDetailFragment = PostDetailFragment.newInstance(postId);
        }
    }
}
