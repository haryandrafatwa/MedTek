package com.example.medtek.ui.helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Class Base Fragment
 */

public abstract class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(setContentView(), container, false);
        return setContentView(inflater, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupData(savedInstanceState);
        setupView();
    }

    protected abstract View setContentView(LayoutInflater inflater, ViewGroup container);

    protected abstract void setupData(@Nullable Bundle savedInstanceState);

    protected abstract void setupView();

    protected abstract void destroyView();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyView();
    }
}
