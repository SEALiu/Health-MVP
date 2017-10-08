package cn.sealiu.health.data.response;

import cn.sealiu.health.data.bean.DataBean;

/**
 * Created by liuyang
 * on 2017/10/8.
 */

public class HistoryDataResponse {
    private int count;
    private DataBean[] HistoryData;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public DataBean[] getHistoryData() {
        return HistoryData;
    }

    public void setHistoryData(DataBean[] historyData) {
        HistoryData = historyData;
    }
}
