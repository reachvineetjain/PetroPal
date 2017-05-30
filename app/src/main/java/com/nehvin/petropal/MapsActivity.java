package com.nehvin.petropal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locMgr;
    private LocationListener locListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        }
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
        }
        Location lastUnkownLocation = fetchBestLocation();
        if (lastUnkownLocation != null) {
            updateCurrentLoc(lastUnkownLocation);
        }
    }

    private Location fetchBestLocation() {
        Location locationGPS = null;
        Location locationNetwork = null;

        // get both but return more accurate of GPS & network location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationGPS = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationNetwork = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (locationGPS == null && locationNetwork == null)
            return null;
        else
            if (locationGPS == null)
                return locationNetwork;
        else
            if (locationNetwork == null)
                return locationGPS;
        else
            return (locationGPS.getAccuracy() < locationNetwork.getAccuracy() ? locationGPS : locationNetwork);
    }

    private void updateCurrentLoc(Location lastUnkownLocation) {
        LatLng currentLocation = new LatLng(lastUnkownLocation.getLatitude(), lastUnkownLocation.getLongitude());

//        String nameOfPlace = getAddressOnMarker(lastUnkownLocation);

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLocation)); //.title("Your Current Location")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
    }

    @NonNull
    private String getAddressOnMarker(Location lastUnkownLocation) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String nameOfPlace = "";
        try {
            List<Address> address = geocoder.getFromLocation(lastUnkownLocation.getLatitude(), lastUnkownLocation.getLongitude(), 1);

            if (address != null && address.size() > 0) {
                if (address.get(0).getThoroughfare() != null )
                {
                    if (address.get(0).getSubThoroughfare() != null )
                    {
                        nameOfPlace += address.get(0).getSubThoroughfare()+" ";
                    }
                    nameOfPlace += address.get(0).getThoroughfare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(nameOfPlace == "")
        {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:HH yyyyMMdd");
            nameOfPlace = sdf.format(new Date());
        }

        return nameOfPlace;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Intent intent = getIntent();
        Location localLocation = fetchBestLocation();

        if(intent.getStringExtra("zipcode") != null && "" != (intent.getStringExtra("zipcode")))
        {
            Geocoder geoCoder = new Geocoder(getApplicationContext(),Locale.getDefault());
            try
            {
                List<Address> addr = geoCoder.getFromLocationName(intent.getStringExtra("zipcode").toString(),1);
                if(addr != null && addr.size()>0)
                {
                    localLocation = new Location(LocationManager.GPS_PROVIDER);
                    localLocation.setLatitude(addr.get(0).getLatitude());
                    localLocation.setLongitude(addr.get(0).getLongitude());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        locListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location)
            {

//                if (location != null) {
//                    updateCurrentLoc(location);
//                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (Build.VERSION.SDK_INT < 23)
        {
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
        }
        else
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
            {
                locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
                Location location = fetchBestLocation();
//                updateCurrentLoc(location);
            }
        }
        updateCurrentLoc(localLocation);
    }
}