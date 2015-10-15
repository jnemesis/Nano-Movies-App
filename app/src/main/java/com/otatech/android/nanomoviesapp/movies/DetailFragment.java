package com.otatech.android.nanomoviesapp.movies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linearlistview.LinearListView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    public static final String TAG = DetailFragment.class.getSimpleName();

    static final String DETAIL_MOVIE = "DETAIL_MOVIE";

    private Movie movie;

    private ImageView ivCover;

    private TextView tvTitle;
    private TextView tvPlot;
    private TextView tvRelease;
    private TextView tvRatings;

    private LinearListView llvTrailers;
    private LinearListView llvReviews;

    private CardView cvReviewSpace;
    private CardView cvTrailerSpace;

    private Adapters.TrailerAdapter trailerAdapter;
    private Adapters.ReviewAdapter reviewAdapter;

    private ScrollView mDetailLayout;

    private Toast mToast;

    private ShareActionProvider mShareActionProvider;

    // the first trailer video to share
    private Movie.Trailer mTrailer;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (movie != null) {
            inflater.inflate(R.menu.menu_fragment_detail, menu);

            final MenuItem action_favorite = menu.findItem(R.id.action_favorite);
            MenuItem action_share = menu.findItem(R.id.action_share);
            /*
            action_favorite.setIcon(Utility.intWinner(getActivity(), movie.getIntId()) == 1 ?
                    R.drawable.abc_btn_rating_star_on_mtrl_alpha :
                    R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            */
            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    return Movie.Utility.intWinner(getActivity(), movie.getIntId());
                }

                @Override
                protected void onPostExecute(Integer isFavorited) {
                    action_favorite.setIcon(isFavorited == 1 ?
                            R.drawable.prize_winner :
                            R.drawable.tool);
                }
            }.execute();

            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_share);

            if (mTrailer != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_favorite:
                if (movie != null) {
                    // check if movie is in favorites or not
                    new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... params) {
                            return Movie.Utility.intWinner(getActivity(), movie.getIntId());
                        }

                        @Override
                        protected void onPostExecute(Integer isFavorited) {
                            // if it is in favorites
                            if (isFavorited == 1) {
                                // delete from favorites
                                new AsyncTask<Void, Void, Integer>() {
                                    @Override
                                    protected Integer doInBackground(Void... params) {
                                        return getActivity().getContentResolver().delete(
                                                DbContract.MovieEntry.MOVIEURI,
                                                DbContract.MovieEntry.MOVIEID + " = ?",
                                                new String[]{Integer.toString(movie.getIntId())}
                                        );
                                    }

                                    @Override
                                    protected void onPostExecute(Integer rowsDeleted) {
                                        item.setIcon(R.drawable.tool);
                                        if (mToast != null) {
                                            mToast.cancel();
                                        }
                                        mToast = Toast.makeText(getActivity(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT);
                                        mToast.show();
                                    }
                                }.execute();
                            }
                            // if it is not in favorites
                            else {
                                // add to favorites
                                new AsyncTask<Void, Void, Uri>() {
                                    @Override
                                    protected Uri doInBackground(Void... params) {
                                        ContentValues values = new ContentValues();

                                        values.put(DbContract.MovieEntry.MOVIEID, movie.getIntId());
                                        values.put(DbContract.MovieEntry.MOVIETITLE, movie.getStrTitle());
                                        values.put(DbContract.MovieEntry.MOVIECOVER, movie.getStrCover());
                                        values.put(DbContract.MovieEntry.MOVIEPOSTER, movie.getStrBackDrop());
                                        values.put(DbContract.MovieEntry.MOVIEPLOT, movie.getStrPlot());
                                        values.put(DbContract.MovieEntry.MOVIERATING, movie.getIntRatings());
                                        values.put(DbContract.MovieEntry.MOVIERELEASE, movie.getStrRelease());

                                        return getActivity().getContentResolver().insert(DbContract.MovieEntry.MOVIEURI,
                                                values);
                                    }

                                    @Override
                                    protected void onPostExecute(Uri returnUri) {
                                        item.setIcon(R.drawable.prize_winner);
                                        if (mToast != null) {
                                            mToast.cancel();
                                        }
                                        mToast = Toast.makeText(getActivity(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                                        mToast.show();
                                    }
                                }.execute();
                            }
                        }
                    }.execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            movie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mDetailLayout = (ScrollView) rootView.findViewById(R.id.detail_layout);

        if (movie != null) {
            mDetailLayout.setVisibility(View.VISIBLE);
        } else {
            mDetailLayout.setVisibility(View.INVISIBLE);
        }

        ivCover = (ImageView) rootView.findViewById(R.id.detail_image);

        tvTitle = (TextView) rootView.findViewById(R.id.detail_title);
        tvPlot = (TextView) rootView.findViewById(R.id.detail_overview);
        tvRelease = (TextView) rootView.findViewById(R.id.detail_date);
        tvRatings = (TextView) rootView.findViewById(R.id.detail_vote_average);

        llvTrailers = (LinearListView) rootView.findViewById(R.id.detail_trailers);
        llvReviews = (LinearListView) rootView.findViewById(R.id.detail_reviews);

        cvReviewSpace = (CardView) rootView.findViewById(R.id.detail_reviews_cardview);
        cvTrailerSpace = (CardView) rootView.findViewById(R.id.detail_trailers_cardview);

        trailerAdapter = new Adapters.TrailerAdapter(getActivity(), new ArrayList<Movie.Trailer>());
        llvTrailers.setAdapter(trailerAdapter);

        llvTrailers.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view,
                                    int position, long id) {
                Movie.Trailer trailer = trailerAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getStrKey()));
                startActivity(intent);
            }
        });

        reviewAdapter = new Adapters.ReviewAdapter(getActivity(), new ArrayList<Movie.Review>());
        llvReviews.setAdapter(reviewAdapter);

        if (movie != null) {

            String image_url = Movie.Utility.strBuildURL(342, movie.getStrBackDrop());

            Glide.with(this).load(image_url).into(ivCover);

            tvTitle.setText(movie.getStrTitle());
            tvPlot.setText(movie.getStrPlot());

            String movie_date = movie.getStrRelease();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                String date = DateUtils.formatDateTime(getActivity(),
                        formatter.parse(movie_date).getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                tvRelease.setText(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvRatings.setText(Integer.toString(movie.getIntRatings()));
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (movie != null) {
            new FetchTrailersTask().execute(Integer.toString(movie.getIntId()));
            new FetchReviewsTask().execute(Integer.toString(movie.getIntId()));
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getStrTitle() + " " +
                "http://www.youtube.com/watch?v=" + mTrailer.getStrKey());
        return shareIntent;
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Movie.Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Movie.Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Movie.Trailer> results = new ArrayList<>();

            for(int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                // Only show Trailers which are on Youtube
                if (trailer.getString("site").contentEquals("YouTube")) {
                    Movie.Trailer trailerModel = new Movie.Trailer(trailer);
                    results.add(trailerModel);
                }
            }

            return results;
        }

        @Override
        protected List<Movie.Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie.Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    cvTrailerSpace.setVisibility(View.VISIBLE);
                    if (trailerAdapter != null) {
                        trailerAdapter.clear();
                        for (Movie.Trailer trailer : trailers) {
                            trailerAdapter.add(trailer);
                        }
                    }

                    mTrailer = trailers.get(0);
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareMovieIntent());
                    }
                }
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Movie.Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Movie.Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            List<Movie.Review> results = new ArrayList<>();

            for(int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                results.add(new Movie.Review(review));
            }

            return results;
        }

        @Override
        protected List<Movie.Review> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie.Review> reviews) {
            if (reviews != null) {
                if (reviews.size() > 0) {
                    cvReviewSpace.setVisibility(View.VISIBLE);
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();
                        for (Movie.Review review : reviews) {
                            reviewAdapter.add(review);
                        }
                    }
                }
            }
        }
    }
}