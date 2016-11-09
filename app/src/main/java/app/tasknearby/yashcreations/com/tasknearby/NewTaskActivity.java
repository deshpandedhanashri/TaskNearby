package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

import static android.R.id.input;

//TODO: Support edit operation

public class NewTaskActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_PLACE_PICKER = 1;
    public static final int REQUEST_SAVED_PLACES = 2;
    EditText mTaskNameInput, mLocationNameInput;
    TextView remindDistanceTV, mExpiryDateTV;
    CheckBox alarmCheckBox;
    Task mTask;
    LatLng latLng;
    Utility utility;
    private Typeface mTfRegular ;

    class Task {
        String mTaskName;
        String mTaskLocation;
        String mTaskColor;
        int mColorCode;
        boolean isAlarmEnabled;
        int mRemindDistance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtask);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mTaskNameInput = (EditText) findViewById(R.id.task_name_input);
        mLocationNameInput = (EditText) findViewById(R.id.locationNameInput);
        remindDistanceTV = (TextView) findViewById(R.id.remind_distance_tv);
        alarmCheckBox = (CheckBox) findViewById(R.id.alarm_checkBox);

        findViewById(R.id.select_from_saved).setOnClickListener(this);
        findViewById(R.id.pick_from_map).setOnClickListener(this);
        findViewById(R.id.alarmLayout).setOnClickListener(this);
        findViewById(R.id.remindDistanceLayout).setOnClickListener(this);
        findViewById(R.id.expireLayout).setOnClickListener(this);

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setTypeface(Typeface.DEFAULT_BOLD);
        saveButton.setOnClickListener(this);

        mLocationNameInput.setVisibility(View.INVISIBLE);
        mTask = new Task();
        mTask.mRemindDistance = 75;
        mTask.mColorCode = R.color.Tangerine;
        mTask.mTaskColor = "Tangerine";
        utility = new Utility();
        if(!utility.isMetric(this))
            remindDistanceTV.setText("75 yd");

        mTfRegular = Typeface.createFromAsset(getAssets(), "fonts/RalewayMedium.ttf");
        overrideFonts(this, findViewById(R.id.content_new_task));
        remindDistanceTV.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.select_from_saved:
                intent = new Intent(this, SavedLocationListActivity.class);
                startActivityForResult(intent, REQUEST_SAVED_PLACES);
                break;
            case R.id.pick_from_map:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.alarmLayout:
                alarmCheckBox.setChecked(!alarmCheckBox.isChecked());
                break;
