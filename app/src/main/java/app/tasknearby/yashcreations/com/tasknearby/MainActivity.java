package app.tasknearby.yashcreations.com.tasknearby;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import app.tasknearby.yashcreations.com.tasknearby.service.FusedLocationService;

public class MainActivity extends ActionBarActivity {

    boolean isServiceRunning = false;
    private final String TASKSFRAGMENT_TAG = "TFTAG";
    ToggleButton toggle;
    Utility utility=new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TasksFragment(), TASKSFRAGMENT_TAG)
                    .commit();
        }
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toggle = (ToggleButton) this.findViewById(R.id.toggle);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            utility.showGpsOffDialog(this);
        }

        if (getAppStatus() && checkPlayServices()) {
            startServ();
            toggle.setChecked(true);
            setToggleBg(true);
        } else setToggleBg(false);

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                if (toggle.isChecked()) {
                    if (!isServiceRunning)              //If service is not running then start it!
                        startServ();
                    setToggleBg(true);                  //Background Set to Green
                    editor.putString(MainActivity.this.getString(R.string.pref_status_key), "enabled");
                }
                else {
                    if (isServiceRunning)
                        stopServ();
                    setToggleBg(false);
                    editor.putString(MainActivity.this.getString(R.string.pref_status_key), "disabled");
                }
                editor.commit();
            }
        });
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        1000).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    void startServ() {
        startService(new Intent(this, FusedLocationService.class));
        isServiceRunning = true;
    }

    void stopServ() {
        stopService(new Intent(this, FusedLocationService.class));
        isServiceRunning = false;
    }

    void setToggleBg(boolean green) {
        Drawable bg = toggle.getBackground();
        int color = ContextCompat.getColor(this, R.color.Tomato);
        if (green)
            color = ContextCompat.getColor(this,R.color.Green);

        if (bg instanceof ShapeDrawable)
            ((ShapeDrawable) bg).getPaint().setColor(color);
        else if (bg instanceof GradientDrawable)
            ((GradientDrawable) bg).setColor(color);
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
            //TODO: Change this to SnackBar
            Toast.makeText(this, "Task Added!", Toast.LENGTH_SHORT).show();
            TextView tv = (TextView) this.findViewById(R.id.textView);
            tv.setVisibility(View.INVISIBLE);
        }
    }

    public boolean getAppStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String m = prefs.getString(this.getString(R.string.pref_status_key),
                this.getString(R.string.pref_status_default));
        if (m.equals("enabled"))
            return true;
        else
            return false;
    }
}
