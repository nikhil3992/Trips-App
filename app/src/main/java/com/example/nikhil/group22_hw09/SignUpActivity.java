package com.example.nikhil.group22_hw09;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private static final String TAG = "com.example.nikhil.group22_hw09.SignUpActivity";
    Button signUpButton, cancelButton, uploadPhotoButton;
    EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    TextView registerTextView;
    Bitmap userImageBitmap;
    DatabaseReference rootReference;
    Spinner genderSpinner;
    final static int SELECTED_PICTURE = 1;
    Uri uploadImageUri;
    final static int REQUEST_WRITE_PERMISSION = 786;
    StorageReference emailRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initializeViews();
        Intent intent = getIntent();
        if (intent!=null && intent.getExtras()!=null && intent.getExtras().containsKey("uid")) {
            handleUpdateProfile(intent.getExtras().getString("uid"));
        }
        mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://group22hw09a-91340.appspot.com");

    }

    private void handleUpdateProfile(String uid) {
        DatabaseReference ref = rootReference.child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                firstNameEditText.setText(user.getFirstName());
                lastNameEditText.setText(user.getLastName());
                emailEditText.setText(user.getEmail());
                passwordEditText.setText(user.getPassword());
                userImageBitmap = decodeBase64(user.getImageUrl());
                emailEditText.setVisibility(View.INVISIBLE);
                passwordEditText.setVisibility(View.INVISIBLE);
                confirmPasswordEditText.setVisibility(View.INVISIBLE);
                registerTextView.setText(R.string.update);
                signUpButton.setText(R.string.upd);
                uploadPhotoButton.setText(R.string.update_photo);
                if (user.getGender().equals("Male"))
                    genderSpinner.setSelection(0);
                else
                    genderSpinner.setSelection(1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeViews() {

        registerTextView = (TextView) findViewById(R.id.registerTextView);
        firstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        uploadPhotoButton = (Button) findViewById(R.id.uploadPhotoButton);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);

        uploadPhotoButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        userImageBitmap = null;

        rootReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadPhotoButton:
                requestPermission();
                break;
            case R.id.signUpButton:
                if (getIntent().getExtras()!=null && getIntent().getExtras().containsKey("uid")) {
                    handleUpdate();
                } else {
                    handleSignUp();
                }

                break;
            case R.id.cancelButton:
                finish();
                break;
            default:
                break;
        }

    }

    private void handleUpdate() {

        if (firstNameEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "First Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (lastNameEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Last Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference childRef = rootReference.child("users").child(getIntent().getStringExtra("uid"));
        User user = new User(firstNameEditText.getText().toString(), lastNameEditText.getText().toString(),
                genderSpinner.getSelectedItem().toString(), emailEditText.getText().toString(), passwordEditText.getText().toString(), encodeTobase64(userImageBitmap));
        user.setUid(getIntent().getStringExtra("uid"));
        childRef.setValue(user);
        Toast.makeText(SignUpActivity.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
        finish();

    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);

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
                    uploadImageUri = data.getData();
                    Log.d("nikhil", "upload image uri is " + uploadImageUri.toString());

                    emailRef = storageRef.child("images/");

                    try {
                        userImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uploadImageUri);
                        Toast.makeText(SignUpActivity.this,"Photo uploaded successfully",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                   /* UploadTask uploadTask = emailRef.putBytes(data1);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(SignUpActivity.this, "oops! couldn't upload picture", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Toast.makeText(SignUpActivity.this, "Picture uploaded in Firebase", Toast.LENGTH_SHORT).show();

                            DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid).child("messages");
                            String messageKey = mDatabase1.push().getKey();
                            Message message = new Message();
                            message.setKey(messageKey);
                            message.setImageUrl(downloadUrl.toString());
                            Date date = new Date();
                            message.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                            message.setText("");
                            message.setComment("n");
                            message.setUid(currentUid);
                            mDatabase1.child(messageKey).setValue(message);

                        }
                    });*/
                }
                break;
            case 200:


        }
    }

    public String encodeTobase64(Bitmap image) {

        if (image != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 90, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);
        }
        return "";

    }

    public Bitmap decodeBase64(String input) {
        if(!input.isEmpty()) {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        }
        return null;
    }


    private void handleSignUp() {

        if (firstNameEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "First Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (lastNameEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Last Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (emailEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (passwordEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (confirmPasswordEditText.getText().toString().isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Confirm Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            DatabaseReference childRef = rootReference.child("users").push();
                            String uid = childRef.getKey();
                            User user = new User(firstNameEditText.getText().toString(), lastNameEditText.getText().toString(),
                                    genderSpinner.getSelectedItem().toString(), emailEditText.getText().toString(), passwordEditText.getText().toString(), encodeTobase64(userImageBitmap));
                            user.setUid(uid);
                            childRef.setValue(user);
                            DatabaseReference friendsRef = rootReference.child("friends").child(uid);
                            friendsRef.setValue(uid);
                            Toast.makeText(SignUpActivity.this, "Registration Successful",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            /*Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                            startActivity(intent);*/

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Registration Failed. Try Again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
