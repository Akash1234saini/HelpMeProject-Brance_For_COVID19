package com.proapps.akashsaini.helpme;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class GovernmentNumbersFragment extends Fragment {

    private static final String TAG = GovernmentNumbersFragment.class.getSimpleName();
    private static final int REQUEST_CODE_CALL_PHONE = 1000;

    Intent callIntent;

    ArrayList<Number> number;
    NumberAdapter numberAdapter;

    public GovernmentNumbersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_govarment_number, container, false);

        number = new ArrayList<Number>();
        numberAdapter = new NumberAdapter((AppCompatActivity) getActivity(), number);
        final ListView listView = rootView.findViewById(R.id.listView);
        listView.setAdapter(numberAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getContext()), android.R.layout.select_dialog_item);
                adapter.add("Call");
                adapter.add("Open In Browser");
                adapter.add("Edit Before Dial");
                adapter.add("Copy Phone Number");
                adapter.add("View Details");
                adapter.add("Share");
                adapter.add("Quit");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String currentNumber = number.get(position).getmNumber();
                        String currentNumberTitle = number.get(position).getmNumberTitle();
                        String stateOrTerritory = number.get(position).getmStateOrTerritory();

                        switch (i){
                            case 0:
                                setCallingIntent(currentNumber);
                                waitForPermissionGranted((AppCompatActivity) getActivity());
                                break;
                            case 1:
                                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                                String driveUrl = "http://drive.google.com/viewerng/viewer?embedded=true&url=";
                                String fileUrl = "https://www.mohfw.gov.in/coronvavirushelplinenumber.pdf";
                                webIntent.setData(Uri.parse(driveUrl + fileUrl));
                                startActivity(webIntent);
                                break;
                            case 2:
                                Uri uri = Uri.parse("tel:" + currentNumber);
                                Intent openDialerIntent = new Intent(Intent.ACTION_DIAL, uri);
                                startActivity(openDialerIntent);
                                break;
                            case 3:
                                android.content.ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("Source Text", currentNumber);
                                assert clipboardManager != null;
                                clipboardManager.setPrimaryClip(clipData);
                                break;
                            case 4:
                                AlertDialog.Builder numberDetailDialog = new AlertDialog.Builder(getContext());
                                numberDetailDialog.setTitle("Detail")
                                        .setMessage(currentNumberTitle + "\n" + currentNumber + "\n" + "(" + stateOrTerritory + ")")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).show();
                                break;
                            case 5:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "COVID19 Emergency");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, currentNumberTitle + "\n" + currentNumber);
                                startActivity(Intent.createChooser(shareIntent, "Share via"));
                                break;
                            case 6:
                                dialogInterface.cancel();
                                break;
                        }

                    }
                }).show();

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean isChecked = sharedPreferences.getBoolean("AlertDialogCheckBox", false);

                String currentNumber = number.get(i).getmNumber();
                setCallingIntent(currentNumber);
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
                                sharedPreferences.edit().putBoolean("AlertDialogCheckBox", true).apply();

                            waitForPermissionGranted((AppCompatActivity) getActivity());
                            dialogInterface.dismiss();
                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (checkBox.isChecked())
                                        sharedPreferences.edit().putBoolean("AlertDialogCheckBox", true).apply();
                                    dialogInterface.cancel();
                                }
                            }).show();
                } else{
                    // Call without asking to make call
                    waitForPermissionGranted((AppCompatActivity) getActivity());
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDataToList();
    }

    private void attachDataToList() {

        number.clear();

        number.add(new Number("Central Helpline Number", "+91-11-23978046", "For all states"));

        number.add(new Number("All in one emergeny number", "112", "Dial this number for any kind of help " +
                "in coronavirus lockdown in 16 state and Territories"));

        number.add(new Number("Andhra Pradesh", "0866-2410978", "State"));
        number.add(new Number("Arunachal Pradesh", "9436055743", "State"));
        number.add(new Number("Assam", "6913347770", "State"));
        number.add(new Number("Bihar", "104", "State"));
        number.add(new Number("Chhattisgarh", "104", "State"));
        number.add(new Number("Goa", "104", "State"));
        number.add(new Number("Gujarat", "104", "State"));
        number.add(new Number("Haryana", "8558893911", "State"));
        number.add(new Number("Himachal Pradesh ", "104", "State"));
        number.add(new Number("Jharkhand", "104", "State"));
        number.add(new Number("Karnataka", "104", "State"));
        number.add(new Number("Kerala", "0471-2552056", "State"));
        number.add(new Number("Madhya Pradesh ", "0755-2527177", "State"));
        number.add(new Number("Maharashtra", "020-26127394", "State"));
        number.add(new Number("Manipur", "3852411668", "State"));
        number.add(new Number("Meghalaya", "108", "State"));
        number.add(new Number("Mizoram", "102", "State"));
        number.add(new Number("Nagaland", "7005539653", "State"));
        number.add(new Number("Odisha", "9439994859", "State"));
        number.add(new Number("Punjab", "104", "State"));
        number.add(new Number("Rajasthan", "0141-2225624", "State"));
        number.add(new Number("Sikkim", "104", "State"));
        number.add(new Number("Tamil Nadu", "044-29510500", "State"));
        number.add(new Number("Telangana", "104", "State"));
        number.add(new Number("Tripura", "0381-2315879", "State"));
        number.add(new Number("Uttarakhand", "104", "State"));
        number.add(new Number("Uttar Pradesh", "18001805145", "State"));
        number.add(new Number("West Bengal", "1800313444222, 03323412600", "State"));

        number.add(new Number("Andaman and Nicobar Islands", "03192-232102", "Union Territory"));
        number.add(new Number("Chandigarh", "9779558282", "Union Territory"));
        number.add(new Number("Dadra and Nagar Haveli and Daman & Diu ", "104", "Union Territory"));
        number.add(new Number("Delhi ", "011-22307145", "Union Territory"));
        number.add(new Number("Jammu & Kashmir", "01912520982, 0194-2440283", "Union Territory"));
        number.add(new Number("Ladakh", "01982256462", "Union Territory"));
        number.add(new Number("Lakshadweep", "104", "Union Territory"));
        number.add(new Number("Puducherry", "104", "Union Territory"));

        SharedPreferences sharedPres = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sortBy = sharedPres.getString(
                getString(R.string.settings_government_sort_by_key),
                getString(R.string.settings_government_sort_by_default)
        );

        if (sortBy.equals(getString(R.string.settings_government_sort_by_ascending_value)))
            Collections.sort(number, Number.sortByAscending);

        else if (sortBy.equals(getString(R.string.settings_government_sort_by_descending_value)))
            Collections.sort(number, Number.sortByDescending);

        numberAdapter.notifyDataSetChanged();
    }

    private void setCallingIntent(String number) {
        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
    }

    private void waitForPermissionGranted(AppCompatActivity context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.CALL_PHONE},
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
                Toast.makeText(getContext(), "Please allow permissions to make call", Toast.LENGTH_SHORT).show();
            }
    }
}