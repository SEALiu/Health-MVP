package cn.sealiu.health;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import okhttp3.Request;

/**
 * Created by liuyang
 * on 2017/9/12.
 */

public class BaseActivity extends AppCompatActivity {
    public static final boolean D = true;
    public static final String TAG = "BaseActivity";
    public static final String IDENTITY_USER = "1";
    public static final String IDENTITY_DOCTOR = "2";
    public static final String SERVER_IP = "server_ip";

    // 模拟器调试地址
    //public static final String REMOTE_URL = "http://10.0.2.2:8080";
    // 真机调试地址
    public static String REMOTE_URL = "";

    public static SharedPreferences sharedPref, settingPref;

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences("Health", Context.MODE_PRIVATE);
        settingPref = PreferenceManager.getDefaultSharedPreferences(this);

        context = getApplicationContext();
    }

    public static Request buildHttpGetRequest(String short_url) {
        REMOTE_URL = sharedPref.getString(SERVER_IP, "");
        if (REMOTE_URL.equals("")) return null;

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

    public static Context getContext() {
        return context;
    }
}
