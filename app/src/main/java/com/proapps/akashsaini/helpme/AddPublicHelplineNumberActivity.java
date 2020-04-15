package com.proapps.akashsaini.helpme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Objects;

public class AddPublicHelplineNumberActivity extends AppCompatActivity {

    // get instance
    private EditText mNameEditText, mMobile1EditText, mMobile2EditText, mStateEditText, mPinEditText, mCityEditText, mAddressLine1EditText, mAddressLine2EditText;
    private static final String TAG = AddPublicHelplineNumberActivity.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserReference;
    private DatabaseReference mUserDetailReference;
    private FirebaseUser mUser;

    public static final int RC_SIGN_IN = 1;
    
    private String mAddress;
    private String mCity;
    private String mState;
    private String mMobile1;
    private String mName;

    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference userDetailRef;
    
    // No network connection toast
    private Toast mNoNetworkToast;

    // Instance of NetworkInfo and NetworkManager to check weather if internet i connected or not.
    NetworkInfo networkInfo;
    ConnectivityManager conMgr;
    
    private String mCurrentMobile1;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_public_helpline_number);

        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mMobile1EditText = (EditText) findViewById(R.id.phone1EditText);
        mMobile2EditText = (EditText) findViewById(R.id.phone2EditText);
        mStateEditText = (EditText) findViewById(R.id.stateEditText);
        mPinEditText = (EditText) findViewById(R.id.pinEditText);
        mCityEditText = (EditText) findViewById(R.id.cityEditText);
        mAddressLine1EditText = (EditText) findViewById(R.id.addressLine1EditText);
        mAddressLine2EditText = (EditText) findViewById(R.id.addressLine2EditText);
        mProgressBar = findViewById(R.id.progressBar);
        
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = mFirebaseDatabase.getReference("users");
        mUserDetailReference = mFirebaseDatabase.getReference();
        mUser = mFirebaseAuth.getCurrentUser();
        
        mNoNetworkToast = Toast.makeText(this, R.string.enable_to_upload_data, Toast.LENGTH_LONG);

        // Get a reference to the connectivity manager to check the state of network connectivity
        conMgr = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean noNetwork = checkNetworkConnection();
        if (!noNetwork) {
            mNoNetworkToast.show();
        }

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    // user is sighed in
                    mProgressBar.setVisibility(View.VISIBLE);
                    fillUserDataInEditors();
                } else {
                    // user is signed out
                }
            }
        };


        userDetailRef = mFirebaseDatabase.getReference("users");
    }

    private boolean checkNetworkConnection() {

        // Get details on the currently active default data network
        assert conMgr != null;
        networkInfo = conMgr.getActiveNetworkInfo();

        // If there is not network connection, fetch data
        return networkInfo != null && networkInfo.isConnected();
    }

    private void fillUserDataInEditors() {
        attachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_public_number_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:

                boolean noNetwork = checkNetworkConnection();
                if (!noNetwork){
                    mNoNetworkToast.show();
                } else{
                    boolean isAllRight = checkMandatoryFields();
                    if (isAllRight) {
                        if (!checkValidationOfPhoneNumber()) {
                            mMobile1EditText.setError("Invalid number");
                        } else {
                            if (TextUtils.equals(mCurrentMobile1, mMobile1EditText.getText().toString())) {
                                Log.i(TAG, "same phone number");

                                uploadingTask();
                            } else {
                                Log.i(TAG, "not same phone number");

                                // now user trying to change his/her number so we delete the old uid from realtime database and authentication and assign new uid.

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if (user != null){

                                    // Realtime database
                                    removeUserFromDatabase();
                                    // Firestore
                                    removeFromFirebaseAuth();
                                }

                                String name = mNameEditText.getText().toString();
                                String mobile1 = mMobile1EditText.getText().toString();
                                String mobile2 = mMobile2EditText.getText().toString();
                                String state = mStateEditText.getText().toString();
                                String pin = mPinEditText.getText().toString();
                                String city = mCityEditText.getText().toString();
                                String address1 = mAddressLine1EditText.getText().toString();
                                String address2 = mAddressLine2EditText.getText().toString();

                                Intent phoneVerification = new Intent(AddPublicHelplineNumberActivity.this, VerificationCodeActivity.class);
                                phoneVerification.putExtra("mob1", mobile1);
                                phoneVerification.putExtra("phoneCode", "+91");
                                phoneVerification.putExtra("name", name);
                                phoneVerification.putExtra("mob2", mobile2);
                                phoneVerification.putExtra("state", state);
                                phoneVerification.putExtra("city", city);
                                phoneVerification.putExtra("pin", pin);
                                phoneVerification.putExtra("address1", address1);
                                phoneVerification.putExtra("address2", address2);
                                startActivity(phoneVerification);
                            }
                        }
                    }
                }
                break;
            case R.id.action_delete:

                noNetwork = checkNetworkConnection();
                if (!noNetwork){
                    mNoNetworkToast.show();
                } else if(mUser == null){
                    Toast.makeText(this, R.string.not_sign_in_yet, Toast.LENGTH_SHORT).show();
                } else{
                    deletingTask();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeUserFromDatabase() {
        mUserReference.child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.i(TAG, "remove user from database successful!");
                } else{
                    Log.i(TAG, "remove user from database failed!");
                    Toast toast = Toast.makeText(AddPublicHelplineNumberActivity.this, R.string.facing_error, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void removeFromFirebaseAuth() {
        mUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.i(TAG, "remove user from auth successful!");
                } else{
                    Log.i(TAG, "remove user from auth failed!");
                    Toast toast = Toast.makeText(AddPublicHelplineNumberActivity.this, R.string.facing_error, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private boolean checkValidationOfPhoneNumber() {
        return !TextUtils.isEmpty(mMobile1EditText.getText().toString().trim()) && mMobile1EditText.getText().toString().trim().length() == 10;
    }

    private void deletingTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure")
                .setMessage("You want to delete your number permanently!\n(You can add number later)")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Toast.makeText(AddPublicHelplineNumberActivity.this, "Deleting...", Toast.LENGTH_SHORT).show();
                        deleteNumber();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    private void uploadingTask() {

        String mobile2 = mMobile2EditText.getText().toString();
        String pin = mPinEditText.getText().toString();
        String mUserUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String address2 = mAddressLine2EditText.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean licenseAgreed = sharedPreferences.getBoolean("licenseAccepted", false);

        AddPublicNumber publicNumber = new AddPublicNumber(mUserUID, mName, mMobile1, mobile2, mState, pin, mCity, mAddress, address2, licenseAgreed);
        mUserReference.child(mUser.getUid()).setValue(publicNumber)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(AddPublicHelplineNumberActivity.this, "You'are ready to help", Toast.LENGTH_SHORT).show();
                            launchActivity();
                        } else{
                            Toast.makeText(AddPublicHelplineNumberActivity.this, "Something wants wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteNumber() {

        userDetailRef.child(mUser.getUid()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(AddPublicHelplineNumberActivity.this, "Your number deleted permanently", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            launchActivity();
                        } else{
                            Toast.makeText(AddPublicHelplineNumberActivity.this, "Something wants wrong! Try Later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void launchActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed In!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed In Cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            attachDatabaseReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        dettachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {

        if (mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    AddPublicNumber publicNumber = dataSnapshot.child(mUser.getUid()).getValue(AddPublicNumber.class);
                    if (publicNumber != null) {
                        assignDataInEditors(publicNumber.getmName(), publicNumber.getmMob1(), publicNumber.getmMob2(), publicNumber.getmState(), publicNumber.getmPin(),
                                publicNumber.getmCity(), publicNumber.getmAddr1(), publicNumber.getmAddr2());

                        // assigning current mobile 1 number in string variable.
                        mCurrentMobile1 = publicNumber.getmMob1();
                    }
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {            }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {            }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {            }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {            }
            };
            mUserDetailReference.addChildEventListener(mChildEventListener);
        }
    }

    private void assignDataInEditors(String name, String mob1, String mob2, String state, String pin, String city, String addr1, String addr2) {
        mNameEditText.setText(name);
        mMobile1EditText.setText(mob1);
        mMobile2EditText.setText(mob2);
        mStateEditText.setText(state);
        mPinEditText.setText(pin);
        mCityEditText.setText(city);
        mAddressLine1EditText.setText(addr1);
        mAddressLine2EditText.setText(addr2);
    }

    private void dettachDatabaseReadListener(){
        if (mChildEventListener != null){
            mUserDetailReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private boolean checkMandatoryFields() {

        return checkNameValidation()
                & checkMobile1Validation()
                & checkStateValidation()
                & checkCityValidation()
                & checkAddress1Validation();
    }

    private boolean checkAddress1Validation() {
        mAddress = mAddressLine1EditText.getText().toString().trim();
        if (mAddress.isEmpty()){
            mAddressLine1EditText.setError("Field is empty");
            return false;
        } else{
            mAddressLine1EditText.setError(null);
            return true;
        }
    }

    private boolean checkCityValidation() {
        mCity = mCityEditText.getText().toString().trim();
        if (mCity.isEmpty()){
            mCityEditText.setError("Field is empty");
            return false;
        } else{
            mCityEditText.setError(null);
            return true;
        }
    }

    private boolean checkStateValidation() {
        mState = mStateEditText.getText().toString().trim();
        if (mState.isEmpty()){
            mStateEditText.setError("Field is empty");
            return false;
        } else{
            mStateEditText.setError(null);
            return true;
        }
    }

    private boolean checkMobile1Validation() {
        mMobile1 = mMobile1EditText.getText().toString().trim();
        if (mMobile1.isEmpty()){
            mMobile1EditText.setError("Field is empty");
            return false;
        } else{
            mMobile1EditText.setError(null);
            return true;
        }
    }

    private boolean checkNameValidation() {
        mName = mNameEditText.getText().toString().trim();
        if (mName.isEmpty()){
            mNameEditText.setError("Field is empty");
            return false;
        } else{
            mNameEditText.setError(null);
            return true;
        }
    }
}
