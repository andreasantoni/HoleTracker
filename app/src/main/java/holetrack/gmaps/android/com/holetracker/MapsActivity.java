package holetrack.gmaps.android.com.holetracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import holetrack.gmaps.android.com.holetracker.Utility.GPSTracker;
import holetrack.gmaps.android.com.holetracker.data.LocationsContentProvider;
import holetrack.gmaps.android.com.holetracker.data.LocationsDB;

public class MapsActivity extends ActionBarActivity  {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    private void setUpMap() {
        setContentView(R.layout.activity_map_fragment);
        addMapFragment();
        /*mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker").snippet("Snippet"));

        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!").snippet("Consider yourself located"));*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_map) {
            addMarkerCurrentPosition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void addMapFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        MapFragment fragment = new MapFragment();
        transaction.add(R.id.mapView, fragment);
        transaction.commit();
    }

    private void addMarkerCurrentPosition()
    {
        GPSTracker gps = new GPSTracker(this.getApplicationContext());
        double lat = 0;
        double lng = 0;
        if (!gps.canGetLocation()) {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        lat = gps.getLatitude();
        lng = gps.getLongitude();

        LatLng location = new LatLng(lat, lng);

        // Drawing the marker in the Google Maps
        drawMarker(location);
        // Creating an instance of ContentValues
        ContentValues contentValues = new ContentValues();

        // Setting latitude in ContentValues
        contentValues.put(LocationsDB.FIELD_LAT, lat);

        // Setting longitude in ContentValues
        contentValues.put(LocationsDB.FIELD_LNG, lng);

        // Setting zoom in ContentValues
        contentValues.put(LocationsDB.FIELD_ZOOM, mMap.getCameraPosition().zoom);

        // Creating an instance of LocationInsertTask
        LocationInsertTask insertTask = new LocationInsertTask();

        // Storing the latitude, longitude and zoom level to SQLite database
        insertTask.execute(contentValues);

        Toast.makeText(getApplicationContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();
    }

    private void drawMarker(LatLng point) {
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.title("Attenzione Buca!!!!");
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        // Setting latitude and longitude for the marker
        markerOptions.position(point);

        // Adding marker on the Google Map
        mMap.addMarker(markerOptions);

        // Moving CameraPosition to last clicked position
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

        // Setting the zoom level in the map on last position  is clicked
        mMap.animateCamera(CameraUpdateFactory.zoomTo(0));
    }

    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void> {
        @Override
        protected Void doInBackground(ContentValues... contentValues) {

            /** Setting up values to insert the clicked location into SQLite database */
            getApplicationContext().getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }
}
