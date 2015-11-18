package com.conestogac.msd.mysimplechat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by CHANGHO on 2015-11-16.
 * To save message data at ListView
 * Need to register before init
 */
@ParseClassName("Message")
public class Message extends ParseObject {
    public String getUserId() {
        return getString("userId");
    }

    public String getBody() {
        return getString("body");
    }

    public void setUserId(String userId) {
        put("userId", userId);
    }

    public void setBody(String body) {
        put("body", body);
    }
}
