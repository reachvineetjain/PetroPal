package com.nehvin.petropal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by Vineet K Jain on 6/3/2017.
 */

public class PetroUtils {

    private static String TAG = PetroUtils.class.getSimpleName();

    public static Location fetchBestLocation(MapsActivity mapsActivity) {
        LocationManager locMgr = (LocationManager) mapsActivity.getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = null;
        Location locationNetwork = null;

        // get both but return more accurate of GPS & network location
        if (ContextCompat.checkSelfPermission(mapsActivity.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /*
    https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=19.2082566,72.8355851
    &radius=2000&type=gas_station&key=AIzaSyByJOismjcOPvQaciexz6uiPa89kGAOvQ4
    */
    private static final String baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
            "radius=3000&type=gas_station&key=AIzaSyByJOismjcOPvQaciexz6uiPa89kGAOvQ4";
    private static final String LOCATION = "location";

    public static URL buildUrl(Location loc) {
        String latlng = loc.getLatitude()+","+loc.getLongitude();
        Uri builtUri = Uri.parse(baseURL).buildUpon()
                .appendQueryParameter(LOCATION, latlng)
//                .appendQueryParameter(PARAM_SORT, sortBy)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Log.i(TAG,url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static ArrayList<PetrolPumpData> extractPetrolPumpData(String jsonData) {

        // Create an empty ArrayList that we can start adding listOfPetrolPumps to
        ArrayList<PetrolPumpData> listOfPetrolPumps = new ArrayList<PetrolPumpData>();
        String lat;
        String lng;
        String name;
        String addr;

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            JSONObject petrolPumps = new JSONObject(jsonData);
            JSONArray results = petrolPumps.optJSONArray("results");
            if(results != null && results.length() > 0)
            {
                for(int i=0; i < results.length(); i++)
                {
                    JSONObject petrolPump = results.getJSONObject(i);
                    JSONObject geometry = petrolPump.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");
                    lat = location.getString("lat");
                    lng = location.getString("lng");
                    name = petrolPump.getString("name");
                    addr = petrolPump.getString("vicinity");
                    listOfPetrolPumps.add(new PetrolPumpData(lat,lng,name,addr));
                }
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, "Problem parsing the petrol pump JSON results", e);
        }

        // Return the list of earthquakes
        return listOfPetrolPumps;
//        return null;
    }

    public static void getAddressAt(LatLng location, MapsActivity mapsActivity)
    {
        Geocoder geocoder = new Geocoder(mapsActivity.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> address = geocoder.getFromLocation(location.latitude,location.longitude,10);

            if(address != null && address.size()>0)
            {
                Log.i(TAG,address.get(0).toString());
                StringBuilder addressBuild = new StringBuilder();
                addressBuild.append("Address Line 1: ").append(address.get(0).getAddressLine(0)).append("\n");
                addressBuild.append("Address Line 2: ").append(address.get(0).getAddressLine(1)).append("\n");
                addressBuild.append("Address Line 3: ").append(address.get(0).getAddressLine(2)).append("\n");
                addressBuild.append("Address Line 4: ").append(address.get(0).getAddressLine(3)).append("\n");
                addressBuild.append("Feature Name: ").append(address.get(0).getFeatureName()).append("\n");
                addressBuild.append("Premesis: ").append(address.get(0).getPremises()).append("\n");
                addressBuild.append("Locality: ").append(address.get(0).getLocality()).append("\n");
//                addressBuild.append("Bundle: ").append(address.get(0).getExtras().toString()).append("\n");
                addressBuild.append("Feature Name: ").append(address.get(0).getPhone()).append("\n");
                Log.i(TAG,addressBuild.toString());
//                Toast.makeText(mapsActivity, "Inside getAddress", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}