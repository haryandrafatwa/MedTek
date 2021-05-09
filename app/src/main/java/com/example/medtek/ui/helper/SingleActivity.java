package com.example.medtek.ui.helper;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;

import com.example.medtek.App;
import com.example.medtek.R;

import net.mrbin99.laravelechoandroid.Echo;

import java.util.HashMap;
import java.util.Map;

/**
 * Class Base Activity
 */

public abstract class SingleActivity extends AppCompatActivity {
    private static final String TAG = SingleActivity.class.getSimpleName();
    public static Map<Class<? extends Activity>, Activity> launched = new HashMap<Class<? extends Activity>, Activity>();

    public static Echo echo;
    public MutableLiveData<Object> receivedEvent = new MutableLiveData<>();


    public static void isLoading(Boolean status, View view) {
        App.getInstance().runOnUiThread(() -> {
            if (status) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Activity activity = launched.get(this.getClass());
        if (activity != null)
            activity.finish();
        launched.put(this.getClass(), this);
        super.onCreate(savedInstanceState);
        if (VcontentView() != null) {
            setContentView(VcontentView());
        } else if (VcontentView() == null && IcontentView() != 0) {
            setContentView(IcontentView());
        }
        setupData(savedInstanceState);
        setupView();
    }

    protected abstract View VcontentView();

    protected abstract int IcontentView();

    protected abstract void setupData(@Nullable Bundle savedInstanceState);

    protected abstract void setupView();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Activity activity = launched.get(this.getClass());
        if (activity == this)
            launched.remove(this.getClass());
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_primary);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.colorPrimary));
            getSupportActionBar().setTitle("");
        }
    }

    public void setupToolbar(String title) {
        setupToolbar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_back_secondary);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}

