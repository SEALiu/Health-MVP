package cn.sealiu.health.statistic;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public interface StatisticContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();

        void showInfo(String msg);

        void showInfo(int strId);
    }

    interface Presenter extends BasePresenter {

    }
}
