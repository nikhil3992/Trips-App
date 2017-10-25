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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FriendsAdapter.ItemClickInterface {

    RecyclerView recyclerView;
    Button addFriendsButton;
    FriendsAdapter adapter;
    DatabaseReference rootReference;
    List<User> friendsList;
    TextView noFriendsTextView;
    String uid;
    List<String> friendsUIDList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        User user = intent.getParcelableExtra("user");
        uid = user.getUid();
        initializeViews();
        adapter = new FriendsAdapter(this,friendsList,this,"MainActivity");
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

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
                Intent intent1 = new Intent(MainActivity.this,SignUpActivity.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.trips:
                Intent tripsIntent = new Intent(MainActivity.this,TripsActivity.class);
                tripsIntent.putExtra("particularTrip",false);
                tripsIntent.putExtra("uid",uid);
                startActivity(tripsIntent);
                break;
            case R.id.friendRequests:
                Intent intent = new Intent(MainActivity.this,FriendRequestActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.logout:
                Intent intent2 = new Intent(MainActivity.this,LoginActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                finish();
                break;
            default:
                break;

        }
        return true;
    }

    private void initializeViews() {

        friendsList = new ArrayList<>();
        friendsUIDList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        noFriendsTextView = (TextView) findViewById(R.id.noFriendsTextView);

        addFriendsButton = (Button) findViewById(R.id.addFriendsButton);
        addFriendsButton.setOnClickListener(this);

        adapter = new FriendsAdapter(MainActivity.this,friendsList,MainActivity.this,"MainActivity");
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        rootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference friendsUIDReference = rootReference.child("friends").child(uid);

        friendsUIDReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                friendsUIDList.clear();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String uid = userSnapshot.getValue(String.class);
                    Log.d("nikhil","user to string is "+uid);
                    friendsUIDList.add(uid);

                }
                getFriendsFromUIDs(friendsUIDList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("nikhil","friends UID list uis "+friendsUIDList.toString());

    }

    private void getFriendsFromUIDs(List<String> uids) {
        DatabaseReference userRef = rootReference.child("users");
        for(String uid : uids) {
            DatabaseReference childRef = userRef.child(uid);
            childRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Log.d("nikhil","user to string is "+user.toString());
                    friendsList.add(user);
                    recyclerView.setVisibility(View.VISIBLE);
                    noFriendsTextView.setVisibility(View.INVISIBLE);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        Log.d("nikhil","friendsList is "+friendsList.toString());
        adapter = new FriendsAdapter(MainActivity.this,friendsList,MainActivity.this,"MainActivity");
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(friendsList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noFriendsTextView.setVisibility(View.INVISIBLE);
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            noFriendsTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addFriendsButton:
                Intent addFriendsIntent = new Intent(MainActivity.this,AddFriendsActivity.class);
                addFriendsIntent.putExtra("uid",uid);
                startActivity(addFriendsIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onViewTripsButtonClick(User user) {

        Intent tripsIntent = new Intent(MainActivity.this,TripsActivity.class);
        tripsIntent.putExtra("particularTrip",true);
        tripsIntent.putExtra("uid",uid);
        tripsIntent.putExtra("user",user);
        startActivity(tripsIntent);
    }

    @Override
    public void onAddFriendsButtonClick(User user) {

    }
}
