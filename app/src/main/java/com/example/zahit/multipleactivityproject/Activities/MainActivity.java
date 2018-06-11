package com.example.zahit.multipleactivityproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private CheckBox rememberMeCheckBox;
    private TextView errorTextView;

    private FirebaseAuth authentication;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load views
        emailInput = ((EditText) findViewById(R.id.logEmailInput));
        passwordInput = ((EditText) findViewById(R.id.logPasswordInput));
        errorTextView = ((TextView) findViewById(R.id.logErrorTextView));
        errorTextView.setText("");
        rememberMeCheckBox = ((CheckBox) findViewById(R.id.logRememberMeCheckBox));

        //Loads information from previous succesful login.
        rememberMeInfoLoad();

        //Listener for remember me box, as, when the box is no longer selected,
        //It should unsave the information immediately.
        rememberMeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    SharedPreferences pref = getSharedPreferences("rememberMeInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    //Empty info.
                    editor.putBoolean("isChecked", false);
                    editor.putString("email", "");
                    editor.putString("password", "");
                    editor.commit();
                }
            }
        });

        //Firebase connection
        authentication = FirebaseAuth.getInstance();
        user = authentication.getCurrentUser();
        if (user != null)
            signInUser(user);

        if (savedInstanceState != null) {
            //Bundle being loaded after rotation to keep the username.
            emailInput.setText(savedInstanceState.getString("SavedEmail"));
            passwordInput.setText(savedInstanceState.getString("SavedPassword"));
        }
    }

    private void rememberMeInfoLoad() {
        //Loads information from the shared preferences and sets the edit text views accordingly.
        SharedPreferences pref = getSharedPreferences("rememberMeInfo", Context.MODE_PRIVATE);
        boolean isChecked = pref.getBoolean("isChecked", false);
        String email = pref.getString("email", "");
        String password = pref.getString("password", "");
        rememberMeCheckBox.setChecked(isChecked);
        emailInput.setText(email);
        passwordInput.setText(password);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.logLoginButton) {

            //On entering the Lobby, the username is saved.
            final String email = emailInput.getText().toString();
            final String password = passwordInput.getText().toString();
            if (email.length() == 0 || password.length() == 0) {
                errorTextView.setText(R.string.loginFail);
                return;
            }
            authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        user = authentication.getCurrentUser();
                        //Remember me checked and succesful login, therefore it should be saved.
                        if (rememberMeCheckBox.isChecked()) {
                            SharedPreferences pref = getSharedPreferences("rememberMeInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("isChecked", true);
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.commit();
                        }
                        signInUser(user);

                    } else {
                        errorTextView.setText(R.string.loginFail);

                    }
                }
            });

        } else if (view.getId() == R.id.logRegisterButton) {
            //Register intent
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
        }
    }

    private void signInUser(FirebaseUser user) {
        //Lobbies intent, gone when the login is successful.
        Intent intent = new Intent(this, LobbyListActivity.class);
        startActivity(intent);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        //Bundle being saved for rotation
        super.onSaveInstanceState(bundle);
        bundle.putString("SavedEmail", emailInput.getText().toString());
        bundle.putString("SavedPassword", passwordInput.getText().toString());

    }

    @Override
    public void onBackPressed() {
        //NOTHING
    }
}
