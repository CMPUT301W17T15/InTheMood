package com.example.austin.inthemood;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;


/** This activity handles all of creating, editing, and deletion of a User's Mood.
 * The activity knows if it is working with an existing mood if it receives a non-
 * negative (and thus valid) Mood index from from an intent extra. If working with
 * an existing Mood, the User can change the fields of the Mood (except the date,
 * which is set in real time for the User), and then save those changes with the
 * Save button, or delete the Mood with the Delete button. If no existing Mood was
 * supplied, then the User can only add a new Mood with the values in the text /
 * dropdown fields.
 *
 * TODO: Get the scenario of an existing mood
 */
public class addEditMood extends AppCompatActivity  implements
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = -1;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final int PERMISSION_ACCESS_FINE_LOCATION_REQUEST = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private boolean locationPermission;
    private boolean highAccuracyLocationSettings;
    private addEditMood activity = this;
    private dataControler controller;
    private String FILENAME = "file.sav";
    private Mood targetMood;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    //UI Elements
    private Spinner moodSpinner;
    private Spinner scenarioSpinner;
    private EditText triggerText;
    private Button saveButton;
    private Button deleteButton;
    private GoogleApiClient mGoogleApiClient;
    private Switch locationSwitch;

    //Mood Index
    int moodIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_mood);

        //Initialize UI Elements
        moodSpinner = (Spinner) findViewById(R.id.addEditMoodMoodSpinner);
        scenarioSpinner = (Spinner) findViewById(R.id.addEditMoodScenarioSpinner);
        triggerText = (EditText) findViewById(R.id.addEditMoodsTriggerText);
        saveButton = (Button) findViewById(R.id.addEditMoodSaveButton);
        deleteButton = (Button) findViewById(R.id.addEditMoodDeleteButton);
        locationSwitch = (Switch) findViewById(R.id.locationSwitch);

        //Grab the data controller
        loadFromFile();

        // Create an instance of GoogleAPIClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // create LocationRequest Object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(MILLISECONDS_PER_SECOND * 5)
                .setFastestInterval(MILLISECONDS_PER_SECOND * 1);

        /*
         * Spinner initialization shamelessly taken from https://developer.android.com/guide/topics/ui/controls/spinner.html
         */

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> moodAdapter = ArrayAdapter.createFromResource(this,
                R.array.moods, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        moodSpinner.setAdapter(moodAdapter);

        ArrayAdapter<CharSequence> socialAdapter = ArrayAdapter.createFromResource(this,
                R.array.social_situations, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        scenarioSpinner.setAdapter(socialAdapter);

        //Check if a mood was passed in
        Intent intent = getIntent();
        //TODO: get the scenario of a mood
        moodIndex = intent.getIntExtra("Mood index", -1);
        if (moodIndex != -1) {
            targetMood = controller.getCurrentUser().getMyMoodsList().get(moodIndex);
            moodSpinner.setSelection(moodAdapter.getPosition(targetMood.getMoodName()));
            //scenarioSpinner.setSelection(socialAdapter.getPosition(targetMood.get));
            triggerText.setText(targetMood.getMoodDescription());
        } else {
            // Hide the delete button, since you can't delete a Mood that doesn't exist!
            deleteButton.setVisibility(View.GONE);
        }

        /*
         * Code that is run when the Save button is clicked. Saves the user input and creates a mood
         * and adds it to the list of moods made by the Current User.
         *
         */

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String moodName = moodSpinner.getSelectedItem().toString();
                String scenario = scenarioSpinner.getSelectedItem().toString();
                String trigger = triggerText.getText().toString();

                //If making a new Mood:
                if (moodIndex == -1) {
                    if (mLastLocation == null) {
                        getLocation();
                    }
                    Mood newMood = new Mood();
                    newMood.setMoodName(moodName);
                    newMood.setMoodDescription(trigger);
                    // add location to mood here
                    controller.getCurrentUser().addMood(newMood);
                } else {
                    // Edit the existing Mood with the changes supplied.
                    targetMood.setMoodName(moodName);
                    targetMood.setMoodDescription(trigger);
                }
                Intent intent = new Intent(activity, MyMoods.class);
                saveInFile();
                startActivity(intent);
            }
        });

        /*
         * Code that is run when the Delete button is clicked. Deletes the Mood and removes
         * it from the list of the User's Moods.
         */
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.getCurrentUser().removeMood(targetMood);
                Intent intent = new Intent(activity, MyMoods.class);
                saveInFile();
                startActivity(intent);
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    return;
                }
                // get location
                if (!checkLocationPermision()) {
                    requestLocationPermission();
                }

                checkLocationSettings();

                // location might be null on first call. check at mood creation
                if (locationPermission && highAccuracyLocationSettings) {
                    getLocation();
                }
            }
        });
    }


    /*
     * get the device location. the location permission must be granted to the app
     * and the settings must be modified before calling this method or else the app will crash.
     */
    private void getLocation() {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } else {
                handleNewLocation(location);
            }
        } catch (SecurityException e) {
            checkLocationSettings();
        }
    }

    private boolean checkLocationPermision() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
            return true;
        } else {
            locationPermission = false;
            return false;
        }
    }

    /*
     * request the location permission from the user.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION_REQUEST);
    }

    /*
     * ask the user if they want to change the location settings
     * based on http://blog.teamtreehouse.com/beginners-guide-location-android
     * accessed on March 27, 2017
     */
    private void checkLocationSettings() {
        if (mGoogleApiClient != null) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5 * 1000);
            locationRequest.setFastestInterval(1 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            highAccuracyLocationSettings = true;
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                highAccuracyLocationSettings = false;
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            highAccuracyLocationSettings = false;
                            break;
                    }
                }
            });
        }
    }

    /*
     * check the result from requesting the location permission
     * from https://stackoverflow.com/questions/33865445/gps-location-provider-requires-access-fine-location-permission-for-android-6-0
     * accessed on March 27, 2017
     */

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermission = true;

                } else {
                    locationPermission = false;
                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        highAccuracyLocationSettings = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        highAccuracyLocationSettings = false;
                }
        }
    }

    // Load the data controller stored in the specified file.
    // Taken from: the CMPUT301 lonelyTwitter lab examples
    private void loadFromFile() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();

            Type objectType = new TypeToken<dataControler>() {}.getType();
            controller = gson.fromJson(in, objectType);
        } catch (FileNotFoundException e) {
            User firstUser = new User("admin", "admin");
            controller = new dataControler(firstUser);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    // Save the data controller into the specified file.
    // Taken from: the CMPUT301 lonelyTwitter lab examples
    private void saveInFile() {
        try {

            FileOutputStream fos = openFileOutput(FILENAME,0);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            Gson gson = new Gson();
            gson.toJson(controller, writer);
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } else {
                handleNewLocation(location);
            }
        } catch (SecurityException e) {}
    }

    private void handleNewLocation(Location location) {
        mLastLocation = location;
//        Toast.makeText(activity, mLastLocation.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(addEditMood.class.getSimpleName(), "Location services connection failed with code "
                    + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}
