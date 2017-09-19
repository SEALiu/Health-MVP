package cn.sealiu.health.register;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.login.LoginContract;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface RegisterContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showNoNetWorkError();

        void showPhoneEmptyError();

        void showPwdEmptyError();

        void showRePwdEmptyError();

        void showCodeEmptyError();

        void showPhoneFormatError();

        void showPwdFormatError();

        void showCodeFormatError();

        void showRePwdError();

        void showPhoneRegistered();

        void showRegisterError(String reason);

        void showRegisterSuccess();

        void gotoHome();

        void gotoFindBluetooth();
    }

    interface Presenter extends BasePresenter {
        void register(String phone, String pwd, String rePwd, boolean isDoctor, String code);
    }
}
