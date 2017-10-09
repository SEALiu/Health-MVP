package cn.sealiu.health.chooserecevier;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.User;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface ChooseReceiverContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void setLoadingIndicator(boolean active);

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoLogin();

        void showNoUsers();

        void showUsers(List<User> users);
    }

    interface Presenter extends BasePresenter {
        void loadUsers();
    }
}
