package cn.sealiu.health.data.response;

import cn.sealiu.health.data.bean.Responsible;

/**
 * Created by liuyang
 * on 2017/7/24.
 */

public class ResponsibleResponse {
    private Responsible[] BondUser;

    public Responsible[] getBondUser() {
        return BondUser;
    }

    public void setBondUser(Responsible[] bondUser) {
        BondUser = bondUser;
    }
}
