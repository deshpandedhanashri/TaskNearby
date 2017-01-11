package app.tasknearby.yashcreations.com.tasknearby.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import app.tasknearby.yashcreations.com.tasknearby.Constants;

/**
 * Created by Yash on 13/10/15.
 */
public class ActivityDetectionService extends IntentService {
    public final String TAG = "ActivityDetection";

    public ActivityDetectionService() { super("ActivityDetectionService"); }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();

        Intent localIntent = new Intent(Constants.ACTIVITY_DETECTION_INTENT_FILTER);
        localIntent.putParcelableArrayListExtra(Constants.ReceiverIntentExtra, detectedActivities);
        LocalBroadcastManager local = LocalBroadcastManager.getInstance(this);
        if (local == null)
            Log.i(TAG, "Local Broadcast Manager is NUll");
        local.sendBroadcast(localIntent);
        Log.i(TAG, "Activity Broadcast Sent!");
    }
}
