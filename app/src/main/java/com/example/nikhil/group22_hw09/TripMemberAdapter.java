package com.example.nikhil.group22_hw09;

/**
 *
 * File name - TripMemberAdapter.java
 * Full Name - Nikhil Jonnalagadda
 *
 *
 * **/
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
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


public class TripMemberAdapter extends RecyclerView.Adapter<TripMemberAdapter.UserHolder> {

    private List<User> list;
    private LayoutInflater inflater;
    private ItemClickInterface itemClickInterface;
    private boolean isOwner;

    public TripMemberAdapter(Context context,List<User> list,ItemClickInterface itemClickInterface,boolean isOwner) {

        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.itemClickInterface = itemClickInterface;
        this.isOwner = isOwner;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.trip_members_layout,parent,false);
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
        if(!isOwner) {
            holder.viewTripsButton.setVisibility(View.INVISIBLE);
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
            userImageView = (ImageView) itemView.findViewById(R.id.memberImageView);
            nameTextView = (TextView) itemView.findViewById(R.id.memberNameTextView);
            viewTripsButton = (Button) itemView.findViewById(R.id.removeMemberButton);
            viewTripsButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.removeMemberButton:
                    User user = list.get(getAdapterPosition());
                    itemClickInterface.onRemoveMemberButtonClick(user,getAdapterPosition());
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
        void onRemoveMemberButtonClick(User user,int pos);
    }
}

