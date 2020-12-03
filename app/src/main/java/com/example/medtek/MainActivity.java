package com.example.medtek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.HomeFragment;
import com.example.medtek.Pasien.Model.UserModel;
import com.example.medtek.Pasien.Others.OthersFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ChipNavigationBar chipNavigationBar;
    FragmentManager fragmentManager;

    private String access, refresh;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chipNavigationBar = findViewById(R.id.bottomBar);
        loadData(MainActivity.this);

        Call<ResponseBody> callUser = RetrofitClient.getInstance().getApi().getUser("Bearer "+access);
        callUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        String s = response.body().string();
                        JSONObject obj = new JSONObject(s);
                        if (obj.getInt("role_id") == 1){
                            if (savedInstanceState==null){
                                chipNavigationBar.setItemSelected(R.id.homee,true);
                                fragmentManager = getSupportFragmentManager();
                                HomeFragment homeFragment = new HomeFragment();
                                fragmentManager.beginTransaction().replace(R.id.frameFragment,homeFragment).addToBackStack("FragmentHomePasien").commit();
                            }

                            chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(int i) {

                                    Fragment fragment = null;
                                    switch (i){
                                        case R.id.chat:
                                            fragment = new ChatFragment();
                                            TAG = "FragmentChatPasien";
                                            break;
                                        case R.id.homee:
                                            fragment = new HomeFragment();
                                            TAG = "FragmentHomePasien";
                                            break;
                                        case R.id.others:
                                            fragment = new OthersFragment();
                                            TAG = "FragmentOthersPasien";
                                            break;
                                    }

                                    if (fragment!= null){
                                        fragmentManager = getSupportFragmentManager();
                                        fragmentManager.beginTransaction().replace(R.id.frameFragment,fragment).addToBackStack(TAG).commit();
                                    }

                                }
                            });
                        }else if(obj.getInt("role_id") == 2){
                            if (savedInstanceState==null){
                                chipNavigationBar.setItemSelected(R.id.homee,true);
                                fragmentManager = getSupportFragmentManager();
                                com.example.medtek.Dokter.Home.HomeFragment homeFragment = new com.example.medtek.Dokter.Home.HomeFragment();
                                fragmentManager.beginTransaction().replace(R.id.frameFragment,homeFragment,"FragmentHomeDokter").addToBackStack("FragmentHomeDokter").commit();
                            }

                            chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(int i) {

                                    Fragment fragment = null;
                                    switch (i){
                                        case R.id.chat:
                                            fragment = new ChatFragment();
                                            TAG = "FragmentChatDokter";
                                            break;
                                        case R.id.homee:
                                            fragment = new  com.example.medtek.Dokter.Home.HomeFragment();
                                            TAG = "FragmentHomeDokter";
                                            break;
                                        case R.id.others:
                                            fragment = new OthersFragment();
                                            TAG = "FragmentOthersDokter";
                                            break;
                                    }

                                    if (fragment!= null){
                                        fragmentManager = getSupportFragmentManager();
                                        fragmentManager.beginTransaction().replace(R.id.frameFragment,fragment,TAG).addToBackStack(TAG).commit();
                                    }

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
            }
        });

    }

    public void loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        this.access = sharedPreferences.getString("token", "");
        this.refresh = sharedPreferences.getString("refresh_token", "");
    }

}
