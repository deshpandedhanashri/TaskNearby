package app.tasknearby.yashcreations.com.tasknearby;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


public class TaskDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = TaskDetailActivity.class.getSimpleName();
    private static final int REQUEST_CODE_EDIT = 1 ;
    private Cursor mCursor;
    private Utility utility = new Utility();
    private TextView mDistanceView;
    private boolean isTaskDone ;
    private String mTaskID ;
    private Button mMarkAsDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        TextView mTaskNameView = (TextView) findViewById(R.id.task_name_view);
        TextView mLocationView = (TextView) findViewById(R.id.detail_location_name);
        mDistanceView = (TextView) findViewById(R.id.detail_distance);
        TextView mAlarmStatusView = (TextView) findViewById(R.id.detail_alarm);
        TextView mExpiryDateView = (TextView) findViewById(R.id.detail_expiry_date);
        mMarkAsDoneButton = (Button) findViewById(R.id.btnMarkDone);
        ImageButton mDirectionsButton = (ImageButton) findViewById(R.id.button_directions);

        Typeface mTfRegular = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        Typeface mTfBold = Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf");
        mTaskNameView.setTypeface(mTfBold);
        mLocationView.setTypeface(mTfRegular);
        mDistanceView.setTypeface(Typeface.DEFAULT_BOLD);
        mAlarmStatusView.setTypeface(mTfRegular);
        mExpiryDateView.setTypeface(mTfRegular);
        mTaskNameView.setMovementMethod(new ScrollingMovementMethod());
        mLocationView.setMovementMethod(new ScrollingMovementMethod());


        mTaskID = getIntent().getStringExtra(Constants.TaskID);
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;
        mCursor = this.getContentResolver().query(uri,
                Constants.PROJECTION_TASKS,
                TasksContract.TaskEntry._ID + "=?",
                new String[]{mTaskID}, null);

        if (mCursor != null && mCursor.moveToFirst()) {
            mTaskNameView.setText(mCursor.getString(Constants.COL_TASK_NAME));
            mLocationView.setText(mCursor.getString(Constants.COL_LOCATION_NAME));
            isTaskDone = mCursor.getString(Constants.COL_DONE).equals("true");
            if (isTaskDone)
                mMarkAsDoneButton.setText(getString(R.string.unmark_done));
            Location currentLocation = utility.getCurrentLocation(this);
            int distance = utility.getDistanceByPlaceName(mCursor.getString(Constants.COL_LOCATION_NAME), currentLocation, this);
            if (distance == 0)
                distance = mCursor.getInt(Constants.COL_MIN_DISTANCE);
            mDistanceView.setText(utility.getDistanceDisplayString(this, distance));

            Log.e(TAG,"String: " + mCursor.getString(Constants.COL_ALARM)+ "bool flag=" + mCursor.getString(Constants.COL_ALARM).equals("true"));
            if (mCursor.getString(Constants.COL_ALARM).equals("true"))
                mAlarmStatusView.setText(getString(R.string.on));
            else
                mAlarmStatusView.setText(getString(R.string.off));
            ((MapFragment) getFragmentManager().findFragmentById(R.id.detail_map)).getMapAsync(this);
        }

        mMarkAsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markButtonClicked(!isTaskDone);
            }
        });
        mDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = utility.getLatLngByPlaceName(TaskDetailActivity.this, mCursor.getString(Constants.COL_LOCATION_NAME));
                Log.e(TAG, "sending " + "google.navigation:q=" + latLng.latitude + "," + latLng.longitude);
                Uri uri = Uri.parse("google.navigation:q=" + latLng.latitude + "," + latLng.longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

    }

    private void markButtonClicked(boolean newStatus) {
        ContentValues taskValues = new ContentValues();
        String status = "false";
        if(newStatus)
            status = "true" ;
          taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, status);

        this.getContentResolver().update(
                TasksContract.TaskEntry.CONTENT_URI,
                taskValues, TasksContract.TaskEntry._ID + "=?",
                new String[]{mCursor.getString(Constants.COL_TASK_ID)}
        );
        if(newStatus)
            mMarkAsDoneButton.setText(getString(R.string.unmark_done));
        else
            mMarkAsDoneButton.setText(getString(R.string.mark_done));
        isTaskDone = newStatus;
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
                        mCursor.getString(Constants.COL_LOCATION_NAME)), 15)
        );
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_red_a400_36dp))
                .anchor(0.5f, 1.0f)
                .position(utility.getLatLngByPlaceName(this, mCursor.getString(Constants.COL_LOCATION_NAME))));
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
            deleteTask(mCursor.getString(Constants.COL_TASK_ID), true);
            return true;
        } else if (id == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String m = "Task Name: " + mCursor.getString(Constants.COL_TASK_NAME) +
                    "\nTask Location: " + mCursor.getString(Constants.COL_LOCATION_NAME) +
                    "\nDistance From Current Location: " + mDistanceView.getText().toString() + "\n#Task Nearby App";
            intent.setType("text/plain");
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_delete_teal_500_24dp)
                .setTitle("Delete Task")
                .setMessage("Delete \""+ mCursor.getString(Constants.COL_TASK_NAME) + "\"?" );

        builder.setPositiveButton(getString(R.string.delete_command),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TaskDetailActivity.this.getContentResolver().delete(TasksContract.TaskEntry.CONTENT_URI, TasksContract.TaskEntry._ID + "=?",
                                new String[]{task_ID});
                        mCursor.close();
                        finish();
                    }
                });
        builder.setNegativeButton(getString(R.string.cancel_command),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        if (showDialog)
            builder.show();
        else {
            getContentResolver().delete(TasksContract.TaskEntry.CONTENT_URI, TasksContract.TaskEntry._ID + "=?",
                    new String[]{task_ID});
            mCursor.close();
            finish();
        }
        return 0;
    }

    public void editTask() {
        Intent eIntent = new Intent(this, NewTaskActivity.class);
        eIntent.putExtra(Constants.KEY_EDIT_TASK_NAME, mCursor.getString(Constants.COL_TASK_NAME)) ;
        eIntent.putExtra(Constants.KEY_EDIT_TASK_LOCATION, mCursor.getString(Constants.COL_LOCATION_NAME)) ;
        eIntent.putExtra(Constants.KEY_EDIT_ALARM, mCursor.getString(Constants.COL_ALARM).equals("true"));
        eIntent.putExtra(Constants.KEY_EDIT_REMIND_DIS, mCursor.getInt(Constants.COL_REMIND_DIS)) ;
        startActivityForResult(eIntent, REQUEST_CODE_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT) {
            if (resultCode == RESULT_OK)
                deleteTask(mTaskID, false);
        }
    }

}



