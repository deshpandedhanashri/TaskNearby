package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        TextView mAppNameView = (TextView) findViewById(R.id.app_name_view_about);
        TextView mAppDescView = (TextView) findViewById(R.id.app_description_view);

        Typeface mTfRegular = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        mAppNameView.setTypeface(mTfRegular);
        mAppDescView.setTypeface(mTfRegular);

        findViewById(R.id.button_star).setOnClickListener(this);
        ;
        findViewById(R.id.button_share).setOnClickListener(this);
        findViewById(R.id.about_card).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        final String appPackageName = getPackageName();
        final String appUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
        switch (v.getId()) {
            case R.id.button_star:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                }
                break;
            case R.id.button_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                String m = getString(R.string.share_base_string) + appUrl;
                intent.setType("text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Intent.EXTRA_TEXT, m);
                if (intent.resolveActivity(AboutActivity.this.getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(AboutActivity.this, "No app found to share the Details!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about_card:
                Intent eIntent = new Intent(Intent.ACTION_SENDTO);
                eIntent.setType("text/plain");
                eIntent.setData(Uri.parse("mailto:"));
                eIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email_id)});
                eIntent.putExtra(Intent.EXTRA_SUBJECT, "Task Nearby App");
                startActivity(eIntent);
                break;
        }
    }
}
