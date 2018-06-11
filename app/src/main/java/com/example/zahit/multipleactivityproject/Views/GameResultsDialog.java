package com.example.zahit.multipleactivityproject.Views;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.Activities.GameActivity;
import com.example.zahit.multipleactivityproject.R;

import java.util.ArrayList;

/**
 * Created by Zahit on 11-Jun-18.
 */

public class GameResultsDialog  extends Dialog implements View.OnClickListener {
    public Activity activity;
    public Button backButton;
    private ArrayList<String> scores;

    public GameResultsDialog(Activity activity, ArrayList<String> scores) {
        super(activity);
        this.activity = activity;
        this.scores = scores;
    }

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        //Dialog like properties set.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game_results_dialog);
        setCancelable(false);
        //Relevant buttons and text views with their listeners.
        backButton = (Button) findViewById(R.id.postGameFinishButton);
        backButton.setOnClickListener(this);

        for(int i = 0;i<scores.size();i++){
            TextView textView = (TextView) findViewById(activity.getResources().getIdentifier("leaderboard" + (i+1),"id",activity.getPackageName()));
            textView.setText(scores.get(i));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.postGameFinishButton) {
            //dismisses the dialog (making it disappear)
            dismiss();
            ((GameActivity) activity).returnToLobbyList();
        }
    }

}

