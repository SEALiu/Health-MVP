package cn.sealiu.health.setting;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.Post;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface SettingContract {

    interface View extends BaseView<Presenter>{
        boolean isActive();

        void setLoadingIndicator(boolean active);

        void showInterfaceError();

        void showError(String error);
    }

    interface Presenter extends BasePresenter {

    }
}
