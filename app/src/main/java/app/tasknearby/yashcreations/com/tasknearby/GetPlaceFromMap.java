package app.tasknearby.yashcreations.com.tasknearby;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class GetPlaceFromMap extends ActionBarActivity {
    public static String LATITUDE = "lat", LONGITUDE = "lon";

    GoogleMap map;
    LatLng finalPoint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_place_from_map);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();

        map.setMyLocationEnabled(true);

        map.setBuildingsEnabled(true);



        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
            Utility.showGpsOffDialog(this);
        }

        Location location = Utility.getCurrentLocation(this);
        if (location != null) {

            CameraPosition cameraPosition = new CameraPosition.Builder().
                    target(new LatLng(location.getLatitude(), location.getLongitude())).
                    tilt(30).
                    zoom(15).
                    bearing(0f).
                    build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition)); //CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                map.clear();
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1))
                        .anchor(0.5f, 1.0f) // Anchors the marker on the bottom left
                        .position(point));
                finalPoint = point;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_select) {
            if (finalPoint == null) {
                Toast.makeText(this, "Select a Location First!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = GetPlaceFromMap.this.getIntent();
                intent.putExtra(LATITUDE, finalPoint.latitude).putExtra(LONGITUDE, finalPoint.longitude);
                GetPlaceFromMap.this.setResult(RESULT_OK, intent);
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
