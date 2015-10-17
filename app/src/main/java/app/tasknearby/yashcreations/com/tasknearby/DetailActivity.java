package app.tasknearby.yashcreations.com.tasknearby;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


public class DetailActivity extends ActionBarActivity {
    public static int finishChecker;

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
    static final int COL_DONE=5;
    static final int COL_MIN_DISTANCE=6;
    static final int COL_REMIND_DIS=7;
    static final int COL_SNOOZE=8;
    Cursor c;
    GoogleMap map;
    TextView distanceView;
    String doneStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final Location currentLocation = Utility.getCurrentLocation(this);
        int distance = 0;
        finishChecker=0;


        //SETTING MAP FRGMENT
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.detail_map)).getMap();
        map.setMyLocationEnabled(true);


        LinearLayout baseLayout = (LinearLayout) this.findViewById(R.id.detail_base_layout);
        final TextView taskNameView = (TextView) this.findViewById(R.id.detail_task_name);
        TextView locationView = (TextView) this.findViewById(R.id.detail_location_name);
        distanceView = (TextView) this.findViewById(R.id.detail_distance);
        TextView alarmView = (TextView) this.findViewById(R.id.detail_alarm);
        Button markDone=(Button) this.findViewById(R.id.btnMarkDone);

        Intent intent = this.getIntent();
        String ID = intent.getStringExtra("TaskID");
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;

        c = this.getContentResolver().query(uri, PROJECTION,
                TasksContract.TaskEntry._ID + "=?",
                new String[]{ID}, null);

        if (c.moveToFirst()) {
            taskNameView.setText(c.getString(COL_TASK_NAME));
            locationView.setText(c.getString(COL_LOCATION_NAME));
            baseLayout.setBackgroundColor(c.getInt(COL_TASK_COLOR));
            doneStatus=c.getString(COL_DONE);
            if(doneStatus.equals("true"))
            {
                markDone.setText("Mark Not Done");
            }

            distance = Utility.getDistanceByPlaceName(c.getString(COL_LOCATION_NAME), currentLocation, this);
            if(distance==0)
            {distance=c.getInt(COL_MIN_DISTANCE);}
            distanceView.setText(Utility.getDistanceDisplayString(this,distance));

            if (c.getString(COL_ALARM).equals("true"))
                alarmView.setText("On");
            else
                alarmView.setText("Off");


            map.animateCamera(CameraUpdateFactory.newLatLngZoom(Utility.getLatLngByPlaceName(this, c.getString(COL_LOCATION_NAME)), 15));
            map.clear();
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1))
                    .anchor(0.5f, 1.0f) // Anchors the marker on the bottom left
                    .position(Utility.getLatLngByPlaceName(this, c.getString(COL_LOCATION_NAME))));
        }

        markDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(doneStatus.equals("false"))                      //Not Done Yet
                {
                    ContentValues taskValues = new ContentValues();


                    taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, c.getString(COL_TASK_NAME));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME,c.getString(COL_LOCATION_NAME));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, c.getInt(COL_TASK_COLOR));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM,c.getString(COL_ALARM));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE,c.getInt(COL_MIN_DISTANCE));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS,"true");
                    taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME,c.getString(COL_SNOOZE));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE,c.getString(COL_REMIND_DIS));


                    DetailActivity.this.getContentResolver().update(
                        TasksContract.TaskEntry.CONTENT_URI,
                        taskValues, TasksContract.TaskEntry._ID+"=?",
                        new String[]{c.getString(COL_TASK_ID)}
                        );
                    c.close();
                    finish();
                    startActivity(getIntent());


                }
               else
                {

                    ContentValues taskValues = new ContentValues();


                    taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, c.getString(COL_TASK_NAME));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, c.getString(COL_LOCATION_NAME));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, c.getInt(COL_TASK_COLOR));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, c.getString(COL_ALARM));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, c.getInt(COL_MIN_DISTANCE));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS,"false");
                    taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME,c.getString(COL_SNOOZE));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE,c.getString(COL_REMIND_DIS));

                    DetailActivity.this.getContentResolver().update(
                            TasksContract.TaskEntry.CONTENT_URI,
                            taskValues, TasksContract.TaskEntry._ID+"=?",
                            new String[]{c.getString(COL_TASK_ID)}
                    );
                    c.close();
                    finish();
                    startActivity(getIntent());

                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_delete) {
            deleteTask( c.getString(COL_TASK_ID));

            return true;
        }
        else if(id==R.id.action_share)
        {   Intent intent=new Intent(Intent.ACTION_SEND);
            String m="Task Name: "+c.getString(COL_TASK_NAME)+
                    "\nTask Location: "+c.getString(COL_LOCATION_NAME)+
                    "\nDistance From Current Location: "+distanceView.getText().toString()+"\n#Task Nearby App";
            intent.setType("text/plain");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.putExtra(Intent.EXTRA_TEXT,m);
            if(intent.resolveActivity(this.getPackageManager())!=null)
                startActivity(intent);
            else
                Toast.makeText(this,"No app found to share the Details",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public int deleteTask(final String task_ID) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete !");
        alertDialog.setMessage("Delete this Task?");


        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(TasksContract.TaskEntry.CONTENT_URI, TasksContract.TaskEntry._ID + "=?",
                                new String[]{task_ID});
                        c.close();
                        finish();
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
        return 0;
    }


}
