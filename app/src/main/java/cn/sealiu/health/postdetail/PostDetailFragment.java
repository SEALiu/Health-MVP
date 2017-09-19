package cn.sealiu.health.postdetail;

import android.support.v4.app.Fragment;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class PostDetailFragment extends Fragment implements PostDetailContract.View {



    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(String postId){
        return new PostDetailFragment();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setPresenter(PostDetailContract.Presenter presenter) {

    }
}
