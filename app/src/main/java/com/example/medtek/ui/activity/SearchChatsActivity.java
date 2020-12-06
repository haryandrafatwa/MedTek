package com.example.medtek.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medtek.App;
import com.example.medtek.R;
import com.example.medtek.databinding.ActivitySearchChatBinding;
import com.example.medtek.model.ChatsModel;
import com.example.medtek.ui.adapter.ChatsListAdapter;
import com.example.medtek.ui.helper.SingleActivity;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

import static com.example.medtek.App.getContext;
import static com.example.medtek.utils.RecyclerViewUtil.recyclerLinear;
import static java.lang.String.valueOf;

public class SearchChatsActivity extends SingleActivity {
    private static final String TAG = SearchChatsActivity.class.getSimpleName();
    private static final String CHATS_EXTRA = "chats_extra";
    private ActivitySearchChatBinding binding;
    private ArrayList<ChatsModel> chatsModels = new ArrayList<>();
    private ChatsListAdapter chatsListAdapter;

    public static void navigate(Activity activity, ArrayList<ChatsModel> model) {
        Intent intent = new Intent(activity, SearchChatsActivity.class);
        intent.putParcelableArrayListExtra(CHATS_EXTRA, model);
        activity.startActivity(intent);
    }

    @Override
    protected View VcontentView() {
        binding = ActivitySearchChatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected int IcontentView() {
        return 0;
    }

    @Override
    protected void setupData(@Nullable Bundle savedInstanceState) {
        chatsModels = getIntent().getParcelableArrayListExtra(CHATS_EXTRA);
        Log.d(TAG, valueOf(chatsModels.size()));
    }

    @Override
    protected void setupView() {
        chatsListAdapter = new ChatsListAdapter(getContext());
        setupDataRVChats(chatsModels);
        App.getInstance().runOnUiThread(() -> {
            binding.searchView.showSearch();
        });
        binding.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chatsListAdapter.getFilter().filter(newText);
                return false;
            }
        });

        binding.searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                finish();
            }
        });
        binding.searchView.setCursorDrawable(R.drawable.ic_cursor);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void setupDataRVChats(ArrayList<ChatsModel> chatsModels) {
        if (chatsModels.size() > 0) {
            chatsListAdapter.setChatsList(chatsModels);
            Log.d(TAG, "checkIsNoEmpty");
            recyclerLinear(binding.rvChatSearch, LinearLayoutManager.VERTICAL, chatsListAdapter);
            chatsListAdapter.setOnItemClickCallback((model, position) -> {
                ChatRoomActivity.navigate(this, model);
                finish();
            });
            isNoChats(false);
        } else {
            isNoChats(true);
        }
    }

    private void isNoChats(boolean status) {
        if (binding != null) {
            if (status) {
                binding.rvChatSearch.setVisibility(View.GONE);
                binding.llNoChatsHistory.setVisibility(View.VISIBLE);
            } else {
                binding.rvChatSearch.setVisibility(View.VISIBLE);
                binding.llNoChatsHistory.setVisibility(View.GONE);
            }
        }
    }

}

