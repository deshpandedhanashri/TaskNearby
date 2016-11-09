package app.tasknearby.yashcreations.com.tasknearby;

import android.support.v7.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

import static android.os.Build.ID;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = DetailActivity.class.getSimpleName() ;

    int REQUEST_CODE_EDIT =1;
    String doneStatus;
    private Cursor cursor ;
    private GoogleMap map ;
    TextView mDistanceView ;
    Utility utility = new Utility() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView mTaskNameView = (TextView) findViewById(R.id.detail_task_name);
        TextView mLocationView = (TextView) findViewById(R.id.detail_location_name);
        mDistanceView = (TextView) findViewById(R.id.detail_distance);
        TextView mAlarmStatusView = (TextView) findViewById(R.id.detail_alarm);
        Button mMarkAsDoneButton = (Button) findViewById(R.id.btnMarkDone);
        ((MapFragment) getFragmentManager().findFragmentById(R.id.detail_map)).getMapAsync(this);

        Location currentLocation = utility.getCurrentLocation(this);
        int distance = 0;

        Intent intent = this.getIntent();
        String TaskID = intent.getStringExtra(Constants.TaskID);
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;

        cursor = this.getContentResolver().query(uri,
                Constants.PROJECTION_TASKS,
                TasksContract.TaskEntry._ID + "=?",
                new String[]{ID}, null);

        if (cursor.moveToFirst()) {
            mTaskNameView.setText(cursor.getString(Constants.COL_TASK_NAME));
            mLocationView.setText(cursor.getString(Constants.COL_LOCATION_NAME));
            doneStatus = cursor.getString(Constants.COL_DONE);
            if (doneStatus.equals("true")) {
                mMarkAsDoneButton.setText("Unmark as Done");
                mMarkAsDoneButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Tomato));
            }
            distance = utility.getDistanceByPlaceName(cursor.getString(Constants.COL_LOCATION_NAME), currentLocation, this);
            if (distance == 0)
                distance = cursor.getInt(Constants.COL_MIN_DISTANCE);
            mDistanceView.setText(utility.getDistanceDisplayString(this, distance));

            if (cursor.getString(Constants.COL_ALARM).equals("true"))
                mAlarmStatusView.setText("On");
            else
                mAlarmStatusView.setText("Off");
        }

        mMarkAsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doneStatus.equals("false"))
                    markButtonClicked("true");
                else
                    markButtonClicked("false");
            }
        });
    }

    private void markButtonClicked(String done) {
        ContentValues taskValues = new ContentValues();

        taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, cursor.getString(Constants.COL_TASK_NAME));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, cursor.getString(Constants.COL_LOCATION_NAME));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, cursor.getInt(Constants.COL_TASK_COLOR));
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, cursor.getString(Constants.COL_ALARM));
        taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, cursor.getInt(Constants.COL_MIN_DISTANCE));
        taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, done);
        taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, cursor.getString(Constants.COL_SNOOZE));
        taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, cursor.getString(Constants.COL_REMIND_DIS));

        DetailActivity.this.getContentResolver().update(
                TasksContract.TaskEntry.CONTENT_URI,
                taskValues, TasksContract.TaskEntry._ID + "=?",
                new String[]{cursor.getString(Constants.COL_TASK_ID)}
        );
        cursor.close();
        finish();
        startActivity(getIntent());
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                utility.getLatLngByPlaceName(this,
                        cursor.getString(Constants.COL_LOCATION_NAME)), 15)
        );
        map.clear();
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_red_a400_36dp))
                .anchor(0.5f, 1.0f)
                .position(utility.getLatLngByPlaceName(this, cursor.getString(Constants.COL_LOCATION_NAME))));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            deleteTask(cursor.getString(Constants.COL_TASK_ID), true);
            return true;
        } else if (id == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String m = "Task Name: " + cursor.getString(Constants.COL_TASK_NAME) +
                    "\nTask Location: " + cursor.getString(Constants.COL_LOCATION_NAME) +
                    "\nDistance From Current Location: " + mDistanceView.getText().toString() + "\n#Task Nearby App";
            intent.setType("text/plain");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.putExtra(Intent.EXTRA_TEXT, m);
            if (intent.resolveActivity(this.getPackageManager()) != null)
                startActivity(intent);
            else
                Toast.makeText(this, "No app found to share the Details", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_edit) {
            editTask();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int deleteTask(final String task_ID, boolean showDialog) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Delete !");
        alertDialog.setMessage("Delete this Task?");

        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DetailActivity.this.getContentResolver().delete(TasksContract.TaskEntry.CONTENT_URI, TasksContract.TaskEntry._ID + "=?",
                                new String[]{task_ID});
                        cursor.close();
                        finish();
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        if (showDialog)
            alertDialog.show();
        else {
            getContentResolver().delete(TasksContract.TaskEntry.CONTENT_URI, TasksContract.TaskEntry._ID + "=?",
                    new String[]{task_ID});
            cursor.close();
            finish();
        }

        return 0;
    }

    public void editTask() {
        Intent editIntent = new Intent(this, NewTaskActivity.class);
        editIntent.putExtra(Constants.tName, cursor.getString(Constants.COL_TASK_NAME));
        editIntent.putExtra(Constants.tLocation, cursor.getString(Constants.COL_LOCATION_NAME));
        editIntent.putExtra(Constants.tColor, cursor.getInt(Constants.COL_TASK_COLOR));
        editIntent.putExtra(Constants.tAlarm, cursor.getString(Constants.COL_ALARM));
        editIntent.putExtra(Constants.tRemDis, cursor.getInt(Constants.COL_REMIND_DIS));

        startActivityForResult(editIntent, REQUEST_CODE_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT) {
            if (resultCode == RESULT_OK)
                deleteTask(ID, false);
        }
    }

}





