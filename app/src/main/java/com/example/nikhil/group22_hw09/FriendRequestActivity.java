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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.ItemClickInterface {

    RecyclerView recyclerView;
    FriendRequestAdapter adapter;
    DatabaseReference rootReference;
    List<User> friendRequestList;
    List<String> friendRequestUIDList;
    String uid;
    TextView noFriendRequestsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        initializeViews();

    }

    private void initializeViews() {

        noFriendRequestsTextView = (TextView) findViewById(R.id.noFriendRequestsTextView);
        friendRequestList = new ArrayList<>();
        friendRequestUIDList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        rootReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference friendRequestsRef = rootReference.child("receivedFriendRequest").child(uid);
        friendRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String uids = userSnapshot.getValue(String.class);
                    Log.d("nikhil","user to string is "+uids);
                   friendRequestUIDList.add(uids);
                }
                getFriendsFromUIDs(friendRequestUIDList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Log.d("nikhil","friendRequestList is "+friendRequestList.toString());
        adapter = new FriendRequestAdapter(FriendRequestActivity.this,friendRequestList,FriendRequestActivity.this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(friendRequestList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noFriendRequestsTextView.setVisibility(View.INVISIBLE);
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            noFriendRequestsTextView.setVisibility(View.VISIBLE);
        }
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
                    friendRequestList.add(user);
                    recyclerView.setVisibility(View.VISIBLE);
                    noFriendRequestsTextView.setVisibility(View.INVISIBLE);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        Log.d("nikhil","friendsList is "+friendRequestList.toString());

        if(friendRequestList.size() > 0) {
            adapter = new FriendRequestAdapter(FriendRequestActivity.this,friendRequestList,FriendRequestActivity.this);
            recyclerView.setVisibility(View.VISIBLE);
            noFriendRequestsTextView.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            recyclerView.setVisibility(View.INVISIBLE);
            noFriendRequestsTextView.setVisibility(View.VISIBLE);
        }
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
                Intent intent1 = new Intent(FriendRequestActivity.this,SignUpActivity.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.trips:
                Intent tripsIntent = new Intent(FriendRequestActivity.this,TripsActivity.class);
                tripsIntent.putExtra("particularTrip",false);
                tripsIntent.putExtra("uid",uid);
                startActivity(tripsIntent);
                break;
            case R.id.friendRequests:
                Intent intent = new Intent(FriendRequestActivity.this,FriendRequestActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.logout:
                Intent intent2 = new Intent(FriendRequestActivity.this,LoginActivity.class);
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
    public void onAcceptButtonClick(User user) {

        DatabaseReference reference = rootReference.child("friends").child(uid).child(user.getUid());
        reference.setValue(user.getUid());

        DatabaseReference reference1 = rootReference.child("friends").child(user.getUid()).child(uid);
        reference1.setValue(uid);

        DatabaseReference reference2 = rootReference.child("receivedFriendRequest").child(uid).child(user.getUid());
        reference2.removeValue();

        rootReference.child("sentFriendRequest").child(user.getUid()).child(uid).removeValue();

        friendRequestList.remove(user);
        adapter.notifyDataSetChanged();
        Toast.makeText(FriendRequestActivity.this,"You are now friends with "+user.getFirstName()+" "+user.getLastName(),Toast.LENGTH_LONG)
                .show();

    }

    @Override
    public void onDeclineButtonClick(User user) {

        DatabaseReference reference1 = rootReference.child("receivedFriendRequest").child(uid).child(user.getUid());
        reference1.removeValue();
        rootReference.child("sentFriendRequest").child(user.getUid()).child(uid).removeValue();
        friendRequestList.remove(user);
        adapter.notifyDataSetChanged();
        Toast.makeText(FriendRequestActivity.this,"Friend request Declined",Toast.LENGTH_LONG)
                .show();
    }
}
