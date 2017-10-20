package com.teamkelly.kelly;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

/**
 * Created by himsun on 2017. 9. 3..
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // LED configuration.
    private static final int NUM_LEDS = 50;
    private static final int LED_BRIGHTNESS = 31; // 0 ... 31
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

    private LedColors[] Colors = null;
    private String userkey = "";
    private String parterkey = "";

    private Users userData = new Users();
    private Users parterData = new Users();
    private ConnectUser connectUser;

    private final int todayConst = 36;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference colorRef = database.getReference("colors");
    DatabaseReference demoRef = database.getReference("demo");
    DatabaseReference dataRef = database.getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Log.d(TAG, "init start");
        ledinit();
        Log.d(TAG, "init end");

      //  ledColorSetupDemo();
     //   ledGradationDemo();

        colorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                Colors = new LedColors[(int)dataSnapshot.getChildrenCount()];

                int k=0;
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    LedColors color = postSnapshot.getValue(LedColors.class);
                    Log.d("color","color = "+ Color.rgb(color.r, color.g, color.b));
                    color.color = Color.rgb(color.r, color.g, color.b);
                    Colors[k++] = color;
                }
//

                for(int i=6;i<=6;i++) {
                    for(int j=1;j<=7;j++) {
                        switch (j) {
                            case 1 :
                                mLedColors[(i-1)*7+j] = Colors[0].color;
                                break;
                            case 2:
                                mLedColors[(i-1)*7+j] = Colors[1].color;
                                break;
                            case 3:
                                mLedColors[(i-1)*7+j] = Colors[2].color;
                                break;
                            case 4:
                                mLedColors[(i-1)*7+j] = Colors[3].color;
                                break;
                            case 5:
                                mLedColors[(i-1)*7+j] = Colors[4].color;
                                break;
                            case 6:
                                mLedColors[(i-1)*7+j] = Colors[5].color;
                                break;
                            case 7:
                                mLedColors[(i-1)*7+j] = Colors[4].color;
                                break;
                        }
                    }
                }

                try {
                    mLedstrip.write(mLedColors);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        demoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    ConnectUser useritem = user.getValue(ConnectUser.class);
                    if(user.getKey().equals("user1")) {
                        userkey = useritem.key;
                    } else if(user.getKey().equals("user2")) {
                        parterkey = useritem.key;
                    }
                    Log.e(TAG,user.toString());
                }

                dataRef.child(userkey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int k=0;
                        Log.e(TAG,dataSnapshot.toString());
                        userData.dates = new Dates[(int)dataSnapshot.child("dates").getChildrenCount()];

                        for(DataSnapshot date: dataSnapshot.child("dates").getChildren()) {
                            Log.e(TAG,date.toString());
                            int key = Integer.parseInt(date.getKey());
                            Log.e(TAG,date.child("color").getValue(Integer.class).toString());
                            userData.dates[key].color = date.child("color").getValue(Integer.class);
                            userData.dates[key].date = date.child("date").getValue(Integer.class);
                            userData.dates[key].month = date.child("month").getValue(Integer.class);
                            for(DataSnapshot color: date.child("colors").getChildren()) {
                                userData.dates[key].colors[Integer.parseInt(color.getKey())] = color.getValue(Integer.class);
                            }
                        }
                        userData.mode = dataSnapshot.child("mode").getValue(Integer.class);
                        userData.name = dataSnapshot.child("name").getValue(String.class);

                        setLedColors();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
                dataRef.child(parterkey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int k=0;
                        Log.e(TAG,dataSnapshot.toString());
                        parterData.dates = new Dates[(int)dataSnapshot.child("dates").getChildrenCount()];

                        for(DataSnapshot date: dataSnapshot.child("dates").getChildren()) {
                            parterData.dates[k++] = date.getValue(Dates.class);
                        }
                        parterData.mode = dataSnapshot.child("mode").getValue(Integer.class);
                        parterData.name = dataSnapshot.child("name").getValue(String.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                }
            }
//
//            for(int i=0;i<1;i++) {
//                for(int j=0;j<7;j++) {
//                    switch (i) {
//                        case 0:
//                            mLedColors[i*7+j] = Color.RED;
//                            break;
//                        case 1:
//                            mLedColors[i*7+j] = Color.YELLOW;
//                            break;
//                        case 2:
//                            mLedColors[i*7+j] = Color.BLUE;
//                            break;
//                        case 3:
//                            mLedColors[i*7+j] = Color.GREEN;
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            }
//            mLedColors[3] = Color.BLUE;
//            mLedColors[13] = Color.RED;
//            mLedColors[17] = Color.YELLOW;
//            mLedColors[28] = Color.RED;
//            mLedColors[29] = Color.YELLOW;
//            mLedColors[30] = Color.YELLOW;

            for(int i=6;i<=6;i++) {
                for(int j=1;j<=7;j++) {
                    switch (j) {
                        case 1 :
                            mLedColors[xyToindex(i,j)] = Color.YELLOW;
                            break;
                        case 2:
                            mLedColors[xyToindex(i,j)] = Color.RED;
                            break;
                        case 3:
                            mLedColors[xyToindex(i,j)] = Color.BLUE;
                            break;
                        case 4:
                            mLedColors[xyToindex(i,j)] = Color.GREEN;
                            break;
                        case 5:
                            mLedColors[xyToindex(i,j)] = Color.RED;
                            break;
                        case 6:
                            mLedColors[xyToindex(i,j)] = Color.RED;
                            break;
                        case 7:
                            mLedColors[xyToindex(i,j)] = Color.BLUE;
                            break;
                    }
                }
            }

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

    private void setLedColors() {
        Log.d(TAG, "set color");

        Dates dates[];
        if(userData.mode==1) {
            dates = userData.dates;
        } else {
            dates = parterData.dates;
        }

        mLedColors = new int[NUM_LEDS];
        for(int i=0;i<dates.length;i++) {
            final Dates item =dates[i];
            mLedColors[dateToIndex(item.date)] = item.color;

            if(item.date == 21) {
                for(int j=0;j<item.colors.length;j++) {
                    mLedColors[todayConst+j] = item.colors[j];
                }
            }
        }

        try {
            mLedstrip.write(mLedColors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable mAnimateRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                Log.d(TAG,"frame"+mFrame);

        //        mLedColors[0] = changeColor(Color.RED,Color.BLUE,mFrame);
                mLedColors[1] = changeColor(Color.RED,Color.RED,mFrame);
                mLedColors[2] = changeColor(Color.BLUE,Color.YELLOW,mFrame);
                mLedColors[3] = changeColor(Color.BLUE,Color.GREEN,mFrame);
                mLedColors[4] = changeColor(Color.YELLOW,Color.YELLOW,mFrame);
                mLedColors[5] = changeColor(Color.YELLOW,Color.YELLOW,mFrame);
                mLedColors[6] = changeColor(Color.BLUE,Color.YELLOW,mFrame);
                mLedColors[7] = changeColor(Color.BLUE,Color.BLUE,mFrame);
                mLedColors[xyToindex(2,1)] = changeColor(Color.BLUE,Color.RED,mFrame);
                mLedColors[xyToindex(2,2)] = changeColor(Color.YELLOW,Color.YELLOW,mFrame);

                for(int i=6;i<=6;i++) {
                    for(int j=1;j<=7;j++) {
                        switch (j) {
                            case 1 :
                                mLedColors[xyToindex(i,j)] = Colors[0].color;
                                break;
                            case 2:
                                mLedColors[xyToindex(i,j)] = Colors[1].color;
                                break;
                            case 3:
                                mLedColors[xyToindex(i,j)] = Colors[2].color;
                                break;
                            case 4:
                                mLedColors[xyToindex(i,j)] = Colors[3].color;
                                break;
                            case 5:
                                mLedColors[xyToindex(i,j)] = Colors[4].color;
                                break;
                            case 6:
                                mLedColors[xyToindex(i,j)] = Colors[5].color;
                                break;
                            case 7:
                                mLedColors[xyToindex(i,j)] = Colors[4].color;
                                break;
                        }
                    }
                }

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
        //    mHandler.postDelayed(mAnimateRunnable, FRAME_DELAY_MS);
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

    int dateToIndex(int date) {
        if((date/7)%2==0) {
            return date;
        } else {
            final int dateconst[] = {6,4,2,0,-2,4,6};
            return date+dateconst[date%7];
        }
    }
}