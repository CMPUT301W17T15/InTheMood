package com.example.austin.inthemood;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by olivier on 2017-03-10.
 */

public class dataControler {
    private ArrayList<User> userList = new ArrayList<>();
    private int userCount;
    private String currentUserName;
    Context context;

    /**
     * Instantiates a new dataControler. This should be called once. after the first user in the
     * system has been registered.
     *
     * @param firstUser first registered user in our database
     * @param myContext
     */
    public dataControler(User firstUser, Context myContext){
        userCount = 1;
        this.userList = new ArrayList<User>();
        this.userList.add(firstUser);
        this.context = myContext;
    }

    /**
     * Instantiates a new dataControler without context. (used for JUnit testing)
     *
     * @param firstUser first registered user in our database
     */
    public dataControler(User firstUser){
        userCount = 1;
        this.userList = new ArrayList<User>();
        this.userList.add(firstUser);
    }

    /**
     * set the current user interacting with the system in the data controler
     *
     * @param currentUser user interacting with the system
     */
    public void setCurrentUser(User currentUser){
        this.currentUserName = currentUser.getName();
    }

    /**
     * get current user interacting with the app
     *
     * @return user interacting with app
     */
    public User getCurrentUser() {

        return searchForUserByName(currentUserName);
    }

    /**
     * gets the list of registered users in the local database
     *
     * @return list of users
     */
    public ArrayList<User> getUserList() {
        return userList;
    }

    /**
     * update user in userList
     *
     * @param user the updated user replacing old user
     */
    public void updateUserList(User user) {
        for (int i = 0; i < userList.size(); i++){
            if (userList.get(i).getName() == user.getName()){
                userList.set(i, user);
            }
        }
    }

    /**
     * adds user to list of registered users (userList)
     * @param user user being added
     */
    public void addToUserList(User user){
        userList.add(user);
        userCount += 1;
    }

    /**
     * checks to see if a user is registered (is in userList) and his corresponding password matches database
     *
     * @param name name being checked for in database
     * @param password corresponding password being checked for in database
     * @return User if login successful or null if unsuccessful
     */
    public User verifyLogIn(String name, String password){
        for (int i = 0; i < userList.size(); i++){
            if (userList.get(i).getName().equals(name)) {
                if (userList.get(i).getPassword().equals(password)) {
                    return userList.get(i);

                }
            }
        }
        return null;
    }

    /**
     * send follow request. followee added to user's (Owner's) followRequests and user added to followee's followerRequests.
     *
     * @param user user wishing to follow followee
     * @param followee person be asked to be followed
     */
    public void requestToFollow(User user, String followee){
        user.addToMyFollowRequests(followee);
        searchForUserByName(followee).addToMyFollowerRequests(user.getName());
    }

    /**
     * grant user (followerName) follow permission to follow user (owner)
     *
     * @param user user being requested to follow by followerName
     * @param followerName user requesting to follow user (owner)
     */
    public void grantFollowPermission(User user, String followerName){
        user.addToMyFollowersList(followerName);
        user.removeFollowerRequest(followerName);
        searchForUserByName(followerName).removeFollowRequest(user.getName());
        searchForUserByName(followerName).addToMyFollowingList(user.getName());
    }

    /**
     * deny user (followerName) requesting follow permission to follow user (owner)
     *
     * @param user user being requested to follow by followerName
     * @param followerName user requesting to follow user (owner)
     */
    public void denyFollowPermission(User user, String followerName){
        user.removeFollowerRequest(followerName);
        searchForUserByName(followerName).removeFollowRequest(user.getName());
    }

    /**
     * search userList for a user by name, return null if not found
     *
     * @param name of user being searched for
     * @return User with name name, return null if user not in userList
     */
    public User searchForUserByName(String name) {
        for (int i = 0; i < userList.size(); i++){
            if (userList.get(i).getName().equals(name)){
                return userList.get(i);
            }
        }
        return null;
    }

