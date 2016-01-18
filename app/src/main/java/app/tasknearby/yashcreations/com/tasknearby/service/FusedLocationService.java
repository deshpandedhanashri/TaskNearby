package app.tasknearby.yashcreations.com.tasknearby.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import app.tasknearby.yashcreations.com.tasknearby.AlarmActivity;
import app.tasknearby.yashcreations.com.tasknearby.DetailActivity;
import app.tasknearby.yashcreations.com.tasknearby.R;
import app.tasknearby.yashcreations.com.tasknearby.Utility;
import app.tasknearby.yashcreations.com.tasknearby.Constants;
import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

/**
 * Created by Yash on 28/05/15.
 */
public class FusedLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {

    Cursor c;
    String placeName;
    boolean mReceivingLocationUpdates = false;
    int placeDistance;
    int ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    final String TAG = "FusedLocationService";
    private ActivityDetectionReceiver mReceiver;
    Utility utility = new Utility();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "FusedService Started!");

        if (checkPlayServices()) {
            buildGoogleApiClient();

            mReceiver = new ActivityDetectionReceiver();                //Registering Activity Recognition Receiver
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.INTENT_FILTER));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String pref_string = prefs.getString(getString(R.string.pref_accuracy_key), getString(R.string.pref_accuracy_default));
            if (pref_string.equals(getString(R.string.pref_accuracy_balanced)))
                ACCURACY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

            mLocationRequest = new LocationRequest();
            createLocationRequest();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        return START_STICKY;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, null,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mReceivingLocationUpdates = true;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mReceivingLocationUpdates = false;
    }

    protected void createLocationRequest() {
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FATEST_INTERVAL);
        mLocationRequest.setPriority(ACCURACY);
        mLocationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT);
    }

    protected void startActivityUpdates() {
        if (!mGoogleApiClient.isConnected())
            return;

        ActivityRecognition.ActivityRecognitionApi
                .requestActivityUpdates(mGoogleApiClient, Constants.ActDetectionInterval_ms, getPendingIntent())
                .setResultCallback(this);
    }

    protected void stopActivityUpdates() {
        if (!mGoogleApiClient.isConnected())
            return;

        ActivityRecognition.ActivityRecognitionApi
                .removeActivityUpdates(mGoogleApiClient, getPendingIntent())
                .setResultCallback(this);
    }

    protected PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, ActivityDetectionIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void updateDatabaseDistance(int placeDistance) {

        ContentValues taskValues = new ContentValues();
        taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, c.getString(Constants.COL_TASK_NAME));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, c.getString(Constants.COL_LOCATION_NAME));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, c.getInt(Constants.COL_TASK_COLOR));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, c.getString(Constants.COL_ALARM));
        taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, placeDistance);
        taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, c.getString(Constants.COL_DONE));

        this.getContentResolver().update(
                TasksContract.TaskEntry.CONTENT_URI,
                taskValues, TasksContract.TaskEntry._ID + "=?",
                new String[]{c.getString(Constants.COL_TASK_ID)}
        );
    }

    public void showNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constants.TaskID, c.getString(Constants.COL_TASK_ID));
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(c.getString(Constants.COL_TASK_NAME))
                .setContentText(c.getString(Constants.COL_LOCATION_NAME))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

       if(Build.VERSION.SDK_INT<16)
           notificationManager.notify(0, notificationBuilder.getNotification());
       else
           notificationManager.notify(0,notificationBuilder.build());
    }

    @Override
    public void onDestroy() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            stopActivityUpdates();
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        startLocationUpdates();
        startActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

/**********
 * Main method that decides what to do! *
 * *******/
    @Override
    public void onLocationChanged(Location loc)
    {
        c = this.getContentResolver().query(TasksContract.TaskEntry.CONTENT_URI,              //Querying the databse for all tasks
                Constants.PROJECTION_TASKS,null, null, null);

        int remindDistance;
        boolean notMarkedDone;

        while (c.moveToNext()) {
            placeName = c.getString(Constants.COL_LOCATION_NAME);
            remindDistance=c.getInt(Constants.COL_REMIND_DIS);
            notMarkedDone=c.getString(Constants.COL_DONE).equals("false");
            placeDistance = utility.getDistanceByPlaceName(placeName, loc, this);

            updateDatabaseDistance(placeDistance);                                           // Put the new distance into the database

            if ((placeDistance <= remindDistance)
                    && (placeDistance != 0)
                    && notMarkedDone
                    && !isAlreadyRunning()
                    && !isSnoozed())
            {
                showNotification();
                if (c.getString(Constants.COL_ALARM).equals("true"))
                {
                    Log.e(TAG, "Starting Alarm Activity=====");
                    Intent alarmIntent = new Intent(this, AlarmActivity.class);
                    alarmIntent.putExtra(Constants.TaskID, c.getString(Constants.COL_TASK_ID));
                    alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(alarmIntent);
                }
            }
        }
        c.close();
    }

    public boolean isAlreadyRunning() {
        SharedPreferences sp = getSharedPreferences("ACTIVITYINFO", MODE_PRIVATE);
        boolean active = sp.getBoolean("active", false);
        Log.e(TAG, "From SharedPrefs we got  " + active);
        if(active==true)
            Log.e(TAG,"Already Running Alarm....");
        return active;
    }

    public boolean isSnoozed() {
        if (System.currentTimeMillis() < c.getLong(Constants.COL_SNOOZE))
            return true;
        return false;
    }                 //TODO:Implement this


    @Override
    public void onResult(Status status) {
        if (status.isSuccess())
            Log.e(TAG, "Activity Detection Initiated Successfully");
        else
            Log.e(TAG, "Activity Detection Failed!");
    }

    public class ActivityDetectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "BroadCast Received");

            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(Constants.ReceiverIntentExtra);
            int conf;

            for (DetectedActivity i : detectedActivities)
            {
                conf = i.getConfidence();
                switch (i.getType())
                {
                    case DetectedActivity.STILL:        //The Device is Still
                        if (conf > 50 && mReceivingLocationUpdates) {
                            stopLocationUpdates();
                            Log.e(TAG, "Still,hence Stopping Location Updates!");
                        }
                        break;

                    case DetectedActivity.IN_VEHICLE:
                        if (conf > 50) {
                            restartLocationUpdates(5000);
                        }
                        break;
                    case DetectedActivity.ON_BICYCLE:
                    case DetectedActivity.RUNNING:
                        if (conf > 60)
                            restartLocationUpdates(5000);
                        else if (conf > 50)
                            restartLocationUpdates(10000);

                        break;
                    case DetectedActivity.WALKING:
                    case DetectedActivity.ON_FOOT:
                        if (conf > 50)
                            restartLocationUpdates(15000);
                        break;
                    case DetectedActivity.UNKNOWN:
                        if (conf > 60)
                            restartLocationUpdates(10000);
                        break;
                }
            }
        }

        void restartLocationUpdates(long m) {

            if (Constants.UPDATE_INTERVAL != m || !mReceivingLocationUpdates) {
                Log.i(TAG, "Restarting with UPDATE_INTERVAL= " + m);
                Constants.UPDATE_INTERVAL = m;
                createLocationRequest();

                if (mReceivingLocationUpdates)
                    stopLocationUpdates();
                startLocationUpdates();
            } else
                Log.i(TAG, "Update Interval Is Same as before ,So not restarting!");
        }
    }

    public String getActivityString(int detectedActivityType) {     //JUST for Testing

        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot1";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.STILL:
                return "still ";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.WALKING:
                return "walking";
            default:
                return "UNDETECTABLE";
        }
    }

}
