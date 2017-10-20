package com.teamkelly.kelly;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by himsun on 2017. 10. 20..
 */
@IgnoreExtraProperties
public class ConnectUser {
    public String key;
    public String name;

    public ConnectUser() {
    }

    public ConnectUser(String key, String name) {
        this.key = key;
        this.name = name;
    }
}
