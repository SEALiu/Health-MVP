package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/8/2.
 */

public class CommentsResponse {
    private Comment[] commentList;

    public Comment[] getCommentList() {
        return commentList;
    }

    public void setCommentList(Comment[] commentList) {
        this.commentList = commentList;
    }
}
