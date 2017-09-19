package cn.sealiu.health.forum;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TimeUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.sealiu.health.BaseActivity;
import cn.sealiu.health.data.bean.Post;
import cn.sealiu.health.data.bean.PostListResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static cn.sealiu.health.BaseActivity.D;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class ForumPresenter implements ForumContract.Presenter {

    private static final String TAG = "ForumPresenter";

    private final ForumContract.View mForumView;

    public ForumPresenter(@NonNull ForumContract.View view) {
        mForumView = checkNotNull(view, "forumView cannot be null");
        mForumView.setPresenter(this);
    }

    @Override
    public void loadPosts(int startNum) {
        mForumView.setLoadingIndicator(true);

        OkHttpClient okHttpClient = new OkHttpClient();

        Request getPostListRequest = BaseActivity.buildHttpGetRequest(
                "/post/getPostList?startNumber=" + startNum + "&limitNumber=" +
                        ForumFragment.ITEM_PER_PAGE);
        okHttpClient.newCall(getPostListRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (D) Log.e(TAG, e.getMessage());
                mForumView.showError(e.getLocalizedMessage());
                mForumView.setLoadingIndicator(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (D) Log.e(TAG, json);

                PostListResponse postListResponse = new Gson().fromJson(json, PostListResponse.class);

                if (postListResponse.getPostsList().length == 0) {
                    if (!mForumView.isActive()){
                        return;
                    }
                    mForumView.showNoPost();
                    return;
                }

                List<Post> posts = new ArrayList<>();

                posts.addAll(Arrays.asList(postListResponse.getPostsList()));

                if (!mForumView.isActive()) {
                    return;
                }
                mForumView.setLoadingIndicator(false);
                mForumView.showPosts(posts);
            }
        });
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // add a post result
    }

    @Override
    public void addNewPost() {

    }

    @Override
    public void start() {
        loadPosts(ForumFragment.startNum);
    }
}