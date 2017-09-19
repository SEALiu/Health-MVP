package cn.sealiu.health.userdetail;

import android.os.Bundle;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;

public class UserDetailActivity extends BaseActivity {

    public static final String EXTRA_USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detail_act);
    }
}
