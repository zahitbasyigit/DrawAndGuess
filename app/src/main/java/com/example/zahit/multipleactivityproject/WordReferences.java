package com.example.zahit.multipleactivityproject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

/**
 * Created by Zahit on 29-Apr-18.
 */

public class WordReferences {
    private static int numberOfWords = 92;

    public static String generateRandomWordKey(){
        String key = "";
        Random rgen = new Random();
        int randomNumber = rgen.nextInt(numberOfWords - 1) + 1;
        int lengthOfRandomNumber = (int) (Math.log10(randomNumber) + 1);

        for(int i = 0; i<4-lengthOfRandomNumber;i++){
            key+="0";
        }

        key+=randomNumber;

        return key;

    }

    public static DatabaseReference getWordReference(String wordKey, String languageCode){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference().child("words/" + languageCode + "/" + wordKey);
        return ref;
    }



}
