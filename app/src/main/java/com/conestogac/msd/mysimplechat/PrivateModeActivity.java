package com.conestogac.msd.mysimplechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class PrivateModeActivity extends Activity {
    private static final String TAG = "PrivateModeActivity";
    private static String sUserId;
    private static String sPrivateToUserId;
    private boolean mFirstLoad;
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 500;
    // Create a handler which can run code periodically
    private Handler handler = new Handler();

    private static final String USER_ID_KEY = "Id";
    private static final String TO_KEY = "To";
    private static final String PRIVATE_KEY = "Private";
    private boolean isRefreshed;

    private EditText etMessage;
    private Button btSend;
    private ListView lvChat;
    private ArrayList<Message> mMessages;
    private ChatListAdapter mAdapter;
    private List<String> mNameList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        ParseUser user;
        sUserId= intent.getStringExtra("UserID");
        sPrivateToUserId = intent.getStringExtra("PrivateToUserId");
        mNameList.add(sUserId);
        mNameList.add(sPrivateToUserId);
        ParseQuery<ParseUser> query = ParseUser.getQuery().whereContainedIn(USER_ID_KEY, mNameList);
        setupMessagePosting();

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

    // Setup message field and posting
    private void setupMessagePosting() {
        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (Button) findViewById(R.id.btSend);
        lvChat = (ListView) findViewById(R.id.lvChat);
        mMessages = new ArrayList<Message>();
        // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
        lvChat.setTranscriptMode(1);
        mFirstLoad = true;
        mAdapter = new ChatListAdapter(PrivateModeActivity.this, sUserId, mMessages);
        lvChat.setAdapter(mAdapter);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = etMessage.getText().toString();
                // Use Message model to create new messages now
                Message message = new Message();
                message.setUserId(sUserId);
                message.setToId(sPrivateToUserId);
                message.setBody(body);
                message.setPrivate(true);
                ParseQuery<ParseUser> query = ParseUser.getQuery().whereContains(USER_ID_KEY,sPrivateToUserId);

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
        //query (id=me and to=sPrivateToUserId) or  (id=sPrivateToUserId and to=id)
        query.whereEqualTo(PRIVATE_KEY, true);
        query.whereContainedIn(USER_ID_KEY, mNameList);
        query.whereContainedIn(TO_KEY, mNameList);

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
}
