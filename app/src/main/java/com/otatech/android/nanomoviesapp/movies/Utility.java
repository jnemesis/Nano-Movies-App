package com.otatech.android.nanomoviesapp.movies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.otatech.android.nanomoviesapp.movies.data.MovieContract;

public class Utility {

    public static int isFavorited(Context context, int id) {
        Cursor cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", new String[] { Integer.toString(id) }, null );
        try {
            int numRows = cursor.getCount();
            cursor.close();
            return numRows;
        } catch (NullPointerException npe) {
            Log.wtf("Cursor NPE: ", "ERROR: " + npe.toString() );
        }
        return 0;
    }

    public static String buildImageUrl(int width, String fileName) {
        return "http://image.tmdb.org/t/p/w" + Integer.toString(width) + fileName;
    }
}
