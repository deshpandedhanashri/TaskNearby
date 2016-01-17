package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class GetPlaceFromMap extends ActionBarActivity {

    GoogleMap map;
    LatLng finalPoint = null;
    Utility utility=new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_place_from_map);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(true);



        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER))
            utility.showGpsOffDialog(this);

        Location location = utility.getCurrentLocation(this);
        if (location != null)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                map.clear();
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1))
                        .anchor(0.5f, 1.0f) // Anchors the marker on the sharp point
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
                intent.putExtra(Constants.LATITUDE, finalPoint.latitude).putExtra(Constants.LONGITUDE, finalPoint.longitude);
                GetPlaceFromMap.this.setResult(RESULT_OK, intent);
                finish();
            }
            return true;
        }
        else if(id==android.R.id.home)
        {   finish();
            return true;}

        return super.onOptionsItemSelected(item);
    }


}
