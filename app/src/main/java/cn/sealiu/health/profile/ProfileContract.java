package cn.sealiu.health.profile;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.User;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface ProfileContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showInfo(String msg);

        void showInfo(int strId);

        void setLoadingIndicator(boolean active);

        void updateUserInfo(User user);

        void showUserInfo();
    }

    interface Presenter extends BasePresenter {
        void loadUserInfo();

        void changeUsername(String name);

        void changeGender(int gender);

        void changeAge(int age);

        void changePhone(String phone);

        void changeEmail(String email);

        void changePassword(String oldPwd, String newPwd);
    }
}
