package com.example.austin.inthemood;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

public class MyFriends extends AppCompatActivity {

    //UI Elements
    private ListView friendsListView;
    private Button findFriendsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        //Initialize UI Elements
        friendsListView = (ListView) findViewById(R.id.myFriendsListView);
        findFriendsButton = (Button) findViewById(R.id.findFriendsButton);

    }
}
