package cn.sealiu.health.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.sealiu.health.data.local.DataPersistenceContract.DataEntry;
import cn.sealiu.health.data.local.DataStatusPersistenceContract.DataStatusEntry;

/**
 * Created by liuyang
 * on 2017/9/26.
 */

public class HealthDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Health.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String BOOLEAN_TYPE = " INTEGER";

    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_DATA_ENTRIES =
            "CREATE TABLE " + DataPersistenceContract.DataEntry.TABLE_NAME + " (" +
                    DataEntry.COLUMN_NAME_ID + TEXT_TYPE + " PRIMARY KEY," +
                    DataEntry.COLUMN_NAME_MID + TEXT_TYPE + COMMA_SEP +
                    DataEntry.COLUMN_NAME_SEQUENCE + TEXT_TYPE + COMMA_SEP +
                    DataEntry.COLUMN_NAME_AA + TEXT_TYPE + COMMA_SEP +
                    DataEntry.COLUMN_NAME_BB + TEXT_TYPE + COMMA_SEP +
                    DataEntry.COLUMN_NAME_CC + TEXT_TYPE + COMMA_SEP +
                    DataEntry.COLUMN_NAME_DD + TEXT_TYPE + COMMA_SEP +
                    DataEntry.COLUMN_NAME_TIME + TEXT_TYPE +
                    " )";
    private static final String SQL_CREATE_DATA__STATUS_ENTRIES =
            "CREATE TABLE " + DataStatusPersistenceContract.DataStatusEntry.TABLE_NAME + " (" +
                    DataStatusEntry.COLUMN_NAME_ID + TEXT_TYPE + " PRIMARY KEY," +
                    DataStatusEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    DataStatusEntry.COLUMN_NAME_STATUS + INTEGER_TYPE +
                    " )";


    public HealthDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DATA_ENTRIES);
        db.execSQL(SQL_CREATE_DATA__STATUS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Not required as at version 1
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
