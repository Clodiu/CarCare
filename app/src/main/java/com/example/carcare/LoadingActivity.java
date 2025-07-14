package com.example.carcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Setam manual culoarea barii de notificare
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_tint_color));

        //Se incarca din memorie din MyAppPrefs campul USER_ID pentru a verifica daca deja a exitat o logare in trecut
        //In principiu daca exista salvat un user_id acolo, functioneaza ca un remember-me
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int savedUserId = prefs.getInt("USER_ID", -1);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Daca savedUserId e diferit de -1 inseamna ca am fost conectat in trecut, deci dam skip la pagina de autentificare
                if (savedUserId != -1) {
                    // Exista un user logat - mergi direct in CarListActivity
                    Intent intent = new Intent(LoadingActivity.this, CarListActivity.class);
                    intent.putExtra("USER_ID", savedUserId);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(LoadingActivity.this, AuthActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },500);
    }
}