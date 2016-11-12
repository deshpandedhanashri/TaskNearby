package app.tasknearby.yashcreations.com.tasknearby;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

public class Constants {

    /****  Database Projections  ****/
    /* Location Table's PROJECTIONS */
    public static final String PROJECTION_LOC[] = {
            TasksContract.LocationEntry.TABLE_NAME + "." + TasksContract.LocationEntry._ID,
            TasksContract.LocationEntry.COLUMN_PLACE_NAME,
            TasksContract.LocationEntry.COLUMN_COORD_LAT,
            TasksContract.LocationEntry.COLUMN_COORD_LONG,
            TasksContract.LocationEntry.COLUMN_COUNT,
            TasksContract.LocationEntry.COLUMN_HIDDEN
    };
    public static final int COL_PLACE_NAME = 1;
    public static final int COL_LAT = 2;
    public static final int COL_LON = 3;
    public static final int COL_COUNT = 4;
    public static final int COL_HIDDEN = 5;

    /* Tasks Table's PROJECTIONS */
    public static final String PROJECTION_TASKS[] = {
            TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID,
            TasksContract.TaskEntry.COLUMN_TASK_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_COLOR,
            TasksContract.TaskEntry.COLUMN_DONE_STATUS,
            TasksContract.TaskEntry.COLUMN_MIN_DISTANCE,
            TasksContract.TaskEntry.COLUMN_LOCATION_ALARM,
            TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE,
            TasksContract.TaskEntry.COLUMN_SNOOZE_TIME
    };
    public static final int COL_TASK_ID = 0;
    public static final int COL_TASK_NAME = 1;
    public static final int COL_LOCATION_NAME = 2;
    public static final int COL_TASK_COLOR = 3;
    public static final int COL_DONE = 4;
    public static final int COL_MIN_DISTANCE = 5;
    public static final int COL_ALARM = 6;
    public static final int COL_REMIND_DIS = 7;
    public static final int COL_SNOOZE = 8;

    /** Service's Constants **/
    public final static long ActDetectionInterval_ms = 2000;
    public final static long UPDATE_INTERVAL = 5000;
    public final static long FASTEST_INTERVAL = 3000;
    public final static int SMALLEST_DISPLACEMENT = 1;
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public final static String ReceiverIntentExtra = "detectedActivities";
    public final static String ACTIVITY_DETECTION_INTENT_FILTER = "com.commando.taskNearby" + ReceiverIntentExtra;
    public final static String PREF_IS_ALARM_ACTIVITY_RUNNING = "ACTIVITYINFO" ;
    public final static int SNOOZE_TIME_DURATION = 30 * 60 * 1000;       //this is 30 minutes

    public static final String savedLocation = "loc";
    public static final String LatLngExtra = "latLngExtra";
    public static final String TaskID = "TaskID";
    public static final String KEY_EDIT_TASK_NAME = "EDIT_TASK_NAME";
    public static final String KEY_EDIT_TASK_LOCATION = "EDIT_TASK_LOC";
    public static final String KEY_EDIT_ALARM = "EDIT_TASK_ALARM";
    public static final String KEY_EDIT_REMIND_DIS = "EDIT_TASK_REM_DIS";
    public static final String LATITUDE = "lat", LONGITUDE = "lon";


    public static final String ANALYTICS_KEY_APP_OPENED = "key_app_opened" ;
    public static final String ANALYTICS_KEY_TASK_ADDED = "key_task_added" ;
    public static final String ANALYTICS_KEY_ALARM_TRIGGERED = "key_alarm_triggered" ;
    public static final String ANALYTICS_KEY_ALARM_SNOOZED = "key_alarm_snoozed" ;

}
