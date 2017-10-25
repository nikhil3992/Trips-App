package com.example.nikhil.group22_hw09;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity2 extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String currentUid;

    ChatAdapter chatAdapter;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<Message> messagesList;
    final static int SELECTED_PICTURE = 1;
    final int REQUEST_WRITE_PERMISSION = 786;
    StorageReference emailRef;
    StorageReference storageRef;
    Uri selectedImage;
    String tripID;
    String uid;
    DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
    ImageView imgupload;
    String userName;
    ProgressDialog progressDialog;
    TextView tripDisabledTextView;
    EditText editText;
    ImageView addPhoto,addMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        tripDisabledTextView = (TextView) findViewById(R.id.tripDisabledTextView);
        addMessage = (ImageView) findViewById(R.id.addMessage);
        addPhoto = (ImageView) findViewById(R.id.addPhoto);
        editText = (EditText) findViewById(R.id.editText);

        setTitle("Chat");
        tripID = getIntent().getStringExtra("tripID");
        uid = getIntent().getStringExtra("uid");
        imgupload = (ImageView) findViewById(R.id.imageMessage);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://group22hw09a-91340.appspot.com");
        messagesList = new ArrayList<>();

        DatabaseReference ref = rootReference.child("tripDetails").child(tripID).child("isDisabled");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    boolean isDisabled = snapshot.getValue(boolean.class);
                    if(isDisabled) {
                        tripDisabledTextView.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.INVISIBLE);
                        addMessage.setVisibility(View.INVISIBLE);
                        addPhoto.setVisibility(View.INVISIBLE);

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        currentUid = getIntent().getExtras().getString("uid");
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        chatAdapter = new ChatAdapter(messagesList, ChatActivity2.this, currentUid);
        layoutManager = new LinearLayoutManager(ChatActivity2.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        final DatabaseReference reference = rootReference.child("users").child(currentUid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userName = user.getFirstName()+" "+user.getLastName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase = rootReference.child("tripDetails").child(tripID).child("messages");
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, final String s) {
                Log.d("nikhil","onChildAdded is called");
                final Message message = dataSnapshot.getValue(Message.class);
                DatabaseReference reference1 = mDatabase.child(message.getKey()).child("membersList");
                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String id = snapshot.getValue(String.class);
                            if(id.equals(uid)) {
                                messagesList.add(message);
                                chatAdapter.notifyDataSetChanged();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //messagesList.add(message);
                //chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesList.size() - 1);
                chatAdapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
                    @Override
                    public void onInternalItemClick(final int position, int id) {
                        if (id == R.id.delete) {

                            DatabaseReference reference1 = mDatabase.child(messagesList.get(position).getKey()).child("membersList").child(uid);
                            reference1.removeValue();
                            //mDatabase.child(messagesList.get(position).getKey()).removeValue();
                            messagesList.remove(position);
                            chatAdapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onRowClick(int position, int id) {}
                    @Override
                    public boolean onLongClick(int position, int id) {
                                return false;
                            }
                });
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                chatAdapter.notifyDataSetChanged();
            }

            @Override

            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });
        addMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Message message = new Message();
                Date date1 = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                message.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                message.setText(editText.getText().toString());
                editText.setText("");
                message.setUid(currentUid);

                final String key = mDatabase.push().getKey();
                message.setKey(key);
                message.setImageUrl("");
                message.setFullName(userName);
                message.setComment("n");
             //   messagesList.add(message);
              //  chatAdapter.notifyDataSetChanged();
                mDatabase.child(key).setValue(message);


                DatabaseReference ref = rootReference.child("trips").child(tripID).child("members");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String id = snapshot.getValue(String.class);
                            DatabaseReference reference1 = mDatabase.child(key).child("membersList").child(id);
                            reference1.setValue(id);
                        }

                        DatabaseReference reference1 = mDatabase.child(key).child("membersList");
                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String id = snapshot.getValue(String.class);
                                    if(id.equals(uid)) {
                                        messagesList.add(message);
                                        chatAdapter.notifyDataSetChanged();
                                    }
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
                Intent intent1 = new Intent(ChatActivity2.this,SignUpActivity.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.trips:
                Intent tripsIntent = new Intent(ChatActivity2.this,TripsActivity.class);
                tripsIntent.putExtra("particularTrip",false);
                tripsIntent.putExtra("uid",uid);
                startActivity(tripsIntent);
                break;
            case R.id.friendRequests:
                Intent intent = new Intent(ChatActivity2.this,FriendRequestActivity.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.logout:
                Intent intent2 = new Intent(ChatActivity2.this,LoginActivity.class);
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
    protected void onDestroy() {
        super.onDestroy();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECTED_PICTURE:
                if (resultCode == RESULT_OK) {
                    selectedImage = data.getData();
                    Log.d("selected", selectedImage.toString());

                    emailRef = storageRef.child("images/");
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (bitmap != null)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                    byte[] data1 = baos.toByteArray();

                    UploadTask uploadTask = emailRef.putBytes(data1);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(ChatActivity2.this, "oops! couldn't upload picture", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Toast.makeText(ChatActivity2.this, "Picture uploaded in Firebase", Toast.LENGTH_SHORT).show();

                            final String messageKey = mDatabase.push().getKey();
                            Message message = new Message();
                            message.setKey(messageKey);
                            message.setImageUrl(downloadUrl.toString());
                            Date date = new Date();
                            message.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                            message.setText("");
                            message.setFullName(userName);
                            message.setComment("n");
                            message.setUid(currentUid);
                          //  messagesList.add(message);
                          //  chatAdapter.notifyDataSetChanged();
                            mDatabase.child(messageKey).setValue(message);

                            DatabaseReference ref = rootReference.child("trips").child(tripID).child("members");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String id = snapshot.getValue(String.class);
                                        DatabaseReference reference1 = mDatabase.child(messageKey).child("membersList").child(id);
                                        reference1.setValue(id);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }
                break;
            case 200:


        }
    }
}

