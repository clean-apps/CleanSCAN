package com.babanomania.pdfscanner;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.babanomania.pdfscanner.fileView.FLAdapter;
import com.babanomania.pdfscanner.persistance.Document;
import com.babanomania.pdfscanner.persistance.DocumentViewModel;
import com.babanomania.pdfscanner.utils.UIUtil;

import java.util.List;

public class SearchableActivity extends AppCompatActivity {


    private LinearLayout emptyLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        getSupportActionBar().setTitle(null);

        this.recyclerView = findViewById(R.id.rwSearch);

        UIUtil.setLightNavigationBar( recyclerView, this );

        DocumentViewModel viewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);

        final FLAdapter fileAdapter = new FLAdapter( viewModel, this);
        recyclerView.setAdapter( fileAdapter );

        this.emptyLayout = findViewById(R.id.empty_search_list);
        viewModel.getAllDocuments().observe(this, new Observer<List<Document>>() {
            @Override
            public void onChanged(@Nullable List<Document> documents) {

                if( documents.size() > 0 ){
                    emptyLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

                fileAdapter.setData(documents);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getMenuInflater().inflate(R.menu.searchview_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        MenuItem searchMenuItem = menu.findItem(R.id.menu_searchview);
        searchMenuItem.expandActionView();

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconified(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        doMySearch( newText );
                        return false;
                    }
                });

        return true;
    }

    public void doMySearch( String query ){

        this.recyclerView = findViewById(R.id.rwSearch);

        UIUtil.setLightNavigationBar( recyclerView, this );

        DocumentViewModel viewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);

        final FLAdapter fileAdapter = new FLAdapter( viewModel, this);
        recyclerView.setAdapter( fileAdapter );

        viewModel.search( '%' + query + '%').observe(this, new Observer<List<Document>>() {
            @Override
            public void onChanged(@Nullable List<Document> documents) {


                if( documents.size() > 0 ){
                    emptyLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

                fileAdapter.setData(documents);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

    }
}
