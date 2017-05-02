package com.example.android.playgolf;

import java.sql.Time;
import java.util.Date;

import static com.example.android.playgolf.R.id.exactHCP;
import static com.example.android.playgolf.R.id.teeTime;

/**
 * Created by Harri on 1.11.2016.
 */

public class PlayersRound {
    private int mPlayersRoundId;
    private String mTypeofTee;
    private String mTeeTime;
    private double mExactHCP;
    private int mSlope;
    private int mRoundEventId;
    private int mPlayerId;

    public PlayersRound (int playersRoundId, String typeofTee, String teeTime, double exactHCP, int slope, int roundEventId, int playerId) {
        mPlayersRoundId = playersRoundId;
        mTypeofTee = typeofTee;
        mTeeTime = teeTime;
        mExactHCP = exactHCP;
        mSlope = slope;
        mRoundEventId = roundEventId;
        mPlayerId = playerId;
    }

    public int getPlayersRoundId() { return mPlayersRoundId; }
    public void setPlayersRoundId (int playersRoundId) { mPlayersRoundId = playersRoundId; }

    public String getTypeofTee() {
        return mTypeofTee;
    }
    public void setTypeofTee(String typeofTee) {
        mTypeofTee = typeofTee;
    }

    public String getTeeTime() {
        return mTeeTime;
    }
    public void setTeeTime(String teeTime) {
        mTeeTime = teeTime;
    }

    public Double getExactHCP() {
        return mExactHCP;
    }
    public void setExactHCP(Double exactHCP) {
        mExactHCP = exactHCP;
    }

    public int getSlope() {
        return mSlope;
    }
    public void setSlope(int slope) {
        mSlope = slope;
    }

    public int getRoundEventId() {
        return mRoundEventId;
    }
    public void setRoundEventId(int roundEventId) {
        mRoundEventId = roundEventId;
    }

    public int getPlayerId() {
        return mPlayerId;
    }
    public void setPlayerId(int playerId) {
        mPlayerId = playerId;
    }

}
