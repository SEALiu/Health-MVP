package cn.sealiu.health.message;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.Message;
import cn.sealiu.health.data.bean.Post;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;
import cn.sealiu.health.profile.ProfileContract;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageFragment extends Fragment implements MessageContract.View {

    private MessageContract.Presenter mPresenter;

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

//        ListView listView = root.findViewById(R.id.patient_list);
//        mNoUsersView = root.findViewById(R.id.no_patient);
//        listView.setAdapter(mUserAdapter);
//
        ScrollChildSwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        //swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadMessages();
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
    public void showNoMessage() {

    }

    @Override
    public void showMessages(List<Message> messages) {

    }

    @Override
    public void showInterfaceError() {

    }

    @Override
    public void showError(String error) {

    }
}
