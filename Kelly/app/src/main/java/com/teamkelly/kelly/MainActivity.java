package com.teamkelly.kelly;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by himsun on 2017. 9. 3..
 */

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
