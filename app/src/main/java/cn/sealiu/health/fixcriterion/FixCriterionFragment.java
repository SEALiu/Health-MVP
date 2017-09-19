package cn.sealiu.health.fixcriterion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sealiu.health.R;
import cn.sealiu.health.login.LoginActivity;
import cn.sealiu.health.main.MainActivity;

import static cn.sealiu.health.BaseActivity.D;
import static cn.sealiu.health.BaseActivity.sharedPref;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/19.
 */

public class FixCriterionFragment extends Fragment implements FixCriterionContract.View, View.OnClickListener {
    private static final String TAG = "FixCriterionFragment";

    private FixCriterionContract.Presenter mPresenter;
    private RadioButton fixBlankRB, fixLooseRB, fixComfortRB, fixTightRB;
    private AppCompatButton skipButton;

    private List<RadioButton> fixRBs = new ArrayList<>();
    private List<TextView> fixResultTVs = new ArrayList<>();

    public FixCriterionFragment() {
    }

    public static FixCriterionFragment newInstance() {
        return new FixCriterionFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fixcriterion_frag, container, false);

        fixBlankRB = root.findViewById(R.id.fix_blank);
        fixLooseRB = root.findViewById(R.id.fix_loose);
        fixComfortRB = root.findViewById(R.id.fix_comfort);
        fixTightRB = root.findViewById(R.id.fix_tight);

        fixRBs.add(fixBlankRB);
        fixRBs.add(fixLooseRB);
        fixRBs.add(fixComfortRB);
        fixRBs.add(fixTightRB);

        TextView fixBlankResultTV = root.findViewById(R.id.fix_blank_result);
        TextView fixLooseResultTV = root.findViewById(R.id.fix_loose_result);
        TextView fixComfortResultTV = root.findViewById(R.id.fix_comfort_result);
        TextView fixTightResultTV = root.findViewById(R.id.fix_tight_result);

        fixResultTVs.add(fixBlankResultTV);
        fixResultTVs.add(fixLooseResultTV);
        fixResultTVs.add(fixComfortResultTV);
        fixResultTVs.add(fixTightResultTV);

        skipButton = root.findViewById(R.id.skip);
        AppCompatButton fixButton = root.findViewById(R.id.fix_criterion_btn);

        fixBlankRB.setOnClickListener(this);
        fixLooseRB.setOnClickListener(this);
        fixComfortRB.setOnClickListener(this);
        fixTightRB.setOnClickListener(this);
        fixButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);

        // init UI
        skipButton.setVisibility(View.GONE);

        mPresenter.loadFixInfo();
        return root;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showError(String msg) {
        if (getView() == null)
            return;
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showError(int strId) {
        showError(getString(strId));
    }

    @Override
    public void gotoLogin() {
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    @Override
    public void updateUI(int[] fixFlag) {
        if (fixFlag.length != 4) {
            if (D) Log.e(TAG, "func: updateUI(fixFlag) the size of elements in fixFlag is not 4");
            return;
        }

        int index = 0;
        boolean isSomeoneFixed = false;
        for (int flag : fixFlag) {
            if (flag == 0) { //unfixed
                fixResultTVs.get(index).setText(R.string.unfixed);
                fixRBs.get(index).setEnabled(true);
            } else { //fixed
                fixResultTVs.get(index).setText(R.string.fixed);
                fixRBs.get(index).setEnabled(false);
                isSomeoneFixed = true;
            }

            index++;
        }

        if (isSomeoneFixed)
            skipButton.setVisibility(View.VISIBLE);
        else
            skipButton.setVisibility(View.GONE);
    }

    @Override
    public void setPresenter(@NonNull FixCriterionContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fix_blank:
                resetAllRadioButton();
                fixBlankRB.setChecked(true);
                break;
            case R.id.fix_loose:
                resetAllRadioButton();
                fixLooseRB.setChecked(true);
                break;
            case R.id.fix_comfort:
                resetAllRadioButton();
                fixComfortRB.setChecked(true);
                break;
            case R.id.fix_tight:
                resetAllRadioButton();
                fixTightRB.setChecked(true);
                break;
            case R.id.skip:
                // TODO: 2017/9/19 remove one line code below
                sharedPref.edit().putBoolean("user-fixed", true).apply();

                getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
                break;
            case R.id.fix_criterion_btn:
                mPresenter.doSentRequest(null, null, "");
                break;
        }
    }

    private void resetAllRadioButton() {
        fixBlankRB.setChecked(false);
        fixLooseRB.setChecked(false);
        fixComfortRB.setChecked(false);
        fixTightRB.setChecked(false);
    }

}
