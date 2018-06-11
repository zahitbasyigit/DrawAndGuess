package com.example.zahit.multipleactivityproject.Activities;

import android.content.Intent;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.Game;
import com.example.zahit.multipleactivityproject.Lobby;
import com.example.zahit.multipleactivityproject.R;
import com.example.zahit.multipleactivityproject.WordReferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LobbyActivity extends AppCompatActivity {

    private FirebaseAuth authentication;
    private FirebaseUser user;

    private TextView lobbyNameTextView;
    private TextView joinedPlayerCountTextView;

    private String lobbyKey;
    private String username;
    private String hostUID;

    private Lobby lobby;

    DatabaseReference lobbyRef;
    ValueEventListener lobbyListener;
    DatabaseReference singleLobbyRef;
    ValueEventListener singleLobbyListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currentlobby);

        //Firebase connection
        authentication = FirebaseAuth.getInstance();
        user = authentication.getCurrentUser();

         //Views loaded
        lobbyNameTextView = (TextView) findViewById(R.id.lobbyName);
        joinedPlayerCountTextView = (TextView) findViewById(R.id.joinedPlayerCount);


        //Some information from the previous intent loaded, such as the key for lobby, which
        //is necessary to update the firebase.
        Intent intent = getIntent();
        String lobbyKey = intent.getStringExtra("lobbyKey");
        username = intent.getStringExtra("Username");
        this.lobbyKey = lobbyKey;
        if(lobbyKey!= null && lobbyKey != ""){
            addDatabaseListeners(lobbyKey);
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        lobbyRef.removeEventListener(lobbyListener);
    }

    private void goBack(){
        finish();
    }

    public void onClick(View view){
        if(view.getId() == R.id.leaveButton){
            removePlayer(username);
            finish();
        }else if(view.getId() == R.id.startGameButton){
            //If game start is clicked
            //Some firebase node needs updating.
            if(lobby == null)
                return;

            if(lobby.getHostUID().equals(user.getUid()))
                startGame();
        }

    }
    private void destroyLobbyIfHost(){

        //This method takes the current users name, lobbies hosts name, and checks if they match
        //If they do, it removes the lobby node.
        singleLobbyRef = FirebaseDatabase.getInstance().getReference().child("lobbies/" + lobbyKey);
        singleLobbyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hostUID = (String) dataSnapshot.child("hostUID").getValue();
                if(hostUID.equals(user.getUid()))
                    singleLobbyRef.removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        singleLobbyRef.addListenerForSingleValueEvent(singleLobbyListener);
    }

    private void startGameActivity(String gameKey){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users/" + user.getUid());
        Map<String,Object> updates = new HashMap<>();
        updates.put("currentlobby",lobbyKey);
        ref.updateChildren(updates);

        Intent intent = new Intent(this,GameActivity.class);
        intent.putExtra("LobbyKey",lobbyKey);
        intent.putExtra("GameKey",gameKey);
        intent.putExtra("Username" ,username);
        startActivity(intent);
    }
    private void addDatabaseListeners(final String lobbyKey){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        lobbyRef = db.getReference().child("lobbies/" + lobbyKey);

        lobbyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    goBack();
                    return;
                }

                //Handles game being started for other players (nonhost)
                Object gameKey = dataSnapshot.child("gameKey").getValue();
                if(gameKey != null && gameKey instanceof  String){
                    startGameActivity((String)gameKey);
                    return;
                }

                //Handles all the updates for when the snapshot changes.
                //Also updates text views.
                lobby = dataSnapshot.getValue(Lobby.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //Firebase listeners.
        lobbyRef.addValueEventListener(lobbyListener);

    }

    private void  updateUI(){
        lobbyNameTextView.setText(lobby.getLobbyName());
        int curNumberOfPlayers = lobby.getCurrentNumberOfPlayers();
        int maxNumberOfPlayers = lobby.getMaximumNumberOfPlayers();
        joinedPlayerCountTextView.setText(curNumberOfPlayers + "/" + maxNumberOfPlayers);

        ArrayList<String> playerNames = lobby.getPlayerNames();
        //Textviews for empty seats and full seats.
        for(int i = 0 ; i < playerNames.size(); i++){
            TextView textView = (TextView) findViewById(getResources().getIdentifier("currentPlayer" + (i+1),"id",getPackageName()));
            textView.setText(playerNames.get(i));
        }

        for(int i = playerNames.size(); i < maxNumberOfPlayers;i++){
            TextView textView = (TextView) findViewById(getResources().getIdentifier("currentPlayer" + (i+1),"id",getPackageName()));
            textView.setText("------");
        }
    }

    private void startGame(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference lobbyRef = FirebaseDatabase.getInstance().getReference().child("lobbies/" + lobbyKey);
        String key = ref.child("games").push().getKey();

        Map<String,Object> updates = new HashMap<>();
        Game game = new Game(lobbyKey);
        game.initGame(lobby.getPlayerNames(), WordReferences.generateRandomWordKey());
        updates.put("/games/" + key + "/game/",game);
        updates.put("/lobbies/" + lobbyKey + "/gameKey",key);
        ref.updateChildren(updates);
    }

    private void removePlayer(String username){
        //After a player leaves the lobby, the firebase database needs to update itself.
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("lobbies/");
        Map<String,Object> updates = new HashMap<>();
        lobby.removePlayer(username);
        updates.put(lobbyKey,lobby);

        //If the host is leaving the lobby, then the firebase node must be removed.
        destroyLobbyIfHost();
        ref.updateChildren(updates);
    }

    @Override
    public void onBackPressed(){
        //NOTHING
    }

}
