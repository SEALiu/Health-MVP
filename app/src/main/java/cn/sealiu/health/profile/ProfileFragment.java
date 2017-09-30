package cn.sealiu.health.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import cn.sealiu.health.R;
import cn.sealiu.health.data.bean.User;
import cn.sealiu.health.main.MainActivity;
import cn.sealiu.health.main.ScrollChildSwipeRefreshLayout;

import static cn.sealiu.health.BaseActivity.IDENTITY_DOCTOR;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

public class ProfileFragment extends Fragment implements ProfileContract.View, View.OnClickListener {

    private static final String ARGUMENT_USER = "USER";
    private ProfileContract.Presenter mPresenter;
    private User user;
    private String userType;

    private TextView usernameTV, genderTV, ageTV, phoneTV, emailTV, midTV;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(@Nullable User user) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARGUMENT_USER, user);
        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setArguments(arguments);
        return profileFragment;
    }

    @Override
    public void setPresenter(@NonNull ProfileContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userType = sharedPref.getString(MainActivity.USER_TYPE, "-1");
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.profile_frag, container, false);

        usernameTV = root.findViewById(R.id.username);
        genderTV = root.findViewById(R.id.gender);
        ageTV = root.findViewById(R.id.age);
        phoneTV = root.findViewById(R.id.phone_number);
        emailTV = root.findViewById(R.id.email);
        midTV = root.findViewById(R.id.machine_id);

        if (userType.equals(IDENTITY_DOCTOR)) {
            root.findViewById(R.id.machine_id_holder).setVisibility(View.GONE);
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
                mPresenter.loadUserInfo();
            }
        });

        //setHasOptionsMenu(true);

        root.findViewById(R.id.change_username).setOnClickListener(this);
        root.findViewById(R.id.change_gender).setOnClickListener(this);
        root.findViewById(R.id.change_age).setOnClickListener(this);
        root.findViewById(R.id.change_phone).setOnClickListener(this);
        root.findViewById(R.id.change_email).setOnClickListener(this);
        root.findViewById(R.id.change_password).setOnClickListener(this);

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
    public void showUserInfo() {
        user = (User) getArguments().getSerializable(ARGUMENT_USER);
        if (user != null) {
            updateUserInfo(user);
        } else {
            mPresenter.loadUserInfo();
        }
    }

    @Override
    public void updateUserInfo(final User user) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usernameTV.setText(user.getUsername() == null ? getString(R.string.undefine) : user.getUsername());
                if (user.getGender() == null || user.getGender() == -1) {
                    genderTV.setText(R.string.undefine);
                } else if (user.getGender() == 0) {
                    genderTV.setText(R.string.female);
                } else if (user.getGender() == 1) {
                    genderTV.setText(R.string.male);
                }

                ageTV.setText(user.getAge() == null ? getString(R.string.undefine) : user.getAge());
                phoneTV.setText(user.getPhone() == null ? getString(R.string.undefine) : user.getPhone());
                emailTV.setText(user.getEmail() == null ? getString(R.string.undefine) : user.getEmail());
                midTV.setText(sharedPref.getString(MainActivity.DEVICE_MID, ""));
                midTV.setText(user.getMid());
            }
        });
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showInfo(String msg) {
        showMessage(msg);
    }

    @Override
    public void showInfo(int strId) {
        showMessage(getString(strId));
    }

    private void showMessage(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_username:
                onChangeUsername();
                break;
            case R.id.change_gender:
                onChangeUserGender();
                break;
            case R.id.change_age:
                onChangeUserAge();
                break;
            case R.id.change_phone:
                onChangePhone();
                break;
            case R.id.change_email:
                onChangeEmail();
                break;
            case R.id.change_password:
                onChangePassword();
                break;
        }
    }

    private void onChangeUserGender() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_gender_dia, null);

        final AppCompatRadioButton femaleRB = view.findViewById(R.id.gender_female);
        final AppCompatRadioButton maleRB = view.findViewById(R.id.gender_male);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.change_gender))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!femaleRB.isChecked() && !maleRB.isChecked()) {
                            showInfo(R.string.profile_unchanged);
                            dialogInterface.dismiss();
                        } else if (femaleRB.isChecked()) {
                            mPresenter.changeGender(0);
                            dialogInterface.dismiss();
                        } else {
                            mPresenter.changeGender(1);
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }

    private void onChangeUsername() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_dia, null);
        final EditText content = view.findViewById(R.id.content);
        content.setInputType(InputType.TYPE_CLASS_TEXT);
        content.setHint(R.string.username);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.change_name))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newName = content.getText().toString();
                        if (newName.isEmpty()) {
                            showInfo(R.string.profile_unchanged);
                            dialogInterface.dismiss();
                        } else {
                            mPresenter.changeUsername(newName);
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }

    private void onChangeUserAge() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_dia, null);
        final EditText content = view.findViewById(R.id.content);
        content.setInputType(InputType.TYPE_CLASS_NUMBER);
        content.setHint(R.string.age);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.change_age))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String ageStr = content.getText().toString();
                        if (ageStr.isEmpty()) {
                            showInfo(R.string.profile_unchanged);
                            dialogInterface.dismiss();
                        } else {
                            mPresenter.changeAge(Integer.valueOf(ageStr));
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }

    private void onChangePhone() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_dia, null);
        final EditText content = view.findViewById(R.id.content);
        content.setInputType(InputType.TYPE_CLASS_PHONE);
        content.setHint(R.string.phone_number);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.change_phone))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPhone = content.getText().toString();
                        if (newPhone.isEmpty()) {
                            showInfo(R.string.profile_unchanged);
                            dialogInterface.dismiss();
                        } else {
                            mPresenter.changePhone(newPhone);
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }

    private void onChangeEmail() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_dia, null);
        final EditText content = view.findViewById(R.id.content);
        content.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        content.setHint(R.string.email);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.change_email))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = content.getText().toString();
                        if (email.isEmpty()) {
                            showInfo(R.string.profile_unchanged);
                            dialogInterface.dismiss();
                        } else {
                            mPresenter.changeEmail(email);
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }

    private void onChangePassword() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_password_dia, null);

        //final EditText content = view.findViewById(R.id.content);
        final EditText oldPwd = view.findViewById(R.id.old_password);
        final EditText newPwd = view.findViewById(R.id.new_password);
        final EditText confirmPwd = view.findViewById(R.id.confirm_new_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setCancelable(false)
                .setTitle(getString(R.string.change_password))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String old = oldPwd.getText().toString();
                        String pwd = newPwd.getText().toString();
                        String confirm = confirmPwd.getText().toString();


                        if (old.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
                            showInfo(R.string.profile_unchanged);
                            dialogInterface.dismiss();
                        } else if (!pwd.equals(confirm)) {
                            confirmPwd.setText("");
                            showInfo(R.string.re_password_not_same);
                        } else {
                            mPresenter.changePassword(old, pwd);
                            dialogInterface.dismiss();
                        }
                    }
                });
        builder.show();
    }
}
