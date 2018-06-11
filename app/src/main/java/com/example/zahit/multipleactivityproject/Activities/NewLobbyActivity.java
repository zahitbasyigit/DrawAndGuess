package com.example.zahit.multipleactivityproject.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.zahit.multipleactivityproject.R;

public class NewLobbyActivity extends AppCompatActivity {

    EditText lobbyNameInput;
    EditText numberOfPlayersInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lobby);


        lobbyNameInput = findViewById(R.id.lobbyNameInput);
        numberOfPlayersInput = findViewById(R.id.numberOfPlayersInput);

        if(savedInstanceState != null){
            //Saved state for rotation.
            lobbyNameInput.setText(savedInstanceState.getString("SavedLobbyName"));
            numberOfPlayersInput.setText(savedInstanceState.getString("SavedLobbyCount"));

        } else {

            //Initial values for the inputs.
            lobbyNameInput.setText(getIntent().getStringExtra("Username") + "'s Lobby!");
            numberOfPlayersInput.setText("4");

        }

    }

    public void onClick(View view){
        if(view.getId() == R.id.goBackButton){
            //If user decides to go back and not create lobby
            //Then the result is set to canceled.
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED,intent);
            finish();
        }else if(view.getId() == R.id.finishCreateLobbyButton){
            String lobbyName = lobbyNameInput.getText().toString();
            String numberOfPlayers = numberOfPlayersInput.getText().toString();
            if(attemptToCreateLobby(lobbyName,numberOfPlayers)){
                int number = Integer.parseInt(numberOfPlayers);

                Intent intent = new Intent();
                intent.putExtra("NewLobbyName", lobbyName);
                intent.putExtra("NewLobbyPlayers" , number);

                //User successfuly created lobby. Result is set to OK.
                setResult(Activity.RESULT_OK,intent);
                finish();
            }

        }

    }

    private boolean attemptToCreateLobby(String lobbyName, String numberOfPlayers){

        //If the lobby name already exists, then attempt fails.
      //If the number is unparsable or not between 2&4 then attempt fails.
        try{
            int number = Integer.parseInt(numberOfPlayers);
            if(!(number >= 2 && number <=4)){
                return false;
            }

        } catch (NumberFormatException e){

            return false;

        }

        //If the lobby name is too short or too long then the attempt fails.
        if(!(lobbyName.length() >= 6 && lobbyName.length() <= 30))
            return false;

        return true;
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        //Saved state for rotation.
        bundle.putString("SavedLobbyName", lobbyNameInput.getText().toString());
        bundle.putString("SavedLobbyCount",numberOfPlayersInput.getText().toString());

    }

}
