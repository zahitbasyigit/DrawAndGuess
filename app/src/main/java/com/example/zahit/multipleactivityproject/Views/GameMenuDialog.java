package com.example.zahit.multipleactivityproject.Views;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.Activities.GameActivity;
import com.example.zahit.multipleactivityproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Zahit on 29-Apr-18.
 */

public class GameMenuDialog extends Dialog implements View.OnClickListener {
    public Activity activity;
    public Button backButton;



    public GameMenuDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        //Dialog like properties set.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gamemenu);
        //Relevant buttons and text views with their listeners.
        backButton = (Button) findViewById(R.id.dialogBackButton);
        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.dialogBackButton){
            //dismisses the dialog (making it disappear)
            dismiss();
        }

    }



}
