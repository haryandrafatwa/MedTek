package com.example.medtek.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class BaseBottomSheetDialog extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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