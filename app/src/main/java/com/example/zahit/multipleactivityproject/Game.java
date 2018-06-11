package com.example.zahit.multipleactivityproject;

import android.provider.ContactsContract;

import com.example.zahit.multipleactivityproject.Activities.GameActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Zahit on 12-May-18.
 */

public class Game {
    public final int MAX_POINT = 200;
    public String lobbyKey;
    public ArrayList<Player> players;
    public Player currentlyDrawingPlayer;
    public String currentWord;
    public int amountOfCorrectGuessesThisTurn;
    public int indexOfCurrentDrawer;
    public int numberOfTurns;
    public boolean gameFinished;

    public Game(){

    }
    public Game(String lobbyKey){
        this.lobbyKey = lobbyKey;
        players = new ArrayList<>();
        indexOfCurrentDrawer = 0;
    }


    public void initGame(ArrayList<String> usernames, String firstWord){
        for(int i = 0;i<usernames.size();i++){
            Player player = new Player(usernames.get(i));
            players.add(player);
        }

        if(players.size() == 0)
            return;

        currentWord = firstWord;
        currentlyDrawingPlayer = players.get(0);
        indexOfCurrentDrawer = 0;
        numberOfTurns = 3;

    }

    public void nextTurn(String newWord){
        if(players.size() == 0) return;
        indexOfCurrentDrawer = (indexOfCurrentDrawer + 1) % players.size();
        currentlyDrawingPlayer = players.get(indexOfCurrentDrawer);
        currentWord = newWord;
        amountOfCorrectGuessesThisTurn = 0;
        for(Player p : players){
            p.reset();
        }

        if(indexOfCurrentDrawer == 0) numberOfTurns--;
        if(numberOfTurns == 0)
            gameFinished = true;
    }

    public void playerGuess(String playerName, String playerLanguage, String guess, final GameActivity gameActivity){
        final Player p = playerByUsername(playerName);
        final Player currentlyDrawingPlayer = getCurrentlyDrawingPlayer();


        final String guessFinal = guess;
        if(playerName.equals(currentlyDrawingPlayer.username) || p == null){
            return;
        }

        if(!p.isAlreadyGuessed()){
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference().child("words/" + playerLanguage + "/" + currentWord + "/");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String word = dataSnapshot.getValue(String.class);
                    if(guessFinal.equals(word)){
                        int pointsGained = MAX_POINT / (int) (Math.pow(2,amountOfCorrectGuessesThisTurn));
                        p.addPoints(pointsGained);
                        currentlyDrawingPlayer.addPoints(pointsGained/2);
                        p.setAlreadyGuessed(true);
                        amountOfCorrectGuessesThisTurn++;
                        gameActivity.updateGame();


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

    }

    private Player playerByUsername(String playerName) {
        for(Player player : players){
            if(player.username.equals(playerName)) return player;
        }

        return null;
    }

    private Player getCurrentlyDrawingPlayer() {
        for(Player player : players){
            if(player.username.equals(currentlyDrawingPlayer.username)) return player;
        }

        return null;
    }

    public void leaveGame(String playerName){
        for(int i = 0;i<players.size();i++){
            if(players.get(i).username.equals(playerName)){
                players.remove(i);
                break;
            }
        }

        if(currentlyDrawingPlayer.username.equals(playerName)){
            nextTurn(WordReferences.generateRandomWordKey());
        }
    }

    @Override
    public String toString(){
        String str = "";
        str+="Current Word: " + currentWord + "\n";
        str+="Current Painter: " + currentlyDrawingPlayer.username + "\n";
        for(Player p : players){
            str+=p.toString() + "\n";
        }

        return str;


    }

}
