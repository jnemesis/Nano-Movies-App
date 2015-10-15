package com.otatech.android.nanomoviesapp.movies.utilities;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.otatech.android.nanomoviesapp.movies.MainFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie implements Parcelable {

    private int intId;
    private String strTitle;
    private String strCover;
    private String strBackDrop;
    private String strPlot;
    private int intRatings;
    private String strRelease;

    public Movie() {

    }

    public Movie(JSONObject movie) throws JSONException {
        this.intId = movie.getInt("id");
        this.strTitle = movie.getString("original_title");
        this.strCover = movie.getString("poster_path");
        this.strBackDrop = movie.getString("backdrop_path");
        this.strPlot = movie.getString("overview");
        this.intRatings = movie.getInt("vote_average");
        this.strRelease = movie.getString("release_date");
    }

    public Movie(Cursor cursor) {
        this.intId = cursor.getInt(MainFragment.MOVIEID);
        this.strTitle = cursor.getString(MainFragment.TITLE);
        this.strCover = cursor.getString(MainFragment.COVER);
        this.strBackDrop = cursor.getString(MainFragment.BACKDROP);
        this.strPlot = cursor.getString(MainFragment.PLOT);
        this.intRatings = cursor.getInt(MainFragment.RATING);
        this.strRelease = cursor.getString(MainFragment.RELEASE);
    }

    public int getIntId() {
        return intId;
    }

    public String getStrTitle() {
        return strTitle;
    }

    public String getStrCover() {
        return strCover;
    }

    public String getStrBackDrop() {
        return strBackDrop;
    }

    public String getStrPlot() {
        return strPlot;
    }

    public int getIntRatings() {
        return intRatings;
    }

    public String getStrRelease() {
        return strRelease;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int intFlags) {
        parcel.writeInt(intId);
        parcel.writeString(strTitle);
        parcel.writeString(strCover);
        parcel.writeString(strBackDrop);
        parcel.writeString(strPlot);
        parcel.writeInt(intRatings);
        parcel.writeString(strRelease);
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel parRead) {
        intId = parRead.readInt();
        strTitle = parRead.readString();
        strCover = parRead.readString();
        strBackDrop = parRead.readString();
        strPlot = parRead.readString();
        intRatings = parRead.readInt();
        strRelease = parRead.readString();
    }

    public static class Review {

        private String strId;
        private String strWriter;
        private String strJibberish;

        public Review() {

        }

        public Review(JSONObject jsonObjReview) throws JSONException {
            this.strId = jsonObjReview.getString("id");
            this.strWriter = jsonObjReview.getString("author");
            this.strJibberish = jsonObjReview.getString("content");
        }

        public String getStrId() { return strId; }

        public String getStrWriter() { return strWriter; }

        public String getStrJibberish() { return strJibberish; }
    }

    public static class Trailer {

        private String strId;
        private String strKey;
        private String strName;
        private String strWebSite;
        private String strType;

        public Trailer() {

        }

        public Trailer(JSONObject jsonObjTrailer) throws JSONException {
            this.strId = jsonObjTrailer.getString("id");
            this.strKey = jsonObjTrailer.getString("key");
            this.strName = jsonObjTrailer.getString("name");
            this.strWebSite = jsonObjTrailer.getString("site");
            this.strType = jsonObjTrailer.getString("type");
        }

        public String getStrId() {
            return strId;
        }

        public String getStrKey() { return strKey; }

        public String getStrName() { return strName; }

        public String getStrWebSite() { return strWebSite; }

        public String getStrType() { return strType; }
    }

    public static class Utility {

        public static int intWinner(Context context, int intId) {
            Cursor cursor = context.getContentResolver().query(DbContract.MovieEntry.MOVIEURI, null,
                    DbContract.MovieEntry.MOVIEID + " = ?", new String[] { Integer.toString(intId) }, null );
            try {
                int intRows = cursor.getCount();
                cursor.close();
                return intRows;
            } catch (NullPointerException npe) {
                Log.wtf("Cursor NPE: ", "ERROR: " + npe.toString());
            }
            return 0;
        }

        public static String strBuildURL(int intWidth, String strFile) {
            return "http://image.tmdb.org/t/p/w" + Integer.toString(intWidth) + strFile;
        }
    }
}
