package com.otatech.android.nanomoviesapp.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.otatech.android.nanomoviesapp.movies.utilities.Movie;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    private boolean boolPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (findViewById(R.id.movie_detail_container) != null) {
            boolPanes = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container,
                        new DetailFragment(), DetailFragment.TAG).commit();
            }
        } else {
            boolPanes = false;
        }
    }

    @Override
    public void onItemSelected(Movie movie) {
        if (boolPanes) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putParcelable(DetailFragment.DETAIL_MOVIE, movie);

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(bundleArgs);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailFragment, DetailFragment.TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.DETAIL_MOVIE, movie);
            startActivity(intent);
        }
    }
}
