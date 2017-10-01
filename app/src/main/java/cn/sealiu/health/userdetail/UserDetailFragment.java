package cn.sealiu.health.userdetail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.login.LoginActivity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class UserDetailFragment extends Fragment implements UserDetailContract.View, View.OnClickListener {
    private static final String TAG = "UserDetailFragment";

    private static final String ARGUMENT_USER_ID = "USER_ID";
    private UserDetailContract.Presenter mPresenter;
    private String userId, userName;

    private TextView baseInfoTV, phoneTV, emailTV;
    private AppCompatButton chooseStatisticBtn;
    private BarChart statisticBarChart;

    public UserDetailFragment() {
    }

    public static UserDetailFragment newInstance(String userId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_USER_ID, userId);
        UserDetailFragment fragment = new UserDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void setPresenter(UserDetailContract.Presenter presenter) {
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
        View root = inflater.inflate(R.layout.user_detail_frag, container, false);

        baseInfoTV = root.findViewById(R.id.base_info);
        phoneTV = root.findViewById(R.id.phone_number);
        emailTV = root.findViewById(R.id.email);
        chooseStatisticBtn = root.findViewById(R.id.choose_statistic);
        statisticBarChart = root.findViewById(R.id.statistic_barchart);

        chooseStatisticBtn.setOnClickListener(this);

        userId = getArguments().getString(ARGUMENT_USER_ID);
        Log.d(TAG, "userId: " + userId);
        //mPresenter.loadUserDetail(userId);

        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.userdetail_frag_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_msg:
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.send_msg_dia, null);
                TextView receiversTV = dialogView.findViewById(R.id.receivers);
                final EditText content = dialogView.findViewById(R.id.content);

                userName = userName == null ? "未命名" : userName;
                receiversTV.setText(String.format("收件人: %s", userName));

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
                                    mPresenter.doSentMsg(userId, contentStr);
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                builder.show();
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
    public void showUserDetail(final User user) {
        userName = user.getUsername();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (user.getGender() != null) {
                    String gender = user.getGender() == 1 ? "男" : "女";

                    baseInfoTV.setText(String.format(getString(R.string.base_info),
                            user.getUsername(),
                            gender,
                            user.getAge()));

                    phoneTV.setText(user.getPhone());
                    emailTV.setText(user.getEmail());
                }
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
    public void gotoLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_statistic:
                PopupMenu popupMenu = new PopupMenu(getActivity(), chooseStatisticBtn);
                popupMenu.getMenuInflater().inflate(R.menu.statistic_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.day_statistic:
                                return true;
                            case R.id.week_statistic:
                                return true;
                            case R.id.month_statistic:
                                return true;
                            case R.id.year_statistic:
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                break;
        }
    }
}