//            case R.id.colorLLayout:
//                showColorSelectionDialog();
//                break;
            case R.id.remindDistanceLayout:
                showRemindDistanceDialog();
                break;
            case R.id.save_button:
                createTask();
                break;
        }

    }

    private void showColorSelectionDialog() {
        final String values[] = {"Tomato", "Tangerine", "Peacock", "Lavender", "Grape", "Pink"};
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Select a color");
        alertDialog.setItems(values, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mTask.mTaskColor = values[item];
                colorNameTV.setText("Color: " + mTask.mTaskColor);
                mTask.mColorCode = utility.getColorCodeFromString(NewTaskActivity.this, values[item]);
                //TODO: Change theme Color
            }
        });
        alertDialog.show();
    }

    private void showRemindDistanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reminder Range");
        final EditText input = new EditText(this);
        if (utility.isMetric(this))
            input.setHint("Range in metres");
        else
            input.setHint("Range in yards");
        input.setRawInputType(InputType.TYPE_CLASS_PHONE);
        builder.setMessage("Please enter the range around the selected location for reminder");
        builder.setView(input);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("OK", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int dis = Integer.parseInt(input.getText().toString());
                    int dis_show = dis;
                    if (!utility.isMetric(NewTaskActivity.this)) {
                        Double d = dis / 1.09361;
                        dis = d.intValue();
                    }
                    mTask.mRemindDistance = dis;
                    remindDistanceTV.setText(utility.getDistanceDisplayString(NewTaskActivity.this, dis_show));
                    InputMethodManager imm = (InputMethodManager) NewTaskActivity.this.getSystemService(NewTaskActivity.this.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    dialog.dismiss();
                } catch (NumberFormatException excep) {
                    Toast.makeText(NewTaskActivity.this, "Please enter the distance correctly!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createTask() {
        mTask.mTaskName = mTaskNameInput.getText().toString();
        mTask.mTaskLocation = mLocationNameInput.getText().toString();
        mTask.isAlarmEnabled = alarmCheckBox.isChecked();

        if (TextUtils.isEmpty(mTask.mTaskName)) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter the task's name!", Snackbar.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(mTask.mTaskLocation) || latLng == null) {
            Snackbar.make(findViewById(android.R.id.content), "Please select the location!", Snackbar.LENGTH_LONG).show();
            return;
        }
        insertLocationIntoDB();
        int distance = utility.getDistanceByPlaceName(
                mTask.mTaskLocation,
                utility.getCurrentLocation(this),
                this);
        insertIntoDB(distance);
        if (distance <= mTask.mRemindDistance && distance != 0)
            Toast.makeText(this, getString(R.string.already_in_region), Toast.LENGTH_LONG).show();
        incrementSelectedLocCount(mTask.mTaskLocation);

        Intent intent = this.getIntent();
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private void insertIntoDB(int currentDistance) {

        ContentValues taskValues = new ContentValues();
        taskValues.put(TasksContract.TaskEntry.COLUMN_TASK_NAME, mTask.mTaskName);
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_NAME, mTask.mTaskLocation);
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_COLOR, mTask.mColorCode);
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, mTask.isAlarmEnabled);
        taskValues.put(TasksContract.TaskEntry.COLUMN_MIN_DISTANCE, currentDistance);
        taskValues.put(TasksContract.TaskEntry.COLUMN_DONE_STATUS, "false");
        taskValues.put(TasksContract.TaskEntry.COLUMN_SNOOZE_TIME, "0");
        taskValues.put(TasksContract.TaskEntry.COLUMN_REMIND_DISTANCE, mTask.mRemindDistance);

        Uri insertedUri = this.getContentResolver()
                .insert(TasksContract.TaskEntry.CONTENT_URI, taskValues);
    }

    private void insertLocationIntoDB(){
        utility.addLocation(
                this,
                mLocationNameInput.getText().toString(),
                latLng.latitude,
                latLng.longitude);
    }

    public void incrementSelectedLocCount(String locationName) {
        Cursor cc = this.getContentResolver().query(TasksContract.LocationEntry.CONTENT_URI,
                new String[]{TasksContract.LocationEntry.COLUMN_COUNT},
                TasksContract.LocationEntry.COLUMN_PLACE_NAME + "=?",
                new String[]{locationName},
                null);

        if (cc.moveToFirst()) {
            int count = cc.getInt(0);
            cc.close();
            ContentValues values = new ContentValues();
            values.put(TasksContract.LocationEntry.COLUMN_COUNT, ++count);
            this.getContentResolver().update(TasksContract.LocationEntry.CONTENT_URI,
                    values,
                    TasksContract.LocationEntry.COLUMN_PLACE_NAME + "=?",
                    new String[]{locationName});
        }
        if (!cc.isClosed())
            cc.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(data==null)
            return;
        if (requestCode == REQUEST_PLACE_PICKER) {
            Place place = PlacePicker.getPlace(this, data);
            if (place.getName().toString().contains("\u00b0"))
                mLocationNameInput.setText(place.getAddress());
            else
                mLocationNameInput.setText(place.getName());
            latLng = place.getLatLng();
            mLocationNameInput.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_SAVED_PLACES) {
            if (resultCode == RESULT_OK) {
                String locationName = data.getStringExtra(Constants.savedLocation);
                latLng = (LatLng) data.getParcelableExtra(Constants.LatLngExtra);
                mLocationNameInput.setText(locationName);
                mLocationNameInput.setVisibility(View.VISIBLE);
            }
        }
    }
    private void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof TextView ) {
                ((TextView) v).setTypeface(mTfRegular);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
    /******************************************************



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

     }

*/