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
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.nehvin.petropal.PetroUtils.buildUrl;
import static com.nehvin.petropal.PetroUtils.fetchBestLocation;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locMgr;
    private LocationListener locListner;
    private String zipcode;
    private static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        zipcode = intent.getStringExtra("zipcode");

//        Location localLocation = null;

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

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "You need to provide location permission to get your current " +
                            "location or the area/zipcode entered by you ", Toast.LENGTH_LONG).show();

// Snackbar works with API 22 and above. This code is compatible from API 15 onwards
//                    Snackbar snackbar = Snackbar.make(getView(), getResources().getString(R.string.message_no_storage_permission_snackbar), Snackbar.LENGTH_LONG);
//                    snackbar.setAction(getResources().getString(R.string.settings), new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (getActivity() == null) {
//                                return;
//                            }
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
//                            intent.setData(uri);
//                            OrderDetailFragment.this.startActivity(intent);
//                        }
//                    });
//                    snackbar.show();
                }

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
            {
//                    locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
//                Location location = PetroUtils.fetchBestLocation(this);
//                localLocation = fetchBestLocation();
//                updateCurrentLoc(location);
            }
        }

        updateLOCBasedonUserInput();

    }

    private void updateLOCBasedonUserInput() {
        Location localLocation = null;
        if(zipcode != null && !TextUtils.isEmpty(zipcode))
        {
            Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try
            {
                List<Address> addr = geoCoder.getFromLocationName(zipcode,1);
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
        else
        {
            localLocation = fetchBestLocation(this);
        }
        if(localLocation != null){
            URL url = buildUrl(localLocation);
            GasStationData gdt = new GasStationData();
            ArrayList<PetrolPumpData> petrolPumpDataArrayList=null;
            try {
                String listofGasStations = gdt.execute(url).get();
                petrolPumpDataArrayList = PetroUtils.extractPetrolPumpData(listofGasStations);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            updateCurrentLoc(localLocation,petrolPumpDataArrayList);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        }
        else
        {
            //TODO - gracefull exit of application and not show some weird locations
            Log.i(TAG, "Location permission was NOT granted.Showing for current location");
        }
    }

    public void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
        }
        Location lastUnkownLocation = fetchBestLocation(this);
        if (lastUnkownLocation != null) {
            updateLOCBasedonUserInput();
        }
    }

    public void updateCurrentLoc(Location lastUnkownLocation) {
        LatLng currentLocation = new LatLng(lastUnkownLocation.getLatitude(), lastUnkownLocation.getLongitude());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLocation)); //.title("Your Current Location")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
    }

    public void updateCurrentLoc(Location lastUnkownLocation, ArrayList<PetrolPumpData> listOfPumps) {

        if (listOfPumps != null && listOfPumps.size()>0) {
            mMap.clear();
            addPetrolPumpsOnMap(listOfPumps);
        }

        LatLng currentLocation = new LatLng(lastUnkownLocation.getLatitude(), lastUnkownLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

    }



    public void addPetrolPumpsOnMap(ArrayList<PetrolPumpData> listOfPumps)
    {
        PetrolPumpData pp_data;
        for (int i =0; i<listOfPumps.size();i++) {
            pp_data = listOfPumps.get(i);
            LatLng currentLocation = new LatLng(Double.parseDouble(pp_data.getPp_lat()),
                    Double.parseDouble(pp_data.getPp_lng()));
            PetroUtils.getAddressAt(currentLocation,this);
            if (pp_data.getPp_name().contains("Hindustan")) {
                mMap.addMarker(new MarkerOptions().position(currentLocation)
                        .title(pp_data.getPp_name()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            else
            {
                if (pp_data.getPp_name().contains("Bharat")) {
                    mMap.addMarker(new MarkerOptions().position(currentLocation)
                            .title(pp_data.getPp_name()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                }
                else {
                    if (pp_data.getPp_name().contains("Indian")) {
                        mMap.addMarker(new MarkerOptions().position(currentLocation)
                                .title(pp_data.getPp_name()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    } else {
                        mMap.addMarker(new MarkerOptions().position(currentLocation)
                                .title(pp_data.getPp_name()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    }
                }
            }
        }
    }


    @NonNull
    public String getAddressOnMarker(Location lastUnkownLocation) {
        Geocoder geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());

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

    private class GasStationData extends AsyncTask<URL, Void, String>{
        @Override
        protected String doInBackground(URL... params) {
            URL queryString = params[0];
            String queryResult = null;
            try {
                queryResult = PetroUtils.getResponseFromHttpUrl(queryString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return queryResult;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG, s);
        }
    }
}