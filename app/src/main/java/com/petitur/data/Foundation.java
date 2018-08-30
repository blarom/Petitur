package com.petitur.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.firestore.GeoPoint;
import com.petitur.resources.Utilities;

import java.util.Arrays;
import java.util.List;

public class Foundation implements Parcelable {

    public Foundation() { }
    Foundation(String name, String street, String city, String country) {
        this.st = street;
        this.nm = name;
        this.ct = city;
        this.cn = country;
    }
    public Foundation(String ownerfirebaseUid) {
        this.oI = ownerfirebaseUid;
        setUniqueIdentifierFromDetails();
    }

    protected Foundation(Parcel in) {
        nm = in.readString();
        oI = in.readString();
        uI = in.readString();
        wb = in.readString();
        cn = in.readString();
        se = in.readString();
        ct = in.readString();
        st = in.readString();
        stN = in.readString();
        cP = in.readString();
        cE = in.readString();
        iUT = in.createStringArrayList();
        gac = in.readString();
        tP = in.readString();
        gP = in.readString();
        aP = in.readString();
        sZ = in.readString();
        dRP = in.readString();
        cRP = in.readString();
        pRP = in.readString();
        cLP = in.readString();
        gKP = in.readByte() != 0;
        gCP = in.readByte() != 0;
        gDP = in.readByte() != 0;
        csP = in.readByte() != 0;
        hTP = in.readByte() != 0;
        snP = in.readByte() != 0;
        dP = in.readInt();
        srT = in.readString();
        srA = in.readByte() != 0;
    }

    public static final Creator<Foundation> CREATOR = new Creator<Foundation>() {
        @Override
        public Foundation createFromParcel(Parcel in) {
            return new Foundation(in);
        }

        @Override
        public Foundation[] newArray(int size) {
            return new Foundation[size];
        }
    };

    private String nm = "Default"; //name
    public String getNm() {
        return nm;
    }
    public void setNm(String nm) {
        this.nm = nm;
    }

    private String oI; // ownerfirebaseUid
    public String getOI() {
        return oI;
    }
    public void setOI(String oI) {
        this.oI = oI;
        setUniqueIdentifierFromDetails();
    }

    private String uI = ""; //unique identifier
    public String getUI() {
        return Utilities.cleanIdentifierForFirebase(uI);
    }
    public void setUI(String uI) {
        this.uI = Utilities.cleanIdentifierForFirebase(uI);
    }
    public void setUniqueIdentifierFromDetails() {
        if (!TextUtils.isEmpty(uI)) { } //Keep the uI
        else if (TextUtils.isEmpty(oI)) uI = nm + "-" + ct + "-" + cn;
        else uI = oI;
        uI = Utilities.cleanIdentifierForFirebase(uI);
    }

    private String wb = ""; //website
    public String getWb() {
        return wb;
    }
    public void setWb(String wb) {
        this.wb = wb;
    }

    private String cn = ""; //country
    public String getCn() {
        return cn;
    }
    public void setCn(String cn) {
        this.cn = cn;
    }

    private String se = ""; //state
    public String getSe() {
        return se;
    }
    public void setSe(String se) {
        this.se = se;
    }

    private String ct = ""; //city
    public String getCt() {
        return ct;
    }
    public void setCt(String ct) {
        this.ct = ct;
    }

    private String st = ""; //street
    public String getSt() {
        return st;
    }
    public void setSt(String st) {
        this.st = st;
    }

    private String stN = ""; //Street number
    public String getStN() {
        return stN;
    }
    public void setStN(String stN) {
        this.stN = stN;
    }

    private String cP = ""; //contect phone
    public String getCP() {
        return cP;
    }
    public void setCP(String cP) {
        this.cP = cP;
    }

    private String cE = ""; //contact email
    public String getCE() {
        return cE;
    }
    public void setCE(String cE) {
        this.cE = cE;
    }

    private List<String> iUT = Arrays.asList("","","","","",""); //Image upload times
    public List<String> getIUT() {
        return iUT;
    }
    public void setIUT(List<String> iUT) {
        this.iUT = iUT;
    }

    private String gac; //Geocoder address Country (requires internet to update)
    public String getGaC() {
        return gac;
    }
    public void setGaC(String gac) {
        this.gac = gac;
    }

