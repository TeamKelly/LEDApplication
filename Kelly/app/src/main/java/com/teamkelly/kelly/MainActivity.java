package com.teamkelly.kelly;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
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
    private static final int LED_BRIGHTNESS = 10; // 0 ... 31
    private static final Apa102.Mode LED_MODE = Apa102.Mode.BGR;

    private static final String SPIPORT = "SPI0.0";

    private Apa102 mLedstrip;
    private static final String MODE_MINE = "BCM5";
    private static final String MODE_PARTNER = "BCM6";

    private Gpio[] mMode;
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

    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.d(TAG, "GPIO1 " + mMode[0].getValue() + " GPIO2 " + mMode[1].getValue());
                if (mMode[0].getValue()) {
                    dataRef.child(userkey).child("mode").setValue(1); //<mode
                } else if (mMode[1].getValue()) {
                    dataRef.child(userkey).child("mode").setValue(0); //<mode
                }
            } catch (IOException e) {
                Log.w(TAG, "Gpio read error", e);
            }
            return true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Log.d(TAG, "init start");
        ledinit();
        Log.d(TAG, "init end");

      //  ledColorSetupDemo();
     //   ledGradationDemo();
        mMode = new Gpio[2];
        PeripheralManagerService manager = new PeripheralManagerService();
        try {
            mMode[0] = manager.openGpio(MODE_MINE);
            mMode[1] = manager.openGpio(MODE_PARTNER);

            for (int i = 0; i < 2; i++) {
                mMode[i].setDirection(Gpio.DIRECTION_IN);
                mMode[i].setActiveType(Gpio.ACTIVE_LOW);

                mMode[i].setEdgeTriggerType(Gpio.EDGE_RISING);
                mMode[i].registerGpioCallback(mGpioCallback);
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }
        colorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                Colors = new LedColors[(int)dataSnapshot.getChildrenCount()+1];

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    LedColors color = postSnapshot.getValue(LedColors.class);
                    Log.d("color","color = "+ Color.rgb(color.r, color.g, color.b));
                    color.color = Color.rgb(color.r, color.g, color.b);
                    Colors[Integer.parseInt(postSnapshot.getKey())] = color;
                }
//
//
//                for(int i=6;i<=6;i++) {
//                    for(int j=1;j<=7;j++) {
//                        switch (j) {
//                            case 1 :
//                                mLedColors[(i-1)*7+j] = Colors[0].color;
//                                break;
//                            case 2:
//                                mLedColors[(i-1)*7+j] = Colors[1].color;
//                                break;
//                            case 3:
//                                mLedColors[(i-1)*7+j] = Colors[2].color;
//                                break;
//                            case 4:
//                                mLedColors[(i-1)*7+j] = Colors[3].color;
//                                break;
//                            case 5:
//                                mLedColors[(i-1)*7+j] = Colors[4].color;
//                                break;
//                            case 6:
//                                mLedColors[(i-1)*7+j] = Colors[5].color;
//                                break;
//                            case 7:
//                                mLedColors[(i-1)*7+j] = Colors[4].color;
//                                break;
//                        }
//                    }
//                }

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


                dataRef.child(parterkey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       parterData.dates = new Dates[(int)dataSnapshot.child("dates").getChildrenCount()];

                        for(DataSnapshot date: dataSnapshot.child("dates").getChildren()) {
                            Log.e(TAG,date.toString());
                            int key = Integer.parseInt(date.getKey());
                            Log.e(TAG,date.child("color").getValue(Integer.class).toString());
                            parterData.dates[key] = new Dates();
                            parterData.dates[key].color = date.child("color").getValue(Integer.class);
                            parterData.dates[key].date = date.child("date").getValue(Integer.class);
                            parterData.dates[key].month = date.child("month").getValue(Integer.class);
                            parterData.dates[key].colors = new int[(int)date.child("colors").getChildrenCount()+1];
                            for(DataSnapshot color: date.child("colors").getChildren()) {
                                parterData.dates[key].colors[Integer.parseInt(color.getKey())] = color.getValue(Integer.class);
                            }
                        }
                        parterData.mode = dataSnapshot.child("mode").getValue(Integer.class);
                        parterData.name = dataSnapshot.child("name").getValue(String.class);

                        Log.d(TAG,parterData.dates[0].date + " "+ parterData.dates[0].color );
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });

                dataRef.child(userkey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e(TAG,dataSnapshot.toString());
                        userData.dates = new Dates[(int)dataSnapshot.child("dates").getChildrenCount()];

                        for(DataSnapshot date: dataSnapshot.child("dates").getChildren()) {
                            Log.e(TAG,date.toString());
                            int key = Integer.parseInt(date.getKey());
                            Log.e(TAG,date.child("color").getValue(Integer.class).toString());
                            userData.dates[key] = new Dates();
                            userData.dates[key].color = date.child("color").getValue(Integer.class);
                            userData.dates[key].date = date.child("date").getValue(Integer.class);
                            userData.dates[key].month = date.child("month").getValue(Integer.class);
                            userData.dates[key].colors = new int[(int)date.child("colors").getChildrenCount()+1];
                            for(DataSnapshot color: date.child("colors").getChildren()) {
                                userData.dates[key].colors[Integer.parseInt(color.getKey())] = color.getValue(Integer.class);
                            }
                        }
                        userData.mode = dataSnapshot.child("mode").getValue(Integer.class);
                        userData.name = dataSnapshot.child("name").getValue(String.class);

                        Log.d(TAG,userData.dates[0].date + " "+ userData.dates[0].color );
                        setLedColors();
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
            for (int i = 0; i < 2; i++) {
                mMode[i].unregisterGpioCallback(mGpioCallback);
                mMode[i].close();
            }
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

    private void setLedColors() {

        try {
            Log.d(TAG, "GPIO1 " + mMode[0].getValue() + " GPIO2 " + mMode[1].getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "set color");
        mLedColors = new int[NUM_LEDS];
        Log.d(TAG,String.valueOf(userData.mode));
        if(userData.mode == 1 ) {
            for(int i=0;i<userData.dates.length;i++) {
                mLedColors[dateToIndex(userData.dates[i].date)] = Colors[userData.dates[i].color].color;
                if(userData.dates[i].date == userData.dates.length) {
                    for(int j=0;j<userData.dates[i].colors.length;j++) {
                        Log.d(TAG,userData.dates[i].colors[j]+":");
                        if(userData.dates[i].colors[j] == 0) {
                            mLedColors[todayConst+j] = 0;
                        }
                        else {
                            mLedColors[todayConst+j] = Colors[userData.dates[i].colors[j]].color;
                        }
                    }
                }
            }
        } else {
            for(int i=0;i<parterData.dates.length;i++) {
                mLedColors[dateToIndex(parterData.dates[i].date)] = Colors[parterData.dates[i].color].color;
                if(parterData.dates[i].date == parterData.dates.length) {
                    for(int j=0;j<parterData.dates[i].colors.length;j++) {
                        if(parterData.dates[i].colors[j] == 0) {
                            mLedColors[todayConst+j] = 0;
                        }
                        else {
                            mLedColors[todayConst+j] = Colors[parterData.dates[i].colors[j]].color;
                        }
                    }
                }
            }
        }
        try {
            mLedstrip.write(mLedColors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int dateToIndex(int date) {
        if(((date-1)/7)%2==0) {
            return date;
        } else {
            final int dateconst[] = {6,4,2,0,-2,-4,-6};
            return date+dateconst[(date-1)%7];
        }
    }
}