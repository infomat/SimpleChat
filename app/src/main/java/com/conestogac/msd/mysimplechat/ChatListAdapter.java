package com.conestogac.msd.mysimplechat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by CHANGHO on 2015-11-16.
 * To display message as a list
 * Gravatar site is used with userID hashing
 * Picasso library is used to download image from Gravatar
 */
public class ChatListAdapter extends ArrayAdapter<Message> {
    public String mUserId;

    public ChatListAdapter(Context context, String userId, List<Message> messages) {
        super(context, 0, messages);
        this.mUserId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.chat_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.idOther = (TextView)convertView.findViewById(R.id.tvProfileLeft);
            holder.idMe = (TextView)convertView.findViewById(R.id.tvProfileRight);
            holder.body = (TextView)convertView.findViewById(R.id.tvBody);
            convertView.setTag(holder);
        }
        final Message message = (Message)getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        boolean isMe;
        // Show textid left or right based on the logged-in user.
        // Display the profile image to the right for our user, left for other users.
        try {
            isMe = message.getUserId().equals(mUserId);
        } catch (NullPointerException e) {
            isMe = false;
        }
        if (isMe) {
            holder.idMe.setVisibility(View.VISIBLE);
            holder.idOther.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            holder.idOther.setVisibility(View.VISIBLE);
            holder.idMe.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }
        final TextView profileView = isMe ? holder.idMe : holder.idOther;
        profileView.setText(message.getUserId());
        holder.body.setText(message.getBody());

        return convertView;
    }


    final class ViewHolder {
        public TextView idOther;
        public TextView idMe;
        public TextView body;
    }

}