package com.conestogac.msd.mysimplechat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by CHANGHO on 2015-11-16.
 * To display message as a list
 * Gravatar site is used with userID hashing
 * Picasso library is used to download image from Gravatar
 */
public class ChatListAdapter extends ArrayAdapter<Message> {
    public String mUserId;
    Map<String, Integer> colorTable = new HashMap<String, Integer>();

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
            holder.id = (TextView)convertView.findViewById(R.id.tvProfileLeft);
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
            holder.id.setTextColor(Color.WHITE);
            holder.id.setBackgroundResource(R.color.myid);
            holder.id.setText("ME:\n" + message.getUserId());
        } else {
            if (message.getUserId().equals("")) {
                holder.id.setTextColor(Color.WHITE);
                holder.id.setBackgroundResource(R.color.anonymous);
                holder.id.setText("Anonymous");
            } else {
                int colorCode = getColorId(message.getUserId());
                int  yiq = ((Color.red(colorCode)*299)+(Color.green(colorCode)*587)+(Color.red(colorCode)*114))/1000;
                //to make text recognizable, set text color according to background color contrast
                if (yiq >= 128) {
                    holder.id.setTextColor(Color.BLACK);
                } else {
                    holder.id.setTextColor(Color.WHITE);
                }
                holder.id.setBackgroundColor(colorCode);
                holder.id.setText(message.getUserId());
            }
        }

        holder.body.setText(message.getBody());
        holder.body.setSelected(true);
        holder.body.requestFocus();
        return convertView;
    }


    final class ViewHolder {
        public TextView id;
        public TextView body;
    }

    private int getColorId(String userId){
        if (!colorTable.containsKey(userId))
            colorTable.put(userId,generateRandomColor());
        return colorTable.get(userId);
    }

    private int generateRandomColor(){
        Random rand = new Random();

        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        return Color.rgb(r, g, b);
    }


}