package com.example.carcare;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

//Un adaptor de stare pentru fragmentele din CarHistoryActivity care se foloseste de pozitia iconitei selectate
//pentru a crea fragmentul corespunzator

public class MyPagerAdapter extends FragmentStateAdapter {

    private final SparseArray<Fragment> fragments = new SparseArray<>();

    public MyPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new CalendarFragment();
                break;
            case 2:
                fragment = new AICamFragment();
                break;
            case 3:
                fragment = new SettingsFragment();
                break;
        }
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4; // numarul de taburi/fragments
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
}
