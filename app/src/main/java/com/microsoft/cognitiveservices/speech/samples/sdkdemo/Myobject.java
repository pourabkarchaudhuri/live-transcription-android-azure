package com.microsoft.cognitiveservices.speech.samples.sdkdemo;

import android.os.Parcel;
import android.os.Parcelable;

public class Myobject implements Parcelable {
    String mData;

    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mData);
    }

    public static final Parcelable.Creator<Myobject> CREATOR = new Parcelable.Creator<Myobject>() {
        public Myobject createFromParcel(Parcel in) {
            return new Myobject(in);
        }

        public Myobject[] newArray(int size) {
            return new Myobject[size];
        }
    };

    Myobject(Parcel in) {
        mData = in.readString();
    }

    Myobject() {
    }
}
