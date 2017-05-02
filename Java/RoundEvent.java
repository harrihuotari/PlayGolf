package com.example.android.playgolf;

import android.location.Location;

import java.util.Date;

/**
 * Created by Harri on 1.11.2016.
 */

public class RoundEvent {
    private String mRoundName;
    private String mTheDate;
    private double mTemperature;
    private int mRain;
    private int mSun;
    private int mWind;
    private String mWindDirection;
    private String mClouds;
    private int mCourseId;

    public RoundEvent(String roundName, String theDate, double temperature, int rain, int sun, int wind, String windDirection, String clouds, int courseId) {
        mRoundName = roundName;
        mTheDate = theDate;
        mTemperature = temperature;
        mRain = rain;
        mSun = sun;
        mWind = wind;
        mWindDirection = windDirection;
        mClouds = clouds;
        mCourseId = courseId;
    }

    public String getRoundName() {
        return mRoundName;
    }

    public void setRoundName(String roundName) {
        mRoundName = roundName;
    }

    public String getTheDate() {
        return mTheDate;
    }

    public void setTheDate(String theDate) {
        mTheDate = theDate;
    }

    public Double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(Double temperature) {
        mTemperature = temperature;
    }

    public int getRain() {
        return mRain;
    }

    public void setRain(int rain) {
        mRain = rain;
    }

    public int getSun() {
        return mSun;
    }

    public void setSun(int sun) {
        mSun = sun;
    }

    public int getWind() {
        return mWind;
    }

    public void setWind(int wind) {
        mWind = wind;
    }


    public String getWindDirection() {
        return mWindDirection;
    }

    public void setWindDirection(String windDirection) {
        mWindDirection = windDirection;
    }

    public String getClouds() {
        return mClouds;
    }

    public void setClouds(String clouds) {
        mClouds = clouds;
    }

    public int getCourseId() {
        return mCourseId;
    }

    public void setCourseId(int courseId) {
        mCourseId = courseId;
    }
}
