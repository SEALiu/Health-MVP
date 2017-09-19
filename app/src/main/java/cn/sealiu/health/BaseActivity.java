package cn.sealiu.health;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import cn.sealiu.health.data.bean.BaseResponse;
import okhttp3.Request;

import static android.content.ContentValues.TAG;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class BaseActivity extends AppCompatActivity {
    public static final boolean D = true;
    public static final String TAG = "BaseActivity";
    public static final String IDENTITY_USER = "1";
    public static final String IDENTITY_DOCTOR = "2";

    // 模拟器调试地址
    //public static final String REMOTE_URL = "http://10.0.2.2:8080";
    // 真机调试地址
    public static final String REMOTE_URL = "http://192.168.1.137:8080";

    public static SharedPreferences sharedPref, settingPref;
    public static ProgressDialog mProgressDialog;

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static IntentFilter gattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences("Health", Context.MODE_PRIVATE);
        settingPref = PreferenceManager.getDefaultSharedPreferences(this);

        context = getApplicationContext();
    }

    public static Request buildHttpGetRequest(String short_url) {
        return new Request.Builder()
                .url(REMOTE_URL + short_url)
                .header("Content-Type", "text/html; charset=utf-8")
                .addHeader("User-Agent", "android")
                .get()
                .build();
    }

    public static void hideKeyboard() {
        InputMethodManager inputMethodMgr = (InputMethodManager)
                BaseActivity.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodMgr.toggleSoftInput(0, 0);
    }

    public void showProgressDialog(String initContent) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(initContent);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();

        // 5分钟后自动隐藏加载框
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 300000);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public static Context getContext() {
        return context;
    }
    /*
    public void insertDataDB(DBHelper dbHelper, Object obj) {
        DataBean bean = (DataBean) obj;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DataEntry.COLUMN_NAME_ID, bean.getId());
        values.put(DataEntry.COLUMN_NAME_MID, bean.getMid());
        values.put(DataEntry.COLUMN_NAME_SEQUENCE, bean.getSequence());
        values.put(DataEntry.COLUMN_NAME_AA, bean.getAa());
        values.put(DataEntry.COLUMN_NAME_BB, bean.getBb());
        values.put(DataEntry.COLUMN_NAME_CC, bean.getCc());
        values.put(DataEntry.COLUMN_NAME_DD, bean.getDd());
        values.put(DataEntry.COLUMN_NAME_TIME, bean.getTime());
        values.put(DataEntry.COLUMN_NAME_SYNC, bean.getSync());

        db.insert(DataEntry.TABLE_NAME, null, values);
    }
    */
}
