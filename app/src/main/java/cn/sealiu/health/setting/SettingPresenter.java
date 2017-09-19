package cn.sealiu.health.setting;

import android.support.annotation.NonNull;

import cn.sealiu.health.profile.ProfileContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class SettingPresenter implements SettingContract.Presenter {

    private final SettingContract.View mSettingView;

    @Override
    public void start() {

    }

    public SettingPresenter(@NonNull SettingContract.View settingView) {
        mSettingView = checkNotNull(settingView);
        mSettingView.setPresenter(this);
    }
}
