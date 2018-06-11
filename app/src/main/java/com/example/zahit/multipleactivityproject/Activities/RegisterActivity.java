package com.example.zahit.multipleactivityproject.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private TextView errorTextView;

    private FirebaseAuth authentication;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Loads views
        emailInput = ((EditText) findViewById(R.id.regEmailInput));
        usernameInput = ((EditText) findViewById(R.id.regUsernameInput));
        passwordInput = ((EditText) findViewById(R.id.regPasswordInput1));
        confirmPasswordInput = ((EditText) findViewById(R.id.regPasswordInput2));

        //Initially empty error text.
        errorTextView = ((TextView) findViewById(R.id.regErrorTextView));
        errorTextView.setText("");

        //Firebase authentication
        authentication = FirebaseAuth.getInstance();
        user = authentication.getCurrentUser();
        if(user != null)
            signInUser(user);

        if(savedInstanceState!=null){
            //Bundle being loaded after rotation to keep the username.
            emailInput.setText(savedInstanceState.getString("SavedEmail"));
            usernameInput.setText(savedInstanceState.getString("SavedUsername"));
            passwordInput.setText(savedInstanceState.getString("SavedPassword1"));
            confirmPasswordInput.setText(savedInstanceState.getString("SavedPassword2"));
        }
    }

    public void onClick(View view){
        if(view.getId() == R.id.regRegisterButton){
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            //Checks if two passwords are equal
            if(!password.equals(confirmPassword)){
                errorTextView.setText(R.string.registerPWMatchFail);
                return;
            }

            //Checks if strings are null.
            if(email == null || password == null || confirmPassword == null){
                errorTextView.setText(R.string.registerFail);
                return;
            }

            //Checks if inputs of length 0 are provided, as, this is not handled by firebase.
            if(email.length() == 0 ||password.length() == 0 || confirmPassword.length() == 0){
                errorTextView.setText(R.string.registerFail);
                return;
            }

            authentication.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //Successful register, information is saved to the firebase database (username)
                        String username = usernameInput.getText().toString();
                        user = authentication.getCurrentUser();
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference ref = db.getReference();

                        Map<String,Object> updates = new HashMap<>();
                        updates.put("/users/" + user.getUid() + "/username",username);
                        updates.put("/users/" + user.getUid() + "/language" ,"TR");
                        ref.updateChildren(updates);
                        signInUser(user);

                    }else {
                        errorTextView.setText(R.string.registerFail);
                    }
                }
            });

        }else if(view.getId() == R.id.regBackButton) {
            //Goes back to the login activity, main
            finish();
        }


    }

    private void signInUser(FirebaseUser user){
        //Successful register.
        Intent intent = new Intent(this,LobbyListActivity.class);
        startActivity(intent);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        //Bundle being saved for rotation
        super.onSaveInstanceState(bundle);
        bundle.putString("SavedEmail", emailInput.getText().toString());
        bundle.putString("SavedUsername", usernameInput.getText().toString());
        bundle.putString("SavedPassword1", passwordInput.getText().toString());
        bundle.putString("SavedPassword2", confirmPasswordInput.getText().toString());

    }

    @Override
    public void onBackPressed(){
        //NOTHING
    }

}
