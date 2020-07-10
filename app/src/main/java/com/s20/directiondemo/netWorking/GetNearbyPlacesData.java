package com.s20.directiondemo.netWorking;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, Void, String> {

String googlePlacesData;
GoogleMap mMap;
String url;
    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        FetchURL fetchURL = new FetchURL();
        try {
            googlePlacesData = fetchURL.readURL(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String,String>> nearbyPlaces = null;
        DataParser dataParser = new DataParser();
        nearbyPlaces = dataParser.parsePlace(s);
        showNearbyPlaces(nearbyPlaces);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaces) {
        mMap.clear();
        for(int i=0; i<nearbyPlaces.size(); i++){
            HashMap<String,String> googlePlace = nearbyPlaces.get(i);
            String placeName = googlePlace.get("placeName");
            String vicinity = googlePlace.get("vicinity");
            String refernce = googlePlace.get("reference");
            double lat = Double.parseDouble(googlePlace.get("latitiude"));
            double lng = Double.parseDouble(googlePlace.get("longitude"));

            LatLng latLng = new LatLng(lat, lng);

            MarkerOptions options = new MarkerOptions().position(latLng)
                    .title(placeName + " : " + vicinity)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.addMarker(options);
        }
    }
}
