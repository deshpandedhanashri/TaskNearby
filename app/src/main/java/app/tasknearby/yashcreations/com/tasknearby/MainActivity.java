package app.tasknearby.yashcreations.com.tasknearby;



import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import app.tasknearby.yashcreations.com.tasknearby.service.FusedLocationService;

public class MainActivity extends ActionBarActivity {

    long previousUpdateInterval=5*1000;
    private final String TASKSFRAGMENT_TAG="TFTAG";
    boolean previousAppStatus;


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TasksFragment(),TASKSFRAGMENT_TAG)
                    .commit();
        }
        if(getAppStatus()&&checkPlayServices())
            {startService(new Intent(this,FusedLocationService.class));}
        previousUpdateInterval=Utility.getUpdateInterval(this);
        previousAppStatus=getAppStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            previousUpdateInterval=Utility.getUpdateInterval(this);
            previousAppStatus=getAppStatus();
            Intent settingIntent=new Intent(this,SettingsActivity.class);
            startActivity(settingIntent);
            return true;      }
        else if(id==R.id.action_about)
        {
            startActivity(new Intent(this,AboutActivity.class));
            return true;
        }
        else if(id==R.id.action_help)
        {
            startActivity(new Intent(this,HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show();
            TasksFragment ff = (TasksFragment)getSupportFragmentManager().findFragmentByTag(TASKSFRAGMENT_TAG);
            if ( null != ff )
                ff.refreshLoader();
            TextView tv=(TextView)this.findViewById(R.id.textView);
            tv.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    protected void onRestart() {
        TasksFragment ff = (TasksFragment)getSupportFragmentManager().findFragmentByTag(TASKSFRAGMENT_TAG);
        if ( null != ff )
            ff.refreshLoader();

        if(previousUpdateInterval!=Utility.getUpdateInterval(this))
        {
            stopService(new Intent(this, FusedLocationService.class));
            startService(new Intent(this, FusedLocationService.class));
            previousUpdateInterval=Utility.getUpdateInterval(this);
        }


        if(previousAppStatus!=getAppStatus()&&checkPlayServices())
        {
            if(!getAppStatus())
                stopService(new Intent(this, FusedLocationService.class));
            else
                startService(new Intent(this, FusedLocationService.class));

        previousAppStatus=getAppStatus();
        }
        super.onRestart();
    }

    public boolean getAppStatus()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String m = prefs.getString(this.getString(R.string.pref_status_key),
                this.getString(R.string.pref_status_default));
        if(m.equals("enabled"))
           return true;
        else
            return false;
    }
}
