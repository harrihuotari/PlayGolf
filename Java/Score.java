package com.example.android.playgolf;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import static android.R.attr.data;
import static java.lang.Integer.parseInt;

/**
 * Created by Harri on 21.10.2016.
 */

public class Score implements Parcelable {

    private int mHoleId;
    private int mHoleNumber;
    private int mHolePar;
    private int mHoleHCP;
    private int mHoleScore;
    private int mSlopeScore;
    private int mPutts;
    private boolean mFairwayHit;
    private boolean mGreenHit;
    private boolean mHoleDetailsFilledIn;

    public Score (int holeId, int holeNumber, int holePar, int holeHCP, int holeScore, int slopeScore, int putts, boolean fairwayHit, boolean greenHit, boolean holeDetailsFilledIn) {
        mHoleId = holeId;
        mHoleNumber = holeNumber;
        mHolePar = holePar;
        mHoleHCP = holeHCP;
        mHoleScore = holeScore;
        mSlopeScore = slopeScore;
        mPutts = putts;
        mFairwayHit = fairwayHit;
        mGreenHit = greenHit;
        mHoleDetailsFilledIn = holeDetailsFilledIn;
    }

    protected Score(Parcel in) {
        mHoleId = in.readInt();
        mHoleNumber = in.readInt();
        mHolePar = in.readInt();
        mHoleHCP = in.readInt();
        mHoleScore = in.readInt();
        mSlopeScore = in.readInt();
        mPutts = in.readInt();
        mFairwayHit = in.readByte() != 0;
        mGreenHit = in.readByte() != 0;
        mHoleDetailsFilledIn = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mHoleId);
        dest.writeInt(mHoleNumber);
        dest.writeInt(mHolePar);
        dest.writeInt(mHoleHCP);
        dest.writeInt(mHoleScore);
        dest.writeInt(mSlopeScore);
        dest.writeInt(mPutts);
        dest.writeByte( (byte) (mFairwayHit ? 1 : 0));
        dest.writeByte( (byte) (mGreenHit ? 1 : 0));
        dest.writeByte( (byte) (mHoleDetailsFilledIn ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Score> CREATOR = new Creator<Score>() {
        @Override
        public Score createFromParcel(Parcel in) {
            return new Score(in);
        }

        @Override
        public Score[] newArray(int size) {
            return new Score[size];
        }
    };

    public int getHoleId() {

        return mHoleId;
    }

    public int getHoleNumber() {

        return mHoleNumber;
    }

    public int getHolePar() {
        return mHolePar;
    }

    public int getHoleHCP() {
        return mHoleHCP;
    }
    public int getHoleScore() {
        return mHoleScore;
    }
    public int getSlopeScore() {
        return mSlopeScore;
    }

    public int getPutts() {
        return mPutts;
    }

    public boolean getFairwayHit() {
        return mFairwayHit;
    }

    public boolean getGreenHit() {
        return mGreenHit;
    }

    public boolean getHoleDetailsFilledIn() {return mHoleDetailsFilledIn; }

    public void setHoleId (int holeId) {
        mHoleId = holeId;
    }

    public void setHoleScore (int holeScore) {
        mHoleScore = holeScore;
    }
    public void setSlopeScore (int slopeScore) {
        mSlopeScore = slopeScore;
    }

    public void setPutts (int putts) {
        mPutts = putts;
    }
    public void setFairwayHit (boolean fairwayHit) {
        mFairwayHit = fairwayHit;
    }
    public void setGreenHit (boolean greenHit) {
        mGreenHit = greenHit;
    }
    public void setHoleDetailsFilledIn (boolean holeDetailsFilledIn) {mHoleDetailsFilledIn = holeDetailsFilledIn; }

}
