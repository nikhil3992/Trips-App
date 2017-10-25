package com.example.nikhil.group22_hw09;

import android.os.Parcel;
import android.os.Parcelable;

public class Trip implements Parcelable {

    private String title,location,imageUrl,tripID,ownerID;

    public Trip() {}


    public Trip(String title, String location, String imageUrl) {
        this.title = title;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    protected Trip(Parcel in) {
        title = in.readString();
        location = in.readString();
        imageUrl = in.readString();
        tripID = in.readString();
        ownerID = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(location);
        dest.writeString(imageUrl);
        dest.writeString(tripID);
        dest.writeString(ownerID);
    }
}
