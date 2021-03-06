package cn.sealiu.health.data.local;

import android.provider.BaseColumns;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public final class DataStatusPersistenceContract {
    public DataStatusPersistenceContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class DataStatusEntry implements BaseColumns {
        public static final String TABLE_NAME = "datastatus";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TIME = "time";
        //status = 1: 已请求，本地保存
        //status = 2: 已请求，设备无数据
        //status = 3: 未请求
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_SYNC = "sync";
    }
}
