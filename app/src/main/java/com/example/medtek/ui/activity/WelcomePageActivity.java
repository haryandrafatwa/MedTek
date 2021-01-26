package com.example.medtek.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medtek.R;
import com.example.medtek.ui.dokter.auth.LoginDokterActivity;
import com.example.medtek.ui.pasien.auth.LoginPasienActivity;

public class WelcomePageActivity extends AppCompatActivity {

    private Button dokter,pasien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        dokter = findViewById(R.id.btn_dokter);
        pasien = findViewById(R.id.btn_pasien);

        dokter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dokter = new Intent(WelcomePageActivity.this, LoginDokterActivity.class);
                startActivity(dokter);
            }
        });

        pasien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pasien = new Intent(WelcomePageActivity.this, LoginPasienActivity.class);
                startActivity(pasien);
            }
        });

    }

    public static void saveData(Context context, String TEXT) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", TEXT);
        editor.apply();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
