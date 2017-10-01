package cn.sealiu.health.message;

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
import cn.sealiu.health.data.bean.Message;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageFragment extends Fragment implements MessageContract.View {

    private MessageContract.Presenter mPresenter;
    private MessageAdapter mMsgAdapter;
    private View noMsgView;

    public MessageFragment() {
    }

    public static MessageFragment newInstance(){
        return new MessageFragment();
    }

    @Override
    public void setPresenter(@NonNull MessageContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMsgAdapter = new MessageAdapter(new ArrayList<Message>(0));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.message_frag, container, false);

        ListView listView = root.findViewById(R.id.message_list);
        noMsgView = root.findViewById(R.id.no_message);
        listView.setAdapter(mMsgAdapter);

        getActivity().findViewById(R.id.fab_add_message)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: 2017/10/1 群／单发消息
                    }
                });

        ScrollChildSwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadMessages();
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
    public void showNoMessage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noMsgView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showMessages(final List<Message> messages) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMsgAdapter.replaceData(messages);
                noMsgView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showInfo(String msg) {
        if (getView() != null)
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int strId) {
        showInfo(getString(strId));
    }

    @Override
    public void gotoLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private static class MessageAdapter extends BaseAdapter {

        private List<Message> mMessages;

        private MessageAdapter(List<Message> msgs) {
            mMessages = checkNotNull(msgs);
        }

        private void replaceData(List<Message> msgs) {
            mMessages = checkNotNull(msgs);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mMessages.size();
        }

        @Override
        public Message getItem(int i) {
            return mMessages.get(i);
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
                rowView = inflater.inflate(R.layout.message_item, viewGroup, false);
            }

            final Message msg = getItem(i);

            TextView contactNameTV = rowView.findViewById(R.id.contact_name);
            TextView content = rowView.findViewById(R.id.content);

            contactNameTV.setText(msg.getFromWho());
            content.setText(msg.getContent());

            return rowView;
        }
    }
}
