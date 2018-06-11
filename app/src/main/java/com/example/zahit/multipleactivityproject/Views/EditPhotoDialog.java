package com.example.zahit.multipleactivityproject.Views;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.zahit.multipleactivityproject.Activities.ProfileActivity;
import com.example.zahit.multipleactivityproject.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import jp.wasabeef.blurry.Blurry;

/**
 * Created by Zahit on 15-May-18.
 */

public class EditPhotoDialog extends Dialog implements View.OnClickListener {
    public Activity activity;
    public ImageView editingImageView;
    public Uri imageURI;
    public Bitmap grayScaledImageBitmap;
    public Bitmap normalImageBitmap;
    public ColorFilter grayscaleFilter;
    public boolean normalFilterMode = true;



    public EditPhotoDialog(Activity activity) {
        super(activity);
        this.activity = activity;

    }

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        //Dialog like properties set.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_photo_dialog);
        //Relevant buttons and text views with their listeners.
        Button cropImageButton = (Button) findViewById(R.id.cropImageButton);
        cropImageButton.setOnClickListener(this);
        Button grayScaleImageButton = (Button) findViewById(R.id.grayScaleImageButton);
        grayScaleImageButton.setOnClickListener(this);
        Button blurImageButton = (Button) findViewById(R.id.blurImageButton);
        blurImageButton.setOnClickListener(this);
        Button editPhotoBackButton = (Button) findViewById(R.id.editPhotoBackButton);
        editPhotoBackButton.setOnClickListener(this);
        editingImageView = (ImageView) findViewById(R.id.editedImageView);

        //Create grayscale color filter for later usages.
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        grayscaleFilter = colorMatrixColorFilter;


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cropImageButton) {
            if(imageURI == null)
                return;

            //Use crop image api.
            CropImage.activity(imageURI).start(activity);



        }else if(view.getId() == R.id.grayScaleImageButton){

            //If grayscale is requested, change the bitmap to grayscale version and vice versa.
            if(normalFilterMode){
                editingImageView.setImageBitmap(grayScaledImageBitmap);
                normalFilterMode = false;
            }else{
                editingImageView.setImageBitmap(normalImageBitmap);
                normalFilterMode = true;
             }
        }else if(view.getId() == R.id.blurImageButton){

            //Use blur image api.
            Blurry.with(activity).from(normalImageBitmap).into(editingImageView);
            normalImageBitmap = ((BitmapDrawable) editingImageView.getDrawable()).getBitmap();
            generateGrayscaleBitmap();


        }else if(view.getId() == R.id.editPhotoBackButton){
            //dismisses the dialog (making it disappear)
            //also sets the profile photo to this edited photo after dismissing.
            dismiss();
            if (normalFilterMode) {
                ((ProfileActivity) activity).profileImageView.setImageBitmap(normalImageBitmap);
            }else{
                ((ProfileActivity) activity).profileImageView.setImageBitmap(grayScaledImageBitmap);
            }
        }


    }
    public void generateGrayscaleBitmap(){

        //Generates the grayscale image.
        int width, height;
        height = normalImageBitmap.getHeight();
        width = normalImageBitmap.getWidth();

        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(grayscaleFilter);
        c.drawBitmap(normalImageBitmap, 0, 0, paint);
        grayScaledImageBitmap = newBitmap;
    }




}
