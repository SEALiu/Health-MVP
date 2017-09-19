package cn.sealiu.health.main;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.User;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface DoctorContract {
    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showUsers(List<User> users);

        void showNoUsers();

        void gotoLogin();

        void showInterfaceError();

        boolean isActive();
    }

    interface Presenter extends BasePresenter {
        void loadUsers();
    }
}
