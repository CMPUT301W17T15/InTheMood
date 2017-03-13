package com.example.austin.inthemood;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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


/**
 * A login screen that offers login via username/password.
 */
public class ExistingUserLogin extends AppCompatActivity{
    /**
     *  To pass in a message to the next activity
     */
    public static final String EXTRA_MESSAGE = "com.example.inthemood.MESSAGE";
    private static final String FILENAME = "file.sav";
    public dataControler controller;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;

    private TextView eL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_user_login);

        eL = (TextView) findViewById(R.id.eLogin);
        eL.setVisibility(View.GONE);

        // Initialize the data controller.
        loadFromFile();


        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.user);
        mPasswordView = (EditText) findViewById(R.id.password);

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

            Type objectType = new TypeToken<dataControler>() {}.getType();
            controller = gson.fromJson(in, objectType);
        } catch (FileNotFoundException e) {
            User firstUser = new User("admin", "admin");
            controller = new dataControler(firstUser);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

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

    /**
     * Called when the log in button is clicked.
     *
     * @param view
     */
    public void login(View view) {
        String name = mUserView.getText().toString();
        String pass = mPasswordView.getText().toString();
        User userLogin = controller.verifyLogIn(name, pass);
        if (userLogin != null) {
            Intent intent = new Intent(this, MainUser.class);
            //EditText editText = (EditText) findViewById(R.id.user);
            //String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            controller.setCurrentUser(userLogin);
            saveInFile();

            startActivity(intent);
        } else {
            eL.setVisibility(View.VISIBLE);
        }
    }

    public void register(View view) {
        Intent intent = new Intent(this, NewUserLogin.class);
        saveInFile();
        startActivity(intent);
    }

}

