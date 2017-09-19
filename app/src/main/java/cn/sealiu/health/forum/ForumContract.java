package cn.sealiu.health.forum;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.Post;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface ForumContract {

    interface View extends BaseView<Presenter>{
        boolean isActive();

        void setLoadingIndicator(boolean active);

        void showNoPost();

        void showPosts(List<Post> posts);

        void showInterfaceError();

        void showError(String error);
    }

    interface Presenter extends BasePresenter {
        void loadPosts(int startNum);

        void result(int requestCode, int resultCode);

        void addNewPost();
    }
}
