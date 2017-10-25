package com.example.nikhil.group22_hw09;

/**
 *
 * File name - FriendsAdapter.java
 * Full Name - Nikhil Jonnalagadda
 *
 *
 * **/
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;


public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.UserHolder> {

    private List<User> list;
    private LayoutInflater inflater;
    private ItemClickInterface itemClickInterface;


    public FriendRequestAdapter(Context context,List<User> list,ItemClickInterface itemClickInterface) {

        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.itemClickInterface = itemClickInterface;

    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.friend_request_layout,parent,false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {

        User user = list.get(position);
        holder.nameTextView.setText(user.getFirstName()+" "+user.getLastName()+" sent you a friend request");
        Bitmap bitmap = decodeBase64(user.getImageUrl());
        if(bitmap!=null)
            holder.userImageView.setImageBitmap(bitmap);
        else
            holder.userImageView.setImageResource(R.drawable.ic_person_black_18dp);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView userImageView;
        private TextView nameTextView;
        private Button acceptButton,declineButton;
        private View itemContainer;

        public UserHolder(View itemView) {

            super(itemView);
            userImageView = (ImageView) itemView.findViewById(R.id.friendImageView);
            nameTextView = (TextView) itemView.findViewById(R.id.friendNameTextView);
            acceptButton = (Button) itemView.findViewById(R.id.acceptButton);
            declineButton = (Button) itemView.findViewById(R.id.declineButton);
            acceptButton.setOnClickListener(this);
            declineButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.acceptButton:
                    User user = list.get(getAdapterPosition());
                    itemClickInterface.onAcceptButtonClick(user);
                    break;
                case R.id.declineButton:
                    User user1 = list.get(getAdapterPosition());
                    itemClickInterface.onDeclineButtonClick(user1);
                    break;
                default:
                    break;
            }

        }
    }

    public Bitmap decodeBase64(String input) {

        if(input.isEmpty()) {
            return null;
        }
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
    }

    public interface ItemClickInterface extends Serializable {
        void onAcceptButtonClick(User user);
        void onDeclineButtonClick(User user);
    }
}

