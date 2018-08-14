package com.petitur.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.petitur.resources.Utilities;

import java.util.Arrays;
import java.util.List;

public class Family implements Parcelable{

    public Family() {

    }
    public Family(String pseudonym, String street, String city, String country) {
        this.pn = pseudonym;
        this.st = street;
        this.ct = city;
        this.cn = country;
    }
    public Family(String firebaseUid) {
        this.oI = firebaseUid;
        setUniqueIdentifierFromDetails();
    }


    protected Family(Parcel in) {
        oI = in.readString();
        pn = in.readString();
        uI = in.readString();
        em = in.readString();
        cp = in.readString();
        cn = in.readString();
        se = in.readString();
        ct = in.readString();
        st = in.readString();
        xp = in.readString();
        fD = in.readByte() != 0;
        aD = in.readByte() != 0;
        FAD = in.readByte() != 0;
        fP = in.readString();
        hOE = in.readByte() != 0;
        hOD = in.readByte() != 0;
        hOC = in.readByte() != 0;
        hOL = in.readByte() != 0;
        hD = in.readByte() != 0;
        hDW = in.readString();
        hDM = in.readByte() != 0;
        hDN = in.readByte() != 0;
        hDa = in.readByte() != 0;
        hDE = in.readByte() != 0;
        gac = in.readString();
        galt = in.readString();
        galg = in.readString();
        iUT = in.createStringArrayList();
        aP = in.readString();
        sZ = in.readString();
        rP = in.readString();
        gP = in.readString();
        bP = in.readString();
        iP = in.readString();
        tP = in.readString();
    }

    public static final Creator<Family> CREATOR = new Creator<Family>() {
        @Override
        public Family createFromParcel(Parcel in) {
            return new Family(in);
        }

        @Override
        public Family[] newArray(int size) {
            return new Family[size];
        }
    };

    private String oI; //Owner identifier
    public String getOI() {
        return oI;
    }
    public void setOI(String oI) {
        this.oI = oI;
        setUniqueIdentifierFromDetails();
    }

    private String pn = ""; //Pseudonym
    public String getPn() {
        return pn;
    }
    public void setPn(String pseudonym) {
        this.pn = pseudonym;
    }

    private String uI = ""; //unique identifier
    public String getUI() {
        return Utilities.cleanIdentifierForFirebase(uI);
    }
    public void setUI(String uniqueIdentifier) {
        this.uI = Utilities.cleanIdentifierForFirebase(uniqueIdentifier);
    }
    public void setUniqueIdentifierFromDetails() {
        if (!TextUtils.isEmpty(uI)) { } //Keep the uI
        else if (TextUtils.isEmpty(oI)) uI = pn + "-" + em;
        else uI = oI;
        uI = Utilities.cleanIdentifierForFirebase(uI);
    }

    private String em = ""; //email
    public String getEm() {
        return em;
    }
    public void setEm(String em) {
        this.em = em;
    }

