package com.example.zahit.multipleactivityproject.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.zahit.multipleactivityproject.DrawSegment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

/**
 * Created by Zahit on 14-May-18.
 */

//CodeReference
//The following class and most of its code is referenced (and modified for this project) from
//https://github.com/firebase/AndroidDrawing/blob/master/app/src/main/java/com/firebase/drawing/DrawingView.java
//It is an open source code provided by firebase itself for firebase shared drawing.
public class PaintView extends View {

    private float scale = 1;
    public int width;
    public int height;

    private float previousX, previousY;

    private Paint paint;
    private Paint bitmapPaint;
    private Bitmap bitmap;
    private Canvas canvas;

    private Path path;
    private DrawSegment currentDrawSegment;

    Context context;

    public String gameKey;
    public DatabaseReference ref;
    public boolean allowDraw = false;

    public PaintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context=context;
        path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scale = Math.min(1.0f*w/getWidth(),1.0f*h/ getHeight());
        width = w;
        height = h;
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
    }

    public void clearDrawing() {
        setDrawingCacheEnabled(false);

        onSizeChanged(width, height, width, height);
        invalidate();

        setDrawingCacheEnabled(true);
    }

    private void touchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        previousX = x;
        previousY = y;
        currentDrawSegment = new DrawSegment();
        currentDrawSegment.addPoint((int)previousX,(int) previousY);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - previousX);
        float dy = Math.abs(y - previousY);
        if (dx >= 4 || dy >= 4) {
            path.quadTo(previousX, previousY, (x + previousX)/2, (y + previousY)/2);
            previousX = x;
            previousY = y;
            currentDrawSegment.addPoint((int)previousX,(int) previousY);
        }
    }

    private void touchUp() {
        path.lineTo(previousX, previousY);
        canvas.drawPath(path, paint);
        path.reset();

        DrawSegment segment = new DrawSegment();
        for (Point point: currentDrawSegment.getPoints()) {
            segment.addPoint((int)Math.round(point.x / scale), (int)Math.round(point.y / scale));
        }

        DatabaseReference keyRef = ref.push();
        keyRef.setValue(segment);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(allowDraw){
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    invalidate();
                    break;
            }
        }

        return true;
    }

    public static Path getPathForPoints(List<Point> points, double scale) {
        Path path = new Path();
        Point current = points.get(0);
        path.moveTo(Math.round(scale * current.x), Math.round(scale * current.y));
        Point next = null;
        for (int i = 1; i < points.size(); ++i) {
            next = points.get(i);
            path.quadTo(
                    Math.round(scale * current.x), Math.round(scale * current.y),
                    Math.round(scale * (next.x + current.x) / 2), Math.round(scale * (next.y + current.y) / 2)
            );
            current = next;
        }
        if (next != null) {
            path.lineTo(Math.round(scale * next.x), Math.round(scale * next.y));
        }
        return path;
    }

    public void addDatabaseListeners() {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DrawSegment segment = dataSnapshot.getValue(DrawSegment.class);
                canvas.drawPath(getPathForPoints(segment.getPoints(), scale), paint);
                invalidate();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                clearDrawing();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void clearDrawingForAll() {
        ref.removeValue();
    }
}

