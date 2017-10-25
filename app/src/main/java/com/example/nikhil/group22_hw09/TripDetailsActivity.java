package com.example.nikhil.group22_hw09;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity implements View.OnClickListener, TripMemberAdapter.ItemClickInterface {

    ImageView tripImageView;
    TextView tripNameTextView,tripLocationTextView;
    RecyclerView tripMembersRecyclerView;
    Button removeTripButton,addMembersButton,addLocationsButton,deleteLocationsButton,seeRoundTripButton;
    String uid;
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    TripMemberAdapter tripMemberAdapter;
    ArrayList<String> memberUIDList;
    ArrayList<User> memberList;
    String tripID;
    ArrayList<String> addMembersList;
    boolean isOwner = false;
    int PLACE_PICKER_REQUEST = 1;
    List<Integer> deleteList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        initializeViews();
        setTripDetails();

    }

    private void setTripDetails() {

        Intent intent = getIntent();
        Trip trip = intent.getParcelableExtra("trip");
        uid = intent.getStringExtra("uid");
        tripID = trip.getTripID();
        if(uid.equals(trip.getOwnerID())) {
            isOwner = true;
        }

        tripMemberAdapter = new TripMemberAdapter(this,memberList,this,isOwner);
        tripMembersRecyclerView.setAdapter(tripMemberAdapter);
        tripMemberAdapter.notifyDataSetChanged();


        tripNameTextView.setText(trip.getTitle());
        tripLocationTextView.setText("");
        if (trip.getImageUrl().isEmpty()) {
            tripImageView.setImageResource(R.drawable.ic_drive_eta_black_18dp);
        } else {
            tripImageView.setImageBitmap(decodeBase64(trip.getImageUrl()));
        }
        if(!trip.getOwnerID().equals(uid)) {
            removeTripButton.setVisibility(View.INVISIBLE);
            addMembersButton.setVisibility(View.INVISIBLE);
        }

        DatabaseReference ref = rootReference.child("trips").child(trip.getTripID()).child("locationNames");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder builder = new StringBuilder();
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    builder.append(snap.getValue(String.class));
                    builder.append(" - ");

                }
                tripLocationTextView.setText(builder.substring(0,builder.length()-3));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference reference = rootReference.child("trips").child(trip.getTripID()).child("members");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getValue(String.class);
                memberUIDList.add(uid);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memberList.clear();
                for (String id : memberUIDList) {
                    DatabaseReference reference1 = rootReference.child("users").child(id);
                    reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            memberList.add(user);
                            tripMemberAdapter.notifyDataSetChanged();
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

    public Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte,0,decodedByte.length);
    }

    private void initializeViews() {

        tripImageView = (ImageView) findViewById(R.id.tripImageView);
        tripNameTextView = (TextView) findViewById(R.id.tripName);
        tripLocationTextView = (TextView) findViewById(R.id.tripLocation);

        tripMembersRecyclerView = (RecyclerView) findViewById(R.id.tripMembersRecyclerView);
        tripMembersRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(TripDetailsActivity.this, LinearLayoutManager.VERTICAL, false);
        tripMembersRecyclerView.setLayoutManager(layoutManager);

        removeTripButton = (Button) findViewById(R.id.removeTripButton);
        addMembersButton = (Button) findViewById(R.id.addMembersButton);
        addLocationsButton = (Button) findViewById(R.id.addTripLocations);
        seeRoundTripButton = (Button) findViewById(R.id.seeRoundTrip);
        deleteLocationsButton = (Button) findViewById(R.id.deleteLocations);

        removeTripButton.setOnClickListener(this);
        addMembersButton.setOnClickListener(this);
        addLocationsButton.setOnClickListener(this);
        seeRoundTripButton.setOnClickListener(this);
        deleteLocationsButton.setOnClickListener(this);


        memberUIDList = new ArrayList<>();
        memberList = new ArrayList<>();
        addMembersList = new ArrayList<>();



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
                Intent intent1 = new Intent(TripDetailsActivity.this,SignUpActivity.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.trips:
                Intent tripsIntent = new Intent(TripDetailsActivity.this,TripsActivity.class);
                tripsIntent.putExtra("particularTrip",false);
                tripsIntent.putExtra("uid",uid);
                startActivity(tripsIntent);
                break;
            case R.id.friendRequests:
                Intent intent = new Intent(TripDetailsActivity.this,FriendRequestActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.logout:
                Intent intent2 = new Intent(TripDetailsActivity.this,LoginActivity.class);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addMembersButton:
                handleAddMembersButtonClick();
                break;
            case R.id.removeTripButton:
                handleRemoveTripButtonClick();
                break;
            case R.id.addTripLocations:
                handleAddTripsButtonClick();
                break;
            case R.id.deleteLocations:
                handleDeleteLocationsButtonClick();
                break;
            case R.id.seeRoundTrip:
                Intent intent = new Intent(TripDetailsActivity.this,MapsActivity.class);
                intent.putExtra("tripID",tripID);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void handleRemoveTripButtonClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(TripDetailsActivity.this);
        builder.setTitle("Remove Trip").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                disableTrip();
            }
        }).create().show();



    }

    private void disableTrip() {

        final DatabaseReference reference = rootReference.child("tripDetails").child(tripID).child("messages");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if(message.getUid().equals(uid)) {
                        DatabaseReference ref = reference.child(message.getKey());
                        ref.removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference ref = rootReference.child("tripDetails").child(tripID).child("isDisabled");
        ref.setValue(true);
        Toast.makeText(TripDetailsActivity.this,"Trip has been disabled",Toast.LENGTH_SHORT).show();
    }

    private void handleDeleteLocationsButtonClick() {
        DatabaseReference ref  = rootReference.child("trips").child(tripID).child("locationNames");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final List<String> items = new ArrayList<String>();
                final List<String> items1 = new ArrayList<String>();
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    String value = snap.getValue(String.class);
                    String key = snap.getKey();
                    items1.add(key);
                    items.add(value);
                }
                final CharSequence[] list = new CharSequence[items.size()];
                final CharSequence[] list2 = new CharSequence[items.size()];
                for(int i=0;i<items.size();i++) {
                    list[i] = items.get(i);
                    list2[i] = items1.get(i);
                }

                if(list.length == 1) {
                    Toast.makeText(TripDetailsActivity.this,"Cannot delete last location",Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(TripDetailsActivity.this);
                builder.setTitle("Delete Locations").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleDeleteSelectedLocations(list2,deleteList);
                    }
                }).setMultiChoiceItems(list, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked) {
                            deleteList.add(which);
                        } else {
                            deleteList.remove(which);
                        }
                    }
                }).create().show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void handleDeleteSelectedLocations(CharSequence[] list, List<Integer> delList) {

        for(Integer p : delList) {

            DatabaseReference ref = rootReference.child("trips").child(tripID).child("locationNames").child(list[p].toString());
            ref.removeValue();

            DatabaseReference ref2 = rootReference.child("trips").child(tripID).child("LatLng").child(list[p].toString());
            ref2.removeValue();
        }
        Toast.makeText(TripDetailsActivity.this,"Location deleted from Trip",Toast.LENGTH_SHORT).show();
        deleteList.clear();

    }

    private void handleAddTripsButtonClick() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(TripDetailsActivity.this);
            startActivityForResult(intent,PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                String address = place.getAddress().toString();
                String name = place.getName().toString();
                String latlng = place.getLatLng().toString();
                Log.d("nikhil","address is "+address+" name is "+name+" latlng is "+latlng);
                handleNewLocationAdded(place);
             //   getLocationFromAddress(this,address);
            }
        }
    }

    private void handleNewLocationAdded(Place place) {

        DatabaseReference ref = rootReference.child("trips").child(tripID).child("locationNames").push();
        String key1 = ref.getKey();
        ref.setValue(place.getAddress().toString());

        DatabaseReference latRef = rootReference.child("trips").child(tripID).child("LatLng").child(key1);
        String latLong = place.getLatLng().latitude+""+","+place.getLatLng().longitude;
        latRef.setValue(latLong);

        Toast.makeText(TripDetailsActivity.this,"Location added to trip",Toast.LENGTH_SHORT).show();

    }

