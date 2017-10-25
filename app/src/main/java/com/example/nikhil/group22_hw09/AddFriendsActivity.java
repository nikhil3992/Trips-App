package com.example.nikhil.group22_hw09;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddFriendsActivity extends AppCompatActivity implements FriendsAdapter.ItemClickInterface{

    RecyclerView recyclerView;
    FriendsAdapter adapter;
    DatabaseReference rootReference;
    List<User> userList;
    String uid;
    HashMap<String,String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        initializeViews();
    }


    private void initializeViews() {

        userList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        rootReference = FirebaseDatabase.getInstance().getReference();


        DatabaseReference friendsRef = rootReference.child("friends").child(uid);
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getValue(String.class);
                    map.put(uid,uid);
                }
                Log.d("nikhil","map size is "+map.size());
                DatabaseReference requestRef = rootReference.child("sentFriendRequest").child(uid);
                requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot reqSnapshot : dataSnapshot.getChildren()) {
                            String req = reqSnapshot.getValue(String.class);
                            if(!map.containsKey(req)) {
                                map.put(req,req);
                            }
                        }

                        DatabaseReference userRef = rootReference.child("users");
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    User user = userSnapshot.getValue(User.class);
                                    if(!map.containsKey(user.getUid())) {
                                        if(!user.getUid().equals(uid)) {
                                            userList.add(user);
                                        }
                                    }
                                }
                                Log.d("nikhil","user list to string is"+userList.toString());
                                recyclerView.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        Log.d("nikhil","userList is "+userList.toString());
        adapter = new FriendsAdapter(AddFriendsActivity.this,userList,AddFriendsActivity.this,"AddFriendsActivity");
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(userList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onViewTripsButtonClick(User user) {

    }

    @Override
    public void onAddFriendsButtonClick(User user) {
        handleFriendRequest(user);
    }

    private void handleFriendRequest(final User user) {

        DatabaseReference friendRequestRef = rootReference.child("sentFriendRequest").child(uid);
        friendRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    rootReference.child("sentFriendRequest").setValue(uid);
                }
                DatabaseReference friendRequestRef = rootReference.child("sentFriendRequest").child(uid).child(user.getUid());
                friendRequestRef.setValue(user.getUid());

                rootReference.child("receivedFriendRequest").child(user.getUid()).child(uid).setValue(uid);
                Toast.makeText(AddFriendsActivity.this,"Friend request sent",Toast.LENGTH_SHORT).show();
                userList.remove(user);
                adapter.notifyDataSetChanged();
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
                Intent intent1 = new Intent(AddFriendsActivity.this,SignUpActivity.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.trips:
                Intent tripsIntent = new Intent(AddFriendsActivity.this,TripsActivity.class);
                tripsIntent.putExtra("particularTrip",false);
                tripsIntent.putExtra("uid",uid);
                startActivity(tripsIntent);
                break;
            case R.id.friendRequests:
                Intent intent = new Intent(AddFriendsActivity.this,FriendRequestActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.logout:
                finish();
                break;
            default:
                break;

        }
        return true;
    }
}
