package com.proapps.akashsaini.helpme;

import java.util.Comparator;

public class LiveCoronaPatient {

    private int mFlagImage;
    private String mCountry;
    private String mCases;
    private String mDeaths;
    /*Comparator for sorting in ascending order the list by Public Number State*/
    public static final Comparator<LiveCoronaPatient> sortByCasesAscending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            int cases1 = 0;
            int cases2 = 0;
            if (!data1.getmCases().equals("null") && !data2.getmCases().equals("null")) {
                cases1 = Integer.parseInt(data1.getmCases());
                cases2 = Integer.parseInt(data2.getmCases());
            }

            return cases2 - cases1;
        }
    };
    private String mRecovered;
    /*Comparator for sorting in ascending order the list by Public Number Name*/
    public static Comparator<LiveCoronaPatient> sortByCountryAscending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            String country1 = data1.getmCountry().toUpperCase();
            String country2 = data2.getmCountry().toUpperCase();

            return country1.compareTo(country2);
        }
    };

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

    /*Comparator for sorting in descending order the list by Public Number Name*/
    public static Comparator<LiveCoronaPatient> sortByCountryDescending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            String country1 = data1.getmCountry().toUpperCase();
            String country2 = data2.getmCountry().toUpperCase();

            return country2.compareTo(country1);
        }
    };
    /*Comparator for sorting in descending order the list by Public Number State*/
    public static Comparator<LiveCoronaPatient> sortByCasesDescending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            int cases1 = 0;
            int cases2 = 0;
            if (!data1.getmCases().equals("null") && !data2.getmCases().equals("null")) {
                cases1 = Integer.parseInt(data1.getmCases());
                cases2 = Integer.parseInt(data2.getmCases());
            }

            return cases1 - cases2;
        }
    };
    /*Comparator for sorting in ascending order the list by Public Number City*/
    public static Comparator<LiveCoronaPatient> sortByDeathAscending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            int deaths1 = 0;
            int deaths2 = 0;
            if (!data1.getmDeaths().equals("null") && !data2.getmDeaths().equals("null")) {
                deaths1 = Integer.parseInt(data1.getmDeaths());
                deaths2 = Integer.parseInt(data2.getmDeaths());
            }

            return deaths2 - deaths1;
        }
    };
    /*Comparator for sorting in descending order the list by Public Number City*/
    public static Comparator<LiveCoronaPatient> sortByDeathDescending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            int deaths1 = 0;
            int deaths2 = 0;
            if (!data1.getmDeaths().equals("null") && !data2.getmDeaths().equals("null")) {
                deaths1 = Integer.parseInt(data1.getmDeaths());
                deaths2 = Integer.parseInt(data2.getmDeaths());
            }

            return deaths1 - deaths2;
        }
    };
    /*Comparator for sorting in ascending order the list by Public Number Address*/
    public static Comparator<LiveCoronaPatient> sortByRecoveredAscending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            int recovered1 = 0;
            int recovered2 = 0;
            if (!data1.getmRecovered().equals("null") && !data2.getmRecovered().equals("null")) {
                recovered1 = Integer.parseInt(data1.getmRecovered());
                recovered2 = Integer.parseInt(data2.getmRecovered());
            }

            return recovered2 - recovered1;
        }
    };
    /*Comparator for sorting in descending order the list by Public Number Address*/
    public static Comparator<LiveCoronaPatient> sortByRecoveredDescending = new Comparator<LiveCoronaPatient>() {

        public int compare(LiveCoronaPatient data1, LiveCoronaPatient data2) {
            int recovered1 = 0;
            int recovered2 = 0;
            if (!data1.getmRecovered().equals("null") && !data2.getmRecovered().equals("null")) {
                recovered1 = Integer.parseInt(data1.getmRecovered());
                recovered2 = Integer.parseInt(data2.getmRecovered());
            }

            return recovered1 - recovered2;
        }
    };
    private String mUrl;

    public LiveCoronaPatient(int mFlagImage, String mCountry, String mCases, String mDeaths, String mRecovered, String mUrl) {
        this.mFlagImage = mFlagImage;
        this.mCountry = mCountry;
        this.mCases = mCases;
        this.mDeaths = mDeaths;
        this.mRecovered = mRecovered;
        this.mUrl = mUrl;
    }

    public String getmUrl() {
        return mUrl;
    }
}
