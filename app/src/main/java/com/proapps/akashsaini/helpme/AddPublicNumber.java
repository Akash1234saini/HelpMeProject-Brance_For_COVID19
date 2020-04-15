package com.proapps.akashsaini.helpme;

import java.util.Comparator;

public class AddPublicNumber {

    private String mUserUID;
    private String mName;
    private String mMob1;
    private String mMob2;
    private String mState;
    private String mPin;
    private String mCity;
    private String mAddr1;
    private String mAddr2;
    private boolean mAgreement;

    public AddPublicNumber(){

    }

    public AddPublicNumber(String mUserUID, String mName, String mMob1, String mMob2, String mState, String mPin, String mCity, String mAddr1, String mAddr2, boolean mAgreement) {
        this.mUserUID = mUserUID;
        this.mName = mName;
        this.mMob1 = mMob1;
        this.mMob2 = mMob2;
        this.mState = mState;
        this.mPin = mPin;
        this.mCity = mCity;
        this.mAddr1 = mAddr1;
        this.mAddr2 = mAddr2;
        this.mAgreement = mAgreement;
    }

    public String getmUserUID() {
        return mUserUID;
    }

    public String getmName() {
        return mName;
    }

    public String getmMob1() {
        return mMob1;
    }

    public String getmMob2() {
        return mMob2;
    }

    public String getmState() {
        return mState;
    }

    public String getmPin() {
        return mPin;
    }

    public String getmCity() {
        return mCity;
    }

    public String getmAddr1() {
        return mAddr1;
    }

    public String getmAddr2() {
        return mAddr2;
    }

    /*Comparator for sorting in ascending order the list by Public Number Name*/
    public static Comparator<AddPublicNumber> sortByNameAscending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String Name1 = data1.getmName().toUpperCase();
            String Name2 = data2.getmName().toUpperCase();

            return Name1.compareTo(Name2);
        }};

    /*Comparator for sorting in descending order the list by Public Number Name*/
    public static Comparator<AddPublicNumber> sortByNameDescending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String Name1 = data1.getmName().toUpperCase();
            String Name2 = data2.getmName().toUpperCase();

            return Name2.compareTo(Name1);
        }};

    /*Comparator for sorting in ascending order the list by Public Number State*/
    public static Comparator<AddPublicNumber> sortByStateAscending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String state1 = data1.getmState().toUpperCase();
            String state2 = data2.getmState().toUpperCase();

            return state1.compareTo(state2);
        }};

    /*Comparator for sorting in descending order the list by Public Number State*/
    public static Comparator<AddPublicNumber> sortByStateDescending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String state1 = data1.getmState().toUpperCase();
            String state2 = data2.getmState().toUpperCase();

            return state2.compareTo(state1);
        }};

    /*Comparator for sorting in ascending order the list by Public Number City*/
    public static Comparator<AddPublicNumber> sortByCityAscending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String city1 = data1.getmCity().toUpperCase();
            String city2 = data2.getmCity().toUpperCase();

            return city1.compareTo(city2);
        }};

    /*Comparator for sorting in descending order the list by Public Number City*/
    public static Comparator<AddPublicNumber> sortByCityDescending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String city1 = data1.getmCity().toUpperCase();
            String city2 = data2.getmCity().toUpperCase();

            return city2.compareTo(city1);
        }};

    /*Comparator for sorting in ascending order the list by Public Number Address*/
    public static Comparator<AddPublicNumber> sortByAddressAscending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String add1 = data1.getmAddr1().toUpperCase();
            String add2 = data2.getmAddr2().toUpperCase();

            return add1.compareTo(add2);
        }};

    /*Comparator for sorting in descending order the list by Public Number Address*/
    public static Comparator<AddPublicNumber> sortByAddressDescending = new Comparator<AddPublicNumber>() {

        public int compare(AddPublicNumber data1, AddPublicNumber data2) {
            String add1 = data1.getmAddr1().toUpperCase();
            String add2 = data2.getmAddr2().toUpperCase();

            return add2.compareTo(add1);
        }};
}