/*    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
            Log.d("nikhil","lat is "+p1.latitude+" long is "+p1.longitude);
        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }*/

    private void handleAddMembersButtonClick() {
        DatabaseReference friendRef = rootReference.child("friends").child(uid);
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addMembersList.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String id = snap.getValue(String.class);
                    addMembersList.add(id);
                }
                final ArrayList<Integer> selectedItems = new ArrayList<Integer>();
                DatabaseReference ref = rootReference.child("trips").child(tripID).child("members");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            String id = snap.getValue(String.class);
                            if (addMembersList.contains(id)) {
                                addMembersList.remove(id);
                            }
                        }
                        final CharSequence[] items = new CharSequence[addMembersList.size()];
                        for(int i=0;i<items.length;i++) {
                            items[i] = "";
                        }

                        DatabaseReference ref = rootReference.child("users");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ArrayList<String> list = new ArrayList<String>();
                                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                                    User user = snap.getValue(User.class);
                                    if(addMembersList.contains(user.getUid())) {
                                        list.add(user.getFirstName()+" "+user.getLastName());
                                    }
                                }

                                for(int i=0;i<items.length;i++) {
                                    items[i] = list.get(i);
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(TripDetailsActivity.this);
                                builder.setTitle("Add members").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        handleAddSelectedMembers(addMembersList,selectedItems);
                                    }
                                }).setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if(isChecked) {
                                            selectedItems.add(which);
                                        } else {
                                            selectedItems.remove(which);
                                        }
                                    }
                                }).create().show();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                     //   items[i] = addMembersList.get(i);


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

    private void handleAddSelectedMembers(ArrayList<String> addMembersList, ArrayList<Integer> selectedItems) {

        for(Integer p : selectedItems) {
            DatabaseReference ref = rootReference.child("trips").child(tripID).child("members").child(addMembersList.get(p));
            ref.setValue(addMembersList.get(p));

            DatabaseReference reference = rootReference.child("userTrips").child(addMembersList.get(p)).child(tripID);
            reference.setValue(tripID);
        }
    }

    @Override
    public void onRemoveMemberButtonClick(User user,int pos) {

        Log.d("nikhil","onRemoveMemberButtonClick is called used id is "+user.getUid());
        DatabaseReference reference = rootReference.child("trips").child(tripID).child("members").child(user.getUid());
        reference.removeValue();
        Toast.makeText(TripDetailsActivity.this,user.getFirstName()+" "+user.getLastName()+" removed from trip",Toast.LENGTH_SHORT).show();
        memberUIDList.remove(pos);
        memberList.remove(pos);
        tripMemberAdapter.notifyDataSetChanged();
    }
}
