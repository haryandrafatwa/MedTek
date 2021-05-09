package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medtek.App;
import com.example.medtek.R;

import static com.example.medtek.ui.activity.MainActivity.BUNDLE_ALREADY_LOGIN;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.LOGIN_STATUS;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.TAG;

public class SplashScreen extends AppCompatActivity {

    private final int waktu_loading=1000;
    private TextView tv_username;
    private String refresh,access;
    private Boolean isLoggin;

    public static void navigate(Activity activity) {
        Intent intent = new Intent(activity, SplashScreen.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        isLoggin = getIntent().getBooleanExtra(BUNDLE_ALREADY_LOGIN, false);
        splashScreenDelayFirst();

    }

    private void splashScreenDelayFirst() {
        App.getInstance().runOnUIThreadDelay(() -> {
            Log.d(TAG(SplashScreen.class), "checkLoginStatus: " + (Boolean) getData(LOGIN_STATUS));
            finish();
        }, 1200);
    }

    public void loadData()  {
//        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        String access = sharedPreferences.getString("token", "");
//        String refresh = sharedPreferences.getString("refresh_token", "");
        this.access = (String) getData(ACCESS_TOKEN);
        this.refresh = (String) getData(REFRESH_TOKEN);
    }

    public void saveData(String access, String refresh) {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", access);
        editor.putString("refresh_token", refresh);
        editor.apply();
    }

}
