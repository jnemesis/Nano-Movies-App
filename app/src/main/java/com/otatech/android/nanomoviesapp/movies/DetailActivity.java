package com.otatech.android.nanomoviesapp.movies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        if (savedInstanceState == null) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putParcelable(DetailFragment.DETAIL_MOVIE,
                    getIntent().getParcelableExtra(DetailFragment.DETAIL_MOVIE));

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundleArgs);

            getSupportFragmentManager().beginTransaction().add(R.id.movie_detail_container, detailFragment).commit();
        }
    }
}
