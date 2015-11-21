/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.conestogac.msd.mysimplechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {
  public static final String PREFS_NAME = "msd.conestogac.mysimplechat.MyPrefsFile";

  private static final String TAG = "ChatActivity";
  private static String sUserId;
  private static String sPrivateToUserId;
  private static final String USER_ID_KEY = "Id";
  private static final String TO_KEY = "To";
  private static final String PRIVATE_KEY = "Private";
  private boolean isRefreshed;
  private EditText etMessage;
  private Button btSend;
  private ListView lvChat;
  private ArrayList<Message> mMessages;
  private ChatListAdapter mAdapter;
  // Keep track of initial load to scroll to the bottom of the ListView
  private boolean mFirstLoad;
  private static final int MAX_CHAT_MESSAGES_TO_SHOW = 500;

  // Create a handler which can run code periodically
  private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

    this.restorePreferences();
    if (sUserId == ""){
      GetUserID();
    }

    // User login
    if (ParseUser.getCurrentUser() != null) { // start with existing user
      startWithCurrentUser();
    } else { // If not logged in, login as a new anonymous user
      login();
    }
      isRefreshed = true;
      // Run the runnable object defined every 100ms
      handler.postDelayed(runnable, 1000);

  }
  // Defines a runnable which is run every 100ms
  private Runnable runnable = new Runnable() {
    @Override
    public void run() {
      if (isRefreshed == true) {
        isRefreshed = false;
        refreshMessages();
      }
      handler.postDelayed(this, 1000);
    }
  };

  private void refreshMessages() {
    receiveMessage();
  }


  // Create an anonymous user using ParseAnonymousUtils and set sUserId
  private void login() {
    ParseAnonymousUtils.logIn(new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException e) {
        if (e != null) {
          Log.d(TAG, "Anonymous login failed: " + e.toString());
        } else {
          startWithCurrentUser();
        }
      }
    });
  }

  // Get the userId from the cached currentUser object
  private void startWithCurrentUser() {
   //Get sUserId from user  sUserId = ParseUser.getCurrentUser().getObjectId();
    setupMessagePosting();
  }

  // Setup message field and posting
  private void setupMessagePosting() {
    etMessage = (EditText) findViewById(R.id.etMessage);
    btSend = (Button) findViewById(R.id.btSend);
    lvChat = (ListView) findViewById(R.id.lvChat);
    mMessages = new ArrayList<Message>();
    // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
    lvChat.setTranscriptMode(1);
    mFirstLoad = true;
    mAdapter = new ChatListAdapter(ChatActivity.this, sUserId, mMessages);
    lvChat.setAdapter(mAdapter);

    lvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Postion: "+position);
        Log.d(TAG, "UserID: "+mMessages.get(position).getUserId());
        Log.d(TAG, "Message: " + mMessages.get(position).getBody());
        if (mMessages.get(position).getUserId() != null && !mMessages.get(position).getUserId().equals(sUserId)) {
          sPrivateToUserId = mMessages.get(position).getUserId();
          gotoPrivateMode();
        }
      }
    });

    btSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String body = etMessage.getText().toString();
        // Use Message model to create new messages now
        Message message = new Message();
        message.setUserId(sUserId);
        message.setBody(body);
        message.setPrivate(false);
        message.saveInBackground(new SaveCallback() {
          @Override
          public void done(ParseException e) {
            receiveMessage();
          }
        });
        etMessage.setText("");
      }
    });
  }

  // Query messages from Parse so we can load them into the chat adapter
  private void receiveMessage() {
    int rowCount;
    // Construct query to execute
    ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
    query.whereEqualTo(PRIVATE_KEY, false);
    // Configure limit and sort order
    query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
    query.orderByDescending("createdAt");

    // Execute query to fetch all messages from Parse asynchronously
    // This is equivalent to a SELECT query with SQL
    query.findInBackground(new FindCallback<Message>() {
      public void done(List<Message> messages, ParseException e) {
        if (e == null) {
          mMessages.clear();

          // Iterate in reverse.
          for (int index = messages.size()-1; index >=0; index--){
            mMessages.add(messages.get(index));
          }

          mAdapter.notifyDataSetChanged(); // update adapter
          // Scroll to the bottom of the list on initial load
          if (mFirstLoad) {
            lvChat.setSelection(mAdapter.getCount() - 1);
            mFirstLoad = false;
          }
          isRefreshed = true;
        } else {
          Log.d("message", "Error: " + e.getMessage());
        }
      }
    });
  }

  private void restorePreferences() {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    sUserId = settings.getString(USER_ID_KEY, "");
  }
  private void savePreferences() {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();

    editor.putString(USER_ID_KEY, sUserId);

    // Commit the edits!
    editor.commit();
    Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
  }

  private void GetUserID() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.getUserId);

    // Set up the input
    final EditText input = new EditText(this);

    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);

    // Set up the buttons
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        sUserId = input.getText().toString();

        //todo update member variable of array adapter
        mAdapter.mUserId = sUserId;

        savePreferences();
        Toast.makeText(getApplicationContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show();
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }

  private void gotoPrivateMode() {
    AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
    newDialog.setTitle(R.string.titlePrivateMode);
    newDialog.setMessage(getString(R.string.noticePrivateMode) +" "+sPrivateToUserId +" messages");
    newDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();

        Intent intent = new Intent(getApplicationContext(), PrivateModeActivity.class);
        intent.putExtra("UserID", sUserId);
        intent.putExtra("PrivateToUserId", sPrivateToUserId);
        startActivity(intent);
      }
    });
    newDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });
    newDialog.show();
  }
}
