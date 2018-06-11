package com.example.zahit.multipleactivityproject;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Zahit on 20-May-18.
 */

public class DrawSegment {
    public ArrayList<Point> points;

    public DrawSegment(){
        points = new ArrayList<>();
    }

    public void addPoint(int x,int y){
        points.add(new Point(x,y));
    }

    public ArrayList<Point> getPoints(){
        return points;
    }
}
