package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class PostListResponse {
    private String status;
    private Post[] PostsList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Post[] getPostsList() {
        return PostsList;
    }

    public void setPostsList(Post[] postsList) {
        PostsList = postsList;
    }
}
