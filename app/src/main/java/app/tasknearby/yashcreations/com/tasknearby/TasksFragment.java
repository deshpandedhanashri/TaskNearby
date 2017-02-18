package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

/**
 * Created by Yash on 22/04/15.
 */
public class TasksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final int REQUEST_CODE_ADD_TASK = 5;
    public static int distance = 0;
    final int LOADER_ID = 0;

    private TasksAdapter mTaskAdapter;
    View rootView;
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_main, container,false);

        ListView listview = (ListView) rootView.findViewById(R.id.listView_task);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fabMain);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        mTaskAdapter = new TasksAdapter(getActivity(),null, 0);
        listview.setAdapter(mTaskAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Cursor c = mTaskAdapter.getCursor();
                if (c != null && c.moveToPosition(pos)) {

                    Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                    intent.putExtra(Constants.TaskID, c.getString(Constants.COL_TASK_ID));
                    startActivity(intent);
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewTaskActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK);
            }
        });

        return rootView;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String sortOrder = TasksContract.TaskEntry.COLUMN_DONE_STATUS + " ASC, " + TasksContract.TaskEntry.COLUMN_MIN_DISTANCE + " ASC ";
        Uri uri = TasksContract.TaskEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, Constants.PROJECTION_TASKS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTaskAdapter.swapCursor(data);
        TextView noTasksTV = (TextView) rootView.findViewById(R.id.textView);
        if(data.getCount()==0) {
            noTasksTV.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf"));
            noTasksTV.setVisibility(View.VISIBLE);
        }
        else
            noTasksTV.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTaskAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

}