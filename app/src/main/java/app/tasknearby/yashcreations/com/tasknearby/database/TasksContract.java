package app.tasknearby.yashcreations.com.tasknearby.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Yash on 22/04/15.
 */
public class TasksContract {


    public static final String CONTENT_AUTHORITY = "com.yashcreations.tasknearby.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static String PATH_TASKS = "tasks";
    public static String PATH_LOCATION = "location";

    public static final class LocationEntry implements BaseColumns {

        public static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + '/' + CONTENT_AUTHORITY + '/' + PATH_LOCATION;
        //public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + '/' + CONTENT_AUTHORITY + '/' + PATH_LOCATION;

        public static final String TABLE_NAME = "location";
        public static final String COLUMN_PLACE_NAME = "location_name";
        public static final String COLUMN_COORD_LAT = "coord_Lat";
        public static final String COLUMN_COORD_LONG = "coord_Lon";
        public static final String COLUMN_COUNT = "use_count";
        public static final String COLUMN_HIDDEN = "hidden";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    public static final class TaskEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + '/' + CONTENT_AUTHORITY + '/' + PATH_TASKS;
        //public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + '/' + CONTENT_AUTHORITY + '/' + PATH_WEATHER + '/' + PATH_LOCATION + '#';

        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_TASK_NAME = "task_name";
        public static final String COLUMN_LOCATION_NAME = "place";
        public static final String COLUMN_LOCATION_COLOR = "color";
        public static final String COLUMN_LOCATION_ALARM = "alarm";
        public static final String COLUMN_MIN_DISTANCE = "min_dist";
        public static final String COLUMN_DONE_STATUS="done";
        public static final String COLUMN_REMIND_DISTANCE="remind_distance";
        public static final String COLUMN_SNOOZE_TIME="snooze_time";

        public static Uri buildTaskUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
