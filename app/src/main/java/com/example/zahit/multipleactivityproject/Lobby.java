package com.example.zahit.multipleactivityproject;

import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

/**
 * Created by Zahit on 18-Mar-18.
 */

public class Lobby {
    private String lobbyName;
    private int currentNumberOfPlayers;
    private int maximumNumberOfPlayers;
    private ArrayList<String> playerNames;
    private String hostUID;

    @Exclude
    private TableRow tableRow;
    @Exclude
    private TextView textView;

    public Lobby(){
        this.playerNames = new ArrayList<String>();
    }

    public Lobby(String lobbyName, int maxNumberOfPlayers, String hostUID){
        this.lobbyName = lobbyName;
        this.currentNumberOfPlayers = 0;
        this.maximumNumberOfPlayers = maxNumberOfPlayers;
        this.hostUID = hostUID;
        this.playerNames = new ArrayList<String>();
    }

    public Lobby(String lobbyName, int currentNumberOfPlayers, int maximumNumberOfPlayers, TableRow tableRow, TextView textView){
        this.lobbyName = lobbyName;
        this.currentNumberOfPlayers = currentNumberOfPlayers;
        this.maximumNumberOfPlayers = maximumNumberOfPlayers;
        this.tableRow = tableRow;
        this.textView = textView;
        this.playerNames = new ArrayList<String>();
    }

    public String getLobbyName(){
        return lobbyName;
    }
    public int getMaximumNumberOfPlayers(){
        return maximumNumberOfPlayers;
    }
    public TableRow getTableRow(){ return tableRow; }
    public TextView getTextView(){ return textView; }
    public int getCurrentNumberOfPlayers(){ return currentNumberOfPlayers; }
    public ArrayList<String> getPlayerNames(){ return playerNames; }
    public String getHostUID(){ return hostUID; }

    public boolean addPlayer(String playerName) {
        if(currentNumberOfPlayers >= maximumNumberOfPlayers) return false;

        currentNumberOfPlayers++;
        playerNames.add(playerName);
        return true;
    }

    public void removePlayer(String playerName){
        currentNumberOfPlayers--;
        playerNames.remove(playerName);
    }
}
