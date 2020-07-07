package com.s20.directiondemo;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public interface IPassData {
    void destinationSelected(LatLng location, GoogleMap map);
}
