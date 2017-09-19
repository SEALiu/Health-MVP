package cn.sealiu.health.profile;

import android.support.annotation.NonNull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class ProfilePresenter implements ProfileContract.Presenter {

    private final ProfileContract.View mProfileView;

    @Override
    public void start() {

    }

    public ProfilePresenter(@NonNull ProfileContract.View profileView) {
        mProfileView = checkNotNull(profileView);
        mProfileView.setPresenter(this);
    }
}
