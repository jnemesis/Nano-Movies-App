package com.otatech.android.nanomoviesapp.movies.utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract {

    public static final String CONTENT = "com.otatech.android.nanomoviesapp.movies";

    public static final Uri BASEURI = Uri.parse("content://" + CONTENT);

    public static final String MOVPATH = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri MOVIEURI =
                BASEURI.buildUpon().appendPath(MOVPATH).build();

        public static final String MOVIETYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT + "/" + MOVPATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT + "/" + MOVPATH;

        public static final String TABLE = "movie";

        public static final String MOVIEID = "movie_id";
        public static final String MOVIETITLE = "title";
        public static final String MOVIECOVER = "image";
        public static final String MOVIEPOSTER = "image2";
        public static final String MOVIEPLOT = "overview";
        public static final String MOVIERATING = "rating";
        public static final String MOVIERELEASE = "date";

        public static Uri buildMovieUri(long lonId) {
            return ContentUris.withAppendedId(MOVIEURI, lonId);
        }
    }
}