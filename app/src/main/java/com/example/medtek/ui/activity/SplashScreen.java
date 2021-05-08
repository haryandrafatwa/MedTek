package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.network.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.ui.activity.MainActivity.BUNDLE_ALREADY_LOGIN;
import static com.example.medtek.utils.PropertyUtil.LOGIN_STATUS;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.PropertyUtil.searchData;
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
//            if (!searchData(LOGIN_STATUS) || !(Boolean) getData(LOGIN_STATUS)) {
//                Intent home=new Intent(SplashScreen.this,WelcomePageActivity.class);
//                startActivity(home);
//                finish();
//            } else {
//                MainActivity.navigate(SplashScreen.this, false);
//            }
            finish();
        }, 1200);
    }

    private void startSplash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                loadData();
//                Toasty.info(SplashScreen.this,refresh).show();
                if (!access.isEmpty() && !refresh.isEmpty()){
                    Call<ResponseBody> call = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()){
                                try {
                                    String s = response.body().string();
                                    JSONObject obj = new JSONObject(s);
                                    Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                    saveData(obj.getString("access_token"),obj.getString("refresh_token"));
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                try {
                                    String s = response.errorBody().string();
                                    JSONObject obj = new JSONObject(s);
                                    if (obj.getString("message").equals("Unauthenticated.")){
                                        Call<ResponseBody> callRefresh = RetrofitClient.getInstance().getApi().refresh_token(refresh);
                                        callRefresh.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (response.isSuccessful()){
                                                    try {
                                                        String s = response.body().string();
                                                        JSONObject obj = new JSONObject(s);
                                                        if (obj.has("message")){
                                                            Toasty.info(SplashScreen.this, getString(R.string.tokenexpired)).show();
                                                            Intent home=new Intent(SplashScreen.this,WelcomePageActivity.class);
                                                            startActivity(home);
                                                            finish();
                                                        }else{
                                                            Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                            finish();
                                                            saveData(obj.getString("access_token"),obj.getString("refresh_token"));
                                                        }
                                                    } catch (JSONException | IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Toasty.info(SplashScreen.this, "Refresh Failed");
                                                Intent home=new Intent(SplashScreen.this,WelcomePageActivity.class);
                                                startActivity(home);
                                                finish();
                                            }
                                        });
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toasty.info(SplashScreen.this, "Access Failed");
                            Intent home=new Intent(SplashScreen.this,WelcomePageActivity.class);
                            startActivity(home);
                            finish();
                        }
                    });
                }else{
                    Toasty.info(SplashScreen.this, "Access & Refresh Token is Empty");
                    Intent home=new Intent(SplashScreen.this,WelcomePageActivity.class);
                    startActivity(home);
                    finish();
                }
            }
        },waktu_loading);
    }

    public void loadData()  {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        String access = sharedPreferences.getString("token", "");
        String refresh = sharedPreferences.getString("refresh_token", "");
        this.access =  access;
        this.refresh = refresh;
    }

    public void saveData(String access, String refresh) {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", access);
        editor.putString("refresh_token", refresh);
        editor.apply();
    }

}
