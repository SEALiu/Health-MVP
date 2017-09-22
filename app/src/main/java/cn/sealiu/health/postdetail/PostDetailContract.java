package cn.sealiu.health.postdetail;

import java.util.List;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;
import cn.sealiu.health.data.bean.Comment;
import cn.sealiu.health.data.bean.Post;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public interface PostDetailContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void setLoadingIndicator(boolean active);

        void showPostDetail(Post post);

        void showComments(List<Comment> comments);

        void showInfo(String msg);

        void showInfo(int strId);

        void gotoForumActivity();

        void showNoComments();
    }

    interface Presenter extends BasePresenter {
        void loadPostDetail(String postId);

        void loadComment(String postId, int startNum);

        void addComment(String comment);
    }
}
