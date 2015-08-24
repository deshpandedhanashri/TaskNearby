package app.tasknearby.yashcreations.com.tasknearby.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import app.tasknearby.yashcreations.com.tasknearby.AlarmActivity;
import app.tasknearby.yashcreations.com.tasknearby.DetailActivity;
import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.Utility;
import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


/**
 * Created by Yash on 28/05/15.
 */
public class FusedLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
                                                LocationListener{

    private static final String TAG = FusedLocationService.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
     // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private long UPDATE_INTERVAL = 5000;
    private long FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 1; // 1 meter

    Cursor c;



    String PROJECTION[] = {
            TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID,
            TasksContract.TaskEntry.COLUMN_TASK_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_COLOR,
            TasksContract.TaskEntry.COLUMN_LOCATION_ALARM,
            TasksContract.TaskEntry.COLUMN_DONE_STATUS,
            TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE,
            TasksContract.TaskEntry.COLUMN_SNOOZE_TIME


    };

    static final int COL_TASK_ID = 0;
    static final int COL_TASK_NAME = 1;
    static final int COL_LOCATION_NAME = 2;
    static final int COL_TASK_COLOR = 3;
    static final int COL_ALARM = 4;
    static final int COL_DONE=5;
    static final int COL_REMIND_DISTANCE=6;
    static final int COL_SNOOZE_TIME=7;

    String placeName;
    int placeDistance;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        Log.e(TAG, "===========Service Started=============");
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }
        UPDATE_INTERVAL=Utility.getUpdateInterval(this);
        FATEST_INTERVAL=(Utility.getUpdateInterval(this)-3000);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
        return START_STICKY;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,null,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                            }
            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location loc){

        Log.e(TAG,"==============RUNNING===============");

        c=this.getContentResolver().query(TasksContract.TaskEntry.CONTENT_URI,              //QUERYING THE DATABASE FOR TASKS
                PROJECTION, null, null, null);


        while (c.moveToNext())
        {
            placeName = c.getString(COL_LOCATION_NAME);
            placeDistance = Utility.getDistanceByPlaceName(placeName, loc, this);
            updateDatabaseDistance(placeDistance);                  // PUT THE NEW MIN DISTANCE INTO DATABASE

            if ((placeDistance <= c.getInt(COL_REMIND_DISTANCE))&&(placeDistance != 0)&&(c.getString(COL_DONE).equals("false")))
            {   showNotification();

                if(c.getString(COL_ALARM).equals("true")&&(c.getLong(COL_SNOOZE_TIME)<System.currentTimeMillis()))                         //Alarm ActivityTriggering Module
                {
                    asyncAlarm m = new asyncAlarm();
                    m.execute(c.getString(COL_TASK_ID));
                }
            }
        }

        c.close();

    }
    public class asyncAlarm extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... TaskID) {
            Intent alarmIntent=new Intent(FusedLocationService.this, AlarmActivity.class);
            alarmIntent.putExtra("TaskID",TaskID[0]);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(alarmIntent);
            return null;
        }
    }


    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
            }

        super.onDestroy();
    }

    public void updateDatabaseDistance(int placeDistance)
    {

        ContentValues taskValues = new ContentValues();

        taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, c.getString(COL_TASK_NAME));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME,c.getString(COL_LOCATION_NAME));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, c.getInt(COL_TASK_COLOR));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM,c.getString(COL_ALARM));
        taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, placeDistance);
        taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS,c.getString(COL_DONE));

        this.getContentResolver().update(
                TasksContract.TaskEntry.CONTENT_URI,
                taskValues, TasksContract.TaskEntry._ID + "=?",
                new String[]{c.getString(COL_TASK_ID)}
        );

    }




    public void showNotification()
    {   NotificationManager notificationManager=(NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this,DetailActivity.class);
        intent.putExtra("TaskID",c.getString(COL_TASK_ID));
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification n  = new Notification.Builder(this)
                .setContentTitle(c.getString(COL_TASK_NAME))
                .setContentText(c.getString(COL_LOCATION_NAME))
                .setSmallIcon(R.drawable.marker1)
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        notificationManager.notify(0, n);
    }

}
