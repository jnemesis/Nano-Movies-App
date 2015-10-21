package com.otatech.android.nanomoviesapp.movies.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DBVERSION = 1;

    static final String DBNAME = "movie.db";

    public DbHelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + DbContract.MovieEntry.TABLE + " (" +
                DbContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DbContract.MovieEntry.MOVIEID + " INTEGER NOT NULL, " +
                DbContract.MovieEntry.MOVIETITLE + " TEXT NOT NULL, " +
                DbContract.MovieEntry.MOVIECOVER + " TEXT, " +
                DbContract.MovieEntry.MOVIEPOSTER + " TEXT, " +
                DbContract.MovieEntry.MOVIEPLOT + " TEXT, " +
                DbContract.MovieEntry.MOVIERATING + " INTEGER, " +
                DbContract.MovieEntry.MOVIERELEASE + " TEXT);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sdlLiteDb, int intOldVer, int intNewVer) {
        sdlLiteDb.execSQL("DROP TABLE IF EXISTS " + DbContract.MovieEntry.TABLE);
        onCreate(sdlLiteDb);
    }
}
