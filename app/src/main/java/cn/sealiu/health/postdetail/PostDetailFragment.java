package cn.sealiu.health.postdetail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.Comment;
import cn.sealiu.health.data.bean.Post;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class PostDetailFragment extends Fragment implements PostDetailContract.View {
    private static final String ARGUMENT_POST_ID = "POST_ID";
    private PostDetailContract.Presenter mPresenter;
    private CommentAdapter mListAdapter;

    private String postId;

    private TextView titleTV, authorTV, timeTV, contentTV;
    private View noCommentView;

    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(String postId){
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_POST_ID, postId);
        PostDetailFragment fragment = new PostDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void setPresenter(PostDetailContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new CommentAdapter(new ArrayList<Comment>(0));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.postdetail_frag, container, false);

        titleTV = root.findViewById(R.id.post_title);
        authorTV = root.findViewById(R.id.author_name);
        timeTV = root.findViewById(R.id.time);
        contentTV = root.findViewById(R.id.content);

        ListView listView = root.findViewById(R.id.comment_list);
        listView.setAdapter(mListAdapter);

        getActivity().findViewById(R.id.fab_add_post)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onAddComment();
                    }
                });

        // set up no post view
        noCommentView = root.findViewById(R.id.no_comment);

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
                postId = getArguments().getString(ARGUMENT_POST_ID);
                mPresenter.loadPostDetail(postId);
            }
        });

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
    public void showPostDetail(final Post post) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titleTV.setText(post.getTitle());
                authorTV.setText(post.getUser().getUsername());
                timeTV.setText(post.getTime());
                contentTV.setText(post.getContent());
            }
        });
    }

    @Override
    public void showComments(final List<Comment> comments) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListAdapter.replaceData(comments);

                noCommentView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showInfo(String msg) {
        if (getView() == null) return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int strId) {
        showInfo(getString(strId));
    }

    @Override
    public void gotoForumActivity() {
        NavUtils.navigateUpFromSameTask(getActivity());
    }

    @Override
    public void showNoComments() {
        noCommentView.setVisibility(View.VISIBLE);
    }

    private void onAddComment() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.addpost_comment_dia, null);
        final EditText title = view.findViewById(R.id.title);
        final EditText content = view.findViewById(R.id.content);

        title.setVisibility(View.GONE);
        content.setHint(R.string.write_something);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.add_comment))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String contentStr = content.getText().toString();

                        if (contentStr.isEmpty()) {
                            showInfo(R.string.comment_content_empty);
                        } else {
                            mPresenter.addComment(contentStr);
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }

    private static class CommentAdapter extends BaseAdapter {

        private List<Comment> mComments;

        public CommentAdapter(List<Comment> comments) {
            setList(comments);
        }

        public void replaceData(List<Comment> comments) {
            setList(comments);
            notifyDataSetChanged();
        }

        private void setList(List<Comment> comments) {
            mComments = checkNotNull(comments);
        }

        @Override
        public int getCount() {
            return mComments.size();
        }

        @Override
        public Comment getItem(int i) {
            return mComments.get(i);
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
                rowView = inflater.inflate(R.layout.comment_item, viewGroup, false);
            }

            final Comment comment = getItem(i);

            TextView authorTV = rowView.findViewById(R.id.author_name);
            TextView timeTV = rowView.findViewById(R.id.time);
            TextView commentTV = rowView.findViewById(R.id.comment);

            authorTV.setText(comment.getUser().getUsername());
            timeTV.setText(comment.getTime());
            commentTV.setText(comment.getContent());

            return rowView;
        }
    }
}
