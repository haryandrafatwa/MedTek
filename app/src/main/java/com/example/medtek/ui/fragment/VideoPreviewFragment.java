package com.example.medtek.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.databinding.ExoPlaybackControlViewBinding;
import com.example.medtek.databinding.FragmentVideoPreviewBinding;
import com.example.medtek.ui.helper.BaseFragment;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.example.medtek.utils.Utils.getDurationVideo;

public class VideoPreviewFragment extends BaseFragment {
    private FragmentVideoPreviewBinding binding;
    private ExoPlaybackControlViewBinding exoBinding;

    private final String mediaPath;
    private final String senderName;
    private final String sendTime;

    private SimpleExoPlayer player;

    private boolean playWhenReady;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    public VideoPreviewFragment(String mediaPath, String senderName, String sendTime) {
        this.mediaPath = mediaPath;
        this.senderName = senderName;
        this.sendTime = sendTime;
    }

    @Override
    protected View setContentView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentVideoPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void setupView() {
        exoBinding = ExoPlaybackControlViewBinding.inflate(getLayoutInflater());
        setupToolbar();
        showImage();
        binding.tvDurationVideo.setText(getDurationVideo(mediaPath));
        binding.rlVideoPreview.setVisibility(View.VISIBLE);
        binding.pvVideoPlay.setVisibility(View.GONE);

        binding.rlVideoPreview.setOnClickListener(v -> {
            binding.rlVideoPreview.setVisibility(View.GONE);
            binding.pvVideoPlay.setVisibility(View.VISIBLE);
            if (Util.SDK_INT >= 24) {
                initializePlayer();
                exoBinding.exoPlay.setVisibility(View.INVISIBLE);
                exoBinding.exoPause.setVisibility(View.VISIBLE);
                player.setPlayWhenReady(true);
            }
        });

        exoBinding.exoPlay.setOnClickListener(v -> {
            exoBinding.exoPlay.setVisibility(View.GONE);
            exoBinding.exoPause.setVisibility(View.VISIBLE);
            player.setPlayWhenReady(true);
        });

        exoBinding.exoPause.setOnClickListener(v -> {
            exoBinding.exoPlay.setVisibility(View.VISIBLE);
            exoBinding.exoPause.setVisibility(View.GONE);
            player.setPlayWhenReady(false);
        });
    }

    private void showImage() {
        Glide.with(this)
                .load(mediaPath)
                .into(binding.ivVideoPreview);
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

    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(App.getContext()).build();
        binding.pvVideoPlay.setPlayer(player);

        Uri uri = Uri.parse(mediaPath);
        MediaSource mediaSource = buildMediaSource(uri);

        player.setPlayWhenReady(false);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory factory = new DefaultDataSourceFactory(App.getContext(), getString(R.string.app_name));
        return new ProgressiveMediaSource.Factory(factory).createMediaSource(uri);
    }

    @Override
    protected void destroyView() {
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT < 24 && binding.rlVideoPreview.getVisibility() == View.GONE) || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }
}
