package cn.sealiu.health.fixcriterion;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import cn.sealiu.health.R;
import cn.sealiu.health.profile.ProfileFragment;
import cn.sealiu.health.profile.ProfilePresenter;
import cn.sealiu.health.util.ActivityUtils;

public class FixCriterionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fixcriterion_act);

        // set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.fix_criterion);
        }

        FixCriterionFragment fixCriterionFragment =
                (FixCriterionFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fixCriterionFragment == null) {
            // create the fragment
            fixCriterionFragment = FixCriterionFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), fixCriterionFragment, R.id.contentFrame);
        }
        new FixCriterionPresenter(fixCriterionFragment);
    }
}
