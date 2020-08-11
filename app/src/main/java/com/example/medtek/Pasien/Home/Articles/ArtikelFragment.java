package com.example.medtek.Pasien.Home.Articles;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medtek.API.RetrofitClient;
import com.example.medtek.Pasien.Home.Articles.ArtikelAdapter;
import com.example.medtek.Pasien.Home.Articles.ArtikelAdapterMore;
import com.example.medtek.Pasien.Model.ArtikelModel;
import com.example.medtek.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtikelFragment extends Fragment {

    private interface RecyclerViewReadyCallback{
        void onLayoutReady();
    }

    private RecyclerViewReadyCallback recyclerViewReadyCallback;

    private ChipNavigationBar bottomNavigationView;
    private Toolbar toolbar;
    private EditText searchText;

    private ProgressBar pb_artikel;
    private RelativeLayout rl_artikel;

    private List<ArtikelModel> mListArtikel = new ArrayList<>();;
    private RecyclerView.LayoutManager mLayoutManagerArtikel;
    private ArtikelAdapterMore mAdapterArtikel;
    private RecyclerView rv_artikel;

    private int currentPage,maxPages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_articles, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        recyclerViewReadyCallback = new RecyclerViewReadyCallback() {
            @Override
            public void onLayoutReady() {
                pb_artikel.setVisibility(View.GONE);
                rv_artikel.setVisibility(View.VISIBLE);
            }
        };

        Call<ResponseBody> callArticles = RetrofitClient.getInstance().getApi().getAllArticles();
        callArticles.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String s = response.body().string();
                    if (s!=null){
                        JSONObject object = new JSONObject(s);
                        JSONArray array = new JSONArray(object.getString("data"));
                        mListArtikel.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jo = array.getJSONObject(i);
                            String author = jo.getString("author");
                            JSONObject authorObject = new JSONObject(author);
                            String authorName = authorObject.getString("name");
                            String topic = jo.getString("topic");
                            JSONObject topicObject = new JSONObject(topic);
                            String topicName = topicObject.getString("topic");
                            int id_artikel = jo.getInt("id");
                            String judul = jo.getString("judul");
                            String isi = jo.getString("isi");
                            String slug = jo.getString("slug");
                            String image_url = jo.getString("image");
                            String upload_date = jo.getString("created_at");
                            int isVerified = jo.getInt("is_verified");
                            if (isVerified == 1){
                                mListArtikel.add(new ArtikelModel(id_artikel,judul,isi,slug,image_url,upload_date,authorName,topicName));
//                                mAdapterArtikel.notifyDataSetChanged();
                            }
                            rv_artikel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    if(recyclerViewReadyCallback != null){
                                        recyclerViewReadyCallback.onLayoutReady();
                                    }
                                    rv_artikel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                }
                            });
                        }
                        Collections.reverse(mListArtikel);
                        initRecyclerViewItem();
                    }
                } catch (IOException | JSONException e) {
                    Log.d("maeokmwaklemqweq",e.getMessage());
                    if (e.getMessage().length() > 0){
                        callArticles.clone().enqueue(this);
                    }else{
                        pb_artikel.setVisibility(View.GONE);
                        rv_artikel.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mAdapterArtikel != null){
                    mAdapterArtikel.getFilter().filter(s.toString().trim());
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapterArtikel != null){
                    mAdapterArtikel.getFilter().filter(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mAdapterArtikel != null){
                    mAdapterArtikel.getFilter().filter(s.toString().trim());
                }
            }
        });

    }

    private void initialize(){
        bottomNavigationView = getActivity().findViewById(R.id.bottomBar);
        bottomNavigationView.setVisibility(View.GONE);

        toolbar = getActivity().findViewById(R.id.toolbar);
        setToolbar();

        pb_artikel = getActivity().findViewById(R.id.pb_artikel);
        rl_artikel = getActivity().findViewById(R.id.rl_artikel_empty);
        rv_artikel = getActivity().findViewById(R.id.rv_artikel);
        searchText = getActivity().findViewById(R.id.et_search_artikel);
    }

    private void initRecyclerViewItem(){
        mAdapterArtikel = new ArtikelAdapterMore(mListArtikel,getActivity().getApplicationContext(),getActivity());
        mLayoutManagerArtikel = new LinearLayoutManager(getActivity(),RecyclerView.VERTICAL,false);
        rv_artikel.setAdapter(mAdapterArtikel);
    }

    private void setToolbar() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chevron_left, null);
        drawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(drawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }
}
