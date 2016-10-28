package app.tasknearby.yashcreations.com.tasknearby;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlacePicker;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


public class AddNewTaskActivity extends AppCompatActivity {
    int REQUEST_CODE_GET_FROM_MAP = 3;
    int REQUEST_CODE_SAVED_PLACES = 6;
    int REQUEST_CODE_PLACE_PICKER = 9 ;

    String mTaskLocation = null, mColorName;
    int mColorCode, remindDistance, distance;
    TextView selectedLocationDisplayView, colorButton, remindDistanceView;
    LinearLayout baseLayout;
    ImageButton LocationSelector, selFromMap;
    Button CreateNewTask;
    EditText taskName;
    CheckBox alarmStatus;

    Utility utility = new Utility();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLACE_PICKER) {        //LOCATION SELECTED FROM MAP



        } else if (requestCode == REQUEST_CODE_SAVED_PLACES)                         //LOCATION SELECTED FROM SAVED PLACES
        {
            if (resultCode == RESULT_OK) {
                mTaskLocation = data.getStringExtra(Constants.savedLocation);
                selectedLocationDisplayView.setText(mTaskLocation);
            }
        }
    }

    public void savePlaceDialog(final Context context, final Double latitude, final Double longitude) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Save the new Place");
        final EditText input = new EditText(context);
        alertDialog.setView(input);
        input.setHint("Place's Name");
        alertDialog.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String place = input.getText().toString();
                        mTaskLocation = place;
                        selectedLocationDisplayView.setText(place);
                        utility.addLocation(context, place, latitude, longitude);
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(    //To Hide The Keyboard
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        Toast.makeText(context, "Place Saved", Toast.LENGTH_SHORT).show();
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);

        initialize();

        mTaskLocation = null;
        mColorCode = ContextCompat.getColor(this, R.color.Grape);
        mColorName = "Grape";
        remindDistance = 50;


        String baseText = "Remind when closer than ";
        remindDistanceView.setText(baseText + utility.getDistanceDisplayString(this, remindDistance));
        selectedLocationDisplayView.setText(null);
        colorButton.setText("Color: Grape");

        Intent intent1 = this.getIntent();
        if (intent1.hasExtra(Constants.tName))
            setScreenAsEdit(intent1);

        LocationSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SavedLocationListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SAVED_PLACES);
            }
        });

        selFromMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(AddNewTaskActivity.this), REQUEST_CODE_PLACE_PICKER);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String values[] = {"Tomato", "Tangerine", "Peacock", "Lavender", "Grape", "Pink"};
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddNewTaskActivity.this);

                alertDialog.setTitle("Make your selection");
                alertDialog.setItems(values, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        mColorName = values[item];
                        colorButton.setText("Color: " + mColorName);
                        mColorCode = utility.getColorCodeFromString(AddNewTaskActivity.this, mColorName);
                        baseLayout.setBackgroundColor(mColorCode);
                    }
                });
                alertDialog.show();
            }
        });

        remindDistanceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddNewTaskActivity.this);
                alertDialog.setTitle("Reminding Range");
                final EditText input = new EditText(AddNewTaskActivity.this);

                if (utility.isMetric(AddNewTaskActivity.this))
                    input.setHint("Enter the distance in m");
                else
                    input.setHint("Enter the distance in yd");

                alertDialog.setMessage("Please Enter the closest distance to the Task Location for reminder");
                alertDialog.setView(input);

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            int dis = Integer.parseInt(input.getText().toString());
                            int dis_show = dis;
                            if (!utility.isMetric(AddNewTaskActivity.this)) {
                                Double d = dis / 1.09361;
                                dis = d.intValue();
                            }
                            remindDistance = dis;
                            remindDistanceView.setText("Remind when closer than "
                                    + utility.getDistanceDisplayString(AddNewTaskActivity.this, dis_show));

                            InputMethodManager imm = (InputMethodManager) AddNewTaskActivity.this.getSystemService(    //To Hide The Keyboard
                                    AddNewTaskActivity.this.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (NumberFormatException excep) {
                            Toast.makeText(AddNewTaskActivity.this, "Please enter the distance correctly!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                alertDialog.show();
            }
        });

        CreateNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mTaskLocation == null) {
                    Toast.makeText(AddNewTaskActivity.this, "Please select a Location first!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (taskName.getText().toString().equals("")) {
                    Toast.makeText(AddNewTaskActivity.this, "Please Enter the Task Name !", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                distance = utility.getDistanceByPlaceName(mTaskLocation,
                        utility.getCurrentLocation(AddNewTaskActivity.this),AddNewTaskActivity.this);

                ContentValues taskValues = new ContentValues();

                taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, taskName.getText().toString());
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, mTaskLocation);
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, mColorCode);
                taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, String.valueOf(alarmStatus.isChecked()));
                taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, distance);
                taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, "false");
                taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, "0");
                taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, remindDistance);

                Uri insertedUri = AddNewTaskActivity.this.getContentResolver()
                            .insert(TasksContract.TaskEntry.CONTENT_URI, taskValues);

                if (distance <= remindDistance && distance != 0)
                    Toast.makeText(AddNewTaskActivity.this, getString(R.string.already_in_region), Toast.LENGTH_LONG).show();


                incrementSelectedLocCount(mTaskLocation);

                Intent intent = AddNewTaskActivity.this.getIntent();
                AddNewTaskActivity.this.setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void setScreenAsEdit(Intent intent) {
        CreateNewTask.setText("SAVE EDITS");
        android.support.v7.app.ActionBar aBar = getSupportActionBar();
        if (aBar != null)
            aBar.setTitle("Edit Task");

        String tName = intent.getStringExtra(Constants.tName);
        String tLoc = intent.getStringExtra(Constants.tLocation);
        int tCol = intent.getIntExtra(Constants.tColor, 0);
        String tAlarm = intent.getStringExtra(Constants.tAlarm);
        int tRemDis = intent.getIntExtra(Constants.tRemDis, 50);


        mTaskLocation = tLoc;
        mColorCode = tCol;
        remindDistance = tRemDis;

        taskName.setText(tName);
        selectedLocationDisplayView.setText(tLoc);
        baseLayout.setBackgroundColor(tCol);
        colorButton.setText("Color");
        if (tAlarm.equals("true"))
            alarmStatus.setChecked(true);
        else
            alarmStatus.setChecked(false);


        String dispString = "Remind when closer than " + remindDistance;
        if (utility.isMetric(this))
            dispString += "m";
        else
            dispString += "yd";
        remindDistanceView.setText(dispString);
    }

    public void incrementSelectedLocCount(String locationName)
    {
        Cursor cc=this.getContentResolver().query(TasksContract.LocationEntry.CONTENT_URI,
                new String[]{TasksContract.LocationEntry.COLUMN_COUNT},
                TasksContract.LocationEntry.COLUMN_PLACE_NAME+"=?",
                new String[]{locationName},
                null);

        if(cc.moveToFirst())
        {
            int count=cc.getInt(0);
            cc.close();
            ContentValues values=new ContentValues();
            values.put(TasksContract.LocationEntry.COLUMN_COUNT,++count);
            this.getContentResolver().update(TasksContract.LocationEntry.CONTENT_URI,
                    values,
                    TasksContract.LocationEntry.COLUMN_PLACE_NAME+"=?",
                    new String[]{locationName});
        }
        if(!cc.isClosed())
            cc.close();
    }
}
