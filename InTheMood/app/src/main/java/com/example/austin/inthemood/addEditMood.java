package com.example.austin.inthemood;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
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
public class addEditMood extends AppCompatActivity {
    private addEditMood activity = this;
    private dataControler controller;
    private String FILENAME = "file.sav";
    private Mood targetMood;
    private Bitmap imageBitMap;
    private LocationControllor locationControllor;

    //UI Elements
    private Spinner moodSpinner;
    private Spinner scenarioSpinner;
    private EditText triggerText;
    private Button saveButton;
    private Button deleteButton;
    private Button imageButton;
    private ImageView pictureView;
    private GoogleApiClient mGoogleApiClient;
    private Switch locationSwitch;

    private Boolean isOnline;

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
        imageButton = (Button) findViewById(R.id.imageButton);
        pictureView = (ImageView) findViewById(R.id.imageView);
        locationSwitch = (Switch) findViewById(R.id.locationSwitch);
        isOnline = NetworkStatus.getInstance(this.getBaseContext()).isOnline();

        //Grab the data controller
        loadFromFile();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                takePhoto();
            }
        });

        locationControllor = new LocationControllor(mGoogleApiClient, activity);
        // constructor builds the mGoogleApiClient we need to reset the reference
        mGoogleApiClient = locationControllor.getGoogleApiClient();

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
        moodIndex = intent.getIntExtra("Mood index", -1);
        if (moodIndex != -1) {
            targetMood = controller.getCurrentUser().getMyMoodsList().get(moodIndex);
            moodSpinner.setSelection(moodAdapter.getPosition(targetMood.getMoodName()));
            scenarioSpinner.setSelection(socialAdapter.getPosition(targetMood.getMoodScenario()));
            triggerText.setText(targetMood.getMoodDescription());
            if (targetMood.getLatLng() != null)
                locationSwitch.setChecked(true); // TODO what to do if they want to update location?
            if(targetMood.getMoodImg() != null) {
                pictureView.setImageBitmap(targetMood.getMoodImg());
            }

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
                    Mood newMood = new Mood(controller.getCurrentUser().getName());
                    newMood.setMoodName(moodName);
                    newMood.setMoodDescription(trigger);

                    Location location = locationControllor.getCurrentLocation();

                    if (location != null) {
                        LatLng latLng = LocationControllor.locationToLatLng(location);
                        newMood.setLatLng(latLng);
                    }

                    controller.getCurrentUser().addMood(newMood);


                } else {
                    // Edit the existing Mood with the changes supplied.
                    targetMood.setMoodName(moodName);
                    targetMood.setMoodDescription(trigger);
                    targetMood.setmoodScenario(scenario);
                    if(imageBitMap != null){targetMood.setMoodImg(imageBitMap);}
                }
                Intent intent = new Intent(activity, MyMoods.class);
                Boolean syncSuccess = false;
                if (isOnline) {
                    syncSuccess = controller.ElasticSearchsyncUser(controller.getCurrentUser());
                }
                Log.i("SyncSuccess", syncSuccess.toString());
                saveInFile();
                startActivity(intent);
                finish();
            }
        });

        /*
         * Code that is run when the Delete button is clicked. Deletes the Mood and removes
         * it from the list of the User's Moods.
         */
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.getCurrentUser().removeMood(targetMood);
                Boolean syncSuccess = false;
                if (isOnline) {
                    syncSuccess = controller.ElasticSearchsyncUser(controller.getCurrentUser());
                }
                Log.i("SyncSuccess", syncSuccess.toString());
                Intent intent = new Intent(activity, MyMoods.class);
                saveInFile();
                startActivity(intent);
                finish();
            }
        });

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    locationControllor.stopLocationUpdates();
                    return;
                }

                if (!locationControllor.checkLocationPermission()) {
                    locationControllor.requestLocationPermission();
                }


            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            locationControllor.startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            locationControllor.stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationControllor.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // todo make sure its high accuracy
                        locationControllor.setCanGetLocation(true);
                        locationControllor.startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        locationControllor.setCanGetLocation(false);
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if(extras.get("data") != null){
                        imageBitMap = (Bitmap) extras.get("data");
                        pictureView.setImageBitmap(imageBitMap);
                    }
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
            Log.i("gsonToJson", gson.toJson(controller));
            writer.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    //Start intent to take a photo
    //Adapted from https://developer.android.com/training/camera/photobasics.html
    // and https://github.com/alisajedi/MyCameraTest1
    private void takePhoto()
    {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }

    }
}


