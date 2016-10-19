package com.blaine.bustracker;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable wrapper class for a bus
 *
 * @author Alex Vanyo
 */
public class Bus implements Parcelable {

    private final int mRow;
    private final String mNumber;

    /**
     * @param row    Bus's row, zero-indexed integer
     * @param number Bus number as a String
     */
    public Bus(int row, String number) {
        mRow = row;
        mNumber = number;
    }

    public int getRow() {
        return mRow;
    }

    public String getNumber() {
        return mNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mRow);
        out.writeString(mNumber);
    }

    public static final Parcelable.Creator<Bus> CREATOR = new Parcelable.Creator<Bus>() {
        public Bus createFromParcel(Parcel in) {
            return new Bus(in);
        }

        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };

    private Bus(Parcel in) {
        mRow = in.readInt();
        mNumber = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bus && ((Bus) obj).getNumber().equals(mNumber) && ((Bus) obj).getRow() == mRow;
    }
}
