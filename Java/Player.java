package com.example.android.playgolf;

/**
 * Created by Harri on 30.10.2016.
 */

public class Player {

    private int mPlayerId;
    private String mFirstName;
    private String mLastName;
    private String mCourse;
    private double mHandicap;
    private String mMobileNr;
    private String mEmail;

    public Player (int playerId, String firstName, String lastName, String course, double handicap, String mobileNr, String email) {
        mPlayerId = playerId;
        mFirstName = firstName;
        mLastName = lastName;
        mCourse = course;
        mHandicap = handicap;
        mMobileNr = mobileNr;
        mEmail = email;
    }

    public int getPlayerId() {
        return mPlayerId;
    }

    public void setPlayerId(int playerId) {
        mPlayerId = playerId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String fName) {
        mFirstName = fName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lName) {
        mLastName = lName;
    }

    public String getCourseAbbr() {
        return mCourse;
    }

    public void setCourseAbbr(String course) {
        mCourse = course;
    }
    public double getHandicap() {
        return mHandicap;
    }

    public void setHandicap(double handicap) {
        mHandicap = handicap;
    }
    public String getMobileNr() {
        return mMobileNr;
    }

    public void setMobileNr(String mobileNr) {
        mMobileNr = mobileNr;
    }
    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }
}
