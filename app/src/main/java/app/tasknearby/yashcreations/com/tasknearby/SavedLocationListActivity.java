package app.tasknearby.yashcreations.com.tasknearby;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;


public class SavedLocationListActivity extends AppCompatActivity {

    CursorAdapter mLocationAdapter;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ListView listview = (ListView) this.findViewById(R.id.list_view_location);
        final Uri uri = TasksContract.LocationEntry.CONTENT_URI;

        final String sortOrder = TasksContract.LocationEntry.COLUMN_COUNT + " DESC, "+TasksContract.LocationEntry.COLUMN_PLACE_NAME + " COLLATE NOCASE ASC ";
        cursor= this.getContentResolver().query(uri, Constants.PROJECTION_LOC,
                TasksContract.LocationEntry.COLUMN_HIDDEN+"=?",
                new String[]{"0"},
                sortOrder);

        TextView noLocationView=(TextView) this.findViewById(R.id.no_location_view);
        if(!cursor.moveToFirst())
            noLocationView.setVisibility(View.VISIBLE);

        mLocationAdapter = new CursorAdapter(this, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return LayoutInflater.from(context).inflate(R.layout.list_item_location, viewGroup, false);
            }

            @Override
            public void bindView(View view, final Context context, final Cursor cursor) {

                TextView locNameView = (TextView) view.findViewById(R.id.location_name);
                final String location = cursor.getString(Constants.COL_PLACE_NAME);
                final LatLng latLng = new LatLng(cursor.getDouble(Constants.COL_LAT),cursor.getDouble(Constants.COL_LON)) ;
                locNameView.setText(location);
                locNameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = SavedLocationListActivity.this.getIntent();
                        intent.putExtra(Constants.savedLocation, location);
                        intent.putExtra(Constants.LatLngExtra,latLng);
                        SavedLocationListActivity.this.setResult(RESULT_OK, intent);
                        finish();
                    }
                });

                ImageButton delLoc=(ImageButton)view.findViewById(R.id.delLocation);
                delLoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideLocation(location, context);
                    }
                });
            }

            private void hideLocation(final String location,final Context context)
            {
                final AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("Delete Location")
                        .setIcon(R.drawable.ic_delete_teal_500_24dp)
                        .setMessage("Delete \"" + location + "\" ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues values = new ContentValues();
                                values.put(TasksContract.LocationEntry.COLUMN_HIDDEN, 1);
                                context.getContentResolver().update(TasksContract.LocationEntry.CONTENT_URI,
                                        values,
                                        TasksContract.LocationEntry.COLUMN_PLACE_NAME + "=?",
                                        new String[]{location});
                                cursor.close();
                                cursor= SavedLocationListActivity.this.getContentResolver().query(uri, Constants.PROJECTION_LOC,
                                        TasksContract.LocationEntry.COLUMN_HIDDEN+"=?",
                                        new String[]{"0"},
                                        sortOrder);
                                mLocationAdapter.swapCursor(cursor);
                            }
                        });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        };

        listview.setAdapter(mLocationAdapter);
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
