package com.example.medtek;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medtek.Dokter.LoginDokterActivity;
import com.example.medtek.Pasien.LoginPasienActivity;

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

}
