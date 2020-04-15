package com.proapps.akashsaini.helpme;

import java.util.Comparator;

class Number {

    private String mNumberTitle;
    private String mNumber;
    private String mStateOrTerritory;

    // added new branch.
    // new commant of new branch.

    Number ( String numberTitle, String number, String stateOrTerritory) {
        mNumberTitle = numberTitle;
        mNumber = number;
        mStateOrTerritory = stateOrTerritory;
    }

    String getmNumberTitle() {
        return mNumberTitle;
    }

    String getmNumber() {
        return mNumber;
    }

    String getmStateOrTerritory() {
        return mStateOrTerritory;
    }

    /*Comparator for sorting in ascending order the list by Public Number Address*/
    public static Comparator<Number> sortByAscending = new Comparator<Number>() {

        public int compare(Number data1, Number data2) {
            String add1 = data1.getmNumberTitle().toUpperCase();
            String add2 = data2.getmNumberTitle().toUpperCase();

            return add1.compareTo(add2);
        }};

    /*Comparator for sorting in descending order the list by Public Number Address*/
    public static Comparator<Number> sortByDescending = new Comparator<Number>() {

        public int compare(Number data1, Number data2) {
            String add1 = data1.getmNumberTitle().toUpperCase();
            String add2 = data2.getmNumberTitle().toUpperCase();

            return add2.compareTo(add1);
        }};
}
