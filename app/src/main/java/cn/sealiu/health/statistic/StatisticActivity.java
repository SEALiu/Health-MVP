package cn.sealiu.health.statistic;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.util.ActivityUtils;

public class StatisticActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistic_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.statistic_title);
        }

        StatisticFragment statisticFragment =
                (StatisticFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (statisticFragment == null) {
            statisticFragment = StatisticFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), statisticFragment, R.id.contentFrame);
        }

        new StatisticPresenter(statisticFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //NavUtils.navigateUpFromSameTask(StatisticActivity.this);
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
