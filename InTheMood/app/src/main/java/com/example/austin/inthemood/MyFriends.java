package com.example.austin.inthemood;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class MyFriends extends AppCompatActivity {

    private static final String FILENAME = "file.sav";
    public dataControler controller;
    private ListView myFriendsListView;
    private ArrayList<User> followingList;
    private ArrayList<String> followedUserStringMessage;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        loadFromFile();

        //test
        User admin = controller.searchForUserByName("admin");
        Mood mood1 = new Mood();
        mood1.setMoodName("Sadness");
        admin.addMood(mood1);
        controller.getCurrentUser().addToMyFollowingList(admin);

        //Print to list view. For each followed user, print his name and his most recent mood with mood date
        myFriendsListView = (ListView) findViewById(R.id.myFriendsListView);
        followingList = controller.getCurrentUser().getMyFollowingList();
        followedUserStringMessage = new ArrayList<String>();
        for (int i = 0; i < followingList.size(); i++){
            ArrayList<Mood> followedUserMoods = followingList.get(i).getMyMoodsList();
            if (followedUserMoods.size() > 0) {
                followedUserMoods = controller.sortMoodsByDate(followedUserMoods);
                String message = followingList.get(i).getName() + " felt " +
                        followedUserMoods.get(followedUserMoods.size() - 1).getMoodName() + " on " +
                        followedUserMoods.get(followedUserMoods.size() - 1).getMoodDate();
                followedUserStringMessage.add(message);
            } else {
                String message = followingList.get(i).getName();
                followedUserStringMessage.add(message);

            }

        }
        adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, followedUserStringMessage);
        myFriendsListView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        loadFromFile();
    }

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
            controller = new dataControler(firstUser);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }


}
