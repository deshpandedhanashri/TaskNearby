package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Button sourceCode = (Button) findViewById(R.id.source_code);
        Button feedback = (Button) findViewById(R.id.feedback);
        Button share = (Button) findViewById(R.id.share);
        Button rate = (Button) findViewById(R.id.rate);

        final String appUrl = "https://play.google.com/store/apps/details?id=app.tasknearby.yashcreations.com.tasknearby";

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                final String appPackageName = getPackageName();
                try
                    {startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName))); }
                catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName))); }
            }
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://goo.gl/o821cR";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(AboutActivity.this.getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(AboutActivity.this, "No app found to open the Web page", Toast.LENGTH_SHORT).show();
            }
        });

        sourceCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://github.com/YashVerma1996/TaskNearby";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(AboutActivity.this.getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(AboutActivity.this, "No app found to open the Web page", Toast.LENGTH_SHORT).show();
           }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String m = "Hey!Try out the all new Task Nearby app.This app has awesome location based reminders.Download it from Google Play Store!\nVisit: " + appUrl;
                intent.setType("text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Intent.EXTRA_TEXT, m);
                if (intent.resolveActivity(AboutActivity.this.getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(AboutActivity.this, "No app found to share the Details!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
