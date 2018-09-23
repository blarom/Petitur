package com.petitur.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.firestore.GeoPoint;
import com.petitur.resources.Utilities;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Family implements Parcelable{

    public Family() {

    }
    public Family(String pseudonym, String city, String country) {
        this.pn = pseudonym;
        this.ct = city;
        this.cn = country;
    }
    public Family(String firebaseUid) {
        this.oI = firebaseUid;
        setUniqueIdentifierFromDetails();
    }

    private String oI; //Owner identifier

    protected Family(Parcel in) {
        oI = in.readString();
        pn = in.readString();
        uI = in.readString();
        em = in.readString();
        cp = in.readString();
        cn = in.readString();
        cnL = in.readString();
        ct = in.readString();
        ctL = in.readString();
        se = in.readString();
        seL = in.readString();
        xp = in.readString();
        fD = in.readByte() != 0;
        aD = in.readByte() != 0;
        FAD = in.readByte() != 0;
        wTH = in.readByte() != 0;
        gac = in.readString();
        iUT = in.createStringArrayList();
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
        gBP = in.readByte() != 0;
        csP = in.readByte() != 0;
        hTP = in.readByte() != 0;
        snP = in.readByte() != 0;
        dP = in.readInt();
        srT = in.readString();
        fPI = in.createStringArrayList();

        Double lat = in.readDouble();
        Double lng = in.readDouble();
        geo = new GeoPoint(lat, lng);

        dte = new Date(in.readLong());
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

    private String cnL = ""; //country localized (only used locally)
    public String getCnL() {
        return cnL;
    }
    public void setCnL(String cnL) {
        this.cnL = cnL;
    }

    private String se = ""; //state
    public String getSe() {
        return se;
    }
    public void setSe(String se) {
        this.se = se;
    }

    private String seL = ""; //state localized (only used locally)
    public String getSeL() {
        return seL;
    }
    public void setSeL(String seL) {
        this.seL = seL;
    }

    private String ct = ""; //city
    public String getCt() {
        return ct;
    }
    public void setCt(String ct) {
        this.ct = ct;
    }

    private String ctL = ""; //city localized (only used locally)
    public String getCtL() {
        return ctL;
    }
    public void setCtL(String ctL) {
        this.ctL = ctL;
    }

    private String xp = ""; //experience
    public String getXp() {
        return xp;
    }
    public void setXp(String xp) {
        this.xp = xp;
    }

    private boolean fD = false; //Want to foster pets
    public boolean getFD() {
        return fD;
    }
    public void setFD(boolean fD) {
        this.fD = fD;
    }

    private boolean aD = false; //Want to adopt pets
    public boolean getAD() {
        return aD;
    }
    public void setAD(boolean aD) {
        this.aD = aD;
    }

    private boolean FAD = false; //Want to foster and maybe adopt pets
    public boolean getFAD() {
        return FAD;
    }
    public void setFAD(boolean faD) {
        this.FAD = faD;
    }

    private boolean wTH = false; //Want To Help
    public boolean getWTH() {
        return wTH;
    }
    public void setWTH(boolean wTH) {
        this.wTH = wTH;
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

    private List<String> iUT = Arrays.asList("","","","","",""); //Image upload times
    public List<String> getIUT() {
        return iUT;
    }
    public void setIUT(List<String> iUT) {
        this.iUT = iUT;
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

    private boolean gBP = false; //good with birds preference
    public boolean getGBP() {
        return gBP;
    }
    public void setGBP(boolean gBP) {
        this.gBP = gBP;
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

    private int dP = 1000000; //distance preference in meters (default is 1000km)
    public int getDP() {
        return dP;
    }
    public void setDP(int dP) {
        this.dP = dP;
    }

    private String srT = "Distance (Ascending)"; //sort order
    public String getSrT() {
        return srT;
    }
    public void setSrT(String srT) {
        this.srT = srT;
    }

    private List<String> fPI; //favorite pet ids
    public List<String> getFPI() {
        return fPI;
    }
    public void setFPI(List<String> fPI) {
        this.fPI = fPI;
    }

    private Date dte = new Date(); //last modified date
    public Date getDte() {
        return dte;
    }
    public void setDte(Date dte) {
        this.dte = dte;
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
        parcel.writeString(cnL);
        parcel.writeString(ct);
        parcel.writeString(ctL);
        parcel.writeString(se);
        parcel.writeString(seL);
        parcel.writeString(xp);
        parcel.writeByte((byte) (fD ? 1 : 0));
        parcel.writeByte((byte) (aD ? 1 : 0));
        parcel.writeByte((byte) (FAD ? 1 : 0));
        parcel.writeByte((byte) (wTH ? 1 : 0));
        parcel.writeString(gac);
        parcel.writeStringList(iUT);
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
        parcel.writeByte((byte) (gBP ? 1 : 0));
        parcel.writeByte((byte) (csP ? 1 : 0));
        parcel.writeByte((byte) (hTP ? 1 : 0));
        parcel.writeByte((byte) (snP ? 1 : 0));
        parcel.writeInt(dP);
        parcel.writeString(srT);
        parcel.writeStringList(fPI);

        parcel.writeDouble((geo==null)? 0.0 : geo.getLatitude());
        parcel.writeDouble((geo==null)? 0.0 : geo.getLongitude());

        parcel.writeLong(dte.getTime());
    }
}
