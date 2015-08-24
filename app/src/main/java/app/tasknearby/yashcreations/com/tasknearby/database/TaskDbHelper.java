package app.tasknearby.yashcreations.com.tasknearby.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Yash on 22/04/15.
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    static public final int DATABASE_VERSION = 1;
    static public String DATABASE_NAME = "task_database.db";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_LOCATION_TABLE = " CREATE TABLE " + TasksContract.LocationEntry.TABLE_NAME + "(" +
                TasksContract.LocationEntry._ID + " INTEGER PRIMARY KEY, " +
                TasksContract.LocationEntry.COLUMN_PLACE_NAME + " TEXT UNIQUE NOT NULL, " +
                TasksContract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                TasksContract.LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL" + ");";

        sqLiteDatabase.execSQL(CREATE_LOCATION_TABLE);

        final String CREATE_TASKS_TABLE = " CREATE TABLE " + TasksContract.TaskEntry.TABLE_NAME + "(" +
                TasksContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TasksContract.TaskEntry.COLUMN_TASK_NAME + " TEXT NOT NULL, " +
                TasksContract.TaskEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, " +
                TasksContract.TaskEntry.COLUMN_LOCATION_ALARM + " TEXT NOT NULL, " +
                TasksContract.TaskEntry.COLUMN_LOCATION_COLOR + " TEXT NOT NULL, " +
                TasksContract.TaskEntry.COLUMN_MIN_DISTANCE + " INTEGER NOT NULL, " +
                TasksContract.TaskEntry.COLUMN_DONE_STATUS + " TEXT NOT NULL DEFAULT 'false', "+
                TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE + " INTEGER NOT NULL, "+
                TasksContract.TaskEntry.COLUMN_SNOOZE_TIME + " TEXT NOT NULL, "+

                " FOREIGN KEY (" + TasksContract.TaskEntry.COLUMN_LOCATION_NAME + ") REFERENCES " +
                TasksContract.LocationEntry.TABLE_NAME + "(" + TasksContract.LocationEntry.COLUMN_PLACE_NAME + ")" +
                ");";

        sqLiteDatabase.execSQL(CREATE_TASKS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TasksContract.LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TasksContract.TaskEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
