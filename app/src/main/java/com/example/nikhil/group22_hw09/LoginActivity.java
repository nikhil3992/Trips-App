package com.example.nikhil.group22_hw09;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity  implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 1;

    Button loginButton,signUpButton;
    SignInButton signInWithGoogleButton;
    EditText emailEditText,passwordEditText;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    String uid;
    private static final String TAG = "com.example.nikhil.group22_hw09.LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

    }

    private void initializeViews() {

        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        signInWithGoogleButton = (SignInButton) findViewById(R.id.signInWithGoogleButton);
        signInWithGoogleButton.setSize(SignInButton.SIZE_STANDARD);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);


        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        signInWithGoogleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInWithGoogleButton:
                handleGoogleSignIn();
                break;
            case R.id.loginButton:
                progressDialog.show();
                handleLogin();
                break;
            case R.id.signUpButton:
                Intent signUpIntent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(signUpIntent);
                break;
            default:
                break;
        }
    }

    private void handleLogin() {

        if(emailEditText.getText().toString().isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this,"Email cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }

        else if(passwordEditText.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this,"Password cannot be empty",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            final String email = user.getEmail();
                            DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference userRef = rootReference.child("users");
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for(DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        User user1 = userSnapshot.getValue(User.class);
                                        if(user1.getEmail().equals(email)) {
                                            progressDialog.dismiss();
                                            Log.d("nikhil","user found with email");
                                            uid = user1.getUid();
                                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                            intent.putExtra("user",user1);
                                            startActivity(intent);
                                            finish();
                                            break;
                                        }
                                    }
                                    progressDialog.dismiss();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithEmail:failure", task.getException());
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                           // updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            progressDialog.dismiss();
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("nikhil", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {

            progressDialog.dismiss();
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();
            final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference childRef = rootReference.child("users").push();
            final String uid = childRef.getKey();
            try {
                Picasso.with(this).load(acct.getPhotoUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        progressDialog.dismiss();
                        User user = new User(acct.getGivenName(), acct.getFamilyName(),
                                "Male", acct.getEmail(),"",encodeTobase64(bitmap));
                        user.setUid(uid);
                        childRef.setValue(user);
                        DatabaseReference friendsRef = rootReference.child("friends").child(uid);
                        friendsRef.setValue(uid);
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        intent.putExtra("user",user);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this,"Login Failed. Try again",Toast.LENGTH_LONG).show();
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



    private void handleGoogleSignIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
