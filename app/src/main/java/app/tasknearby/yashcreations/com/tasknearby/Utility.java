package app.tasknearby.yashcreations.com.tasknearby;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.LineNumberInputStream;
import java.net.PortUnreachableException;
import java.util.List;

import app.tasknearby.yashcreations.com.tasknearby.database.TasksContract;

/**
 * Created by Yash on 25/04/15.
 */
public class Utility {

    public static String color;

    public static void savePlaceDialog(final Context context, final Double latitude, final Double longitude) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Save the new Place");
        final EditText input = new EditText(context);
        alertDialog.setView(input);
        input.setHint("Enter the Place's Name");
        alertDialog.setPositiveButton("Save Place",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String place = input.getText().toString();
                        AddNewTaskActivity.mTaskLocation = place;
                        AddNewTaskActivity.selectedLocationDisplayView.setText(place);

                        addLocation(context, place, latitude, longitude);
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(    //To Hide The Keyboard
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        Toast.makeText(context, "Place Saved", Toast.LENGTH_SHORT).show();
                    }
                });
        alertDialog.show();

    }

    public static void selectColorDialog(final Context context) {

        final String values[] = {"Tomato", "Tangerine", "Peacock", "Lavender", "Grape", "Pink"};
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Make your selection");
        alertDialog.setItems(values, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                AddNewTaskActivity.returnedColorName = values[item];
                AddNewTaskActivity.colorButton.setText("Color: " + AddNewTaskActivity.returnedColorName);
                AddNewTaskActivity.returnedColorCode = Utility.getColorCodeFromString(context, AddNewTaskActivity.returnedColorName);
                AddNewTaskActivity.baseLayout.setBackgroundColor(AddNewTaskActivity.returnedColorCode);
            }
        });
        alertDialog.show();


    }

    public static int getColorCodeFromString(Context context, String colorName) {

        int hexCode;

        if (colorName.equals("Tomato"))
            hexCode = Color.parseColor("#ec2d01");
        else if (colorName.equals("Tangerine"))
            hexCode = Color.parseColor("#ffcc00");
        else if (colorName.equals("Lavender"))
            hexCode = Color.parseColor("#b378d3");
        else if (colorName.equals("Peacock"))
            hexCode = Color.parseColor("#1E90FF");
        else if (colorName.equals("Pink"))
            hexCode = Color.parseColor("#FF82AB");
        else //grape
            hexCode = Color.parseColor("#ff6f2da8");

        return hexCode;
    }

    public static void addLocation(Context context, String placeName, Double latitude, Double longitude) {
        Cursor c = context.getContentResolver().query(TasksContract.LocationEntry.CONTENT_URI,
                new String[]{TasksContract.LocationEntry._ID},
                TasksContract.LocationEntry.COLUMN_PLACE_NAME + "=?",
                new String[]{placeName}, null);

        if (!c.moveToFirst())            //Location doesn't exists in the database; hence insert the Location
        {
            ContentValues locationValues = new ContentValues();
            locationValues.put(TasksContract.LocationEntry.COLUMN_PLACE_NAME, placeName);
            locationValues.put(TasksContract.LocationEntry.COLUMN_COORD_LAT, latitude);
            locationValues.put(TasksContract.LocationEntry.COLUMN_COORD_LONG, longitude);

            Uri insertedUri = context.getContentResolver().insert(
                    TasksContract.LocationEntry.CONTENT_URI,
                    locationValues
            );
        }
        c.close();

    }

    public static void showGpsOffDialog(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("No Location Available");
        alertDialog.setMessage("Location is turned Off!\nPlease click Ok to switch it On");
        final EditText input = new EditText(context);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();


    }

    public static Location getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return currentLocation;
    }


    public static int getDistanceByPlaceName(String placeName, Location currentLocation, Context context) {
        float distance = 0;
        Location location = new Location("Dummy");

        Cursor c = context.getContentResolver().query(TasksContract.LocationEntry.CONTENT_URI,
                new String[]{TasksContract.LocationEntry.COLUMN_COORD_LAT, TasksContract.LocationEntry.COLUMN_COORD_LONG, TasksContract.LocationEntry.COLUMN_PLACE_NAME},
                TasksContract.LocationEntry.COLUMN_PLACE_NAME + "=?",
                new String[]{placeName}, null);

        if (c.moveToNext()) {
            location.setLatitude(c.getDouble(0));
            location.setLongitude(c.getDouble(1));
            }
        else {
            Toast.makeText(context, "null Cursor", Toast.LENGTH_LONG).show();
            location.setLatitude(currentLocation.getLatitude());
            location.setLongitude(currentLocation.getLongitude());
        }
        c.close();
        if (currentLocation != null) {
            distance = currentLocation.distanceTo(location);
            int dist = Math.round(distance);

            return dist;
        } else {
            return 0;
        }


    }

    public static LatLng getLatLngByPlaceName(Context context, String placeName) {
        LatLng latLng = null;
        Cursor c = context.getContentResolver().query(TasksContract.LocationEntry.CONTENT_URI,
                new String[]{TasksContract.LocationEntry.COLUMN_COORD_LAT, TasksContract.LocationEntry.COLUMN_COORD_LONG, TasksContract.LocationEntry.COLUMN_PLACE_NAME},
                TasksContract.LocationEntry.COLUMN_PLACE_NAME + "=?",
                new String[]{placeName}, null);
        if (c.moveToNext()) {
            latLng = new LatLng(c.getDouble(0), c.getDouble(1));
        }
        c.close();
        return latLng;
    }


    public static void showRemindDistanceDialog(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Reminding Range");
        final EditText input = new EditText(context);
        if (isMetric(context))
            input.setHint("Enter the distance in m");
        else
            input.setHint("Enter the distance in yd");
        alertDialog.setMessage("Please Enter the closest distance to the Task Location for reminder");
        alertDialog.setView(input);


        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try {
                    int dis = Integer.parseInt(input.getText().toString());
                    int dis_show = dis;
                    if (!isMetric(context)) {
                        Double d = dis / 1.09361;
                        dis = d.intValue();
                    }
                    AddNewTaskActivity.remindDistance = dis;
                    AddNewTaskActivity.remindDistanceView.setText("Remind when closer than " + getDistanceDisplayString(context, dis_show));

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(    //To Hide The Keyboard
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                }
                catch (NumberFormatException excep)
                {
                    Toast.makeText(context,"Please enter the distance correctly!",Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertDialog.show();

    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String getDistanceDisplayString(Context context, int distance) {
        if (isMetric(context)) {
            if (distance < 1000)
                return (distance + "m");
            else
                return ((float) distance / 1000 + "km");
        } else {
            Double d = distance * 1.09361;
            distance = d.intValue();
            if (distance < 1760)
                return (distance + "yd");
            else
                return ((float) distance / 1760 + "mi");
        }
    }

    public static long getUpdateInterval(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String m = prefs.getString(context.getString(R.string.pref_interval_key),
                context.getString(R.string.pref_interval_default));
        if (m.equals("20s"))
            return 20 * 1000;
        else if (m.equals("30s"))
            return 30 * 1000;
        else if (m.equals("45s"))
            return 45 * 1000;
        else if (m.equals("60s")) {
            return 60 * 1000;
        } else
            return 5 * 1000;
    }
}