package cn.sealiu.health.userdetail;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.User;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface UserDetailContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showUserDetail(User user);

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoLogin();
    }

    interface Presenter extends BasePresenter {
        void loadUserDetail(String userId);

        void doSentMsg(String toId, String content);
    }
}
