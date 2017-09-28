package cn.sealiu.health.data.local;

import android.provider.BaseColumns;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public final class DataPersistenceContract {
    public DataPersistenceContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "data";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_MID = "mid";
        public static final String COLUMN_NAME_SEQUENCE = "sequence";
        public static final String COLUMN_NAME_AA = "aa";
        public static final String COLUMN_NAME_BB = "bb";
        public static final String COLUMN_NAME_CC = "cc";
        public static final String COLUMN_NAME_DD = "dd";
        public static final String COLUMN_NAME_TIME = "time";
    }
}
