package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


public class SavedLocationListActivity extends ActionBarActivity {

    CursorAdapter mLocationAdapter;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location_list);

        ListView listview = (ListView) this.findViewById(R.id.list_view_location);
        Uri uri = TasksContract.LocationEntry.CONTENT_URI;
        String sortOrder = TasksContract.LocationEntry.COLUMN_PLACE_NAME + " COLLATE NOCASE ASC ";

        cursor= this.getContentResolver().query(uri, Constants.PROJECTION_LOC, null, null, sortOrder);

        TextView noLocationView=(TextView) this.findViewById(R.id.no_location_view);
        if(!cursor.moveToFirst())
            noLocationView.setVisibility(View.VISIBLE);

        mLocationAdapter = new CursorAdapter(this, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return LayoutInflater.from(context).inflate(R.layout.list_item_location, viewGroup, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView locNameView = (TextView) view.findViewById(R.id.location_name);
                String location = cursor.getString(Constants.COL_PLACE_NAME);
                locNameView.setText(location);
            }
        };

        listview.setAdapter(mLocationAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l)
            {
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                if (c != null)
                {
                    String touchedLocation = c.getString(Constants.COL_PLACE_NAME);
                    Intent intent = SavedLocationListActivity.this.getIntent();
                    intent.putExtra(Constants.savedLocation, touchedLocation);
                    SavedLocationListActivity.this.setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!cursor.isClosed())
            cursor.close();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {finish();
         return true;}
        return super.onOptionsItemSelected(item);
    }
}
