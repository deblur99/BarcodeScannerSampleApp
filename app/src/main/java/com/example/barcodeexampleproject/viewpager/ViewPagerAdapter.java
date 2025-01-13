package com.example.barcodeexampleproject.viewpager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.barcodeexampleproject.fragments.CodeAnalysisFragment;
import com.example.barcodeexampleproject.fragments.LogListFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final int numTabs = 2; // 탭 개수

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new LogListFragment();
        }
        return new CodeAnalysisFragment();
    }

    @Override
    public int getItemCount() {
        return numTabs;
    }
}
