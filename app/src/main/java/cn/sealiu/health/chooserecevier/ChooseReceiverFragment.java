package cn.sealiu.health.chooserecevier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/10/8.
 */

public class ChooseReceiverFragment extends Fragment implements ChooseReceiverContract.View {
    private final static String TAG = "ChooseReceiverFragment";

    private OnFragmentInteractionListener listener;
    private ChooseReceiverContract.Presenter mPresenter;
    private UserAdapter mUserAdapter;
    private View mNoUsersView;

    private List<User> receivers = new ArrayList<>();

    UserItemListener mUserItemListener = new UserItemListener() {
        @Override
        public void onUserChecked(User user, boolean isChecked) {
            if (isChecked) {
                receivers.add(user);
            } else {
                receivers.remove(user);
            }
        }
    };

    public ChooseReceiverFragment() {
    }

    public static ChooseReceiverFragment newInstance() {
        return new ChooseReceiverFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Log.e(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserAdapter = new UserAdapter(new ArrayList<User>(0), mUserItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.choose_recevier_frag, container, false);

        ListView listView = root.findViewById(R.id.patient_list);
        mNoUsersView = root.findViewById(R.id.no_patient);
        listView.setAdapter(mUserAdapter);

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
                mPresenter.loadUsers();
            }
        });

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.choose_receiver_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ok:
                listener.onSelected(receivers);
                return true;
            default:
                return false;
        }
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
    public void showInfo(String msg) {
        checkNotNull(getView());
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
    public void showNoUsers() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNoUsersView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void showUsers(final List<User> users) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserAdapter.replaceData(users);
                mNoUsersView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void setPresenter(@NonNull ChooseReceiverContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    private static class UserAdapter extends BaseAdapter {

        private List<User> mUsers;
        private UserItemListener mUserItemListener;

        private UserAdapter(List<User> users, UserItemListener listener) {
            mUsers = checkNotNull(users);
            mUserItemListener = listener;
        }

        private void replaceData(List<User> users) {
            mUsers = checkNotNull(users);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public User getItem(int i) {
            return mUsers.get(i);
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
                rowView = inflater.inflate(R.layout.user_item, viewGroup, false);
            }

            final User user = getItem(i);

            TextView userNameTV = rowView.findViewById(R.id.username);
            final CheckBox checkBox = rowView.findViewById(R.id.checkbox);
            checkBox.setVisibility(View.VISIBLE);

            String username = user.getUsername() == null ? "未命名" : user.getUsername();
            userNameTV.setText(username);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserItemListener.onUserChecked(user, checkBox.isChecked());
                }
            });

            return rowView;
        }
    }

    interface UserItemListener {
        void onUserChecked(User user, boolean isChecked);
    }

    public interface OnFragmentInteractionListener {
        void onSelected(List<User> users);
    }
}
