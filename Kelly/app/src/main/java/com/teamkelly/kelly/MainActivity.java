package com.teamkelly.kelly;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;

import java.io.IOException;

/**
 * Created by himsun on 2017. 9. 3..
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // LED configuration.
    private static final int NUM_LEDS = 40;
    private static final int LED_BRIGHTNESS = 10; // 0 ... 31
    private static final Apa102.Mode LED_MODE = Apa102.Mode.BGR;

    private static final String SPIPORT = "SPI0.0";

    private Apa102 mLedstrip;
    private int[] mLedColors = new int[NUM_LEDS];

    private Handler mHandler = new Handler();
    private HandlerThread mPioThread;
    private int mFrame = 0;
    private static final int FRAME_DELAY_MS = 100; // 10fps

    private static final int Gradation = 20;

    int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Log.d(TAG, "init start");

        ledinit();
        Log.d(TAG, "init end");

        //ledColorSetupDemo();
        ledGradationDemo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try {
            mLedstrip.close();
            mHandler.removeCallbacks(mPioThread);
            mPioThread.quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void ledinit() {
     //   mLedColors = new int[NUM_LEDS];
        try {
            Log.d(TAG, "Initializing LED strip");
            mLedstrip = new Apa102(SPIPORT, LED_MODE);
            mLedstrip.setBrightness(LED_BRIGHTNESS);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LED strip", e);
        }
    }

    void ledColorSetupDemo() {

        try {
            Log.d(TAG, "set color");

            for(int i=0;i<5;i++) {
                for(int j=0;j<7;j++) {
                    switch (i) {
                        case 0:
                            mLedColors[i*7+j] = Color.RED;
                            break;
                        case 1:
                            mLedColors[i*7+j] = Color.YELLOW;
                            break;
                        case 2:
                            mLedColors[i*7+j] = Color.BLUE;
                            break;
                        case 3:
                            mLedColors[i*7+j] = Color.GREEN;
                            break;
                        default:
                            break;
                    }
                }
            }
            mLedColors[3] = Color.BLUE;
            mLedColors[13] = Color.RED;
            mLedColors[17] = Color.YELLOW;
            mLedColors[28] = Color.RED;
            mLedColors[29] = Color.YELLOW;
            mLedColors[30] = Color.YELLOW;

            mLedstrip.write(mLedColors);
        } catch (IOException e) {
            Log.e(TAG, "Error while writing to LED strip", e);
        }
    }

    void ledGradationDemo() {
        Log.d(TAG,"change start");
        mPioThread = new HandlerThread("pioThread");
        mPioThread.start();
        mHandler = new Handler(mPioThread.getLooper());
        mHandler.post(mAnimateRunnable);
    }


    private Runnable mAnimateRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                Log.d(TAG,"frame"+mFrame);

                mLedColors[0] = changeColor(Color.RED,Color.BLUE,mFrame);
                mLedColors[1] = changeColor(Color.RED,Color.RED,mFrame);
                mLedColors[2] = changeColor(Color.BLUE,Color.YELLOW,mFrame);
                mLedColors[3] = changeColor(Color.BLUE,Color.GREEN,mFrame);
                mLedColors[4] = changeColor(Color.YELLOW,Color.YELLOW,mFrame);
                mLedColors[5] = changeColor(Color.YELLOW,Color.YELLOW,mFrame);
                mLedColors[6] = changeColor(Color.BLUE,Color.YELLOW,mFrame);
                mLedColors[7] = changeColor(Color.BLUE,Color.BLUE,mFrame);
                mLedColors[xyToindex(2,1)] = changeColor(Color.BLUE,Color.RED,mFrame);
                mLedColors[xyToindex(2,2)] = changeColor(Color.YELLOW,Color.YELLOW,mFrame);

                mLedstrip.write(mLedColors);

                mFrame+=flag;

                if(mFrame==Gradation) {
                    flag*=-1;
                }
                else if(mFrame==0) {
                    flag*=-1;
                }

            } catch (IOException e) {
                Log.e(TAG, "Error while writing to LED strip", e);
            }
            mHandler.postDelayed(mAnimateRunnable, FRAME_DELAY_MS);
        }
    };

    int changeColor(int start, int end, int frame) {
        int color;
        int dRed, dGreen, dBlue;
        dRed = (Color.red(end) - Color.red(start))/Gradation;
        dGreen = (Color.green(end) - Color.green(start))/Gradation;
        dBlue = (Color.blue(end) - Color.blue(start))/Gradation;

        color = Color.rgb(Color.red(start) + dRed * frame, Color.green(start) + dGreen * frame, Color.blue(start) + dBlue * frame);

        return color;
    }

    //달력 좌표 index로 변환
    int xyToindex(int x,int y) {
        int ledIndex;
        if(x%2==1) {
            ledIndex = (x-1)*7+y;
        } else {
            ledIndex = x*7-y+1;
        }
        return ledIndex;
    }
}
