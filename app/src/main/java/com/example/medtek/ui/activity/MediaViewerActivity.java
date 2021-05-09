package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.medtek.databinding.ActivityMediaViewerBinding;
import com.example.medtek.model.MediaModel;
import com.example.medtek.ui.adapter.ViewPagerAdapter;
import com.example.medtek.ui.fragment.ImagePreviewFragment;
import com.example.medtek.ui.fragment.VideoPreviewFragment;
import com.example.medtek.ui.helper.SingleActivity;

import java.util.ArrayList;

/**
 * Activity untuk Preview Image/Video
 */

public class MediaViewerActivity extends SingleActivity {
    private static final String BUNDLE_MEDIA = "bundle_media";
    private static final String BUNDLE_NAME = "bundle_name";
    private static final String BUNDLE_TIME = "bundle_time";
    private static final String BUNDLE_ID_CHAT = "bundle_id_chat";
    private ActivityMediaViewerBinding binding;
    private ArrayList<MediaModel> mediaModels = new ArrayList<>();
    private String senderName;
    private String senderTime;
    private int idChat;
    private int currentPosition;

    public static void navigate(Activity activity, ArrayList<MediaModel> model, String senderName, String senderTime, int idChat) {
        Intent intent = new Intent(activity, MediaViewerActivity.class);
        intent.putParcelableArrayListExtra(BUNDLE_MEDIA, model);
        intent.putExtra(BUNDLE_NAME, senderName);
        intent.putExtra(BUNDLE_TIME, senderTime);
        intent.putExtra(BUNDLE_ID_CHAT, idChat);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @Override
    protected View VcontentView() {
        binding = ActivityMediaViewerBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected int IcontentView() {
        return 0;
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        mediaModels = getIntent().getParcelableArrayListExtra(BUNDLE_MEDIA);
        senderName = getIntent().getStringExtra(BUNDLE_NAME);
        senderTime = getIntent().getStringExtra(BUNDLE_TIME);
        idChat = getIntent().getIntExtra(BUNDLE_ID_CHAT, 0);
    }

    @Override
    protected void setupView() {
        binding.vpImageViewer.setAdapter(setupViewPager());
        binding.vpImageViewer.setSwipeable(true);
        binding.vpImageViewer.setCurrentItem(currentPosition);
    }

    private ViewPagerAdapter setupViewPager() {
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        int mediaCount = 0;
        for (MediaModel model : mediaModels) {
            ++mediaCount;
            switch (model.getType()) {
                case IMAGE:
                    pagerAdapter.addFragment(new ImagePreviewFragment(model.getPath(), senderName, senderTime), "Image" + mediaCount);
                    break;
                case VIDEO:
                    pagerAdapter.addFragment(new VideoPreviewFragment(model.getPath(), senderName, senderTime), "Video" + mediaCount);
            }
            if (model.getIdChat() == idChat) {
                currentPosition = --mediaCount;
            }
        }
        return pagerAdapter;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

