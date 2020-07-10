package com.s20.directiondemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.s20.directiondemo.netWorking.GetDirectionsData;
import com.s20.directiondemo.netWorking.GetNearbyPlacesData;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 3000;
    private static final int RADIUS = 1500;

    // use fused location provider
    private FusedLocationProviderClient mClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private LatLng userLocation;

    FragmentManager fragmentManager;
    MapsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        fragment = new MapsFragment();
        fragmentManager.beginTransaction()
                .add(R.id.myContainer, fragment)
                .commit();

        TabLayout tab = findViewById(R.id.tab);
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String url = "";
                switch (tab.getPosition()){
                    case 0:

                        url = getPlaceUrl(userLocation.latitude, userLocation.longitude, "hospital");

                    case 1:

                        url = getPlaceUrl(userLocation.latitude, userLocation.longitude,"restaurant");
                }

                showNearbyPlaces(url);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.getDestination(new IPassData() {
                    @Override
                    public void destinationSelected(Location location, GoogleMap map) {
                        Object[] dataTransfer = new Object[3];
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        String url = getDirectionUrl(latLng);
                        dataTransfer[0] = map;
                        dataTransfer[1] = url;
                        dataTransfer[2] = latLng;

                        GetDirectionsData getDirectionsData = new GetDirectionsData();
                        //execute async
                        getDirectionsData.execute(dataTransfer);
                    }
                });
            }
        });

        if (!hasPermission()) {
            requestLocationPermission();
        } else {
            startUpdateLocation();
        }
    }

    private void showNearbyPlaces(String url) {
        Object[] dataTransfer;
        GoogleMap map = fragment.getmMap();
        dataTransfer = new Object[2];
        dataTransfer[0] = map;
        dataTransfer[1] = url;

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);
    }

    private String getPlaceUrl(double latitude, double longitude, String placeType) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+RADIUS);
        googlePlaceUrl.append("&type="+placeType);
        googlePlaceUrl.append("&key="+getString(R.string.google_maps_key));
        Log.d(TAG, "getplacesURL: " + googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    private String getDirectionUrl(LatLng location) {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin="+userLocation.latitude+","+userLocation.longitude);
        googleDirectionUrl.append("&destination="+location.latitude+","+location.longitude);
        googleDirectionUrl.append("&key="+getString(R.string.google_maps_key));
        Log.d(TAG, "getdirectionalURL: " + googleDirectionUrl.toString());
        return googleDirectionUrl.toString();
    }

    private void startUpdateLocation() {
        mClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                Log.d(TAG, "onLocationResult: " + location);
                fragment.setHomeMarker(location);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Important
        mClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startUpdateLocation();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            fragment.getmMap().clear();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onLaunchShowNearbyPlaces(LatLng location){
        String  url = getPlaceUrl(location.latitude, location.longitude, "hospital");
        showNearbyPlaces(url);
    }
}