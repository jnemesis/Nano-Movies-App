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

    private ScrollView svDetails;

    private Toast toasty;

    private ShareActionProvider shareProvider;

    private Movie.Trailer mtTrailer;

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
            inflater.inflate(R.menu.details_menu, menu);

            final MenuItem miWinnerOrTool = menu.findItem(R.id.action_favorite);
            MenuItem miPirate = menu.findItem(R.id.action_share);

            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void[] params) {
                    return Movie.Utility.intWinner(getActivity(), movie.getIntId());
                }

                @Override
                protected void onPostExecute(Integer isFavorited) {
                    miWinnerOrTool.setIcon(isFavorited == 1 ?
                            R.drawable.prize_winner : R.drawable.tool);
                }
            }.execute();

            shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(miPirate);

            if (mtTrailer != null) {
                shareProvider.setShareIntent(createShareMovieIntent());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int intId = item.getItemId();
        switch (intId) {
            case R.id.action_favorite:
                if (movie != null) {
                    new AsyncTask<Void, Void, Integer>() {
                        @Override
                        protected Integer doInBackground(Void[] params) {
                            return Movie.Utility.intWinner(getActivity(), movie.getIntId());
                        }

                        @Override
                        protected void onPostExecute(Integer intWinner) {
                            if (intWinner == 1) {
                                new AsyncTask<Void, Void, Integer>() {
                                    @Override
                                    protected Integer doInBackground(Void[] params) {
                                        return getActivity().getContentResolver().delete(
                                                DbContract.MovieEntry.MOVIEURI,
                                                DbContract.MovieEntry.MOVIEID + " = ?",
                                                new String[]{Integer.toString(movie.getIntId())}
                                        );
                                    }

                                    @Override
                                    protected void onPostExecute(Integer intDeletedRows) {
                                        item.setIcon(R.drawable.tool);
                                        if (toasty != null) {
                                            toasty.cancel();
                                        }
                                        toasty = Toast.makeText(getActivity(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT);
                                        toasty.show();
                                    }
                                }.execute();
                            }
                            else {
                                new AsyncTask<Void, Void, Uri>() {
                                    @Override
                                    protected Uri doInBackground(Void[] params) {
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
                                    protected void onPostExecute(Uri uriRet) {
                                        item.setIcon(R.drawable.prize_winner);
                                        if (toasty != null) {
                                            toasty.cancel();
                                        }
                                        toasty = Toast.makeText(getActivity(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                                        toasty.show();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundleArgs = getArguments();
        if (bundleArgs != null) {
            movie = bundleArgs.getParcelable(DetailFragment.DETAIL_MOVIE);
        }

        View viewRoot = inflater.inflate(R.layout.details_fragment, container, false);

        svDetails = (ScrollView) viewRoot.findViewById(R.id.detail_layout);

        if (movie != null) {
            svDetails.setVisibility(View.VISIBLE);
        } else {
            svDetails.setVisibility(View.INVISIBLE);
        }

        ivCover = (ImageView) viewRoot.findViewById(R.id.detail_image);

        tvTitle = (TextView) viewRoot.findViewById(R.id.detail_title);
        tvPlot = (TextView) viewRoot.findViewById(R.id.detail_overview);
        tvRelease = (TextView) viewRoot.findViewById(R.id.detail_date);
        tvRatings = (TextView) viewRoot.findViewById(R.id.detail_vote_average);

        llvTrailers = (LinearListView) viewRoot.findViewById(R.id.detail_trailers);
        llvReviews = (LinearListView) viewRoot.findViewById(R.id.detail_reviews);

        cvReviewSpace = (CardView) viewRoot.findViewById(R.id.detail_reviews_cardview);
        cvTrailerSpace = (CardView) viewRoot.findViewById(R.id.detail_trailers_cardview);

        trailerAdapter = new Adapters.TrailerAdapter(getActivity(), new ArrayList<Movie.Trailer>());
        llvTrailers.setAdapter(trailerAdapter);

        llvTrailers.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view, int intPos, long id) {
                Movie.Trailer movTrailer = trailerAdapter.getItem(intPos);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + movTrailer.getStrKey()));
                startActivity(intent);
            }
        });

        reviewAdapter = new Adapters.ReviewAdapter(getActivity(), new ArrayList<Movie.Review>());
        llvReviews.setAdapter(reviewAdapter);

        if (movie != null) {

            String strCoverURL = Movie.Utility.strBuildURL(342, movie.getStrBackDrop());

            Glide.with(this).load(strCoverURL).into(ivCover);

            tvTitle.setText(movie.getStrTitle());
            tvPlot.setText(movie.getStrPlot());

            String strMovieRelease = movie.getStrRelease();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                String strDate = DateUtils.formatDateTime(getActivity(),
                        formatter.parse(strMovieRelease).getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                tvRelease.setText(strDate);
            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            tvRatings.setText(Integer.toString(movie.getIntRatings()));
        }

        return viewRoot;
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
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intentShare.setType("text/plain");
        intentShare.putExtra(Intent.EXTRA_TEXT, movie.getStrTitle() + " " +
                "http://www.youtube.com/watch?v=" + mtTrailer.getStrKey());
        return intentShare;
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Movie.Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Movie.Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject jsonObjTrailer = new JSONObject(jsonStr);
            JSONArray jsonArrTrailer = jsonObjTrailer.getJSONArray("results");

            List<Movie.Trailer> lstTrailerResults = new ArrayList<>();

            for(int i = 0; i < jsonArrTrailer.length(); i++) {
                JSONObject trailer = jsonArrTrailer.getJSONObject(i);
                if (trailer.getString("site").contentEquals("YouTube")) {
                    Movie.Trailer trailerModel = new Movie.Trailer(trailer);
                    lstTrailerResults.add(trailerModel);
                }
            }
            return lstTrailerResults;
        }

        @Override
        protected List<Movie.Trailer> doInBackground(String[] params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String strJSON = null;

            try {
                final String BASEURL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String APIKEY = "api_key";

                Uri uriBuilt = Uri.parse(BASEURL).buildUpon()
                        .appendQueryParameter(APIKEY, getString(R.string.api_key))
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
                Log.wtf(LOG_TAG, "Error ", ioe);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException ioe) {
                        Log.wtf(LOG_TAG, "Error closing stream", ioe);
                    }
                }
            }

            try {
                return getTrailersDataFromJson(strJSON);
            } catch (JSONException je) {
                Log.wtf(LOG_TAG, je.getMessage(), je);
                je.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie.Trailer> lstTrailers) {
            if (lstTrailers != null) {
                if (lstTrailers.size() > 0) {
                    cvTrailerSpace.setVisibility(View.VISIBLE);
                    if (trailerAdapter != null) {
                        trailerAdapter.clear();
                        for (Movie.Trailer trailer : lstTrailers) {
                            trailerAdapter.add(trailer);
                        }
                    }

                    mtTrailer = lstTrailers.get(0);
                    if (shareProvider != null) {
                        shareProvider.setShareIntent(createShareMovieIntent());
                    }
                }
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Movie.Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Movie.Review> getReviewsDataFromJson(String strJSON) throws JSONException {
            JSONObject jsonObjReview = new JSONObject(strJSON);
            JSONArray jsonArrReview = jsonObjReview.getJSONArray("results");

            List<Movie.Review> lstReviewResults = new ArrayList<>();

            for(int i = 0; i < jsonArrReview.length(); i++) {
                JSONObject jsonObject = jsonArrReview.getJSONObject(i);
                lstReviewResults.add(new Movie.Review(jsonObject));
            }

            return lstReviewResults;
        }

        @Override
        protected List<Movie.Review> doInBackground(String[] params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String strJSON = null;

            try {
                final String BASEURL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String APIKEY = "api_key";

                Uri uriBuilt = Uri.parse(BASEURL).buildUpon()
                        .appendQueryParameter(APIKEY, getString(R.string.api_key))
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
                Log.wtf(LOG_TAG, "Error ", ioe);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException ioe) {
                        Log.wtf(LOG_TAG, "Error closing stream", ioe);
                    }
                }
            }

            try {
                return getReviewsDataFromJson(strJSON);
            } catch (JSONException je) {
                Log.wtf(LOG_TAG, je.getMessage(), je);
                je.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie.Review> lstReviews) {
            if (lstReviews != null) {
                if (lstReviews.size() > 0) {
                    cvReviewSpace.setVisibility(View.VISIBLE);
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();
                        for (Movie.Review review : lstReviews) {
                            reviewAdapter.add(review);
                        }
                    }
                }
            }
        }
    }
}