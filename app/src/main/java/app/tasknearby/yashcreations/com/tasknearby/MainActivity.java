package app.tasknearby.yashcreations.com.tasknearby;


import android.Manifest;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;

import app.tasknearby.yashcreations.com.tasknearby.service.FusedLocationService;

public class MainActivity extends AppCompatActivity {

    /**
     * Problems:
     * => GPS turned off dialog box
     */

    public static final String TAG = MainActivity.class.getSimpleName();
    private Utility utility;
    private boolean isServiceRunning = false;

    private SwitchCompat appSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TasksFragment(), TAG)
                    .commit();
        }
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView mTitleView = (TextView) toolbar.findViewById(R.id.toolbarTV);
        mTitleView.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Raleway-SemiBold.ttf"));

        utility = new Utility();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            startApp();
        else
            checkPermissions();
    }

    private void checkPermissions() {
        boolean mFinePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean mCoarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (mFinePermission && mCoarsePermission)
            startApp();
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_permissions_granted), Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void startApp() {
        appSwitch = (SwitchCompat) this.findViewById(R.id.app_switch);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER))
            showGpsOffDialog(this);

        if (isAppEnabled() && utility.checkPlayServices(this)) {
            startServ();
            appSwitch.setChecked(true);
        } else
            appSwitch.setChecked(false);

        appSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                if (appSwitch.isChecked()) {
                    if (!isServiceRunning)              //If service is not running then start it!
                        startServ();
                    editor.putString(MainActivity.this.getString(R.string.pref_status_key), "enabled");
                } else {
                    if (isServiceRunning)
                        stopServ();
                    editor.putString(MainActivity.this.getString(R.string.pref_status_key), "disabled");
                }
                editor.apply();
            }
        });

    }

    private void showGpsOffDialog(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("No Location")
                .setMessage(getString(R.string.gps_off))
                .setPositiveButton("TURN ON",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        });
        alertDialog.show();
    }

    void startServ() {
        startService(new Intent(this, FusedLocationService.class));
        isServiceRunning = true;
    }

    void stopServ() {
        stopService(new Intent(this, FusedLocationService.class));
        isServiceRunning = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Snackbar.make(findViewById(android.R.id.content), "Task Added!", Snackbar.LENGTH_LONG).show();
            TextView tv = (TextView) this.findViewById(R.id.textView);
            tv.setVisibility(View.INVISIBLE);
        }
    }

    public boolean isAppEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String m = prefs.getString(this.getString(R.string.pref_status_key),
                this.getString(R.string.pref_status_default));
        return m.equals("enabled");
    }
}
/*

//    public static boolean isServiceRunning = false;
//    private final String TAG = "TaskFragment";
//    SwitchCompat appSwitch;
//    Utility utility=new Utility();
//    GoogleApiClient mGoogleApiClient ;

    mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
    .addApi(LocationServices.API)
    .build();
    if(mGoogleApiClient != null)
            mGoogleApiClient.connect();







    void dialogShower(){



        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.e(TAG,"ALLOWED.");
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        Log.e(TAG,"RESOLUTION REQUIRED.");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG, "SETTINGS CHANGE UNAVAILABLE");
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        dialogShower();
        Log.e(TAG,"connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"ERROR",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

    }
}*/
