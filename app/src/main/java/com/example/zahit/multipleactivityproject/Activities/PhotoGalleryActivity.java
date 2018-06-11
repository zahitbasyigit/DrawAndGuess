package com.example.zahit.multipleactivityproject.Activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import com.example.zahit.multipleactivityproject.MyAdapter;
import com.example.zahit.multipleactivityproject.R;

import java.util.ArrayList;

public class PhotoGalleryActivity extends AppCompatActivity {

    GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);
        String[] uriStrings = getIntent().getStringArrayExtra("UriStrings");

        //convert string data to uri data by parsing.
        Uri[] uriArray = new Uri[uriStrings.length];
        for(int i = 0;i<uriStrings.length;i++){
            uriArray[i] = Uri.parse(uriStrings[i]);
        }

        //set gridviews adapter to our custom adapter.
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new MyAdapter(this, uriArray,gridView));
    }
}
