package com.nehvin.petropal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{

    RadioGroup rgroup ;
    RadioButton loc_rdButton;
    RadioButton zip_rdButton;
    EditText zipText;
    Intent mapIntent=null;
    GoogleApiClient mGoogleApiClient;
    String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

    }

    private void initialize()
    {
        rgroup = (RadioGroup) findViewById(R.id.radioGroup);
        rgroup.check(R.id.loc_radioButton);
        loc_rdButton = (RadioButton) findViewById(R.id.loc_radioButton);
        zip_rdButton = (RadioButton) findViewById(R.id.zipcode_radioButton);
        zipText = (EditText) findViewById(R.id.zipcode);
        zipText.setEnabled(false);
        mapIntent = new Intent(getApplicationContext(),MapsActivity.class);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.loc_radioButton:
                if (checked) {
                    zipText.setEnabled(false);
                    zipText.setText("");
                }
                    break;
            case R.id.zipcode_radioButton:
                if (checked)
                    zipText.setEnabled(true);
                    break;
        }
    }

    public void getPetrolPumps(View view)
    {
        if(zipText.isEnabled())
        {
            Log.i("VKJ",zipText.getText().toString());
            mapIntent.putExtra("zipcode",zipText.getText().toString());
            mapIntent.putExtra("current",false);
        }
        else
        {
            mapIntent.putExtra("zipcode","");
            mapIntent.putExtra("current",true);
        }
        startActivity(mapIntent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Inside On Start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Inside On Stop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Inside On Pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Inside On Resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Inside On Destroy");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i(TAG, "Inside On Post Create");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(TAG, "Inside On Post Resume");
    }
}