package com.example.zahit.multipleactivityproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.Game;
import com.example.zahit.multipleactivityproject.Player;
import com.example.zahit.multipleactivityproject.R;
import com.example.zahit.multipleactivityproject.Views.GameMenuDialog;
import com.example.zahit.multipleactivityproject.Views.GameResultsDialog;
import com.example.zahit.multipleactivityproject.Views.PaintView;
import com.example.zahit.multipleactivityproject.WordReferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private final int LENGTH_OF_EACH_TURN = 60000;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;

    private GameMenuDialog dialog;

    private TextView remainingTimeTextView;
    private TextView remainingTurnTextView;
    private EditText guessEditText;

    private FirebaseUser user;

    private Game game;
    private String username;
    private String lobbyKey;
    private String gameKey;
    private String currentWord;
    private String languageCode = "";

    private PaintView myPaintView;
    private CountDownTimer remainingTimeCounter;

    DatabaseReference languageRef;
    ValueEventListener languageListener;
    DatabaseReference gameRef;
    ValueEventListener gameListener;
    DatabaseReference wordRef;
    ValueEventListener wordListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        remainingTimeTextView = findViewById(R.id.remainingTimeTextView);
        remainingTurnTextView = findViewById(R.id.remainingTurnTextView);
        myPaintView = findViewById(R.id.paintView);
        guessEditText = (EditText) findViewById(R.id.gameGuessEditText);
        dialog = new GameMenuDialog(GameActivity.this);

        lobbyKey = getIntent().getStringExtra("LobbyKey");
        gameKey = getIntent().getStringExtra("GameKey");
        username = getIntent().getStringExtra("Username");

        myPaintView.gameKey = gameKey;
        //Sensors being initialized, light and accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }


        //Location manager for gps location.
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Time counter to skip turns.
        remainingTimeCounter = new CountDownTimer(LENGTH_OF_EACH_TURN,1000) {
            @Override
            public void onTick(long l) {
                remainingTimeTextView.setText("" + l / 1000);
            }

            @Override
            public void onFinish() {
                //host decides when to terminate
                if(game.currentlyDrawingPlayer.username.equals(username)){
                    nextTurn();
                    myPaintView.clearDrawing();
                }
            }
        };
        remainingTimeCounter.start();
        addDatabaseListeners();


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        remainingTimeCounter.cancel();
        sensorManager.unregisterListener(this,accelerometer);
        languageRef.removeEventListener(languageListener);
        gameRef.removeEventListener(gameListener);
        wordRef.removeEventListener(wordListener);

    }


    public void onClick(View view) {
        if (view.getId() == R.id.exitGameButton) {
            //Exit button to exit the game, modifies firebase database accordingly.
            leaveGame();
        }else if (view.getId() == R.id.clearDrawingButton) {
            if(game.currentlyDrawingPlayer.username.equals(username)){
                myPaintView.clearDrawingForAll();
            }
        }else if (view.getId() == R.id.gameGuessButton){
            String guess = guessEditText.getText().toString();
            game.playerGuess(username,languageCode,guess.toLowerCase(),this);
        }else if(view.getId() == R.id.skipTurnButton){
            if(game.currentlyDrawingPlayer.username.equals(username)){
                nextTurn();
                myPaintView.clearDrawing();
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Accelerometer math to detect shake.

            long currentTimeMillis = System.currentTimeMillis();
            //If a second has passed between the last successful shake
            if ((currentTimeMillis - lastShakeTime) > 1000) {

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                //Based on the magnitude of the shake, decides if the user actually wanted shake to happen.
                double power = Math.sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH;

                if (power > 3) {
                    lastShakeTime = currentTimeMillis;

                    if (!dialog.isShowing())
                        dialog.show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void addDatabaseListeners() {

        //A lot of database listeners that are nested because they need to wait for previous
        //values to continue.

        final FirebaseDatabase db = FirebaseDatabase.getInstance();

        myPaintView.ref = db.getReference().child("games/" + gameKey + "/drawing/");
        myPaintView.addDatabaseListeners();

        languageRef = db.getReference().child("users/" + user.getUid() + "/language");
        languageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Should really be called only once!");


                languageCode = (String) dataSnapshot.getValue();
                gameRef = db.getReference().child("games/" + gameKey + "/game/");
                System.out.println("GAME KEY : " + gameKey);
                gameListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //game has been removed, return to avoid null exceptions
                        System.out.println("Game data changed!");
                        System.out.println("children count: " + dataSnapshot.getChildrenCount());
                        //reset timer if game's drawer value changes
                        String previousDrawer = "";
                        String newDrawer = "";

                        if(game != null){
                            previousDrawer = game.currentlyDrawingPlayer.username;
                        }

                        if(!dataSnapshot.exists()){
                            System.out.println("game over");
                            GameResultsDialog resultsDialog = new GameResultsDialog(GameActivity.this,calculateScores());
                            resultsDialog.show();
                            return;
                        }
                        game = dataSnapshot.getValue(Game.class);

                        if(game != null){
                            newDrawer = game.currentlyDrawingPlayer.username;
                        }

                        if(!previousDrawer.equals(newDrawer) && !previousDrawer.equals("") && !newDrawer.equals("")){
                            remainingTimeCounter.cancel();
                            remainingTimeCounter.start();
                            myPaintView.clearDrawing();
                        }

                        if(newDrawer.equals(username)){
                            myPaintView.allowDraw = true;
                        }else{
                            myPaintView.allowDraw = false;
                        }


                        if(game.players.size() != 1 && game.amountOfCorrectGuessesThisTurn == game.players.size() - 1 &&
                                game.currentlyDrawingPlayer.username.equals(username)){
                            nextTurn();
                            myPaintView.clearDrawing();
                        }

                        wordRef = db.getReference().child("words/" + languageCode + "/" + game.currentWord);
                        wordListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    return;
                                }

                                currentWord = (String) dataSnapshot.getValue();
                                updateUI();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        wordRef.addValueEventListener(wordListener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };

                gameRef.addValueEventListener(gameListener);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        languageRef.addValueEventListener(languageListener);


    }

    private ArrayList<String> calculateScores() {
        ArrayList<String> scores = new ArrayList<String>();
        ArrayList<Player> players = game.players;
        Collections.sort(players);

        for(int i = 0;i<players.size();i++){
            scores.add(players.get(i).username + ":" + players.get(i).points);
        }

        return scores;
    }

    public void updateGame(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Map<String,Object> updates = new HashMap<>();
        updates.put("/games/" + gameKey + "/game/",game);
        ref.updateChildren(updates);
    }

    private void updateUI(){
        if(game == null || game.players == null)
            return;
        //Update UI whenever the game values are changed.
        for(int i = 0; i<4;i++){
            TextView textView = (TextView) findViewById(getResources().getIdentifier("playerTextView" + (i+1),"id",getPackageName()));
            textView.setText("------");
        }

        for(int i = 0 ; i <game.players.size(); i++){
            TextView textView = (TextView) findViewById(getResources().getIdentifier("playerTextView" + (i+1),"id",getPackageName()));
            textView.setText(game.players.get(i).username + ":" + game.players.get(i).points);
            if(game.currentlyDrawingPlayer.username.equals(game.players.get(i).username)){
                //BLACK COLOR FOR PLAYER DRAWING
                textView.setTextColor(Color.parseColor("#FFFFFF"));
            }else{
                if(game.players.get(i).alreadyGuessed){
                    //GREEN COLOR FOR PLAYER WHO GUESSED CORRECTLY
                    textView.setTextColor(Color.parseColor("#00FF00"));
                }else{
                    //RED COLOR FOR PLAYER STILL GUESSING
                    textView.setTextColor(Color.parseColor("#FF0000"));
                }
            }
        }

        TextView wordText = (TextView) findViewById(R.id.currentWordTextView);

        if(game.currentlyDrawingPlayer.username.equals(username)){
            wordText.setText(currentWord);
        }else{
            wordText.setText("You Are Guessing!");
        }

        remainingTurnTextView.setText(game.numberOfTurns + "/3");


    }

    private void nextTurn(){

        game.nextTurn(WordReferences.generateRandomWordKey());
        if(game.gameFinished){
            //Game over, remove database values.
            DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("games/" + gameKey);
            DatabaseReference lobbyRef = FirebaseDatabase.getInstance().getReference("lobbies/" + lobbyKey);
            gameRef.removeValue();
            lobbyRef.removeValue();
            return;
        }
        updateGame();
        remainingTimeCounter.cancel();
        remainingTimeCounter.start();
    }

    private void leaveGame(){
        game.leaveGame(username);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("users/" + user.getUid() + "/currentlobby", "none");
        updates.put("games/" + gameKey + "/game", game);
        ref.updateChildren(updates);

        if(game.players.size() == 0){
            destroyGameAndLobby();
        }

        returnToLobbyList();
    }

    private void destroyGameAndLobby(){
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("games/" + gameKey);
        gameRef.removeValue();
        DatabaseReference lobbyRef = FirebaseDatabase.getInstance().getReference("lobbies/" + lobbyKey);
        lobbyRef.removeValue();

    }

    public void returnToLobbyList(){
        Intent intent = new Intent(this,LobbyListActivity.class);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {
        //NOTHING
    }



}
