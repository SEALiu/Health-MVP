package cn.sealiu.health.postdetail;

import cn.sealiu.health.BasePresenter;
import cn.sealiu.health.BaseView;

/**
 * Created by liuyang
 * on 2017/9/18.
 */

public class PostDetailContract {
    interface View extends BaseView<Presenter> {
        boolean isActive();
    }

    interface Presenter extends BasePresenter {

    }
}
