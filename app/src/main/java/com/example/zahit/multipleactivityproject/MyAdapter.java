package com.example.zahit.multipleactivityproject;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.zahit.multipleactivityproject.Activities.PhotoGalleryActivity;

import java.net.URI;

/**
 * Created by Zahit on 18-May-18.
 */

public class MyAdapter extends BaseAdapter {

    private Context context;
    public Uri[] uris;
    public GridView gridView;

    public MyAdapter(Context context, Uri[] uris, GridView gridView){
        this.context = context;
        this.uris = uris;
        this.gridView = gridView;

    }


    @Override
    public int getCount() {
        return uris.length;
    }

    @Override
    public Object getItem(int i) {
        return uris[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if(view == null){
            imageView = new ImageView(context);

            imageView.setLayoutParams(new GridView.LayoutParams(350,350));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else{
            imageView = (ImageView) view;
        }

        imageView.setImageURI(uris[i]);

        return imageView;
    }
}
