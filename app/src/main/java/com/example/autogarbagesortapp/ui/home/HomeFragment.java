package com.example.autogarbagesortapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.autogarbagesortapp.R;

public class HomeFragment extends Fragment {

    private ProgressBar mPlasticProgressBar;
    private TextView mPlasticPercentageTextView;
    private ProgressBar mMetalProgressBar;
    private TextView mMetalPercentageTextView;
    private ProgressBar mBiodegradableProgressBar;
    private TextView mBiodegradablePercentageTextView;
    private HomeViewModel mHomeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mPlasticProgressBar = view.findViewById(R.id.plasticProgressBar);
        mPlasticPercentageTextView = view.findViewById(R.id.plasticPercentageTextView);
        mMetalProgressBar = view.findViewById(R.id.metalProgressBar);
        mMetalPercentageTextView = view.findViewById(R.id.metalPercentageTextView);
        mBiodegradableProgressBar = view.findViewById(R.id.biodegradableProgressBar);
        mBiodegradablePercentageTextView = view.findViewById(R.id.biodegradablePercentageTextView);

        mHomeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        mHomeViewModel.getPlasticLevel().observe(getViewLifecycleOwner(), level -> {
            updateUI(level, mPlasticProgressBar, mPlasticPercentageTextView);
        });

        mHomeViewModel.getMetalLevel().observe(getViewLifecycleOwner(), level -> {
            updateUI(level, mMetalProgressBar, mMetalPercentageTextView);
        });

        mHomeViewModel.getBiodegradableLevel().observe(getViewLifecycleOwner(), level -> {
            updateUI(level, mBiodegradableProgressBar, mBiodegradablePercentageTextView);
        });

        mHomeViewModel.fetchData();

        return view;
    }

    private void updateUI(int level, ProgressBar progressBar, TextView percentageTextView) {
        progressBar.setProgress(level);
        percentageTextView.setText(level + "%");
    }
}