package com.proapps.akashsaini.helpme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class LiveCoronaPatientUpdateActivity extends AppCompatActivity
        implements LoaderCallbacks<List<LiveCoronaPatient>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    // https://pomber.github.io/covid19/timeseries.json /* Json code for all country death, recovered and conformed cases */
    // https://bing.com/covid/data

    /** URL for corona patient data from the bind.com */
    private static final String COVID19_REQUEST_URL =
            "https://bing.com/covid/data";

    private static final String TAG = LiveCoronaPatientUpdateActivity.class.getSimpleName();

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int PATIENT_LOADER_ID = 1;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    private ProgressBar loadingIndicator ;

    private LiveCoronaPatientAdapter mAdapter;
    private ListView mListView;

    // instance of global corona patient data
    private TextView mGlobalCases;
    private TextView mGlobalDeaths;
    private TextView mGlobalRecovered;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_corona_patient_update);

        Toolbar toolbar = findViewById(R.id.toolbar);
        mListView = findViewById(R.id.listView);
        mEmptyStateTextView = findViewById(R.id.empty_state);
        loadingIndicator = findViewById(R.id.progress_bar);
        mGlobalCases = findViewById(R.id.global_cases);
        mGlobalDeaths = findViewById(R.id.global_deaths);
        mGlobalRecovered = findViewById(R.id.global_recovered);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        mListView.setEmptyView(mEmptyStateTextView);
        setSupportActionBar(toolbar);

        mAdapter = new LiveCoronaPatientAdapter(this, new ArrayList<LiveCoronaPatient>());
        mListView.setAdapter(mAdapter);

        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        // To change the activity through bottom navigation view
        bottomNavigationView.setSelectedItemId(R.id.action_live);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Intent intent = new Intent(LiveCoronaPatientUpdateActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                return true;
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(PATIENT_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.empty_list_view);
        }
    }

    @Override
    public Loader<List<LiveCoronaPatient>> onCreateLoader(int i, Bundle bundle) {
        return new CoronaPatientLoader(this, COVID19_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<LiveCoronaPatient>> loader, List<LiveCoronaPatient> data) {
// Hide loading indicator because the data has been loaded
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_result_found);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            // Get the first item of list inn new list to show above
            ArrayList<LiveCoronaPatient> globalPatient = new ArrayList<>();
            globalPatient.add(data.get(0));

            // then remove them from all countries data
            data.remove(0);

            // assign values into global text views
            updateUi(globalPatient);

            // then add all data of list of countries
            mAdapter.addAll(data);
            Log.i(TAG, "Size: " + data.size());
        }
    }

    private void updateUi(ArrayList<LiveCoronaPatient> globalPatientList) {

        mGlobalCases.setText(globalPatientList.get(0).getmCases());
        mGlobalDeaths.setText(globalPatientList.get(0).getmDeaths());
        mGlobalRecovered.setText(globalPatientList.get(0).getmRecovered());
    }

    @Override
    public void onLoaderReset(Loader<List<LiveCoronaPatient>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
