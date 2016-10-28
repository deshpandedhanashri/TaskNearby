package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.source_code).setOnClickListener(this);;
        findViewById(R.id.share).setOnClickListener(this);;
        findViewById(R.id.rate).setOnClickListener(this);;
    }

    @Override
    public void onClick(View v) {

        final String appPackageName = getPackageName();
        final String appUrl = "https://play.google.com/store/apps/details?id=" + appPackageName ;
        switch(v.getId()){
            case R.id.rate:
                try
                {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName))); }
                catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                }
                break ;
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                String m = "Hey!Try out the all new Task Nearby app.This app has awesome location based reminders.Download it from Google Play Store!\nVisit: " + appUrl;
                intent.setType("text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Intent.EXTRA_TEXT, m);
                if (intent.resolveActivity(AboutActivity.this.getPackageManager()) != null)
                    startActivity(intent) ;
                else
                    Toast.makeText(AboutActivity.this, "No app found to share the Details!", Toast.LENGTH_SHORT).show();
                break ;
        }
    }
}
