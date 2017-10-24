package cn.sealiu.health.login;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface LoginContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showNoNetWorkError();

        void showPhoneEmptyError();

        void showPwdEmptyError();

        void showLoginError(String reason);

        void showLoginSuccess();

        void restorePhone(String phone);

        void restorePwd(String pwd);

        void gotoFindBluetooth();

        void gotoHome();

        void showInfo(String msg);
    }

    interface Presenter extends BasePresenter {
        void restore();

        void login(String phone, String pwd, boolean isRemember);

        void offlineMode();

        void setCustomIp(String ip);
    }
}
