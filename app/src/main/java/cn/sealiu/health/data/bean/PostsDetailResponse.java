package cn.sealiu.health.data.bean;

/**
 * Created by liuyang
 * on 2017/9/22.
 */

public class PostsDetailResponse {
    private PostResponse[] PostsDetail;

    public PostResponse[] getPostsDetail() {
        return PostsDetail;
    }

    public void setPostsDetail(PostResponse[] postsDetail) {
        PostsDetail = postsDetail;
    }
}
