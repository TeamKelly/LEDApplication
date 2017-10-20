package com.teamkelly.kelly;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by himsun on 2017. 10. 20..
 */
@IgnoreExtraProperties
public class Dates {
    public int date = 0;
    public int month = 0;
    public int color = 0;
    public int colors[];


    public Dates() {
        // Default constructor required for calls to DataSnapshot.getValue(Dates.class)
        colors = new int[7];
        date = 0;
        month = 0;
        color = 0;
    }

    public Dates(int date,int month,int color) {
        this.date = date;
        this.month = month;
        this.color = color;
    }
}
