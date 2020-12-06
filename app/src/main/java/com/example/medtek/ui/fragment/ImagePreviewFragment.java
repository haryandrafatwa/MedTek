package com.example.medtek.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.medtek.R;
import com.example.medtek.databinding.FragmentImagePreviewBinding;
import com.example.medtek.ui.helper.BaseFragment;

public class ImagePreviewFragment extends BaseFragment {
    private FragmentImagePreviewBinding binding;

    private final String mediaPath;
    private final String senderName;
    private final String sendTime;

    public ImagePreviewFragment(String mediaPath, String senderName, String sendTime) {
        this.mediaPath = mediaPath;
        this.senderName = senderName;
        this.sendTime = sendTime;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentImagePreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void setupView() {
        setupToolbar();
        showImage();
    }

    private void showImage() {
        Glide.with(this)
                .load(mediaPath)
                .into(binding.pvImagePreview);
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_back_primary);
        binding.toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        binding.tvSenderName.setText(senderName);
        binding.tvMessageTime.setText(sendTime);
    }

    @Override
    protected void destroyView() {
        binding = null;
    }
}