    private String cp = ""; //cellphone number
    public String getCp() {
        return cp;
    }
    public void setCp(String cp) {
        this.cp = cp;
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

    private String xp = ""; //experience
    public String getXp() {
        return xp;
    }
    public void setXp(String xp) {
        this.xp = xp;
    }

    private boolean fD = false; //Want to foster dogs
    public boolean getFD() {
        return fD;
    }
    public void setFD(boolean fD) {
        this.fD = fD;
    }

    private boolean aD = false; //Want to adopt dogs
    public boolean getAD() {
        return aD;
    }
    public void setAD(boolean aD) {
        this.aD = aD;
    }

    private boolean FAD = false; //Want to foster and maybe adopt dogs
    public boolean getFAD() {
        return FAD;
    }
    public void setFAD(boolean faD) {
        this.FAD = faD;
    }

    private String fP = ""; //Foster period
    public String getFP() {
        return fP;
    }
    public void setFP(String fT) {
        this.fP = fT;
    }

    private boolean hOE = false; //Help organize adoption events - move equipment
    public boolean getHOE() {
        return hOE;
    }
    public void setHOE(boolean hOE) {
        this.hOE = hOE;
    }

    private boolean hOD = false; //Help organize adoption events - move dogs
    public boolean getHOD() {
        return hOD;
    }
    public void setHOD(boolean hOD) {
        this.hOD = hOD;
    }

    private boolean hOC = false; //Help organize adoption events - coordinating
    public boolean getHOC() {
        return hOC;
    }
    public void setHOC(boolean hOC) {
        this.hOC = hOC;
    }

    private boolean hOL = false; //Help organize adoption events - Lending a hand at events
    public boolean getHOL() {
        return hOL;
    }
    public void setHOL(boolean hOL) {
        this.hOL = hOL;
    }

    private boolean hD = false; //Help by dogwalking
    public boolean getHD() {
        return hD;
    }
    public void setHD(boolean hD) {
        this.hD = hD;
    }

    private String hDW = ""; //Help by dogwalking - where
    public String getHDW() {
        return hDW;
    }
    public void setHDW(String hDw) {
        this.hDW = hDw;
    }

    private boolean hDM = false; //Help by dogwalking - morning
    public boolean getHDM() {
        return hDM;
    }
    public void setHDM(boolean hDm) {
        this.hDM = hDm;
    }

    private boolean hDN = false; //Help by dogwalking - noon
    public boolean getHDN() {
        return hDN;
    }
    public void setHDN(boolean hDn) {
        this.hDN = hDn;
    }

    private boolean hDa = false; //Help by dogwalking - afternoon
    public boolean setHDA() {
        return hDa;
    }
    public void getHDA(boolean hDa) {
        this.hDa = hDa;
    }

    private boolean hDE = false; //Help by dogwalking - evening
    public boolean getHDE() {
        return hDE;
    }
    public void setHDE(boolean hDe) {
        this.hDE = hDe;
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

    private List<String> iUT = Arrays.asList("","","","","",""); //Image upload times
    public List<String> getIUT() {
        return iUT;
    }
    public void setIUT(List<String> iUT) {
        this.iUT = iUT;
    }

    private String aP = ""; //age preference
    public String getAP() {
        return aP;
    }
    public void setAP(String aP) {
        this.aP = aP;
    }

    private String sZ = ""; //size preference
    public String getSP() {
        return sZ;
    }
    public void setSP(String sZ) {
        this.sZ = sZ;
    }

    private String rP = ""; //race preference
    public String getRP() {
        return rP;
    }
    public void setRP(String rP) {
        this.rP = rP;
    }

    private String gP = ""; //gender preference
    public String getGP() {
        return gP;
    }
    public void setGP(String gP) {
        this.gP = gP;
    }

    private String bP = ""; //behavior preference
    public String getBP() {
        return bP;
    }
    public void setBP(String bP) {
        this.bP = bP;
    }

    private String iP = ""; //interactions preference
    public String getIP() {
        return iP;
    }
    public void setIP(String iP) {
        this.iP = iP;
    }

    private String tP = "dog"; //pet type preference
    public String gettP() {
        return tP;
    }
    public void settP(String tP) {
        this.tP = tP;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(oI);
        parcel.writeString(pn);
        parcel.writeString(uI);
        parcel.writeString(em);
        parcel.writeString(cp);
        parcel.writeString(cn);
        parcel.writeString(se);
        parcel.writeString(ct);
        parcel.writeString(st);
        parcel.writeString(xp);
        parcel.writeByte((byte) (fD ? 1 : 0));
        parcel.writeByte((byte) (aD ? 1 : 0));
        parcel.writeByte((byte) (FAD ? 1 : 0));
        parcel.writeString(fP);
        parcel.writeByte((byte) (hOE ? 1 : 0));
        parcel.writeByte((byte) (hOD ? 1 : 0));
        parcel.writeByte((byte) (hOC ? 1 : 0));
        parcel.writeByte((byte) (hOL ? 1 : 0));
        parcel.writeByte((byte) (hD ? 1 : 0));
        parcel.writeString(hDW);
        parcel.writeByte((byte) (hDM ? 1 : 0));
        parcel.writeByte((byte) (hDN ? 1 : 0));
        parcel.writeByte((byte) (hDa ? 1 : 0));
        parcel.writeByte((byte) (hDE ? 1 : 0));
        parcel.writeString(gac);
        parcel.writeString(galt);
        parcel.writeString(galg);
        parcel.writeStringList(iUT);
        parcel.writeString(aP);
        parcel.writeString(sZ);
        parcel.writeString(rP);
        parcel.writeString(gP);
        parcel.writeString(bP);
        parcel.writeString(iP);
        parcel.writeString(tP);
    }
}
