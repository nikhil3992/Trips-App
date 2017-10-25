package com.example.nikhil.group22_hw09;

/**
 *
 * File name - FriendsAdapter.java
 * Full Name - Nikhil Jonnalagadda
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.List;


public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.UserHolder> {

    private List<Trip> list;
    private LayoutInflater inflater;
    private ItemClickInterface itemClickInterface;
    private String uid;


    public TripsAdapter(Context context,List<Trip> list,ItemClickInterface itemClickInterface,String uid) {

        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.itemClickInterface = itemClickInterface;
        this.uid = uid;

    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.single_trip_layout,parent,false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserHolder holder, int position) {

        final Trip trip = list.get(position);
        holder.titleTextView.setText(trip.getTitle());
        holder.locationTextView.setText(trip.getLocation());
        Bitmap bitmap = decodeBase64(trip.getImageUrl());
        if (bitmap!=null) {
            holder.tripImageView.setImageBitmap(bitmap);
        } else {
            holder.tripImageView.setImageResource(R.drawable.ic_drive_eta_black_18dp);
        }

        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference().child("userTrips").child(uid);
        tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getValue(String.class);
                    if(uid.equals(trip.getTripID())) {
                        holder.joinTripButton.setText(R.string.chat_room);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView tripImageView;
        private TextView titleTextView;
        private TextView locationTextView;

        private Button joinTripButton;
        private View itemContainer;

        public UserHolder(View itemView) {

            super(itemView);
            tripImageView = (ImageView) itemView.findViewById(R.id.tripImageView);
            titleTextView = (TextView) itemView.findViewById(R.id.friendNameTextView);
            locationTextView = (TextView) itemView.findViewById(R.id.locationTextView);
            itemContainer = itemView.findViewById(R.id.itemContainer);
            itemContainer.setOnClickListener(this);

            joinTripButton = (Button) itemView.findViewById(R.id.joinTripButton);
            joinTripButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.itemContainer:
                    Trip trip1 = list.get(getAdapterPosition());
                    itemClickInterface.onTripClick(trip1);
                    break;
                case R.id.joinTripButton:
                    Trip trip = list.get(getAdapterPosition());
                    itemClickInterface.onJoinTripButtonClick(trip,getAdapterPosition());
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
        void onJoinTripButtonClick(Trip trip,int pos);
        void onTripClick(Trip trip);
    }
}

