package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Yash on 14/10/15.
 */
public class TasksAdapter extends CursorAdapter {
    public TasksAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_task, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView taskDistance = (TextView) view.findViewById(R.id.task_dist_textView);
        TextView taskNameView = (TextView) view.findViewById(R.id.task_name_textView);
        String task = cursor.getString(TasksFragment.COL_TASK_NAME);
        taskNameView.setText(task);

        if(cursor.getString(TasksFragment.COL_DONE).equals("true"))
        {taskNameView.setPaintFlags(taskNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskDistance.setVisibility(View.INVISIBLE);
        }
        else
        { taskNameView.setPaintFlags(taskNameView.getPaintFlags()&(~Paint.STRIKE_THRU_TEXT_FLAG));
            taskDistance.setVisibility(View.VISIBLE);
        }

        TextView taskLoc = (TextView) view.findViewById(R.id.task_location_textView);
        String taskLocation = cursor.getString(TasksFragment.COL_LOCATION_NAME);
        taskLoc.setText(taskLocation);


        TasksFragment.distance = cursor.getInt(TasksFragment.COL_MIN_DISTANCE);
        taskDistance.setText(Utility.getDistanceDisplayString(context,TasksFragment.distance));

        LinearLayout listItemLayout = (LinearLayout) view.findViewById(R.id.list_item_layout);
        int colorCode = cursor.getInt(TasksFragment.COL_TASK_COLOR);

        Drawable bg=listItemLayout.getBackground();
        if(bg instanceof ShapeDrawable)
            ((ShapeDrawable)bg).getPaint().setColor(colorCode);
        else if(bg instanceof GradientDrawable)
            ((GradientDrawable)bg).setColor(colorCode);



        //  listItemLayout.setBackgroundColor(colorCode);


    }
}
