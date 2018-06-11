package com.example.zahit.multipleactivityproject.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.Lobby;
import com.example.zahit.multipleactivityproject.R;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LobbyListActivity extends AppCompatActivity {
    static final int CREATE_LOBBY = 1;

    private HashMap<String,Lobby> lobbies;
    private TableRow selectedLobbyRow;
    private String selectedLobbyKey;
    private String username;

    private FirebaseAuth authentication;
    private FirebaseUser user;

    private boolean lobbyRowEvenColor = true;
    private int previousColor;
    private int curNumber = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Firebase connection
        authentication = FirebaseAuth.getInstance();
        user = authentication.getCurrentUser();

        //Lobby related variables, they are kept for click listener.
        //The hashmap is based on the key that is provided by firebase upon push() method
        //The key returns a lobby, which includes useful fields such as the relevant
        //TableRow, TextView of the TableRow etc.
        lobbies = new HashMap<>();
        selectedLobbyKey = "";
        selectedLobbyRow = null;
        addDatabaseListeners();

    }

    public void onClick(View view) {
        if (view.getId() == R.id.newLobbyButton) {
            //User intents to create a new lobby, however this new activity
            //Needs to know the current lobby names to keep the lobby unique.
            //startActivityForResult is called since the lobby creation may or may not fail.

            Intent intent = new Intent(this, NewLobbyActivity.class);
            intent.putExtra("Username", username);
            startActivityForResult(intent, CREATE_LOBBY);

        }else if(view.getId() == R.id.logoutButton){
            //User logs out.
            authentication.signOut();
            Intent intent = new Intent(this,MainActivity.class);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }else if(view.getId() == R.id.joinButton){
            //User joins a lobby if he can
            if(selectedLobbyKey != ""){
                Lobby lobby = lobbies.get(selectedLobbyKey);
                if(lobby == null)
                    return;

                if(lobby.getCurrentNumberOfPlayers() >= lobby.getMaximumNumberOfPlayers()){
                    return;
                }
                joinGame(selectedLobbyKey);
            }
        }else if(view.getId() == R.id.profileButton){
            //Profile activity started
            Intent intent = new Intent(this,ProfileActivity.class);
            intent.putExtra("Username" , username);
            startActivity(intent);

        }
    }

    private void joinGame(final String key){

        //Since the user is joining the lobby, some necessary fields in the firebase must be updated
        //Such as a new username for the players and an incremented current number of players.
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference lobbyReference = FirebaseDatabase.getInstance().getReference().child("lobbies/" + key);
        lobbyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Lobby lobby = dataSnapshot.getValue(Lobby.class);
                System.out.println(username);
                if(lobby.addPlayer(username)){
                    Map<String,Object> updates = new HashMap<>();
                    updates.put("/lobbies/" + key,lobby);
                    ref.updateChildren(updates);
                    startJoinGameActivity(key);
                }else{
                    //TODO POPUP FAIL ALERT.
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startJoinGameActivity(String key){
        Intent intent = new Intent(this,LobbyActivity.class);
        intent.putExtra("lobbyKey" , key);
        intent.putExtra("Username",username);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Upon returning from create lobby activity, the user either successfully created
        //or failed to create the lobby.

        if (requestCode == CREATE_LOBBY) {
            if(resultCode == RESULT_OK){
                //If successful, then the lobby needs to be added to the firebase

                String lobbyName = data.getStringExtra("NewLobbyName");
                int lobbyNumberPlayers = data.getIntExtra("NewLobbyPlayers",2);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                String key = ref.child("lobbies").push().getKey();
                Map<String,Object> updates = new HashMap<>();
                Lobby lobby = new Lobby(lobbyName, lobbyNumberPlayers,user.getUid());
                updates.put("/lobbies/" + key,lobby);
                ref.updateChildren(updates);
                joinGame(key);
            }

            //Nothing to do if it failed.
        }
    }

    private void createTableRow(String lobbyName, int currentNumberOfPlayers, int maxNumberOfPlayers, final String lobbyKey){
        //The following code creates a new table row to add to the lobbies table
        //The table is divided into 2 pieces.
        //One for lobby name and one for lobby number of players.
        //Weights are different for better visuals.

        TextView lobbyNameTextView = new TextView(this);
        lobbyNameTextView.setText(lobbyName);

        TextView lobbyPlayersTextView = new TextView(this);
        lobbyPlayersTextView.setText(Integer.toString(currentNumberOfPlayers) + "/" + Integer.toString(maxNumberOfPlayers));

        TableRow.LayoutParams highWeightParameters = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT,4);
        TableRow.LayoutParams lowWeightParameters = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT,2);
        lobbyNameTextView.setTextSize(18);
        lobbyPlayersTextView.setTextSize(18);
        highWeightParameters.setMargins(4,0,0,0);
        lowWeightParameters.setMargins(4,0,0,0);

        lobbyNameTextView.setLayoutParams(highWeightParameters);
        lobbyNameTextView.setTextColor(Color.BLACK);
        lobbyPlayersTextView.setLayoutParams(lowWeightParameters);
        lobbyPlayersTextView.setTextColor(Color.BLACK);

        final TableRow newRow = new TableRow(this);
        //UNIQUE ID GENERATOR.
        newRow.setId(0);
        newRow.addView(lobbyNameTextView);
        newRow.addView(lobbyPlayersTextView);

        if(lobbyRowEvenColor){
            newRow.setBackgroundColor(0xFFFFFFFF);
        }else {
            newRow.setBackgroundColor(0xFFBABEC1);
        }

        final boolean localLobbyRowColor = lobbyRowEvenColor;

        //This table row needs to have a listener for when it is clicked, the player should be able to use the join button to join this clicked row.
        newRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedLobbyRow != null)
                    selectedLobbyRow.setBackgroundColor(previousColor);

                selectedLobbyRow = newRow;
                selectedLobbyKey = lobbyKey;
                if(localLobbyRowColor){
                    previousColor = 0xFFFFFFFF;
                }else {
                    previousColor = 0xFFBABEC1;
                }
                newRow.setBackgroundColor(0xFF6B56AA);

            }
        });



        //Table generated with 2 colors for better visual.
        lobbyRowEvenColor = !lobbyRowEvenColor;

        TableLayout lobbyTable = (TableLayout) findViewById(R.id.lobbyTableLayout);

        lobbyTable.addView(newRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        lobbies.put(lobbyKey,new Lobby(lobbyName, currentNumberOfPlayers, maxNumberOfPlayers,newRow,lobbyPlayersTextView));

    }

    private void removeLobby(String key){

        //Removing a row from the table requires some color fixing as well as
        //removing it from the table.

        if(lobbies.get(key) == null) return;

        TableLayout lobbyTable = (TableLayout) findViewById(R.id.lobbyTableLayout);
        TableRow lobbyRow = lobbies.get(key).getTableRow();
        if(lobbyRow == null) return;

        if(selectedLobbyRow == lobbyRow){
            selectedLobbyRow = null;
            selectedLobbyKey = "";
        }

        lobbyTable.removeView(lobbyRow);
        for(int i = 0 ;i<lobbyTable.getChildCount();i++){
            View view = lobbyTable.getChildAt(i);
            if(view instanceof TableRow){
                TableRow row = (TableRow) view;
                if(row == selectedLobbyRow){
                    continue;
                }
                if(i%2 == 1){
                    row.setBackgroundColor(0xFFBABEC1);
                }else {
                    row.setBackgroundColor(0xFFFFFFFF);
                }

            }
        }

    }

    private void modifyTableRow(String key, int currentNumberOfPlayers, int maximumNumberOfPlayers){
        //After a player joins a lobby, its value is updated, this method provides that.
        lobbies.get(key).getTextView().setText(currentNumberOfPlayers + "/" + maximumNumberOfPlayers);
    }
    private void addDatabaseListeners(){

        //Database listeners for when lobbies get created, removed, updated.
        //As well as single listener for username
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usernameRef = db.getReference().child("users/" + user.getUid() + "/username");

        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = (String) dataSnapshot.getValue();
                System.out.println(username);
                TextView textView = findViewById(R.id.usernameTextView);
                textView.setText(getResources().getString(R.string.welcome) + " " + username + "!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference lobbiesRef = db.getReference().child("lobbies");

        lobbiesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String lobbyName = (String) dataSnapshot.child("lobbyName").getValue();
                long currentNumberOfPlayers = (long) dataSnapshot.child("currentNumberOfPlayers").getValue();
                long maximumNumberOfPlayers =  (long) dataSnapshot.child("maximumNumberOfPlayers").getValue();
                String key = (String) dataSnapshot.getKey();
                createTableRow(lobbyName,(int) currentNumberOfPlayers, (int) maximumNumberOfPlayers, key);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = (String) dataSnapshot.getKey();
                int currentNumberOfPlayers = (int) ((long) dataSnapshot.child("currentNumberOfPlayers").getValue());
                int maximumNumberOfPlayers =  (int) ((long) dataSnapshot.child("maximumNumberOfPlayers").getValue());
                modifyTableRow(key,currentNumberOfPlayers,maximumNumberOfPlayers);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeLobby(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
    protected void onSaveInstanceState(Bundle bundle) {

        //On rotation, each lobby needs to be saved and since bundle put functions
        //do not work on custom java objects, relevant arrays are created and sent.
        super.onSaveInstanceState(bundle);

        //TODO

    }

    @Override
    public void onBackPressed(){
        //NOTHING
    }


}
