package com.proapps.akashsaini.helpme;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

public class CoronaPatientLoader extends AsyncTaskLoader<List<LiveCoronaPatient>> {

    /** Tag for log messages */
    private static final String LOG_TAG = CoronaPatientLoader.class.getName();

    /** Query URL */
    private String mUrl;
    /**
     * Constructs a new {@link CoronaPatientLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    CoronaPatientLoader(@NonNull Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<LiveCoronaPatient> loadInBackground() {
        if (mUrl == null) {
            return null;
        } else
            // Perform the network request, parse the response, and extract a list of earthquakes.
            return QuaryUtils.fetchCoronaPatientData(mUrl);
    }
}