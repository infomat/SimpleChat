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

  private static final String TAG = ChatActivity.class.getName();
  private static String sUserId;
  private static String sChatroomId;
  public static final String USER_ID_KEY = "Id";

  private EditText etMessage;
  private Button btSend;
  private ListView lvChat;
  private ArrayList<Message> mMessages;
  private ChatListAdapter mAdapter;
  // Keep track of initial load to scroll to the bottom of the ListView
  private boolean mFirstLoad;
  private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

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
      // Run the runnable object defined every 100ms
      handler.postDelayed(runnable, 100);
  }
  // Defines a runnable which is run every 100ms
  private Runnable runnable = new Runnable() {
    @Override
    public void run() {
      refreshMessages();
      handler.postDelayed(this, 2000);
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
        Log.d(TAG, "Message: "+mMessages.get(position).getBody());
        GetChatRoomID();
/*
        Intent i = new Intent(More.this, NextActvity.class);
        //If you wanna send any data to nextActicity.class you can use
        i.putExtra(String key, value.get(position));

        startActivity(i);*/
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
    // Construct query to execute
    ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
    //Todo
    //query.whereContains(USER_ID_KEY, "Changho");

    // Configure limit and sort order
    query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
    query.orderByAscending("createdAt");
    // Execute query to fetch all messages from Parse asynchronously
    // This is equivalent to a SELECT query with SQL
    query.findInBackground(new FindCallback<Message>() {
      public void done(List<Message> messages, ParseException e) {
        if (e == null) {
          mMessages.clear();
          mMessages.addAll(messages);
          mAdapter.notifyDataSetChanged(); // update adapter
          // Scroll to the bottom of the list on initial load
          if (mFirstLoad) {
            lvChat.setSelection(mAdapter.getCount() - 1);
            mFirstLoad = false;
          }
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

  private void GetChatRoomID() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.getChatroomId);

    // Set up the input
    final EditText input = new EditText(this);

    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);

    // Set up the buttons
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        sChatroomId = input.getText().toString();

        Toast.makeText(getApplicationContext(), getString(R.string.movetoprivate), Toast.LENGTH_SHORT).show();
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
}
