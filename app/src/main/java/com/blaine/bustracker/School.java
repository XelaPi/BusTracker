package com.blaine.bustracker;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Parcelable wrapper class for a school
 *
 * @author Alex Vanyo
 */
public class School implements Parcelable {

    private final int mID;
    private final String mName;
    private final int mNumRows;
    private final ArrayList<String> mRowNames;
    private final int mDefaultRow;

    public School(int id, String name, int numRows, ArrayList<String> rowNames, int defaultRow) {
        mID = id;
        mName = name;
        mNumRows = numRows;
        mRowNames = rowNames;
        mDefaultRow = defaultRow;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public int getNumRows() {
        return mNumRows;
    }

    public ArrayList<String> getRowNames() {
        return mRowNames;
    }

    // TODO: Remove default row
    public int getDefaultRow() {
        return mDefaultRow;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mID);
        out.writeString(mName);
        out.writeInt(mNumRows);
        out.writeStringList(mRowNames);
        out.writeInt(mDefaultRow);
    }

    public static final Parcelable.Creator<School> CREATOR = new Parcelable.Creator<School>() {
        public School createFromParcel(Parcel in) {
            return new School(in);
        }

        public School[] newArray(int size) {
            return new School[size];
        }
    };

    private School(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mNumRows = in.readInt();
        mRowNames = new ArrayList<>();
        in.readStringList(mRowNames);
        mDefaultRow = in.readInt();
    }

    @Override
    public String toString() {
        return getName();
    }
}
