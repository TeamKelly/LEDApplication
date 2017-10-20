package com.teamkelly.kelly;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by himsun on 2017. 10. 20..
 */

@IgnoreExtraProperties
public class Users {
        public int mode;
        public String name;
        public Dates[] dates;

    public Users() {
        mode = 0;
        name = "";
        dates = new Dates[35];
    }

    public Users(String name, int mode, Dates[] dates) {
        this.name = name;
        this.mode = mode;
        this.dates = dates;
    }
}