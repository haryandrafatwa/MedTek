package com.example.medtek.ui.dialog.bottomsheetdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.medtek.databinding.BsdEndSessionConfirmBinding;
import com.example.medtek.ui.activity.ChatRoomActivity;
import com.example.medtek.ui.dialog.BaseBottomSheetDialog;

public class BSDEndSession extends BaseBottomSheetDialog {
    private BsdEndSessionConfirmBinding binding;

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = BsdEndSessionConfirmBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void setupView() {
        binding.btnEndSession.setOnClickListener(v -> {
            ((ChatRoomActivity) getActivity()).sendEndSession();
            dismiss();
        });
        binding.btnClose.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    protected void destroyView() {
        binding = null;
    }
}

