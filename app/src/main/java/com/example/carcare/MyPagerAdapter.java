package com.example.carcare;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

//Un adaptor de stare pentru fragmentele din CarHistoryActivity care se foloseste de pozitia iconitei selectate
//pentru a crea fragmentul corespunzator

public class MyPagerAdapter extends FragmentStateAdapter {

    public MyPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new CalendarFragment();
            case 2: return new SettingsFragment();
            case 3: return new SettingsFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // numarul de taburi/fragments
    }
}
