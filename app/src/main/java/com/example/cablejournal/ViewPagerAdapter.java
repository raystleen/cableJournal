package com.example.cablejournal;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class ViewPagerAdapter extends FragmentStateAdapter {

    //Список имен вкладок
    public static int[] tabs = new int[]{
            R.string.mainTab,
            R.string.pp,
            R.string.ane
    };

    ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        switch (position) {
            case 0:
                return new MainFragment();
            case 1:
                return new PPFragment();
            case 2:
                return new ANEFragment();
        }
        return new MainFragment();
    }

    @Override
    public int getItemCount() {
        return tabs.length;
    }
}
