package cn.sealiu.health.forum;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.Post;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;
import cn.sealiu.health.postdetail.PostDetailActivity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class ForumFragment extends Fragment implements ForumContract.View {

    public static final int ITEM_PER_PAGE = 500;
    public static int startNum = 0;

    private ForumContract.Presenter mPresenter;

    private PostAdapter mListAdapter;

    private View mNoPostsView;

    PostItemListener mItemListener = new PostItemListener() {
        @Override
        public void onPostClick(Post post) {
            if (post == null){
                showError(getString(R.string.request_post_cannot_be_null));
                return;
            }

            Intent intent = new Intent(getContext(), PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getP_id());
            startActivity(intent);
        }
    };

    public ForumFragment() {
    }

    public static ForumFragment newInstance(){
        return new ForumFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new PostAdapter(new ArrayList<Post>(0), mItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.forum_frag, container, false);

        ListView listView = root.findViewById(R.id.post_list);
        listView.setAdapter(mListAdapter);

        getActivity().findViewById(R.id.fab_add_post)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mPresenter.addNewPost();}
        });

        // set up no post view
        mNoPostsView = root.findViewById(R.id.no_post);

        ScrollChildSwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadPosts(startNum);
            }
        });

        //setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null)
            return;
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout = getView().findViewById(R.id.refresh_layout);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(active);
            }
        });
    }

    @Override
    public void showPosts(List<Post> posts) {
        mListAdapter.replaceData(posts);

        mNoPostsView.setVisibility(View.GONE);
    }

    @Override
    public void showNoPost() {
        mNoPostsView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInterfaceError() {
        checkNotNull(getView());
        Snackbar.make(getView(), "get post list interface error", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showError(String error) {
        showMessage(error);
    }

    @Override
    public void setPresenter(@NonNull ForumContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    private static class PostAdapter extends BaseAdapter {

        private List<Post> mPosts;
        private PostItemListener mItemListener;

        public PostAdapter(List<Post> posts, PostItemListener itemListener) {
            setList(posts);
            mItemListener = itemListener;
        }

        public void replaceData(List<Post> posts) {
            setList(posts);
            notifyDataSetChanged();
        }

        private void setList(List<Post> posts) {
            mPosts = checkNotNull(posts);
        }

        @Override
        public int getCount() {
            return mPosts.size();
        }

        @Override
        public Post getItem(int i) {
            return mPosts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.post_item, viewGroup, false);
            }

            final Post post = getItem(i);

            TextView titleTV = rowView.findViewById(R.id.title);
            TextView authorTV = rowView.findViewById(R.id.author_name);
            TextView timeTV = rowView.findViewById(R.id.time);

            titleTV.setText(post.getTitle());
            authorTV.setText(post.getUser().getUsername());
            timeTV.setText(post.getTime());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onPostClick(post);
                }
            });

            return rowView;
        }
    }

    public interface PostItemListener {

        void onPostClick(Post post);
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }
}
