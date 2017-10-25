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


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.UserHolder> {

    private List<User> list;
    private LayoutInflater inflater;
    private ItemClickInterface itemClickInterface;
    private String callingFrom;

    public FriendsAdapter(Context context,List<User> list,ItemClickInterface itemClickInterface,String callingFrom) {

        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.itemClickInterface = itemClickInterface;
        this.callingFrom = callingFrom;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.single_friend_layout,parent,false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {

        User user = list.get(position);
        holder.nameTextView.setText(user.getFirstName()+" "+user.getLastName());
        Bitmap bitmap = decodeBase64(user.getImageUrl());
        if(bitmap!=null) {
            holder.userImageView.setImageBitmap(bitmap);
        } else {
            holder.userImageView.setImageResource(R.drawable.ic_person_black_18dp);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView userImageView;
        private TextView nameTextView;
        private Button viewTripsButton;
        private View itemContainer;

        public UserHolder(View itemView) {

            super(itemView);
            userImageView = (ImageView) itemView.findViewById(R.id.friendImageView);
            nameTextView = (TextView) itemView.findViewById(R.id.friendNameTextView);
            viewTripsButton = (Button) itemView.findViewById(R.id.viewTripsButton);
            if(callingFrom.equals("AddFriendsActivity")) {
                viewTripsButton.setText(R.string.add_as_friend);
            }
            viewTripsButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.viewTripsButton:
                    User user = list.get(getAdapterPosition());
                    if(callingFrom.equals("MainActivity")) {
                        itemClickInterface.onViewTripsButtonClick(user);
                    } else if (callingFrom.equals("AddFriendsActivity")){
                        itemClickInterface.onAddFriendsButtonClick(user);
                    } else if (callingFrom.equals("FriendRequestActivity")) {

                    }
                    break;
                default:
                    break;
            }

        }
    }

    public Bitmap decodeBase64(String input) {

        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
    }

    public interface ItemClickInterface extends Serializable {
        void onViewTripsButtonClick(User user);
        void onAddFriendsButtonClick(User user);
    }
}

