package app.tasknearby.yashcreations.com.tasknearby;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import app.tasknearby.yashcreations.com.tasknearby.database.TaskDbHelper;
import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

/**
 * Created by Yash on 22/04/15.
 */
public class TasksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static LoaderManager loaderManager;
    final static int STATUS = 5;
    String PROJECTION[] = {
            TasksContract.TaskEntry.TABLE_NAME + "." + TasksContract.TaskEntry._ID,
            TasksContract.TaskEntry.COLUMN_TASK_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_NAME,
            TasksContract.TaskEntry.COLUMN_LOCATION_COLOR,
            TasksContract.TaskEntry.COLUMN_DONE_STATUS,
            TasksContract.TaskEntry.COLUMN_MIN_DISTANCE


    };

    public static final int COL_TASK_ID = 0;
    public static final int COL_TASK_NAME = 1;
    public static final int COL_LOCATION_NAME = 2;
    public static final int COL_TASK_COLOR = 3;
    public static final int COL_DONE = 4;
    public static final int COL_MIN_DISTANCE = 5;
    public static int distance = 0;

    public TasksFragment() {
    }

    private TasksAdapter mTaskAdapter;

    public void refreshLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        loaderManager = getLoaderManager();
        getLoaderManager().initLoader(0, null, this);


        TextView noTaskView = (TextView) rootView.findViewById(R.id.textView);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            Utility.showGpsOffDialog(getActivity());
        }

        mTaskAdapter = new TasksAdapter(getActivity(),null, 0);

        ListView listview = (ListView) rootView.findViewById(R.id.listView_task);
        listview.setAdapter(mTaskAdapter);



        //Just for Checking Empty List View
        //TODO: Find alternative and remove this
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;
        String sortOrder = TasksContract.TaskEntry.COLUMN_DONE_STATUS + " ASC, " + TasksContract.TaskEntry.COLUMN_MIN_DISTANCE + " ASC ";
        final Cursor cursor = getActivity().getContentResolver().query(uri, PROJECTION, null, null, sortOrder);
        if(!cursor.moveToFirst())
       {noTaskView.setVisibility(View.VISIBLE);}
        cursor.close();



        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Cursor c = mTaskAdapter.getCursor();
                if (c != null && c.moveToPosition(pos)) {

                    String ID = c.getString(COL_TASK_ID);
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("TaskID", ID);
                    startActivity(intent);//todo:change this to start activity for result,1
                }
            }
        });


        final ImageButton btn_Create = (ImageButton) rootView.findViewById(R.id.btnCreate);
        btn_Create.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent(getActivity(), AddNewTaskActivity.class);
                    startActivityForResult(intent, STATUS);
                    return true;
                }

                return false;
            }
        });

        return rootView;
    }

    public static int LOADER_ID = 0;

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String sortOrder = TasksContract.TaskEntry.COLUMN_DONE_STATUS + " ASC, " + TasksContract.TaskEntry.COLUMN_MIN_DISTANCE + " ASC ";
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, PROJECTION, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTaskAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTaskAdapter.swapCursor(null);
    }
    @Override
    public void onResume() {
        super.onResume();
        refreshLoader();
    }

}