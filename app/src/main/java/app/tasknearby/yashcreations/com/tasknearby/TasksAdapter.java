package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
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
    private Utility utility=new Utility();
    private Typeface mTfRegular, mTfBold ;

    TasksAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mTfRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-Regular.ttf");
        mTfBold = Typeface.createFromAsset(context.getAssets(), "fonts/Raleway-SemiBold.ttf");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_task, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView taskDistance = (TextView) view.findViewById(R.id.task_dist_textView);
        TextView taskNameView = (TextView) view.findViewById(R.id.task_name_textView);
        TextView taskLoc = (TextView) view.findViewById(R.id.task_location_textView);
        LinearLayout listItemLayout = (LinearLayout) view.findViewById(R.id.list_item_layout);

        taskDistance.setTypeface(Typeface.DEFAULT_BOLD);
        taskNameView.setTypeface(mTfRegular);
        taskLoc.setTypeface(mTfRegular);

        TasksFragment.distance = cursor.getInt(Constants.COL_MIN_DISTANCE);
        String task = cursor.getString(Constants.COL_TASK_NAME);
        String taskLocation = cursor.getString(Constants.COL_LOCATION_NAME);

        taskNameView.setText(task);
        taskLoc.setText(taskLocation);
        taskDistance.setText(utility.getDistanceDisplayString(context,TasksFragment.distance));

        int colorCode = cursor.getInt(Constants.COL_TASK_COLOR);

        Drawable bg=listItemLayout.getBackground();
        if(bg instanceof ShapeDrawable)
            ((ShapeDrawable)bg).getPaint().setColor(colorCode);
        else if(bg instanceof GradientDrawable)
            ((GradientDrawable)bg).setColor(colorCode);


        //If mTask is marked as Done, then strikeout the text.
        if(cursor.getString(Constants.COL_DONE).equals("true"))
        {
            taskNameView.setPaintFlags(taskNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskDistance.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.list_item_away_view).setVisibility(View.INVISIBLE);

        }
        else
        {
            taskNameView.setPaintFlags(taskNameView.getPaintFlags()&(~Paint.STRIKE_THRU_TEXT_FLAG));
            taskDistance.setVisibility(View.VISIBLE);
            view.findViewById(R.id.list_item_away_view).setVisibility(View.VISIBLE);
        }
    }
}
