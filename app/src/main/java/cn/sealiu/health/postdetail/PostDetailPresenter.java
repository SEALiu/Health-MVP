package cn.sealiu.health.postdetail;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.BaseResponse;
import cn.sealiu.health.data.bean.Comment;
import cn.sealiu.health.data.bean.CommentsResponse;
import cn.sealiu.health.data.bean.Post;
import cn.sealiu.health.data.bean.PostResponse;
import cn.sealiu.health.data.bean.PostsDetailResponse;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.main.MainActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static cn.sealiu.health.forum.ForumFragment.ITEM_PER_PAGE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/22.
 */

public class PostDetailPresenter implements PostDetailContract.Presenter {
    private static final String TAG = "PostDetailPresenter";

    private final PostDetailContract.View mPostDetailView;
    private final String mPostId;

    public PostDetailPresenter(@NonNull PostDetailContract.View postDetailView, @NonNull String postId) {
        mPostDetailView = checkNotNull(postDetailView);
        mPostDetailView.setPresenter(this);
        mPostId = checkNotNull(postId);
    }

    @Override
    public void start() {
        loadPostDetail(mPostId);
    }

    @Override
    public void loadPostDetail(String postId) {
        mPostDetailView.setLoadingIndicator(true);
        OkHttpClient okHttpClient = new OkHttpClient();

        Request detailRequest = BaseActivity.buildHttpGetRequest("/post/getPostDetail?" +
                "id=" + postId);
        okHttpClient.newCall(detailRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mPostDetailView.showInfo("get post detail interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                PostsDetailResponse postsDetailResponse =
                        new Gson().fromJson(json, PostsDetailResponse.class);

                if (postsDetailResponse.getPostsDetail().length == 0) {
                    mPostDetailView.gotoForumActivity();
                } else {
                    PostResponse postResponse = postsDetailResponse.getPostsDetail()[0];

                    int pid = postResponse.getP_id();
                    String title = postResponse.getTitle();
                    int authorId = postResponse.getAuthor_id();
                    String content = postResponse.getContent();
                    String image = postResponse.getImage();
                    int targetId = postResponse.getTarget_id();
                    String time = postResponse.getTime();
                    int typeId = postResponse.getType_id();
                    User user = postResponse.getUser();

                    Post post = new Post(pid, title, authorId, content, image, targetId, time, typeId,
                            user);

                    mPostDetailView.showPostDetail(post);
                }
            }
        });

        loadComment(postId, 0);

        mPostDetailView.setLoadingIndicator(false);
    }

    @Override
    public void loadComment(String postId, int startNum) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request getCommentsRequest = BaseActivity.buildHttpGetRequest("/post/getCommentList?" +
                "id=" + postId + "&" +
                "startNumber=" + startNum + "&" +
                "limitNumber=" + ITEM_PER_PAGE);

        Log.e(BaseActivity.TAG, getCommentsRequest.url().toString());

        okHttpClient.newCall(getCommentsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(BaseActivity.TAG, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultJson = response.body().string();

                if (D) Log.e(BaseActivity.TAG, resultJson);

                CommentsResponse commentsResponse = new Gson().fromJson(resultJson, CommentsResponse.class);
                List<Comment> comments = new ArrayList<>();
                comments.addAll(Arrays.asList(commentsResponse.getCommentList()));

                if (comments.size() == 0) {
                    mPostDetailView.showNoComments();
                    return;
                }

                mPostDetailView.showComments(comments);
            }
        });
    }

    @Override
    public void addComment(String comment) {
        String userId = sharedPref.getString(MainActivity.USER_ID, "");

        OkHttpClient okHttpClient = new OkHttpClient();
        Request sendCommentRequest = BaseActivity.buildHttpGetRequest("/post/sendPosts?" +
                "author_id=" + userId + "&" +
                "content=" + comment + "&" +
                "target_id=" + mPostId + "&" +
                "type_id=1");

        okHttpClient.newCall(sendCommentRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getLocalizedMessage());
                mPostDetailView.showInfo("send post/'comment' interface error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultJson = response.body().string();

                BaseResponse base = new Gson().fromJson(resultJson, BaseResponse.class);
                if (base.getStatus().equals("200")) {
                    loadComment(mPostId, 0);
                } else {
                    mPostDetailView.showInfo(R.string.add_comment_error);
                }
            }
        });
    }
}
