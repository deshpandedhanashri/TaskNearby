package app.tasknearby.yashcreations.com.tasknearby;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

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

    public static class Task {
        String mTaskName;
        String mTaskLocation;
        boolean isAlarmEnabled;
        int mRemindDistance;
        String mExpiryDate ;
        String mTaskColor;
        int mColorCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtask);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTaskNameInput = (EditText) findViewById(R.id.task_name_input);
        mLocationNameInput = (EditText) findViewById(R.id.locationNameInput);
        remindDistanceTV = (TextView) findViewById(R.id.remind_distance_tv);
        mExpiryDateTV = (TextView) findViewById(R.id.expiryDateTV);
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
            remindDistanceTV.setText(getString(R.string.default_remind_distance_yd));

        mTfRegular = Typeface.createFromAsset(getAssets(), "fonts/RalewayMedium.ttf");
        overrideFonts(this, findViewById(R.id.content_new_task));
        remindDistanceTV.setTypeface(Typeface.DEFAULT_BOLD);
        mExpiryDateTV.setTypeface(Typeface.DEFAULT_BOLD);

        if(getIntent().hasExtra(Constants.KEY_EDIT_TASK_NAME)) {
            Bundle bundle = getIntent().getExtras() ;
            mTaskNameInput.setText(bundle.getString(Constants.KEY_EDIT_TASK_NAME));
            mLocationNameInput.setVisibility(View.VISIBLE);
            mLocationNameInput.setText(bundle.getString(Constants.KEY_EDIT_TASK_LOCATION));
            alarmCheckBox.setChecked(bundle.getBoolean(Constants.KEY_EDIT_ALARM));
            remindDistanceTV.setText(String.valueOf(bundle.getInt(Constants.KEY_EDIT_REMIND_DIS)));
            if(utility.isMetric(this))
                remindDistanceTV.append(" m");
            else
                remindDistanceTV.append(" yd");
            mTask.mRemindDistance = bundle.getInt(Constants.KEY_EDIT_REMIND_DIS) ;
            latLng = utility.getLatLngByPlaceName(this, bundle.getString(Constants.KEY_EDIT_TASK_LOCATION));
        }
/*
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();

        //TODO: Code for ads
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        AdView mAdView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(deviceId).build();
        mAdView.loadAd(adRequest);
        boolean isTestDevice = adRequest.isTestDevice(this);

        Log.e("TAG", "is Admob Test Device ? "+deviceId+" "+isTestDevice);*/
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
                if(!utility.isConnected(this))
                    Toast.makeText(this,getString(R.string.not_connected_internet),Toast.LENGTH_LONG).show();
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
            case R.id.expireLayout:
                showDateSelectionDialog();
                break;
            case R.id.remindDistanceLayout:
                showRemindDistanceDialog();
                break;
            case R.id.save_button:
                createTask();
                break;
        }

    }
    private void showDateSelectionDialog(){
        Date currentDate = Calendar.getInstance().getTime() ;
        DatePickerDialog mDPDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Toast.makeText(NewTaskActivity.this, "Date is: " + year + "/" + month + "/" + dayOfMonth,Toast.LENGTH_LONG).show();
            }
        }, 2013,11,8);
        mDPDialog.show();

    }

    private void showRemindDistanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.remind_dis_dialog_title));
        final EditText input = new EditText(this);
        if (utility.isMetric(this))
            input.setHint(getString(R.string.remind_dis_dialog_hint_m));
        else
            input.setHint(getString(R.string.remind_dis_dialog_hint_yd));
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
        Log.e("TAG", "createTask: Alarm checkbox status: "+ alarmCheckBox.isChecked());

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
        taskValues.put(TasksContract.TaskEntry.COLUMN_LOCATION_ALARM, String.valueOf(mTask.isAlarmEnabled))
        ;
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