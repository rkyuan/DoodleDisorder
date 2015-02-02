package com.improvelectronics.sync.android.samples;

/**
 * Created by Rick on 10/25/2014.
 */
public class TimerThread implements Runnable {
    CurveDisplay disp;
    public void run() {
        while(true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //disp.invalidate();
        }
    }
    public void pass(CurveDisplay curve){
        disp = curve;
    }
}
