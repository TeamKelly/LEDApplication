package com.teamkelly.kelly;


import android.graphics.Color;
import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by himsun on 2017. 10. 20..
 */
@IgnoreExtraProperties
public class LedColors {
    public int r;
    public int g;
    public int b;
    public int color;

    public LedColors() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public LedColors(int r,int g,int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.color = Color.rgb(r, g, b);
    }
}
