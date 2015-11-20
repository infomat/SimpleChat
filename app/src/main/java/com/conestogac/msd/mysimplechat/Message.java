package com.conestogac.msd.mysimplechat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by CHANGHO on 2015-11-16.
 * To save message data at ListView
 * Need to register before init
 */
@ParseClassName("ChatMessages")
public class Message extends ParseObject {
    public String getUserId() {
        return getString("Id");
    }

    public String getBody() {
        return getString("MessageText");
    }

    public Boolean getIsPrivate(){
        return getBoolean(("Private"));
    }

    public String getToId(){
        return getString(("To"));
    }

    public void setUserId(String userId) {
        put("Id", userId);
    }

    public void setBody(String body) {
        put("MessageText", body);
    }

    public void setPrivate(Boolean isPrivate) {
        put("Private", isPrivate);
    }

    public void setToId(String toUserId) {
        put("To", toUserId);
    }
}
