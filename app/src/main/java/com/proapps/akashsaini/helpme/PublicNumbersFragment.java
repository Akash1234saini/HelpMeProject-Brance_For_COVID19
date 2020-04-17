package com.proapps.akashsaini.helpme;


import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class PublicNumbersFragment extends Fragment {

    private final static String TAG = PublicNumbersFragment.class.getSimpleName();
    private static final int REQUEST_CODE_CALL_PHONE = 1000;
    // to check if we are connected to Network
    boolean isConnected = true;
    private ChildEventListener mChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ListView mNumberListView;
    private FirebaseNumberAdapter fragmentNumberAdapter;
    private ArrayList<AddPublicNumber> publicNumbers;
    private Intent callIntent;
    private TextView mErrorTextView;
    // Instance of NetworkInfo and NetworkManager to check weather if internet i connected or not.
    private NetworkInfo networkInfo;
    private ConnectivityManager conMgr;
    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;
    
    private ProgressBar mProgressBar;

    public PublicNumbersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_public_numbers, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityIntent = new Intent(getContext(), AddPublicHelplineNumberActivity.class);
                startActivity(activityIntent);
            }
        });

        mNumberListView = rootView.findViewById(R.id.listView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = mFirebaseDatabase.getReference("users");
        mErrorTextView = rootView.findViewById(R.id.errorTextView);
        mProgressBar = rootView.findViewById(R.id.progressBar);

        mNumberListView.setEmptyView(mErrorTextView);

        // Get a reference to the connectivity manager to check the state of network connectivity
        conMgr = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize message ListView and its adapter
        publicNumbers = new ArrayList<>();
        fragmentNumberAdapter = new FirebaseNumberAdapter((AppCompatActivity) getActivity(), publicNumbers);
        mNumberListView.setAdapter(fragmentNumberAdapter);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mProgressBar.setVisibility(View.VISIBLE);
                onSignedInInitialize();
            }
        };

        mNumberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean isChecked = sharedPreferences.getBoolean("AlertDialogPublicNumberCheckBox", false);

                if (!isChecked) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    ViewGroup viewGroup = rootView.findViewById(android.R.id.content);
                    final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.customview, viewGroup, false);
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
                                    if (checkBox.isChecked())
                                        sharedPreferences.edit().putBoolean("AlertDialogPublicNumberCheckBox", true).apply();
                                    dialogInterface.cancel();
                                }
                            }).show();
                } else {
                    // Call without asking to make call
                    numberChooser(position);
                }
            }
        });

        mNumberListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int listPosition, long l) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), android.R.layout.select_dialog_item);
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
                                    startActivity(new Intent(getActivity(), AddPublicHelplineNumberActivity.class));
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
                        } else {
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

        return rootView;
    }

    private void viewDetails(int position) {
        AlertDialog.Builder numberDetailDialog = new AlertDialog.Builder(
                Objects.requireNonNull(getContext()));
        numberDetailDialog.setTitle(publicNumbers.get(position).getmName())
                .setMessage("STATE: " + publicNumbers.get(position).getmState() + "\n" +
                        "CITY: " + publicNumbers.get(position).getmCity() + "\n" +
                        "ADDRESS LINE 1: " + publicNumbers.get(position).getmAddr1() + "\n" +
                        "ADDRESS LINE 2: " + publicNumbers.get(position).getmAddr2() + "\n" +
                        "MOBILE 1: " + publicNumbers.get(position).getmMob1() + "\n" +
                        "MOBILE 2: " + publicNumbers.get(position).getmMob2())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void shareData(String currentNumber, int position) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "COVID19 Emergency");
        shareIntent.putExtra(Intent.EXTRA_TEXT, publicNumbers.get(position).getmName() + "\n" +
                "STATE: " + publicNumbers.get(position).getmState() + "\n" +
                "PIN: " + publicNumbers.get(position).getmPin() + "\n" +
                "CITY: " + publicNumbers.get(position).getmCity() + "\n" +
                "ADDRESS LINE 1: " + publicNumbers.get(position).getmAddr1() + "\n" +
                "ADDRESS LINE 2: " + publicNumbers.get(position).getmAddr2() + "\n" +
                "MOBILE 1: " + publicNumbers.get(position).getmMob1() + "\n" +
                "MOBILE 2: " + publicNumbers.get(position).getmMob2());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void copyToClipboard(String currentNumber) {
        android.content.ClipboardManager clipboardManager = (ClipboardManager) Objects.requireNonNull(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
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
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
    }

    private void numberChooser(int position) {
        final String mob1 = publicNumbers.get(position).getmMob1();
        final String mob2 = publicNumbers.get(position).getmMob2();
        if (!mob1.isEmpty() && !mob2.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            ArrayAdapter<String> numberChooserAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), android.R.layout.select_dialog_item);
            numberChooserAdapter.add(mob1);
            numberChooserAdapter.add(mob2);
            builder.setAdapter(numberChooserAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        setCallingIntent(mob1);
                        waitForPermissionGranted((AppCompatActivity) getActivity());
                    } else {
                        setCallingIntent(mob2);
                        waitForPermissionGranted((AppCompatActivity) getActivity());
                    }
                }
            }).show();
        } else {
            setCallingIntent(mob1);
            waitForPermissionGranted((AppCompatActivity) getActivity());
        }
    }

    private boolean isCurrentUser(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && user.getUid().equals(publicNumbers.get(position).getmUserUID());
    }

    private void onSignedInInitialize() {
        attachDatabaseReadListener();
    }

