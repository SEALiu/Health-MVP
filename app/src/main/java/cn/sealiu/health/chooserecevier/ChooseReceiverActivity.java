package cn.sealiu.health.chooserecevier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.util.ActivityUtils;

public class ChooseReceiverActivity extends BaseActivity implements
        ChooseReceiverFragment.OnFragmentInteractionListener {

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
            ab.setTitle(R.string.choose_receiver);
        }

        ChooseReceiverFragment chooseReceiverFragment =
                (ChooseReceiverFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (chooseReceiverFragment == null) {
            chooseReceiverFragment = ChooseReceiverFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), chooseReceiverFragment, R.id.contentFrame);
        }

        new ChooseReceiverPresenter(chooseReceiverFragment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavUtils.navigateUpFromSameTask(ChooseReceiverActivity.this);
        setResult(Activity.RESULT_CANCELED);
        return true;
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(ChooseReceiverActivity.this);
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public void onSelected(List<User> receivers) {

        if (receivers.size() == 0) {
            setResult(Activity.RESULT_CANCELED);
        } else {
            String[] ids = new String[receivers.size()];
            String[] names = new String[receivers.size()];

            for (int i = 0; i < receivers.size(); i++) {
                ids[i] = receivers.get(i).getId();
                names[i] = receivers.get(i).getUsername() == null ? "未命名" : receivers.get(i).getUsername();
            }

            Intent intent = new Intent();
            intent.putExtra("ids", ids);
            intent.putExtra("names", names);

            setResult(Activity.RESULT_OK, intent);
        }

        finish();
    }
}
