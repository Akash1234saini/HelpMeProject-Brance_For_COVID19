package com.proapps.akashsaini.helpme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class SearchActivity extends AppCompatActivity {

    private AutoCompleteTextView textView;
    private ImageView closeButtonImageView;
    private ListView mNumberListView;
    private TextView mErrorTextView;

    // Instance of NetworkInfo and NetworkManager to check weather if internet i connected or not.
    private NetworkInfo networkInfo;
    private ConnectivityManager conMgr;

    private static final String TAG = SearchActivity.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserReference;

    private ChildEventListener mChildEventListener;

    private ArrayList<AddPublicNumber> publicNumbers;
    private FirebaseNumberAdapter firebaseNumberAdapter;

    private ArrayList<String> searchingWordsArrayList;
    private ArrayAdapter<String> searchingWordsAdapter;

    private ArrayList<AddPublicNumber> foundWordsArrayList;

    private InputMethodManager imm;

    private Intent callIntent;

    private SharedPreferences sharedPreferences;

    private static final int REQUEST_CODE_CALL_PHONE = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        textView = findViewById(R.id.searchTextView);
        mNumberListView = findViewById(R.id.listView);
        mErrorTextView = findViewById(R.id.errorTextView);
        closeButtonImageView = findViewById(R.id.closeButtonImageView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = mFirebaseDatabase.getReference();

        mNumberListView.setEmptyView(mErrorTextView);
        setSupportActionBar(toolbar);
        textView.setThreshold(1);

        publicNumbers = new ArrayList<>();
        foundWordsArrayList = new ArrayList<>();
        firebaseNumberAdapter = new FirebaseNumberAdapter(this, foundWordsArrayList);
        mNumberListView.setAdapter(firebaseNumberAdapter);

        searchingWordsArrayList = new ArrayList<>();
        searchingWordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchingWordsArrayList);
        textView.setAdapter(searchingWordsAdapter);


        // Get a reference to the connectivity manager to check the state of network connectivity
        conMgr = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean noNetwork = checkNetworkConnection();
        if (!noNetwork){
            mErrorTextView.setText(R.string.connect_net_for_more_result);
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            getKeyboardOpen();
            textView.setFocusableInTouchMode(true);
            textView.requestFocus();
        }

        mNumberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getKeyboardHide(mNumberListView);

                sharedPreferences = SearchActivity.this.getSharedPreferences("AlertDialogPublicNumberCheckBox", MODE_PRIVATE);
                boolean isChecked = sharedPreferences.getBoolean("AlertDialogPublicNumberCheckBox", false);

                if (!isChecked)
                    showPermissionDialog(position);
                else
                    // Call without asking to make call
                    numberChooser(position);;
            }
        });

        /* OnClick method to directly access of features of dialog box */
        mNumberListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int listPosition, long id) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.select_dialog_item);

                adapter.add("Call");
                if (isCurrentUser(listPosition))
                    adapter.add("Edit Your Details");
                adapter.add("Edit Before Dial");
                adapter.add("Copy Phone Number");
                adapter.add("View Details");
                adapter.add("Share");
                adapter.add("Quit");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {

                        String currentNumber = publicNumbers.get(listPosition).getmMob1();

                        if (adapter.getCount() == 7) {
                            switch (position) {
                                case 0:
                                    numberChooser(listPosition);
                                    break;
                                case 1:
                                    startActivity(new Intent(SearchActivity.this, AddPublicHelplineNumberActivity.class));
                                    break;
                                case 2:
                                    openNumberInDialer(currentNumber);
                                    break;
                                case 3:
                                    copyToClipboard(currentNumber);
                                    break;
                                case 4:
                                    viewDetails(listPosition);
                                    break;
                                case 5:
                                    shareData(currentNumber, listPosition);
                                case 6:
                                    dialogInterface.dismiss();
                            }
                        } else{
                            switch (position) {
                                case 0:
                                    numberChooser(listPosition);
                                    break;
                                case 1:
                                    openNumberInDialer(currentNumber);
                                    break;
                                case 2:
                                    copyToClipboard(currentNumber);
                                    break;
                                case 3:
                                    viewDetails(listPosition);
                                    break;
                                case 4:
                                    shareData(currentNumber, listPosition);
                                    break;
                                case 5:
                                    dialogInterface.dismiss();
                            }
                        }
                    }
                }).show();

                return true;
            }
        });

        /* Continuously check the enter char seq to search in array list */
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }



            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (textView.getText().toString().isEmpty()) {
                    closeButtonImageView.setVisibility(View.GONE);
                } else {
                    closeButtonImageView.setVisibility(View.VISIBLE);
                }

