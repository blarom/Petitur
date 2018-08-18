package com.petitur.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pet implements Parcelable{

    public Pet() {}
    Pet(String name, String gender, String race, String street, String city, String country, int age) {
        this.nm = name;
        this.gn = gender;
        this.rc = race;
        this.st = street;
        this.ct = city;
        this.cn = country;
        this.ag = age;
    }
    public Pet(String uI) {
        this.uI = uI;
    }


    protected Pet(Parcel in) {
        nm = in.readString();
        tp = in.readString();
        uI = in.readString();
        afCP = in.readString();
        oI = in.readString();
        fN = in.readString();
        cn = in.readString();
        ct = in.readString();
        se = in.readString();
        st = in.readString();
        stN = in.readString();
        gac = in.readString();
        galt = in.readString();
        galg = in.readString();
        vU = in.createStringArrayList();
        ag = in.readInt();
        sz = in.readString();
        rc = in.readString();
        gn = in.readString();
        hs = in.readString();
        iUT = in.createStringArrayList();
    }

    public static final Creator<Pet> CREATOR = new Creator<Pet>() {
        @Override
        public Pet createFromParcel(Parcel in) {
            return new Pet(in);
        }

        @Override
        public Pet[] newArray(int size) {
            return new Pet[size];
        }
    };

    private String nm = ""; //name
    public String getNm() {
        return nm;
    }
    public void setNm(String nm) {
        this.nm = nm;
    }

    private String tp = "dog"; //type
    public String getTp() {
        return tp;
    }
    public void setTp(String tp) {
        this.tp = tp;
    }

    private String uI = ""; //Unique identifier
    public String getUI() {
        return uI;
    }
    public void setUI(String uI) {
        this.uI = uI;
    }

    private String afCP; //Associated Foundation contact phone
    public String getAFCP() {
        return afCP;
    }
    public void setAFCP(String FCP) {
        this.afCP = FCP;
    }

    private String oI = ""; //Owner identifier
    public String getOI() {
        return oI;
    }
    public void setOI(String oI) {
        this.oI = oI;
    }

    private String fN = ""; //Associated Foundation name
    public String getFN() {
        return fN;
    }
    public void setFN(String fN) {
        this.fN = fN;
    }

    private String cn = ""; //country
    public String getCn() {
        return cn;
    }
    public void setCn(String cn) {
        this.cn = cn;
    }

    private String ct = ""; //city
    public String getCt() {
        return ct;
    }
    public void setCt(String ct) {
        this.ct = ct;
    }

    private String se = ""; //state
    public String getSe() {
        return se;
    }
    public void setSe(String se) {
        this.se = se;
    }

    private String st = ""; //street
    public String getSt() {
        return st;
    }
    public void setSt(String st) {
        this.st = st;
    }

    private String stN = ""; //street number
    public String getStN() {
        return stN;
    }
    public void setStN(String stN) {
        this.stN = stN;
    }

    private String gac; //Geocoder address Country (requires internet to update)
    public String getGaC() {
        return gac;
    }
    public void setGaC(String gac) {
        this.gac = gac;
    }

    private String galt = "0.0"; //Geocoder address Latitude (requires internet to update)
    public String getGaLt() {
        return galt;
    }
    public void setGaLt(String galt) {
        this.galt = galt;
    }

    private String galg = "0.0"; //Geocoder address Longitude (requires internet to update)
    public String getGaLg() {
        return galg;
    }
    public void setGaLg(String galg) {
        this.galg = galg;
    }

    private List<String> vU = new ArrayList<>(); //video urls
    public List<String> getVU() {
        return vU;
    }
    public void setVU(List<String> vU) {
        this.vU = vU;
    }

    private int ag  = 0; // age
    public int getAg() {
        return ag;
    }
    public void setAg(int ag) {
        this.ag = ag;
    }

    private String sz  = "No size available"; //size
    public String getSz() {
        return sz;
    }
    public void setSz(String sz) {
        this.sz = sz;
    }

    private String rc = "No race available"; //race
    public String getRc() {
        return rc;
    }
    public void setRc(String rc) {
        this.rc = rc;
    }

    private String gn  = "No gender available"; //gender
    public String getGn() {
        return gn;
    }
    public void setGn(String gn) {
        this.gn = gn;
    }

    private boolean gk = false; //good with kids
    public boolean getGK() {
        return gk;
    }
    public void setGK(boolean gk) {
        this.gk = gk;
    }

    private boolean gc = false; //good with cats
    public boolean getGC() {
        return gc;
    }
    public void setGC(boolean gc) {
        this.gc = gc;
    }

    private boolean gd = false; //good with dogs
    public boolean getGD() {
        return gd;
    }
    public void setGD(boolean gd) {
        this.gd = gd;
    }

    private boolean cs = false; //castrated
    public boolean getCs() {
        return cs;
    }
    public void setCs(boolean cs) {
        this.cs = cs;
    }

    private boolean hT = false; //house trained
    public boolean getHT() {
        return hT;
    }
    public void setHT(boolean hT) {
        this.hT = hT;
    }

    private boolean sn = false; //special needs
    public boolean getSN() {
        return sn;
    }
    public void setSN(boolean sn) {
        this.sn = sn;
    }

    private String hs  = "No history available"; //history
    public String getHs() {
        return hs;
    }
    public void setHs(String hs) {
        this.hs = hs;
    }

    private List<String> iUT = Arrays.asList("","","","","",""); //Image upload times
    public List<String> getIUT() {
        return iUT;
    }
    public void setIUT(List<String> iUT) {
        this.iUT = iUT;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nm);
        parcel.writeString(tp);
        parcel.writeString(uI);
        parcel.writeString(afCP);
        parcel.writeString(oI);
        parcel.writeString(fN);
        parcel.writeString(cn);
        parcel.writeString(ct);
        parcel.writeString(se);
        parcel.writeString(st);
        parcel.writeString(stN);
        parcel.writeString(gac);
        parcel.writeString(galt);
        parcel.writeString(galg);
        parcel.writeStringList(vU);
        parcel.writeInt(ag);
        parcel.writeString(sz);
        parcel.writeString(rc);
        parcel.writeString(gn);
        parcel.writeString(hs);
        parcel.writeStringList(iUT);
    }
}
