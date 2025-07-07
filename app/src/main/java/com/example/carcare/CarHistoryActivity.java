package com.example.carcare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CarHistoryActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;
    private MyPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_tint_color));

        // Activeaza iconite intunecate in bara de notificari pentru fundal deschis
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        viewPager = findViewById(R.id.view_pager);

        //Setarea adaptorului si creearea legaturii cu viewPager(locul in care se incarca fragmentele)
        pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        //Limita de pagini care raman in memorie, 3 in plus fata de cea curenta
        viewPager.setOffscreenPageLimit(3);

        // Sincronizarea barii de navigatie cu view pager, in functie de ce iconita e selectata din cele 4
        //muta pe pagina respectiva(nu creeaza una noua, ci o refolosese pe cea deja deschisa)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.calendar) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.finance) {
                viewPager.setCurrentItem(2, true);
                return true;
            } else if (id == R.id.settings) {
                viewPager.setCurrentItem(3, true);
                return true;
            }
            return false;
        });

        // Sincronizare inversa cand schimbi se da swipe in viewPager se modifica bara de navigatie
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.calendar);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.finance);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.settings);
                        break;
                }
            }
        });
    }
}
