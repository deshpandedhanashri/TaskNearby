package app.tasknearby.yashcreations.com.tasknearby;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.preference.ListPreference;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;
import app.tasknearby.yashcreations.com.tasknearby.service.FusedLocationService;


public class AddNewTaskActivity extends ActionBarActivity {
    int GET_PLACE_FROM_MAP = 56;
    double defValue = 0;
    public static String mTaskLocation = null;
    public static TextView selectedLocationDisplayView;
    public static String returnedColorName;
    public static int returnedColorCode;
    public static TextView colorButton;
    public static LinearLayout baseLayout;
    public static TextView remindDistanceView;
    public static int remindDistance;
    int distance;
    ImageButton LocationSelector, selFromMap;
    Button CreateNewTask;
    EditText taskName;
    CheckBox alarmStatus;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PLACE_FROM_MAP) {        //LOCATION SELECTED FROM MAP
            if (resultCode == RESULT_OK) {
                double lat = data.getDoubleExtra(GetPlaceFromMap.LATITUDE, defValue);
                double lon = data.getDoubleExtra(GetPlaceFromMap.LONGITUDE, defValue);

                Utility.savePlaceDialog(AddNewTaskActivity.this, lat, lon);

            }
        } else if (requestCode == 6)                         //LOCATION SELECTED FROM SAVED PLACES
        {
            if (resultCode == RESULT_OK) {
                mTaskLocation = data.getStringExtra("loc");
                selectedLocationDisplayView.setText(mTaskLocation);
            }
        }


    }

    public void initialize() {
        baseLayout = (LinearLayout) this.findViewById(R.id.newTaskBaseLayout);
        CreateNewTask = (Button) this.findViewById(R.id.createNewTask);
        taskName = (EditText) this.findViewById(R.id.enter_task_name);
        alarmStatus = (CheckBox) this.findViewById(R.id.alarm_switch);
        LocationSelector = (ImageButton) this.findViewById(R.id.locationSelectorButton);
        selFromMap = (ImageButton) this.findViewById(R.id.selectFromMap);
        remindDistanceView = (TextView) this.findViewById(R.id.remind_distanceView);
        selectedLocationDisplayView = (TextView) this.findViewById(R.id.selected_loc_dip_TView);
        colorButton = (TextView) this.findViewById(R.id.colorTextView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);
        mTaskLocation = null;
        returnedColorCode = Color.parseColor("#ff6f2da8");
        returnedColorName = "Grape";
        remindDistance = 50;

        initialize();

        String baseText="Remind when closer than ";
        remindDistanceView.setText(baseText + Utility.getDistanceDisplayString(this,remindDistance));

        selectedLocationDisplayView.setText(null);
        colorButton.setText("Color: Grape");

        LocationSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SavedLocationListActivity.class);
                startActivityForResult(intent, 6);
            }
        });

        selFromMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GetPlaceFromMap.class);
                startActivityForResult(intent, GET_PLACE_FROM_MAP);
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.selectColorDialog(AddNewTaskActivity.this);
            }
        });

        remindDistanceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.showRemindDistanceDialog(AddNewTaskActivity.this);

            }
        });

        CreateNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mTaskLocation == null) {
                    Toast.makeText(AddNewTaskActivity.this, "Please select a Location first!", Toast.LENGTH_SHORT).show();
                } else if (taskName.getText().toString().equals("")) {
                    Toast.makeText(AddNewTaskActivity.this, "Please Enter the Task Name !", Toast.LENGTH_SHORT).show();
                } else {
                    distance = Utility.getDistanceByPlaceName(mTaskLocation,
                            Utility.getCurrentLocation(AddNewTaskActivity.this),
                            AddNewTaskActivity.this);

                    ContentValues taskValues = new ContentValues();

                    taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, taskName.getText().toString());
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, mTaskLocation);
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, returnedColorCode);
                    taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, String.valueOf(alarmStatus.isChecked()));
                    taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, distance);
                    taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, "false");
                    taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, "0");
                    taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, remindDistance);

                    if(distance<=remindDistance&&distance!=0) {
                       Toast.makeText(AddNewTaskActivity.this,"You are Already within the Selected region!",Toast.LENGTH_LONG).show();
                                  }


                   Uri insertedUri= AddNewTaskActivity.this.getContentResolver().insert(TasksContract.TaskEntry.CONTENT_URI, taskValues);


                    Intent intent = AddNewTaskActivity.this.getIntent();
                    AddNewTaskActivity.this.setResult(RESULT_OK, intent);
                    finish();
                }


            }
        });

    }


}
