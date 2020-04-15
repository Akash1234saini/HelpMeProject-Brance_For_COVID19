package com.proapps.akashsaini.helpme;

public class LiveCoronaPatient {

    private int mFlagImage;
    private String mCountry;
    private String mCases;
    private String mDeaths;
    private String mRecovered;

    public LiveCoronaPatient(int mFlagImage, String mCountry, String mCases, String mDeaths, String mRecovered) {
        this.mFlagImage = mFlagImage;
        this.mCountry = mCountry;
        this.mCases = mCases;
        this.mDeaths = mDeaths;
        this.mRecovered = mRecovered;
    }

    public int getmFlagImage() {
        return mFlagImage;
    }

    public String getmCountry() {
        return mCountry;
    }

    public String getmCases() {
        return mCases;
    }

    public String getmDeaths() {
        return mDeaths;
    }

    public String getmRecovered() {
        return mRecovered;
    }
}
