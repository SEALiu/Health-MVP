package cn.sealiu.health.message;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import cn.sealiu.health.chooserecevier.ChooseReceiverActivity;
import cn.sealiu.health.data.bean.Message;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;

import static android.app.Activity.RESULT_OK;
import static cn.sealiu.health.BaseActivity.IDENTITY_DOCTOR;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

public class MessageFragment extends Fragment implements MessageContract.View {

    private final static String TAG = "MessageFragment";
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

        FloatingActionButton fabAddMessage = getActivity().findViewById(R.id.fab_add_message);
        fabAddMessage.setVisibility(View.GONE);

        String userType = sharedPref.getString(MainActivity.USER_TYPE, "-1");
        if (userType.equals(IDENTITY_DOCTOR)) {

            fabAddMessage.setVisibility(View.VISIBLE);
            fabAddMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 选择收件人
                    Intent intent = new Intent(getActivity(), ChooseReceiverActivity.class);
                    startActivityForResult(intent, MessageActivity.REQUEST_RECEIVER);
                }
            });
        }

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
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noMsgView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showMessages(final List<Message> messages) {
        if (getActivity() == null) return;
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
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MessageActivity.REQUEST_RECEIVER) {
            if (resultCode == RESULT_OK) {
                final String[] ids = data.getStringArrayExtra("ids");
                String[] names = data.getStringArrayExtra("names");

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.send_msg_dia, null);
                TextView receiversTV = dialogView.findViewById(R.id.receivers);
                final EditText content = dialogView.findViewById(R.id.content);

                StringBuilder receivers = new StringBuilder();
                for (String name : names) {
                    receivers.append(name).append(" ");
                }
                receiversTV.setText(String.format("收件人: %s", receivers));

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(getString(R.string.send_msg))
                        .setView(dialogView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String contentStr = content.getText().toString();
                                if (contentStr.isEmpty()) {
                                    showInfo(R.string.message_empty);
                                    dialogInterface.dismiss();
                                } else {
                                    mPresenter.doSentMsg(ids, contentStr);
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                builder.show();

            } else {
                showInfo("未选择收件人");
            }
        }
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

            contactNameTV.setText(msg.getFromWho() == null ? "未命名" : msg.getFromWho());
            content.setText(msg.getContent());

            return rowView;
        }
    }
}