//    private void onSignedOutCleanup(){
//        fragmentNumberAdapter.clear();
//        dettachDatabaseReadListener();
//    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
        fragmentNumberAdapter.clear();
//        attachDatabaseReadListener();

        // Get details on the currently active default data network
        assert conMgr != null;
        networkInfo = conMgr.getActiveNetworkInfo();

        // If there is not network connection, fetch data
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            mErrorTextView.setText(R.string.error_list_view);
            mProgressBar.setVisibility(View.GONE);
        } else
            mErrorTextView.setText(R.string.empty_list_view);
    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        dettachDatabaseReadListener();

    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    publicNumbers.clear();
                    for (DataSnapshot snaps : dataSnapshot.getChildren()) {
                        publicNumbers.add(snaps.getValue(AddPublicNumber.class));
                    }
                    Log.i(TAG, "under");

                    SharedPreferences sharedPres = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String sortBy = sharedPres.getString(
                            getString(R.string.settings_sort_by_key),
                            getString(R.string.settings_sort_by_default)
                    );

                    String orderBy = sharedPres.getString(
                            getString(R.string.settings_order_by_key),
                            getString(R.string.settings_order_by_default)
                    );

                    if (sortBy.equals(getString(R.string.settings_sort_by_ascending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_name_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByNameAscending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_descending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_name_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByNameDescending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_ascending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_state_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByStateAscending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_descending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_state_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByStateDescending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_ascending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_city_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByCityAscending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_descending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_city_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByCityDescending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_ascending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_address_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByAddressAscending);

                    else if (sortBy.equals(getString(R.string.settings_sort_by_descending_value))
                            && orderBy.equals(getString(R.string.settings_order_by_address_value)))
                        Collections.sort(publicNumbers, AddPublicNumber.sortByAddressDescending);

                    mErrorTextView.setText(R.string.empty_list_view);
                    mProgressBar.setVisibility(View.GONE);

                    fragmentNumberAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mUserReference.addChildEventListener(mChildEventListener);
        }
    }

    private void dettachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mUserReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void waitForPermissionGranted(AppCompatActivity context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CODE_CALL_PHONE);

        } else {
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i(TAG, "in on Request Permissions");
        if (requestCode == REQUEST_CODE_CALL_PHONE)
            Log.i(TAG, "in if RCCP");
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            Toast.makeText(getContext(), "Please allow to call", Toast.LENGTH_SHORT).show();
        }
    }
}
