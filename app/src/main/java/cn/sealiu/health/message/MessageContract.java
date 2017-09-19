package cn.sealiu.health.message;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.Message;
import cn.sealiu.health.data.bean.Post;

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

        void showInterfaceError();

        void showError(String error);

    }

    interface Presenter extends BasePresenter {
        void loadMessages();
    }
}
