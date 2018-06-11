package com.example.zahit.multipleactivityproject.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zahit.multipleactivityproject.BuildConfig;
import com.example.zahit.multipleactivityproject.R;
import com.example.zahit.multipleactivityproject.Views.EditPhotoDialog;
import com.example.zahit.multipleactivityproject.Views.NewPhotoChoiceDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    public ImageView profileImageView;
    private NewPhotoChoiceDialog newPhotoChoiceDialog;
    private EditPhotoDialog editPhotoDialog;
    private Button recordAudioButton;
    private Button playAudioButton;

    private MediaRecorder mRecorder;
    private boolean recordState = false;

    private ArrayList<Uri> uris;
    private String username;
    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        newPhotoChoiceDialog = new NewPhotoChoiceDialog(ProfileActivity.this);
        editPhotoDialog = new EditPhotoDialog(ProfileActivity.this);

        //generate uri arraylist to populate later
        uris = new ArrayList<Uri>();

        username = getIntent().getStringExtra("Username");
        ((TextView) findViewById(R.id.profileUsernameTextView)).setText(username);
        recordAudioButton = (Button) findViewById(R.id.profileRecordAudioButton);
        playAudioButton = (Button) findViewById(R.id.profilePlayAudioButton);
        profileImageView = (ImageView) findViewById(R.id.profileImageView);

        //ask the user for read,write and record permissions
        askForPermissions();

        try{
            //direction of file for the audio recording
            mFileName = getFilesDir() + "/profileMessage.3gp";
        }catch(NullPointerException exception){
            exception.printStackTrace();
            Toast.makeText(getApplicationContext(),"Permission failure, nothing will work unless you grant permission!", Toast.LENGTH_LONG).show();
        }

     }

    public void onBackPressed() {
        //stop recording audio when back button is pressed
        stopRecording();
        finish();
    }


    public void onClick(View view) {
        if (view.getId() == R.id.profileBackButton) {
            //stop recording if custom back button is clicked
            stopRecording();
            finish();
        } else if (view.getId() == R.id.profileRecordAudioButton && !recordState) {
            if(startRecording()){
                //start recording audio
                recordState = true;
                recordAudioButton.setText(getText(R.string.stop));
                playAudioButton.setEnabled(false);
            }
        } else if (view.getId() == R.id.profileRecordAudioButton && recordState) {

            //stop recording audio
            recordAudioButton.setText(getText(R.string.recordAudio));
            recordState = false;
            stopRecording();
            playAudioButton.setEnabled(true);
        } else if (view.getId() == R.id.profilePlayAudioButton) {
            //play audio
            playRecording();
        }else if (view.getId() == R.id.profileEditPhotoButton) {
            //open new photo dialog
            if(!newPhotoChoiceDialog.isShowing()){
                newPhotoChoiceDialog.show();
            }
        }
    }

    public void playRecording() {
        //play recording
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(mFileName);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean startRecording() {

        //start recording
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setMaxFileSize(10 * 1024 * 1024);
            mRecorder.prepare();

            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void stopRecording() {

        //stop recording
        try {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri photoURI;

    private void takePicture() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            //use fileprovider for later versions
            photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName()
                    + ".my.package.name.provider", new File(Environment.getExternalStorageDirectory(),"fname_"
                    + String.valueOf(System.currentTimeMillis()) + ".jpg"));
        }else{
            //uri for earlier versions
            photoURI = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"fname_"
                    + String.valueOf(System.currentTimeMillis()) + ".jpg"));
        }

        //image capture intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //photo is successfully taken and retrieved, add it to uris and pop the edit photo dialog for editing.
                uris.add(photoURI);
                if(!editPhotoDialog.isShowing()){
                    editPhotoDialog.normalFilterMode = true;
                    editPhotoDialog.show();
                    editPhotoDialog.imageURI = photoURI;
                    editPhotoDialog.editingImageView.setImageURI(photoURI);
                    editPhotoDialog.normalImageBitmap = ((BitmapDrawable) editPhotoDialog.editingImageView.getDrawable()).getBitmap();
                    editPhotoDialog.generateGrayscaleBitmap();
                }

            }
        }else  if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            //photo is cropped successfully, set fields to this new photo.
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                editPhotoDialog.imageURI = resultUri;
                editPhotoDialog.editingImageView.setImageURI(resultUri);
                editPhotoDialog.normalImageBitmap = ((BitmapDrawable) editPhotoDialog.editingImageView.getDrawable()).getBitmap();
                editPhotoDialog.generateGrayscaleBitmap();


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }

        } else  if (requestCode == 1) {
            //TODO
            //Set result of chosen photo.
        }
    }

    private void askForPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
            //If theres no permission, request it.
            String[] permissions = new String[3];
            permissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
            permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            permissions[2] = Manifest.permission.RECORD_AUDIO;

            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }
    public void requestCameraPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //If theres no permission, request it.
            String[] permissions = new String[1];
            permissions[0] = Manifest.permission.CAMERA;
            ActivityCompat.requestPermissions(this, permissions, 1);
        }else{
            takePicture();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            //If the user gave permission after being asked to, updates the users country code.

            case 0:
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try{
                        mFileName = getFilesDir() + "/profileMessage.3gp";
                    }catch(NullPointerException exception){
                        exception.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Fail!", Toast.LENGTH_LONG).show();
                    }
                }
                break;

                //Permission for camera, if given, open the camera intent.

            case 1:
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }



        }
    }

    public void choosePhotoFromGallery() {

        //Create and start the activity of the intent for choosing photo from gallery, populate the intent with relevant string data.

        String[] urisAsString = new String[uris.size()];
        for(int i = 0;i<uris.size();i++){
            urisAsString[i] = uris.get(i).toString();
        }

        Intent intent = new Intent(this,PhotoGalleryActivity.class);
        intent.putExtra("UriStrings",urisAsString);
        startActivityForResult(intent, 1);
    }
}
