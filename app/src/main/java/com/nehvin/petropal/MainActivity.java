package com.nehvin.petropal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    RadioGroup rgroup ;
    RadioButton loc_rdButton;
    RadioButton zip_rdButton;
    EditText zipText;
    Intent mapIntent=null;


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
        }
        else
        {
            mapIntent.putExtra("zipcode","");
        }

        startActivity(mapIntent);
    }
}