package cn.sealiu.health.statistic;

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public class StatisticPresenter implements StatisticContract.Presenter {

    private final StatisticContract.View mStatisticView;

    @Override
    public void start() {

    }

    public StatisticPresenter(@NonNull StatisticContract.View view) {
        mStatisticView = checkNotNull(view);
        mStatisticView.setPresenter(this);
    }
}
