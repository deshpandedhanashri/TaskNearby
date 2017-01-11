package app.tasknearby.yashcreations.com.tasknearby;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;
import app.tasknearby.yashcreations.com.tasknearby.service.FusedLocationService;

public class AlarmActivity extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener {

    public static final String TAG = AlarmActivity.class.getSimpleName();

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Cursor cursor;
    private Vibrator vibrator;
    private Utility utility = new Utility();
    private FirebaseAnalytics mAnalytics ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mAnalytics = FirebaseAnalytics.getInstance(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        findViewById(R.id.alarm_btn_markDone).setOnClickListener(this);
        findViewById(R.id.alarm_btn_snooze).setOnClickListener(this);
        TextView mTaskNameTV = (TextView) this.findViewById(R.id.alarm_task_name);
        TextView mLocNameTV = (TextView) this.findViewById(R.id.alarm_location);
        TextView mDistanceTV = (TextView) this.findViewById(R.id.alarm_distance);

        Typeface mTfRegular = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        Typeface mTfBold = Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf");
        mTaskNameTV.setTypeface(mTfBold);
        mLocNameTV.setTypeface(mTfRegular);
        mDistanceTV.setTypeface(Typeface.DEFAULT_BOLD);

        String ID = getIntent().getStringExtra(Constants.TaskID);
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;

        cursor = this.getContentResolver().query(uri, Constants.PROJECTION_TASKS,
                TasksContract.TaskEntry._ID + "=?",
                new String[]{ID}, null);

        if (cursor != null && cursor.moveToFirst()) {
            mTaskNameTV.setText(cursor.getString(Constants.COL_TASK_NAME));
            mDistanceTV.setText(utility.getDistanceDisplayString(this, cursor.getInt(Constants.COL_MIN_DISTANCE)));
            mLocNameTV.setText(cursor.getString(Constants.COL_LOCATION_NAME));
            ((MapFragment) getFragmentManager().findFragmentById(R.id.alarm_map)).getMapAsync(this);
        }
        mAnalytics.logEvent(Constants.ANALYTICS_KEY_ALARM_TRIGGERED,new Bundle());
    }

    @Override
    public void onClick(View v) {
        ContentValues taskValues = new ContentValues();
        switch (v.getId()) {
            case R.id.alarm_btn_markDone:
                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();
                taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, "true");
                AlarmActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID + "=?",
                        new String[]{cursor.getString(Constants.COL_TASK_ID)}
                );
                cursor.close();
                finish();
                break;

            case R.id.alarm_btn_snooze:
                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();
                taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, System.currentTimeMillis());
                AlarmActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID + "=?",
                        new String[]{cursor.getString(Constants.COL_TASK_ID)}
                );
                cursor.close();
                mAnalytics.logEvent(Constants.ANALYTICS_KEY_ALARM_SNOOZED,new Bundle());
                finish();
                break;
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            Log.e(TAG, "onMapReady: null map returned");
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                utility.getLatLngByPlaceName(this,
                        cursor.getString(Constants.COL_LOCATION_NAME)), 15)
        );
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_red_a400_36dp))
                .anchor(0.5f, 1.0f)
                .position(utility.getLatLngByPlaceName(this, cursor.getString(Constants.COL_LOCATION_NAME))));
    }


    @Override
    protected void onStart() {
        Log.e(TAG, "onStart: ");
        super.onStart();
        FusedLocationService.isAlarmRunning = true;
        long pattern[] = {1000, 1000};
        vibrator.vibrate(pattern, 0);
        PlaySoundTask m = new PlaySoundTask();
        m.execute();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop: ");
        super.onStop();
        FusedLocationService.isAlarmRunning = false;
//        if (cursor != null && !cursor.isClosed())
//            cursor.close();
        if (vibrator.hasVibrator())
            vibrator.cancel();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "OnDestroy");
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        if (vibrator.hasVibrator())
            vibrator.cancel();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        super.onDestroy();
    }

    public class PlaySoundTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void[] objects) {
            int alarmTone = R.raw.alarm;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AlarmActivity.this);
            String temp = prefs.getString(getString(R.string.pref_tone_key), null);
            Uri uri;

            if (temp == null)
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            else
                uri = Uri.parse(temp);

            try {
                if (uri != null)
                    mMediaPlayer.setDataSource(AlarmActivity.this, uri);
                else
                    mMediaPlayer = MediaPlayer.create(AlarmActivity.this, alarmTone);

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered while playing Alarm!");
            }
            return null;
        }
    }

}

/*










    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        mMarkDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();

                ContentValues taskValues = new ContentValues();
//                taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, cursor.getString(Constants.COL_TASK_NAME));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, cursor.getString(Constants.COL_LOCATION_NAME));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, cursor.getInt(Constants.COL_TASK_COLOR));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, cursor.getString(Constants.COL_ALARM));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, cursor.getInt(Constants.COL_MIN_DISTANCE));
                taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, "true");
//                taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, cursor.getString(Constants.COL_SNOOZE));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, cursor.getString(Constants.COL_REMIND_DIS));

                AlarmActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID + "=?",
                        new String[]{cursor.getString(Constants.COL_TASK_ID)}
                );
                cursor.close();
                finish();
            }
        });

        mSnoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();

                ContentValues taskValues = new ContentValues();

//                taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, cursor.getString(Constants.COL_TASK_NAME));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, cursor.getString(Constants.COL_LOCATION_NAME));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, cursor.getInt(Constants.COL_TASK_COLOR));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, cursor.getString(Constants.COL_ALARM));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, cursor.getInt(Constants.COL_MIN_DISTANCE));
//                taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, cursor.getString(Constants.COL_DONE));
                taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, System.currentTimeMillis());
//                taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, cursor.getString(Constants.COL_REMIND_DIS));

                AlarmActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID + "=?",
                        new String[]{cursor.getString(Constants.COL_TASK_ID)}
                );
                cursor.close();
                finish();
            }
        });
    }




    @Override
    protected void onPause() {
        Log.e("sd", "OnPause........=====>");
//        vibrator.cancel();
//        if (mMediaPlayer.isPlaying())
//            mMediaPlayer.stop();
//        finish();
//        FusedLocationService.isAlarmRunning = false ;
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("TAG", "onStart:================> " );
        long pattern[] = {1000, 1000 } ;
        vibrator.vibrate(pattern, 0);
        PlaySoundTask m = new PlaySoundTask();
        m.execute();
    }
    @Override
    protected void onStop() {
        Log.e("TAG", "=======================>onStop: " );
        super.onStop();
//        if (cursor != null && !cursor.isClosed())
//            cursor.close();
        if (vibrator.hasVibrator())
            vibrator.cancel();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        Log.e("TAG", "OnDestroy======================");
        if (cursor != null && !cursor.isClosed())
            cursor.close();
        if (vibrator.hasVibrator())
            vibrator.cancel();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        super.onDestroy();
    }
}
*/