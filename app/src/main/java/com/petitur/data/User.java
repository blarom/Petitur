package com.petitur.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.petitur.resources.Utilities;

public class User implements Parcelable {

    public User() {}

    public User(String uniqueIdentifier) {
        this.uI = uniqueIdentifier;
    }

    protected User(Parcel in) {
        nm = in.readString();
        oI = in.readString();
        uI = in.readString();
        em = in.readString();
        lC = in.readByte() != 0;
        iF = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private String nm = ""; //name
    public String getNm() {
        return nm;
    }
    public void setNm(String nm) {
        this.nm = nm;
    }

    private String oI = ""; //owner identifier
    public String getOI() {
        return oI;
    }
    public void setOI(String oI) {
        this.oI = oI;
    }

    private String uI = ""; //unique identifier
    public String getUI() {
        return Utilities.cleanIdentifierForFirebase(uI);
    }
    public void setUI(String uI) {
        this.uI = Utilities.cleanIdentifierForFirebase(uI);
    }

    private String em = ""; //email
    public String getEm() {
        return em;
    }
    public void setEm(String em) {
        this.em = em;
    }

    private boolean lC = true; //limit query to country preference
    public boolean getLC() {
        return lC;
    }
    public void setLC(boolean lC) {
        this.lC = lC;
    }

    private boolean iF = false; //isFoundation
    public boolean getIF() {
        return iF;
    }
    public void setIF(boolean iF) {
        this.iF = iF;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nm);
        parcel.writeString(oI);
        parcel.writeString(uI);
        parcel.writeString(em);
        parcel.writeByte((byte) (lC ? 1 : 0));
        parcel.writeByte((byte) (iF ? 1 : 0));
    }
}
