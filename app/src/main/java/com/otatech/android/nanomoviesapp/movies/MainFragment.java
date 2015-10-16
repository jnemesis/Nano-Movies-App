package com.otatech.android.nanomoviesapp.movies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.otatech.android.nanomoviesapp.movies.utilities.Adapters;
import com.otatech.android.nanomoviesapp.movies.utilities.DbContract;
import com.otatech.android.nanomoviesapp.movies.utilities.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private GridView gvGridView;

    private Adapters mgAdapter;

    private static final String SORTKEY = "sort_setting";
    private static final String POPULARITYSORT = "popularity.desc";
    private static final String RATINGSORT = "vote_average.desc";
    private static final String FAVORITESORT = "favorite";
    private static final String MOVIES = "movies";

    private String strSortBy = POPULARITYSORT;

    private ArrayList<Movie> alMovies = null;

    private static final String[] MOVIECOLUMNS = {
            DbContract.MovieEntry._ID,
            DbContract.MovieEntry.MOVIEID,
            DbContract.MovieEntry.MOVIETITLE,
            DbContract.MovieEntry.MOVIECOVER,
            DbContract.MovieEntry.MOVIEPOSTER,
            DbContract.MovieEntry.MOVIEPLOT,
            DbContract.MovieEntry.MOVIERATING,
            DbContract.MovieEntry.MOVIERELEASE
    };

    public static final int ID = 0;
    public static final int MOVIEID = 1;
    public static final int TITLE = 2;
    public static final int COVER = 3;
    public static final int BACKDROP = 4;
    public static final int PLOT = 5;
    public static final int RATING = 6;
    public static final int RELEASE = 7;

    public MainFragment() {
    }

    public interface Callback {
        void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu);

        MenuItem miPopularitySort = menu.findItem(R.id.i_btn_popular);
        MenuItem miRatingsSort = menu.findItem(R.id.i_btn_rating);
        MenuItem miFavoritesSort = menu.findItem(R.id.i_btn_favorite);

        if (strSortBy.contentEquals(POPULARITYSORT)) {
            if (!miPopularitySort.isChecked()) {
                miPopularitySort.setChecked(true);
            }
        } else if (strSortBy.contentEquals(RATINGSORT)) {
            if (!miRatingsSort.isChecked()) {
                miRatingsSort.setChecked(true);
            }
        } else if (strSortBy.contentEquals(FAVORITESORT)) {
            if (!miPopularitySort.isChecked()) {
                miFavoritesSort.setChecked(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int intId = menuItem.getItemId();
        switch (intId) {
            case R.id.i_btn_popular:
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                strSortBy = POPULARITYSORT;
                updateMovies(strSortBy);
                return true;
            case R.id.i_btn_rating:
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                strSortBy = RATINGSORT;
                updateMovies(strSortBy);
                return true;
            case R.id.i_btn_favorite:
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                strSortBy = FAVORITESORT;
                updateMovies(strSortBy);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment, container, false);
        gvGridView = (GridView) view.findViewById(R.id.gv_movies);

        mgAdapter = new Adapters(getActivity(), new ArrayList<Movie>());

        gvGridView.setAdapter(mgAdapter);

        gvGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int intPos, long id) {
                Movie movie = mgAdapter.getItem(intPos);
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SORTKEY)) {
                strSortBy = savedInstanceState.getString(SORTKEY);
            }

            if (savedInstanceState.containsKey(MOVIES)) {
                alMovies = savedInstanceState.getParcelableArrayList(MOVIES);
                mgAdapter.setData(alMovies);
            } else {
                updateMovies(strSortBy);
            }
        } else {
            updateMovies(strSortBy);
        }

        return view;
    }

    private void updateMovies(String strSortBy) {
        if (!strSortBy.contentEquals(FAVORITESORT)) {
            new FetchMoviesTask().execute(strSortBy);
        } else {
            new FetchFavoriteMoviesTask(getActivity()).execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!strSortBy.contentEquals(POPULARITYSORT)) {
            outState.putString(SORTKEY, strSortBy);
        }
        if (alMovies != null) {
            outState.putParcelableArrayList(MOVIES, alMovies);
        }
        super.onSaveInstanceState(outState);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOGTAG = FetchMoviesTask.class.getSimpleName();

        private List<Movie> lstJSONMoviesData(String strJSON) throws JSONException {
            JSONObject jsonObjMovie = new JSONObject(strJSON);
            JSONArray jsonArrMovies = jsonObjMovie.getJSONArray("results");

            List<Movie> lstMovies = new ArrayList<>();

            for(int i = 0; i < jsonArrMovies.length(); i++) {
                JSONObject jsonMovieObject = jsonArrMovies.getJSONObject(i);
                Movie movieModel = new Movie(jsonMovieObject);
                lstMovies.add(movieModel);
            }

            return lstMovies;
        }

        @Override
        protected List<Movie> doInBackground(String[] strParams) {

            if (strParams.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String strJSON = null;

            try {
                final String BASEURL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORTBY = "sort_by";
                final String APIKEY = "api_key";

                Uri uriBuilt = Uri.parse(BASEURL).buildUpon()
                        .appendQueryParameter(SORTBY, strParams[0])
                        .appendQueryParameter(APIKEY, getString(R.string.s_api_key))
                        .build();

                URL url = new URL(uriBuilt.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String strLine;
                while ((strLine = bufferedReader.readLine()) != null) {
                    stringBuffer.append(strLine + "\n");
                }

                if (stringBuffer.length() == 0) {
                    return null;
                }
                strJSON = stringBuffer.toString();
            } catch (IOException ioe) {
                Log.wtf(LOGTAG, "Error ", ioe);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException ioe) {
                        Log.wtf(LOGTAG, "Error closing stream", ioe);
                    }
                }
            }

            try {
                return lstJSONMoviesData(strJSON);
            } catch (JSONException je) {
                Log.wtf(LOGTAG, je.getMessage(), je);
                je.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> lstMovies) {
            if (lstMovies != null) {
                if (mgAdapter != null) {
                    mgAdapter.setData(lstMovies);
                }
                alMovies = new ArrayList<>();
                alMovies.addAll(lstMovies);
            }
        }
    }

    public class FetchFavoriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        private Context context;

        public FetchFavoriteMoviesTask(Context context) {
            this.context = context;
        }

        private List<Movie> getFavoriteMoviesDataFromCursor(Cursor cursor) {
            List<Movie> lstMovieResults = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie(cursor);
                    lstMovieResults.add(movie);
                } while (cursor.moveToNext());
                cursor.close();
            }
            return lstMovieResults;
        }

        @Override
        protected List<Movie> doInBackground(Void[] params) {
            Cursor cursor = context.getContentResolver().query(
                    DbContract.MovieEntry.MOVIEURI,
                    MOVIECOLUMNS, null, null, null);
            return getFavoriteMoviesDataFromCursor(cursor);
        }

        @Override
        protected void onPostExecute(List<Movie> lstMovies) {
            if (lstMovies != null) {
                if (mgAdapter != null) {
                    mgAdapter.setData(lstMovies);
                }
                alMovies = new ArrayList<>();
                alMovies.addAll(lstMovies);
            }
        }
    }
}
