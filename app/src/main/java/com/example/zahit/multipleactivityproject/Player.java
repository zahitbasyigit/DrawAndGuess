package com.example.zahit.multipleactivityproject;

import android.support.annotation.NonNull;

/**
 * Created by Zahit on 12-May-18.
 */

public class Player implements Comparable<Player> {
    public int points;
    public String username;
    public boolean alreadyGuessed;


    public Player(){


    }

    public Player(String username){
        this.points = 0;
        this.username = username;
        this.setAlreadyGuessed(false);
    }

    public void reset(){
        setAlreadyGuessed(false);
    }

    public boolean isAlreadyGuessed() {
        return alreadyGuessed;
    }

    public void setAlreadyGuessed(boolean alreadyGuessed) {
        this.alreadyGuessed = alreadyGuessed;
    }

    public void addPoints(int amt){
        points+=amt;
    }


    @Override
    public String toString(){
        return username + ":" + points;
    }

    @Override
    public int compareTo(@NonNull Player player) {
        return player.points - this.points;
    }
}
