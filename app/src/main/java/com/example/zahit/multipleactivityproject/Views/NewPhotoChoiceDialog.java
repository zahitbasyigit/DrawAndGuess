package com.example.zahit.multipleactivityproject.Views;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.zahit.multipleactivityproject.Activities.GameActivity;
import com.example.zahit.multipleactivityproject.Activities.ProfileActivity;
import com.example.zahit.multipleactivityproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Zahit on 29-Apr-18.
 */

public class NewPhotoChoiceDialog extends Dialog implements View.OnClickListener {
    public Activity activity;



    public NewPhotoChoiceDialog(Activity activity) {
        super(activity);
        this.activity = activity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        //Dialog like properties set.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.newphotodialog);
        //Relevant buttons and text views with their listeners.
        Button photoNewPhotoButton = (Button) findViewById(R.id.photoNewPhoto);
        photoNewPhotoButton.setOnClickListener(this);
        Button photoExistingPhotoButton = (Button) findViewById(R.id.photoExistingPhoto);
        photoExistingPhotoButton.setOnClickListener(this);
        Button photoBackButton = (Button) findViewById(R.id.photoBack);
        photoBackButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.photoNewPhoto) {
            //New photo will be taken, use ProfileActivities method.
            dismiss();
            ((ProfileActivity) activity).requestCameraPermission();

        }else if(view.getId() == R.id.photoExistingPhoto){
            //Existing photo from gallery will be chosen.
            dismiss();
            ((ProfileActivity) activity).choosePhotoFromGallery();

        }else if(view.getId() == R.id.photoBack){
            //dismisses the dialog (making it disappear)
            dismiss();
        }


    }



}
