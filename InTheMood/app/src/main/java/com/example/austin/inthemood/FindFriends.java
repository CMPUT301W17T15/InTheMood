package com.example.austin.inthemood;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;

public class FindFriends extends AppCompatActivity {

    private EditText searchableUserName;

    private User locatedUser;
    private dataControler controller;
    private static final String FILENAME = "file.sav";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        loadFromFile();

        //update current user from elasticSearch

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        loadFromFile();

        //update current user from elasticSearch

    }

    public void searchFriends(View view){
        searchableUserName = (EditText) findViewById(R.id.searchEditText);
        String searchableUserNameString = searchableUserName.toString();

        ElasticSearchController.GetUserByName getUserByName = new ElasticSearchController.GetUserByName();
        getUserByName.execute(searchableUserNameString);
        locatedUser =
    }

    public void followUser(View view){

        //check if request is already pending
        if (!controller.getCurrentUser().getMyFollowRequests().contains(locatedUser.getName())){

            //check if the located user is already being followed
            if (!controller.getCurrentUser().getMyFollowingList().contains(locatedUser.getName())){
                controller.requestToFollow(controller.getCurrentUser(), locatedUser.getName());
            } else {

            }

        } else {

        }


        //upload current user and located user to elasticSearch
        saveInFile();

    }

    //save the data controller. This function is never called in here for the time being
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

    //load the data controller. called at the start of the activity. All data is stored in the controller.
    private void loadFromFile() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            Gson gson = new Gson();

            Type objectType = new TypeToken<dataControler>() {
            }.getType();
            controller = gson.fromJson(in, objectType);
        } catch (FileNotFoundException e) {
            User firstUser = new User("admin", "admin");
            controller = new dataControler(firstUser, this);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