    /**
     * Sort moods in moodsList by date (oldest to newest)
     *
     * pulled from http://stackoverflow.com/questions/36727700/sort-a-list-of-objects-by-date on March 11th, 2017
     *
     * @param moodsList list of moods to sort
     * @return sorted list of moods
     */
    public ArrayList<Mood> sortMoodsByDate(ArrayList<Mood> moodsList){
        Collections.sort(moodsList, new Comparator<Mood>() {
            @Override
            public int compare(Mood mood1, Mood mood2) {
                return mood1.getMoodDate().compareTo(mood2.getMoodDate());
            }
        });
        return moodsList;
    }

    /**
     * searches for moods with String moodName as a moodName and adds them to the filteredMoodList being returned
     *
     * @param moodName the name of mood being searched for
     * @param moodList the list of moods being searched
     * @return the list of moods filtered by particular mood. this returned list is also sorted by date
     */
    public ArrayList<Mood> filterByMood(String moodName, ArrayList<Mood> moodList){
        ArrayList <Mood> filteredMoodList = new ArrayList<Mood>();
        for (int i = 0; i < moodList.size(); i++){
            if (moodList.get(i).getMoodName().equals(moodName)){
                filteredMoodList.add(moodList.get(i));
            }
        }
        return sortMoodsByDate(filteredMoodList);
    }

    /**
     * checks each mood to see if it was recorded in the last week. If so, it is added to the filteredMoodList being returned which is sorted by date
     * pulled from http://stackoverflow.com/questions/883060/how-can-i-determine-if-a-date-is-between-two-dates-in-java on March 11, 2017
     *
     * @param moodList list of moods to be checked if the moods were recorded in the last week
     * @return a list of moods recorded in the last week. this list is also sorted by date
     */
    public ArrayList<Mood> filterByWeek(ArrayList<Mood> moodList){
        ArrayList <Mood> filteredMoodList = new ArrayList<Mood>();

        Calendar cal = Calendar.getInstance();
        Date currentDate = new Date();
        cal.add(Calendar.DATE, -7);
        Date startWeekDate = cal.getTime();

        for (int i = 0; i < moodList.size(); i++){
            if (startWeekDate.compareTo(moodList.get(i).getMoodDate()) * moodList.get(i).getMoodDate().compareTo(currentDate) >= 0){
                filteredMoodList.add(moodList.get(i));
            }
        }
        return sortMoodsByDate(filteredMoodList);
    }

    /**
     * returns a list of moods from moodList that contains the string moodtrigger in the moodDescription
     *
     * @param moodTrigger the mood trigger description being searched for
     * @param moodList the list of moods that we are searching
     * @return a list of moods containing moodTrigger in the moodDescription. This list is sorted by date
     */
    public ArrayList<Mood> filterByTrigger(String moodTrigger, ArrayList<Mood> moodList){
        ArrayList <Mood> filteredMoodList = new ArrayList<Mood>();
        for (int i = 0; i < moodList.size(); i++){
            if (moodList.get(i).getMoodDescription().contains(moodTrigger)){
                filteredMoodList.add(moodList.get(i));
            }
        }
        return sortMoodsByDate(filteredMoodList);
    }

    /**
     * This method checks wether device is online or not
     *
     * pulled from http://stackoverflow.com/questions/30343011/how-to-check-if-an-android-device-is-online on March 27, 2017
     *
     * @return online a boolean indicating whether device is online or not
     */

    public boolean isOnline(){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean online = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            online = true;
        }
        return online;
    }

    /**
     * gets a user from elasticSearch using username
     *
     * @param username string username of user being looked for in elasticSearch
     * @return User being looked for if found or null if found
     */
    public User getElasticSearchUser(String username) {
        ElasticSearchController.GetUserByName getUser = new ElasticSearchController.GetUserByName();
        getUser.execute(username);
        User locatedUser = null;
        try {
            locatedUser = getUser.get();
            assert (true);
        } catch (Exception e) {
            Log.i("Error", "Failed to get user by name");
            return null;
        }
        return locatedUser;
    }
}
