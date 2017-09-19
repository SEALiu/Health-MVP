package cn.sealiu.health.profile;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface ProfileContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();
        void showError();

    }

    interface Presenter extends BasePresenter {

    }
}