    private GeoPoint geo; //Geopoint with latitude then longitude
    public GeoPoint getGeo() {
        return geo;
    }
    public void setGeo(GeoPoint geo) {
        this.geo = geo;
    }

    private String tP = "Dog"; //pet type preference
    public String getTP() {
        return tP;
    }
    public void setTP(String tP) {
        this.tP = tP;
    }

    private String gP = "Any"; //gender preference
    public String getGP() {
        return gP;
    }
    public void setGP(String gP) {
        this.gP = gP;
    }

    private String aP = "Any"; //age preference
    public String getAP() {
        return aP;
    }
    public void setAP(String aP) {
        this.aP = aP;
    }

    private String sZ = "Any"; //size preference
    public String getSP() {
        return sZ;
    }
    public void setSP(String sZ) {
        this.sZ = sZ;
    }

    private String dRP = "Any"; //dog breed preference
    public String getDRP() {
        return dRP;
    }
    public void setDRP(String dRP) {
        this.dRP = dRP;
    }

    private String cRP = "Any"; //cat breed preference
    public String getCRP() {
        return cRP;
    }
    public void setCRP(String cRP) {
        this.cRP = cRP;
    }

    private String pRP = "Any"; //parrot breed preference
    public String getPRP() {
        return pRP;
    }
    public void setPRP(String pRP) {
        this.pRP = pRP;
    }

    private String cLP = "Any"; //coat length preference
    public String getCLP() {
        return cLP;
    }
    public void setCLP(String cLP) {
        this.cLP = cLP;
    }

    private boolean gKP = false; //good with kids preference
    public boolean getGKP() {
        return gKP;
    }
    public void setGKP(boolean gKP) {
        this.gKP = gKP;
    }

    private boolean gCP = false; //good with cats preference
    public boolean getGCP() {
        return gCP;
    }
    public void setGCP(boolean gCP) {
        this.gCP = gCP;
    }

    private boolean gDP = false; //good with dogs preference
    public boolean getGDP() {
        return gDP;
    }
    public void setGDP(boolean gdP) {
        this.gDP = gdP;
    }

    private boolean csP = false; //castrated preference
    public boolean getCsP() {
        return csP;
    }
    public void setCsP(boolean csP) {
        this.csP = csP;
    }

    private boolean hTP = false; //house trained preference
    public boolean getHTP() {
        return hTP;
    }
    public void setHTP(boolean hTP) {
        this.hTP = hTP;
    }

    private boolean snP = false; //special needs preference
    public boolean getSNP() {
        return snP;
    }
    public void setSNP(boolean snP) {
        this.snP = snP;
    }

    private int dP = 0; //distance preference
    public int getDP() {
        return dP;
    }
    public void setDP(int dP) {
        this.dP = dP;
    }

    private String srT = "Distance"; //sort type
    public String getSrT() {
        return srT;
    }
    public void setSrT(String srT) {
        this.srT = srT;
    }

    private boolean srA = true; //sort ascending
    public boolean getSrA() {
        return srA;
    }
    public void setSrA(boolean srA) {
        this.srA = srA;
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
        parcel.writeString(wb);
        parcel.writeString(cn);
        parcel.writeString(se);
        parcel.writeString(ct);
        parcel.writeString(st);
        parcel.writeString(stN);
        parcel.writeString(cP);
        parcel.writeString(cE);
        parcel.writeStringList(iUT);
        parcel.writeString(gac);
        parcel.writeString(tP);
        parcel.writeString(gP);
        parcel.writeString(aP);
        parcel.writeString(sZ);
        parcel.writeString(dRP);
        parcel.writeString(cRP);
        parcel.writeString(pRP);
        parcel.writeString(cLP);
        parcel.writeByte((byte) (gKP ? 1 : 0));
        parcel.writeByte((byte) (gCP ? 1 : 0));
        parcel.writeByte((byte) (gDP ? 1 : 0));
        parcel.writeByte((byte) (csP ? 1 : 0));
        parcel.writeByte((byte) (hTP ? 1 : 0));
        parcel.writeByte((byte) (snP ? 1 : 0));
        parcel.writeInt(dP);
        parcel.writeString(srT);
        parcel.writeByte((byte) (srA ? 1 : 0));
    }
}
