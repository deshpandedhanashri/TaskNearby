package app.tasknearby.yashcreations.com.tasknearby.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Yash on 22/04/15.
 */
public class TasksProvider extends ContentProvider {
    public static TaskDbHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static final int TASKS = 100;
    static final int LOCATION = 300;

    @Override
    public boolean onCreate() {
        Log.e("ContentProvider", "getting Instance");
        mOpenHelper = TaskDbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case TASKS:
                retCursor = mOpenHelper.getReadableDatabase().query(TasksContract.TaskEntry.TABLE_NAME, projection, selection, selArgs, null, null, sortOrder);
                break;
            case LOCATION:
                retCursor = mOpenHelper.getReadableDatabase().query(TasksContract.LocationEntry.TABLE_NAME, projection, selection, selArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    static UriMatcher buildUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = TasksContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, TasksContract.PATH_TASKS, TASKS);
        matcher.addURI(authority, TasksContract.PATH_LOCATION, LOCATION);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                return TasksContract.TaskEntry.CONTENT_TYPE;
            case LOCATION:
                return TasksContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        Uri returnUri;
        Log.v("++LOG++", "=====================IN INsErt......URI matcher resulted in  " + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
            case TASKS:
                _id = db.insert(TasksContract.TaskEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = TasksContract.TaskEntry.buildTaskUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case LOCATION:
                _id = db.insert(TasksContract.LocationEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = TasksContract.LocationEntry.buildLocationUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selArgs) {
        int rowsDeleted = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case TASKS:
                rowsDeleted = db.delete(TasksContract.TaskEntry.TABLE_NAME, selection, selArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(TasksContract.LocationEntry.TABLE_NAME, selection, selArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        db.close();

        return rowsDeleted;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                rowsUpdated = db.update(TasksContract.TaskEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case LOCATION:
                rowsUpdated = db.update(TasksContract.LocationEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri :" + uri);
        }
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return rowsUpdated;
    }
}
