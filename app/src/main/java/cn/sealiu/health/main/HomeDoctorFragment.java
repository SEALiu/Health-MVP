package cn.sealiu.health.main;

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
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.userdetail.UserDetailActivity;

import static com.google.common.base.Preconditions.checkNotNull;

public class HomeDoctorFragment extends Fragment implements DoctorContract.View {

    private DoctorContract.Presenter mPresenter;

    private UserAdapter mUserAdapter;

    private View mNoUsersView;

    UserItemListener mUserItemListener = new UserItemListener() {
        @Override
        public void onUserClick(User user) {
            Intent intent = new Intent(getContext(), UserDetailActivity.class);
            intent.putExtra(UserDetailActivity.EXTRA_USER_ID, user.getId());

            startActivity(intent);
        }
    };

    public HomeDoctorFragment() {
    }

    public static HomeDoctorFragment newInstance(){
        return new HomeDoctorFragment();
    }

    @Override
    public void setPresenter(@NonNull DoctorContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_doctor_frag, container, false);

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

        //setHasOptionsMenu(true);

        return root;
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
    public void showUsers(final List<User> users) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserAdapter.replaceData(users);

                mNoUsersView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showNoUsers() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNoUsersView.setVisibility(View.VISIBLE);
            }
        });
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
    public void showInterfaceError() {
        checkNotNull(getView());
        Snackbar.make(getView(), "get bound user interface error", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private static class UserAdapter extends BaseAdapter{

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
            userNameTV.setText(user.getUsername());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserItemListener.onUserClick(user);
                }
            });

            return rowView;
        }
    }

    interface UserItemListener {
        void onUserClick(User user);
    }

    private void showMessage(String message) {
        checkNotNull(getView());
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }
}
