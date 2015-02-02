package com.improvelectronics.sync.android.samples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Rick on 10/25/2014.
 */


public class CurveDisplay extends View {
    Paint paint = new Paint();
    ArrayList<Path> staticHolder;
    ArrayList<Path> dynamicHolder;
    ArrayList<PointF> maxHolder;
    ArrayList<PointF> minHolder;

    ArrayList<Float> xmomentum;
    ArrayList<Float> ymomentum;
    ArrayList<Float> amomentum;
    ArrayList<Integer> rgb;
    ArrayList<Integer> counter;
    boolean motion = false;

    ArrayList <PointF> dVholder;


    public CurveDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
    }
    public void passPaths(ArrayList<Path> staticCurves,ArrayList<Path> dynamicCurves,ArrayList<Integer> timeCounter,ArrayList<PointF> maxCords,ArrayList<PointF> minCords,ArrayList<Integer> colors, ArrayList<PointF> deltaV, ArrayList<Float> rotations){
        staticHolder = staticCurves;
        dynamicHolder = dynamicCurves;
        counter = timeCounter;

        xmomentum = new ArrayList<Float>();
        ymomentum = new ArrayList<Float>();
        amomentum = rotations;
        rgb = new ArrayList<Integer>();
        maxHolder = maxCords;
        minHolder = minCords;


        dVholder = deltaV;

        rgb = colors;

        if(dynamicHolder!=null) {
            for (int i = 0; i < dynamicHolder.size(); i++) {
                xmomentum.add(new Float(deltaV.get(i).x));
                ymomentum.add(new Float(deltaV.get(i).y));

            }
        }
    }
    public void toggleMotion(boolean m){
        motion = m;
    }

    public void reset(){
        motion = false;
    }
    @Override
    public void onDraw(Canvas canvas) {
        canvas.translate(0,1750);
        canvas.rotate(-90);
        //canvas.save();
        canvas.drawColor(Color.WHITE);

        if(staticHolder!=null) {
            for (int i = 0; i < staticHolder.size(); i++) {
                canvas.drawPath(staticHolder.get(i), paint);
            }
        }

        if(dynamicHolder!=null&&staticHolder!=null) {

            if (dynamicHolder != null) {
                for (int i = 0; i < dynamicHolder.size(); i++) {
                    float transx = xmomentum.get(i).floatValue();
                    float transy = ymomentum.get(i).floatValue();
                    float rot = amomentum.get(i).floatValue();
                    transx *= counter.get(i).intValue();
                    transy *= counter.get(i).intValue();
                    rot *= counter.get(i).intValue();

                    canvas.save();
                    canvas.translate((maxHolder.get(i).x+minHolder.get(i).x)/2+transx,(maxHolder.get(i).y+minHolder.get(i).y)/2+transy);
                    canvas.rotate(rot+180);
                    paint.setColor(rgb.get(i).intValue());
                    canvas.drawPath(dynamicHolder.get(i), paint);
                    paint.setColor(Color.BLACK);
                    canvas.restore();

                    if(motion) {
                        counter.set(i,counter.get(i).intValue()+1);
                    }
                }
            }



        }

    }

}
