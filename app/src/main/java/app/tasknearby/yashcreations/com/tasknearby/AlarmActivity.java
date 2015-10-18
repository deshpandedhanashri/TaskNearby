package app.tasknearby.yashcreations.com.tasknearby;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


public class AlarmActivity extends ActionBarActivity {

    String PROJECTION[] = {
            TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID,
            TasksContract.TaskEntry.COLUMN_TASK_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_COLOR,
            TasksContract.TaskEntry.COLUMN_LOCATION_ALARM,
            TasksContract.TaskEntry.COLUMN_DONE_STATUS,
            TasksContract.TaskEntry.COLUMN_MIN_DISTANCE,
            TasksContract.TaskEntry.COLUMN_SNOOZE_TIME,
            TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE

    };

    static final int COL_TASK_ID = 0;
    static final int COL_TASK_NAME = 1;
    static final int COL_LOCATION_NAME = 2;
    static final int COL_TASK_COLOR = 3;
    static final int COL_ALARM = 4;
    static final int COL_DONE = 5;
    static final int COL_MIN_DISTANCE = 6;
    static final int COL_REMIND_DIS = 7;
    static final int COL_SNOOZE = 8;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    int alarmTone = R.raw.alarm;  //Utility.getPreferredAlarmTone(this);
    Cursor c;
    Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Button stopAlarm = (Button) this.findViewById(R.id.btnStop);
        Button Snooze = (Button) this.findViewById(R.id.snooze);
        TextView taskNameView = (TextView) this.findViewById(R.id.alarm_taskName);
        TextView taskLocView = (TextView) this.findViewById(R.id.alarm_taskLoc);
        TextView taskDisView = (TextView) this.findViewById(R.id.alarmTaskDis);
        LinearLayout baseLayout = (LinearLayout) this.findViewById(R.id.alarmBaseLayout);

        Intent intent = this.getIntent();
        String ID = intent.getStringExtra("TaskID");
        //      Log.e("ID","ID is "+ID);

        Uri uri = TasksContract.TaskEntry.CONTENT_URI;
        c = this.getContentResolver().query(uri, PROJECTION,
                TasksContract.TaskEntry._ID + "=?",
                new String[]{ID}, null);

        if (c.moveToFirst()) {
            taskNameView.setText(c.getString(COL_TASK_NAME));
            taskDisView.setText(Utility.getDistanceDisplayString(this, c.getInt(COL_MIN_DISTANCE)));
            taskLocView.setText(c.getString(COL_LOCATION_NAME));
            baseLayout.setBackgroundColor(c.getInt(COL_TASK_COLOR));

        }
        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();

                ContentValues taskValues = new ContentValues();


                taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, c.getString(COL_TASK_NAME));
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, c.getString(COL_LOCATION_NAME));
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, c.getInt(COL_TASK_COLOR));
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, c.getString(COL_ALARM));
                taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, c.getInt(COL_MIN_DISTANCE));
                taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, "true");
                taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, c.getString(COL_SNOOZE));
                taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, c.getString(COL_REMIND_DIS));


                AlarmActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID + "=?",
                        new String[]{c.getString(COL_TASK_ID)}
                );
                c.close();
                finish();
            }
        });

        Snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();

                ContentValues taskValues = new ContentValues();


                taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, c.getString(COL_TASK_NAME));
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, c.getString(COL_LOCATION_NAME));
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, c.getInt(COL_TASK_COLOR));
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, c.getString(COL_ALARM));
                taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, c.getInt(COL_MIN_DISTANCE));
                taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, c.getString(COL_DONE));
                taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, System.currentTimeMillis() + 4 * 60 * 1000);
                taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, c.getString(COL_REMIND_DIS));


                AlarmActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID + "=?",
                        new String[]{c.getString(COL_TASK_ID)}
                );
                c.close();
                finish();
            }
        });

    }


    public class PlaySoundTask extends AsyncTask

    {
        @Override
        protected Object doInBackground(Object[] objects) {

            mMediaPlayer = MediaPlayer.create(AlarmActivity.this, alarmTone);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();

            return null;
        }
    }

    @Override
    protected void onResume() {

        SharedPreferences sp = getSharedPreferences("ACTIVITYINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.commit();

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        if (ringtone.isPlaying())
            ringtone.stop();

        c.close();
        finish();
        SharedPreferences sp = getSharedPreferences("ACTIVITYINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();

        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(uri!=null)
        {ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();}
        else
        {

            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                PlaySoundTask m = new PlaySoundTask();
                m.execute();
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!c.isClosed()) c.close();

    }

    @Override
    protected void onDestroy() {
        if (ringtone.isPlaying())
            ringtone.stop();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        super.onDestroy();
    }
}
