package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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

    String PROJECTION[] = {
            TasksContract.LocationEntry.TABLE_NAME + "." + TasksContract.LocationEntry._ID,
            TasksContract.LocationEntry.COLUMN_PLACE_NAME,
            TasksContract.LocationEntry.COLUMN_COORD_LAT,
            TasksContract.LocationEntry.COLUMN_COORD_LONG
    };


    static final int COL_PLACE_NAME = 1;
    static final int COL_LAT = 2;
    static final int COL_LON = 3;

    CursorAdapter mLocationAdapter;
    Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location_list);
        Uri uri = TasksContract.LocationEntry.CONTENT_URI;
        String sortOrder = TasksContract.LocationEntry.COLUMN_PLACE_NAME + " COLLATE NOCASE ASC ";
       cursor= this.getContentResolver().query(uri, PROJECTION, null, null, sortOrder);
        TextView noLocationView=(TextView) this.findViewById(R.id.no_location_view);
        if(!cursor.moveToFirst())
        {noLocationView.setVisibility(View.VISIBLE);}

        mLocationAdapter = new CursorAdapter(this, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return LayoutInflater.from(context).inflate(R.layout.list_item_location, viewGroup, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

                TextView locNameView = (TextView) view.findViewById(R.id.location_name);
                String location = cursor.getString(COL_PLACE_NAME);
                locNameView.setText(location);

            }
        };
        ListView listview = (ListView) this.findViewById(R.id.list_view_location);
        listview.setAdapter(mLocationAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);

                if (c != null) {
                    String touchedLocation = c.getString(COL_PLACE_NAME);
                    Intent intent = SavedLocationListActivity.this.getIntent();
                    intent.putExtra("loc", touchedLocation);
                    SavedLocationListActivity.this.setResult(RESULT_OK, intent);
                    finish();

                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {finish();
         return true;}

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!cursor.isClosed()) {cursor.close();
            Log.e("CURSOR","Closing Cursor Saved Location Activity");}
    }
}
