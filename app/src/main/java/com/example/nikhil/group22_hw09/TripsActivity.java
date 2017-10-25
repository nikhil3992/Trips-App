package com.example.nikhil.group22_hw09;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.nikhil.group22_hw09.SignUpActivity.REQUEST_WRITE_PERMISSION;
import static com.example.nikhil.group22_hw09.SignUpActivity.SELECTED_PICTURE;

public class TripsActivity extends AppCompatActivity implements View.OnClickListener,TripsAdapter.ItemClickInterface {

    RecyclerView recyclerView;
    Button addTripsButton;
    TextView noTripsTextView;
    String uid,userID;
    DatabaseReference rootReference;
    List<Trip> tripsList;
    Bitmap coverPhotoBitmap;
    Uri uploadImageUri;
    TripsAdapter tripsAdapter;
    List<String> tripUIDList;
    List<String> friendsUIDList;
    boolean entered;
    final int PLACE_PICKER_REQUEST = 10;
    TextView locationTextView;
    Place newPlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        setTitle("My Trips");
        Intent intent = getIntent();
        noTripsTextView = (TextView) findViewById(R.id.noTripsTextView);
        addTripsButton = (Button) findViewById(R.id.addTripsButton);
        if(intent.getBooleanExtra("particularTrip",false)) {
            User user = intent.getParcelableExtra("user");
            userID = intent.getStringExtra("uid");
            uid = user.getUid();
            addTripsButton.setVisibility(View.INVISIBLE);
            noTripsTextView.setText(R.string.no_trips);

        } else {
            uid = intent.getStringExtra("uid");
            userID = intent.getStringExtra("uid");
        }
        initializeViews();



    }

    private void initializeViews() {

        tripsList = new ArrayList<>();
        tripUIDList = new ArrayList<>();
        friendsUIDList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        tripsAdapter = new TripsAdapter(TripsActivity.this,tripsList,TripsActivity.this,uid);
        recyclerView.setAdapter(tripsAdapter);
        tripsAdapter.notifyDataSetChanged();


        addTripsButton = (Button) findViewById(R.id.addTripsButton);
        addTripsButton.setOnClickListener(this);

        rootReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference tripsRef = rootReference.child("userTrips").child(uid);
        tripsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tripUIDList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getValue(String.class);
                    tripUIDList.add(uid);
                }
                Log.d("nikhil","tripUID list us "+tripUIDList.toString());
               // getTripsFromUIDs(tripUIDList);
                DatabaseReference ref = rootReference.child("friends").child(uid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for(DataSnapshot snap : dataSnapshot.getChildren()) {
                            String uid = snap.getValue(String.class);
                            friendsUIDList.add(uid);
                        }
                        Log.d("nikhil","friends uid list to string is "+friendsUIDList.toString());

                        for (final String id : friendsUIDList) {
                            DatabaseReference tripsRef = rootReference.child("userTrips").child(id);
                            tripsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String uid = snapshot.getValue(String.class);
                                        if(!tripUIDList.contains(uid))
                                            tripUIDList.add(uid);
                                        Log.d("nikhil","trips uid list given is "+tripUIDList.toString());
                                    }
                                    if(id.equals(friendsUIDList.get(friendsUIDList.size()-1))) {
                                        getTripsFromUIDs(tripUIDList);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getTripsFromUIDs(List<String> tripUIDList) {

        tripsList.clear();
        for(String uid: tripUIDList) {
            DatabaseReference tripRef = rootReference.child("trips").child(uid);
            tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    tripsList.add(trip);
                    Log.d("nikhil","trip list is "+tripsList.toString());
                    recyclerView.setVisibility(View.VISIBLE);
                    noTripsTextView.setVisibility(View.INVISIBLE);
                    tripsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addTripsButton:
                handleAddTripsButtonClick();
                break;
            default:
                break;
        }
    }

    private void handleAddTripsButtonClick() {

        View view = getLayoutInflater().inflate(R.layout.add_trips_layout,null,false);
        final EditText titleEditText = (EditText) view.findViewById(R.id.titleEditText);
     //   final EditText locationEditText = (EditText) view.findViewById(R.id.locationEditText);
        locationTextView = (TextView) view.findViewById(R.id.locationTextView);
        Button setLocationButton = (Button) view.findViewById(R.id.setLocationButton);
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSetLocationButtonClick();
            }
        });
        Button addCoverPhotoButton = (Button) view.findViewById(R.id.addCoverPhotoButton);
        addCoverPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(TripsActivity.this);
        builder.setTitle("Create Trip").setView(view).setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(titleEditText.getText().toString().isEmpty() || locationTextView.getText().toString().equals("Location not set")) {
                    Toast.makeText(TripsActivity.this,"Title and Location cannot be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                Trip trip = new Trip(titleEditText.getText().toString(),locationTextView.getText().toString(),encodeTobase64(coverPhotoBitmap));
                trip.setOwnerID(uid);
                DatabaseReference tripRef = rootReference.child("trips").push();
                String key = tripRef.getKey();
                trip.setTripID(key);
                tripRef.setValue(trip);

                tripsList.add(trip);
                tripsAdapter.notifyDataSetChanged();

                DatabaseReference tripMembers = rootReference.child("trips").child(key).child("members").child(uid);
                tripMembers.setValue(uid);

                DatabaseReference userTrips = rootReference.child("userTrips").child(uid).child(key);
                userTrips.setValue(key);


                DatabaseReference ref = rootReference.child("trips").child(key).child("locationNames").push();
                String key1 = ref.getKey();
                ref.setValue(newPlace.getAddress().toString());

                DatabaseReference latRef = rootReference.child("trips").child(key).child("LatLng").child(key1);
                String latLong = newPlace.getLatLng().latitude+""+","+newPlace.getLatLng().longitude;
                latRef.setValue(latLong);


            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    private void handleSetLocationButtonClick() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(TripsActivity.this);
            startActivityForResult(intent,PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }


    private void handleNewLocationAdded(Place place) {
        newPlace = place;

    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);

        } else {
            Intent takepicture = new Intent(Intent.ACTION_PICK);
            takepicture.setType("image/*");
            startActivityForResult(takepicture, SELECTED_PICTURE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "picture"), SELECTED_PICTURE);

        }
    }

    public String encodeTobase64(Bitmap image) {

        if(image != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 90, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);
        }
        return "";

    }

    public Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTED_PICTURE:
                if (resultCode == RESULT_OK) {
                    uploadImageUri = data.getData();
                    Log.d("nikhil","upload image uri is "+ uploadImageUri.toString());
                    try {
                        coverPhotoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uploadImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case PLACE_PICKER_REQUEST:
                if(resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this,data);
                    String address = place.getAddress().toString();
                    String name = place.getName().toString();
                    String latlng = place.getLatLng().toString();
                    Log.d("nikhil","address is "+address+" name is "+name+" latlng is "+latlng);
                    locationTextView.setText(place.getAddress());
                    handleNewLocationAdded(place);
                    //   getLocationFromAddress(this,address);
                }

        }
    }

    @Override
    public void onJoinTripButtonClick(final Trip trip, final int pos) {
        DatabaseReference tripDetailsRef = rootReference.child("trips").child(trip.getTripID()).child("members");
        entered = false;
        tripDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    String uid1 = snap.getValue(String.class);
                    if(uid1.equals(userID)) {
                        entered = true;
                        break;
                    }
                }
                if(entered) {
                    //Chat room
                    Intent in = new Intent(TripsActivity.this,ChatActivity2.class);
                    in.putExtra("uid",userID);
                    in.putExtra("tripID",trip.getTripID());
                    startActivity(in);

                } else {
                    Log.d("nikhil","trip member added tp trip id "+trip.getTripID());
                    Toast.makeText(TripsActivity.this,"You joined the trip: "+trip.getTitle(),Toast.LENGTH_LONG).show();
                    DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference().child("userTrips").child(uid).child(trip.getTripID());
                    tripRef.setValue(trip.getTripID());

                    DatabaseReference tripDetailsRef = rootReference.child("trips").child(trip.getTripID()).child("members").child(uid);
                    tripDetailsRef.setValue(uid);
                    Trip trip1 = new Trip();
                    trip1 = trip;
                    tripsList.remove(pos);
                    tripsList.add(pos,trip1);
                    tripsAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.updateProfile:
                Intent intent1 = new Intent(TripsActivity.this,SignUpActivity.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.trips:
                Intent tripsIntent = new Intent(TripsActivity.this,TripsActivity.class);
                tripsIntent.putExtra("particularTrip",false);
                tripsIntent.putExtra("uid",uid);
                startActivity(tripsIntent);
                break;
            case R.id.friendRequests:
                Intent intent = new Intent(TripsActivity.this,FriendRequestActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.logout:
                Intent intent2 = new Intent(TripsActivity.this,LoginActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish();
                break;
            default:
                break;

        }
        return true;
    }

    @Override
    public void onTripClick(Trip trip) {

        Intent intent = new Intent(TripsActivity.this,TripDetailsActivity.class);
        intent.putExtra("trip",trip);
        intent.putExtra("uid",userID);
        startActivity(intent);
    }
}
