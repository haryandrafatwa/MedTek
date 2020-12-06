package com.example.medtek.ui.dialog.bottomsheetdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.medtek.databinding.BsdChooserPickerBinding;
import com.example.medtek.ui.activity.ChatRoomActivity;
import com.example.medtek.ui.dialog.BaseBottomSheetDialog;

public class BSDMediaPicker extends BaseBottomSheetDialog {

    public static final int STATE_IMAGE = 001;
    public static final int STATE_VIDEO = 002;
    private BsdChooserPickerBinding binding;
    private final int stateNow;

    public BSDMediaPicker(int stateNow) {
        this.stateNow = stateNow;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = BsdChooserPickerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    protected void setupView() {
        binding.llChooseCamera.setOnClickListener(v -> {
            if (stateNow == STATE_IMAGE) {
                ((ChatRoomActivity) getActivity()).getImageFromCamera();
                dismiss();
            } else {
                ((ChatRoomActivity) getActivity()).getVideoFromCamera();
                dismiss();
            }
        });
        binding.llChooseGallery.setOnClickListener(v -> {
            if (stateNow == STATE_IMAGE) {
                ((ChatRoomActivity) getActivity()).getImageFromGallery();
                dismiss();
            } else {
                ((ChatRoomActivity) getActivity()).getVideoFromGallery();
                dismiss();
            }
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

