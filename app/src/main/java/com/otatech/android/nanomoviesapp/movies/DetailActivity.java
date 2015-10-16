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
            bundleArgs.putParcelable(DetailFragment.MOVIE_DETAILS,
                    getIntent().getParcelableExtra(DetailFragment.MOVIE_DETAILS));

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundleArgs);

            getSupportFragmentManager().beginTransaction().add(R.id.fl_container, detailFragment).commit();
        }
    }
}
