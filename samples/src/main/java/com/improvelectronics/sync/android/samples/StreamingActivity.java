/*****************************************************************************
 Copyright © 2014 Kent Displays, Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/

package com.improvelectronics.sync.android.samples;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;


import com.improvelectronics.sync.android.SyncCaptureReport;
import com.improvelectronics.sync.android.SyncPath;
import com.improvelectronics.sync.android.SyncStreamingListener;
import com.improvelectronics.sync.android.SyncStreamingService;

import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

//36 hours, 0 comments, that good practice tho
public class StreamingActivity extends Activity {
  //  DrawView draw;
   // private ArrayList<Path> mobileCurves = new ArrayList<Path>();
   // private ArrayList<Path> staticCurves = new ArrayList<Path>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        draw = new DrawView(this);
//        draw.setBackgroundColor(Color.WHITE);
//        setContentView(draw);

        setContentView(R.layout.activity_streaming);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements SyncStreamingListener {

        private SyncStreamingService mStreamingService;
        private boolean mStreamingServiceBound;
        //private TextView xTextView, yTextView, pressureTextView, stylusDownTetView;
        private CurveDisplay mCurveDisplay;
        private PointF strokePoint;
        private ArrayList<PointF> pointCurve = new ArrayList<PointF>();
        private ArrayList<Path> mobileCurves = new ArrayList<Path>();
        private ArrayList<Path> staticCurves = new ArrayList<Path>();
        private ArrayList<Integer> timeCounter = new ArrayList<Integer>();

        private Path currentCurve = new Path();
        private boolean startOfCurve = true;
        private boolean staticCurve = false;
        private Timer t;

        private ArrayList<PointF> maxCords = new ArrayList<PointF>();
        private ArrayList<PointF> minCords = new ArrayList<PointF>();
        private PointF maxCord;
        private PointF minCord;

        private ArrayList<PointF> deltaV = new ArrayList<PointF>();
        private ArrayList<Float> rotations = new ArrayList<Float>();
        private ArrayList<Integer> rgb = new ArrayList<Integer>();
        private float currentRot = 0;

        private ArrayList<Float> density = new ArrayList<Float>();
        private float theD = 0;
        private float xAverage = 0;
        private float yAverage = 0;
        private ArrayList<PointF> centers = new ArrayList<PointF>();


        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Bind to the ftp service.
            Intent intent = new Intent(getActivity(), SyncStreamingService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_streaming, container, false);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
              mCurveDisplay = (CurveDisplay)getView().findViewById(R.id.curves);

//            xTextView = (TextView)getView().findViewById(R.id.xTextView);
//            yTextView = (TextView)getView().findViewById(R.id.yTextView);
//            pressureTextView = (TextView)getView().findViewById(R.id.pressureTextView);
//            stylusDownTetView = (TextView)getView().findViewById(R.id.stylusDownTextView);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mStreamingServiceBound) {
                // Put the Boogie Board Sync back into MODE_NONE.
                // This way it doesn't use Bluetooth and saves battery life.
                if(mStreamingService.getState() == SyncStreamingService.STATE_CONNECTED) mStreamingService.setSyncMode(SyncStreamingService.MODE_NONE);

                // Don't forget to remove the listener and unbind from the service.
                mStreamingService.removeListener(this);
                getActivity().unbindService(mConnection);
            }
        }

        @Override
        public void onStreamingStateChange(int prevState, int newState) {
                // Put the streaming service in capture mode to get data from Boogie Board Sync.
                if(newState == SyncStreamingService.STATE_CONNECTED) {
                    mStreamingService.setSyncMode(SyncStreamingService.MODE_CAPTURE);
            }
        }

        @Override
        public void onErase() {
            Toast.makeText(getActivity(), "Erase button pushed", Toast.LENGTH_SHORT).show();
            staticCurves = new ArrayList<Path>();
            mobileCurves = new ArrayList<Path>();
            timeCounter = new ArrayList<Integer>();
            maxCords = new ArrayList<PointF>();
            minCords = new ArrayList<PointF>();
            density = new ArrayList<Float>();
            deltaV = new ArrayList<PointF>();
            rotations = new ArrayList<Float>();
            rgb = new ArrayList<Integer>();
            mCurveDisplay.passPaths(staticCurves,mobileCurves,timeCounter,maxCords,minCords,rgb,deltaV,rotations);
            mCurveDisplay.reset();
            mCurveDisplay.invalidate();
        }



        @Override
        public void onSave() {
            Toast.makeText(getActivity(), "Save button pushed", Toast.LENGTH_SHORT).show();
            mCurveDisplay.passPaths(staticCurves, mobileCurves,timeCounter,maxCords,minCords,rgb,deltaV,rotations);
            mCurveDisplay.toggleMotion(true);
            mCurveDisplay.invalidate();
//            long time = System.currentTimeMillis();
//            int cycles = 0;
//            while(true){
//                if(time + cycles*20<System.currentTimeMillis()) {
//                    cycles++;
//                    mCurveDisplay.invalidate();
//                }
//            }
            t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mHandler.obtainMessage().sendToTarget();
                }
            },0,20);
        }
        public Handler mHandler = new Handler(){
            public void handleMessage(Message msg){
                mCurveDisplay.invalidate();
            }
        };

        @Override
        public void onDrawnPaths(List<SyncPath> paths) {

        }

        @Override
        public void onCaptureReport(SyncCaptureReport captureReport) {
//            xTextView.setText(captureReport.getX() + "");
//            yTextView.setText(captureReport.getY() + "");
//            pressureTextView.setText(captureReport.getPressure() + "");
//            if(captureReport.hasTipSwitchFlag()) stylusDownTetView.setVisibility(View.VISIBLE);
//            else stylusDownTetView.setVisibility(View.GONE);
            if(captureReport.hasTipSwitchFlag()) {

                if (startOfCurve) {
                    //check button state
                    if (captureReport.hasBarrelSwitchFlag()) {
                        staticCurve = true;
                    }
                    else{
                        staticCurve = false;
                        maxCord = new PointF(0,0);
                        minCord = new PointF(2000,2000);

                    }
                    startOfCurve = false;
                    currentRot = 0;
                    pointCurve.add(new PointF(captureReport.getX()*1920/20280,captureReport.getY()*1080/13942));
                    //currentCurve.moveTo(captureReport.getX()*1920/20280,captureReport.getY()*1080/13942);

                }
                //currentCurve.lineTo(captureReport.getX()*1920/20280,captureReport.getY()*1080/13942);
                pointCurve.add(new PointF(captureReport.getX()*1920/20280,captureReport.getY()*1080/13942));
                if(pointCurve.size()>2) {
                    int size = pointCurve.size();

                    double prevAng = Math.atan2(pointCurve.get(size-3).y-pointCurve.get(size-2).y,pointCurve.get(size-3).x-pointCurve.get(size-2).x);
                    double currentAng = Math.atan2(pointCurve.get(size-2).y-pointCurve.get(size-1).y,pointCurve.get(size-2).x-pointCurve.get(size-1).x);
                    double difference = Math.abs(currentAng - prevAng +2*Math.PI )%(2*Math.PI);
                    if (difference>Math.PI) difference -= 2*Math.PI;
                    if (difference<-1*Math.PI) difference += 2*Math.PI;
                    if (difference<Math.PI/2&&difference>-1*Math.PI/2){
                        currentRot += difference;
                    }
                }
                xAverage += captureReport.getX()*1920/20280*captureReport.getPressure();
                yAverage += captureReport.getY()*1080/13942*captureReport.getPressure();
                theD+=captureReport.getPressure();
                if(!staticCurve){
                    if(captureReport.getX()*1920/20280>maxCord.x){
                        maxCord.x = captureReport.getX()*1920/20280;
                    }
                    if(captureReport.getX()*1920/20280<minCord.x){
                        minCord.x = captureReport.getX()*1920/20280;
                    }
                    if(captureReport.getY()*1080/13942>maxCord.y){
                        maxCord.y = captureReport.getY()*1080/13942;
                    }
                    if(captureReport.getY()*1080/13942<minCord.y){
                        minCord.y = captureReport.getY()*1080/13942;
                    }
                }
            }
            else{
                startOfCurve = true;
                if(pointCurve.size()!=0) {
                    if (staticCurve) {
                        currentCurve.moveTo(pointCurve.get(0).x,pointCurve.get(0).y);
                        for (int i = 1; i < pointCurve.size(); i++){
                            currentCurve.lineTo(pointCurve.get(i).x,pointCurve.get(i).y);
                        }
                        staticCurves.add(currentCurve);
                    } else {
                        PointF midpoint = new PointF((maxCord.x+minCord.x)/2,(maxCord.y+minCord.y)/2);
                        currentCurve.moveTo(midpoint.x-pointCurve.get(0).x,midpoint.y-pointCurve.get(0).y);
                        for(int i = 1; i < pointCurve.size(); i++){
                            currentCurve.lineTo(midpoint.x-pointCurve.get(i).x,midpoint.y-pointCurve.get(i).y);
                        }

                        PointF dV = new PointF(0,0);
                        dV.x = pointCurve.get(pointCurve.size()-1).x-pointCurve.get(0).x;
                        dV.y = pointCurve.get(pointCurve.size()-1).y-pointCurve.get(0).y;
                        //deltaV.add(dV);
                        //rotations.add(new Float( currentRot));

                        xAverage= xAverage/theD;
                        yAverage= yAverage/theD;
                        centers.add(new PointF(xAverage,yAverage));
//                        maxCord.x = xAverage;
//                        maxCord.y = yAverage;
//                        minCord.x = xAverage;
//                        minCord.y = yAverage;

                        maxCords.add(maxCord);
                        minCords.add(minCord);
                        theD = theD/pointCurve.size();
                        density.add(new Float(theD));
                        theD=0;
                        timeCounter.add(new Integer(0));






                        Random r = new Random();
                        //xtrans stuff
                        double rand = Math.pow((r.nextFloat() - 0.5) * 2.5f,2);
                        if(r.nextBoolean()) rand = rand*-1;
                        rand -= (theD-120)/43;

                        rand = 0;
                        rand += dV.x/25;
                        rand *= 10/Math.sqrt(Math.sqrt(Math.pow(maxCord.x-minCord.x,2)+Math.pow(maxCord.y-minCord.y,2)));
                        dV.x=(float)rand;
                        //ytrans stuff
                        rand = Math.pow((r.nextFloat() - 0.5) * 2.5f,2);
                        if(r.nextBoolean()) rand = rand*-1;
                        rand *= Math.pow(800-(theD-270)/300,0.1);

                        rand = 0;
                        rand += dV.y/25;
                        rand *= 10/Math.sqrt(Math.sqrt(Math.pow(maxCord.x-minCord.x,2)+Math.pow(maxCord.y-minCord.y,2)));
                        dV.y=(float)rand;
                        if(currentRot>3*Math.PI&&Math.sqrt(Math.pow(pointCurve.get(pointCurve.size()-1).x-pointCurve.get(0).x,2)+Math.pow(pointCurve.get(pointCurve.size()-1).y-pointCurve.get(0).y,2))<100){
                            dV.x = 0;
                            dV.y = 0;
                        }
                        //rot stuff
                        rand = Math.pow((r.nextFloat() - 0.5) * 2.5f,2);
                        if(r.nextBoolean()) rand = rand*-1;
                        rand *= Math.pow(800-(theD-270)/300,0.1);
                        rand *= 100/Math.sqrt(Math.sqrt(Math.pow(maxCord.x-minCord.x,2)+Math.pow(maxCord.y-minCord.y,2)));
                        rand = 0;
                        rand = currentRot/20;
                        rotations.add((new Float(rand)));
                        rgb.add(Color.rgb(r.nextInt(200)+20,r.nextInt(200)+20,r.nextInt(200)+20));

                        deltaV.add(dV);






                        mobileCurves.add(currentCurve);

                    }
                    mCurveDisplay.passPaths(staticCurves,mobileCurves,timeCounter,maxCords,minCords,rgb,deltaV,rotations);
                    mCurveDisplay.invalidate();
                    currentCurve = new Path();
                    pointCurve= new ArrayList<PointF>();
                    theD=0;
                }

            }

        }

        private final ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                // Set up the service
                mStreamingServiceBound = true;
                SyncStreamingService.SyncStreamingBinder binder = (SyncStreamingService.SyncStreamingBinder) service;
                mStreamingService = binder.getService();
                mStreamingService.addListener(PlaceholderFragment.this);// Add listener to retrieve events from streaming service.

                // Put the streaming service in capture mode to get data from Boogie Board Sync.
                if(mStreamingService.getState() == SyncStreamingService.STATE_CONNECTED) {
                    mStreamingService.setSyncMode(SyncStreamingService.MODE_CAPTURE);
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                mStreamingService = null;
                mStreamingServiceBound = false;
            }
        };
    }
}