//                String searchingWord = s.toString().length();
                foundWordsArrayList.clear();
                for (AddPublicNumber number: publicNumbers) {
                    if (s.toString().trim().length() >= 1) {
                        if (number.getmName().contentEquals(s)
                                || number.getmState().contentEquals(s)
                                || number.getmCity().contentEquals(s)
                                || number.getmAddr1().contentEquals(s)
                                || number.getmAddr2().contentEquals(s)){
                            foundWordsArrayList.add(number);
                        }
                    }
                }
                firebaseNumberAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /* Dialog box methods for on click and on long click */

    private void shareData(String currentNumber, int position) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "COVID19 Emergency");
        shareIntent.putExtra(Intent.EXTRA_TEXT, foundWordsArrayList.get(position).getmName() + "\n" +
                "STATE: " + foundWordsArrayList.get(position).getmState() + "\n" +
                "PIN: " + foundWordsArrayList.get(position).getmPin() + "\n" +
                "CITY: " + foundWordsArrayList.get(position).getmCity() + "\n" +
                "ADDRESS LINE 1: " + foundWordsArrayList.get(position).getmAddr1() + "\n" +
                "ADDRESS LINE 2: " + foundWordsArrayList.get(position).getmAddr2() + "\n" +
                "MOBILE 1: " + foundWordsArrayList.get(position).getmMob1() + "\n" +
                "MOBILE 2: " + foundWordsArrayList.get(position).getmMob2());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void viewDetails(int position) {
        AlertDialog.Builder numberDetailDialog = new AlertDialog.Builder(
                Objects.requireNonNull(this));
        numberDetailDialog.setTitle(foundWordsArrayList.get(position).getmName())
                .setMessage("STATE: " + foundWordsArrayList.get(position).getmState() + "\n" +
                        "CITY: " + foundWordsArrayList.get(position).getmCity() + "\n" +
                        "PIN: " + foundWordsArrayList.get(position).getmPin() + "\n" +
                        "ADDRESS LINE 1: " + foundWordsArrayList.get(position).getmAddr1() + "\n" +
                        "ADDRESS LINE 2: " + foundWordsArrayList.get(position).getmAddr2() + "\n" +
                        "MOBILE 1: " + foundWordsArrayList.get(position).getmMob1() + "\n" +
                        "MOBILE 2: " + foundWordsArrayList.get(position).getmMob2())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void copyToClipboard(String currentNumber) {
        android.content.ClipboardManager clipboardManager = (ClipboardManager) Objects.requireNonNull(this).getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Source Text", currentNumber);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(clipData);
    }

    private void openNumberInDialer(String currentNumber) {
        Uri uri = Uri.parse("tel:" + currentNumber);
        Intent openDialerIntent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(openDialerIntent);
    }

    private void setCallingIntent(String phoneNumber) {
        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+"7906197963"));
    }

    private void numberChooser(int position) {
        final String mob1 = foundWordsArrayList.get(position).getmMob1();
        final String mob2 = foundWordsArrayList.get(position).getmMob2();
        if (!mob1.isEmpty() && !mob2.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this));
            ArrayAdapter<String> numberChooserAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
            numberChooserAdapter.add(mob1);
            numberChooserAdapter.add(mob2);
            builder.setAdapter(numberChooserAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0){
                        setCallingIntent(mob1);
                        waitForPermissionGranted();
                    } else{
                        setCallingIntent(mob2);
                        waitForPermissionGranted();
                    }
                }
            }).show();
        } else{
            setCallingIntent(mob1);
            waitForPermissionGranted();
        }
    }

    private void waitForPermissionGranted() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE},
                    REQUEST_CODE_CALL_PHONE);

        } else{
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i(TAG, "in on Request Permissions");
        if (requestCode == REQUEST_CODE_CALL_PHONE)
            Log.i(TAG, "in if RCCP");
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startActivity(callIntent);
        } else{
            Toast.makeText(this, "Please allow to call", Toast.LENGTH_SHORT).show();
        }
    }

    /*  */
    private void getKeyboardOpen() {
        imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void getKeyboardHide(View view){
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showPermissionDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.customview, viewGroup, false);
        builder.setView(dialogView);

        final CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
        TextView dontShowAgain = dialogView.findViewById(R.id.dontShowTextView);
        dontShowAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked())
                    checkBox.setChecked(false);
                else
                    checkBox.setChecked(true);
            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (checkBox.isChecked())
                    sharedPreferences.edit().putBoolean("AlertDialogPublicNumberCheckBox", true).apply();

                numberChooser(position);
                dialogInterface.dismiss();
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    private boolean isCurrentUser(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && user.getUid().equals(foundWordsArrayList.get(position).getmUserUID());
    }

    private boolean checkNetworkConnection() {

        // Get details on the currently active default data network
        assert conMgr != null;
        networkInfo = conMgr.getActiveNetworkInfo();

        // If there is not network connection, fetch data
        return networkInfo != null && networkInfo.isConnected();
    }

    public void backButton(View view){
        startActivity(new Intent(this, MainActivity.class));
    }

    public void closeButton(View view){
        textView.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dettachDatabaseReadListener();
        getKeyboardHide(mNumberListView);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    publicNumbers.clear();
                    for (DataSnapshot snaps: dataSnapshot.getChildren()){
                        publicNumbers.add(snaps.getValue(AddPublicNumber.class));
                        searchingWordsArrayList.add(publicNumbers.get(publicNumbers.size()-1).getmName());
                        searchingWordsArrayList.add(publicNumbers.get(publicNumbers.size()-1).getmState());
                        searchingWordsArrayList.add(publicNumbers.get(publicNumbers.size()-1).getmCity());
                        searchingWordsArrayList.add(publicNumbers.get(publicNumbers.size()-1).getmPin());
                        searchingWordsArrayList.add(publicNumbers.get(publicNumbers.size()-1).getmAddr1());
                        searchingWordsArrayList.add(publicNumbers.get(publicNumbers.size()-1).getmAddr2());
                    }
                    removeDuplicateValues();

                    if (publicNumbers.size() != 0)
                        mErrorTextView.setText("");
                    else {
                        mErrorTextView.setText(R.string.connect_net_for_more_result);
                    }
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
            mUserReference.addChildEventListener(mChildEventListener);
        }
    }


    private void dettachDatabaseReadListener(){
        if (mChildEventListener != null){
            mUserReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void removeDuplicateValues() {
        Set<String> set = new HashSet<>(searchingWordsArrayList);
        searchingWordsAdapter.clear();
        searchingWordsAdapter.addAll(set);
    }
}
