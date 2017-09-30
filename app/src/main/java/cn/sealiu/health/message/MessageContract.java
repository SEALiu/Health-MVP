package cn.sealiu.health.message;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.Message;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public interface MessageContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void setLoadingIndicator(boolean active);

        void showNoMessage();

        void showMessages(List<Message> messages);

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoLogin();
    }

    interface Presenter extends BasePresenter {
        void loadMessages();
    }
}
