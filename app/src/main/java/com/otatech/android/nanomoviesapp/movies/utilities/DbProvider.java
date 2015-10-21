package com.otatech.android.nanomoviesapp.movies.utilities;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class DbProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = uriMatcherBuild();
    private DbHelper dbHelperOpen;

    static final int MOVIE = 100;

    static UriMatcher uriMatcherBuild() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String strAuth = DbContract.CONTENT;

        // For each type of MOVIEURI you want to add, create a corresponding code.
        uriMatcher.addURI(strAuth, DbContract.MOVPATH, MOVIE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelperOpen = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor curRet;
        switch (uriMatcher.match(uri)) {
            case MOVIE: {
                curRet = dbHelperOpen.getReadableDatabase().query(
                        DbContract.MovieEntry.TABLE, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("URI UNKNOWN: " + uri + "STUPID HEAD!!!");
        }

        curRet.setNotificationUri(getContext().getContentResolver(), uri);
        return curRet;
    }

    @Override
    public String getType(Uri uri) {
        final int intMatch = uriMatcher.match(uri);

        switch (intMatch) {
            case MOVIE:
                return DbContract.MovieEntry.MOVIETYPE;
            default:
                throw new UnsupportedOperationException("URI UNKNOWN: " + uri + "STUPID HEAD!!!");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase sqLiteDb = dbHelperOpen.getWritableDatabase();
        Uri uriRet;

        switch (uriMatcher.match(uri)) {
            case MOVIE: {
                long _id = sqLiteDb.insert(DbContract.MovieEntry.TABLE, null, values);
                if (_id > 0) {
                    uriRet = DbContract.MovieEntry.buildMovieUri(_id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("URI UNKNOWN: " + uri + "...STUPID HEAD!!!");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return uriRet;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase sqLiteDb = dbHelperOpen.getWritableDatabase();
        int intRowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (uriMatcher.match(uri)) {
            case MOVIE:
                intRowsDeleted = sqLiteDb.delete(
                        DbContract.MovieEntry.TABLE, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("URI UNKNOWN: " + uri + "...STUPID HEAD!!!");
        }
        // Because a null deletes all rows
        if (intRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return intRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase slLiteDb = dbHelperOpen.getWritableDatabase();
        int intUpdatedRow;

        switch (uriMatcher.match(uri)) {
            case MOVIE:
                intUpdatedRow = slLiteDb.update(DbContract.MovieEntry.TABLE, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("URI UNKNOWN: " + uri + "STUPID HEAD!!!");
        }

        if (intUpdatedRow != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return intUpdatedRow;
    }
}
