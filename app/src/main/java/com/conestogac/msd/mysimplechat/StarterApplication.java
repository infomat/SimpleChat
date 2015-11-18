/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.conestogac.msd.mysimplechat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class StarterApplication extends Application {
  public static final String YOUR_APPLICATION_ID = "iet4XKXX6ZU5kSAmJkOeYe9v304eL8LDZRB1xhQL";
  public static final String YOUR_CLIENT_KEY = "NWcLxtQjy9rneD1Dv5XTwanUYcgTwz6d9m6zDpBh";
  @Override
  public void onCreate() {
    super.onCreate();

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);
    // Add parse model
    ParseObject.registerSubclass(Message.class);
    // Add your initialization code here
    Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
    ParseInstallation.getCurrentInstallation().saveInBackground();
    ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);
  }
}
