package com.petitur.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pet implements Parcelable, Comparable<Pet> {

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


    private String nm = ""; //name

    protected Pet(Parcel in) {
        nm = in.readString();
        nmL = in.readString();
        tp = in.readString();
        uI = in.readString();
        oI = in.readString();
        cn = in.readString();
        cnL = in.readString();
        ct = in.readString();
        ctL = in.readString();
        se = in.readString();
        seL = in.readString();
        st = in.readString();
        stL = in.readString();
        stN = in.readString();
        gac = in.readString();
        vU = in.createStringArrayList();
        agR = in.readString();
        ag = in.readInt();
        sz = in.readString();
        rc = in.readString();
        gn = in.readString();
        cL = in.readString();
        gk = in.readByte() != 0;
        gc = in.readByte() != 0;
        gd = in.readByte() != 0;
        cs = in.readByte() != 0;
        hT = in.readByte() != 0;
        sn = in.readByte() != 0;
        hs = in.readString();
        iUT = in.createStringArrayList();
        dt = in.readInt();
        fv = in.readByte() != 0;

        Double lat = in.readDouble();
        Double lng = in.readDouble();
        geo = new GeoPoint(lat, lng);

        vet = new HashMap<>();
        in.readMap(vet,Object.class.getClassLoader());
        fam = new HashMap<>();
        in.readMap(fam,Object.class.getClassLoader());
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

    public String getNm() {
        return nm;
    }
    public void setNm(String nm) {
        this.nm = nm;
    }

    private String nmL = ""; //name in local language
    public String getNmL() {
        return nmL;
    }
    public void setNmL(String nmL) {
        this.nmL = nmL;
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

    private String oI = ""; //Owner identifier
    public String getOI() {
        return oI;
    }
    public void setOI(String oI) {
        this.oI = oI;
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

    private String st = ""; //street
    public String getSt() {
        return st;
    }
    public void setSt(String st) {
        this.st = st;
    }

    private String stL = ""; //street localized (only used locally)
    public String getStL() {
        return stL;
    }
    public void setStL(String stL) {
        this.stL = stL;
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

    private GeoPoint geo; //Geopoint with latitude then longitude
    public GeoPoint getGeo() {
        return geo;
    }
    public void setGeo(GeoPoint geo) {
        this.geo = geo;
    }

    private List<String> vU = new ArrayList<>(); //video urls
    public List<String> getVU() {
        return vU;
    }
    public void setVU(List<String> vU) {
        this.vU = vU;
    }

    private String agR = ""; // age range
    public String getAgR() {
        return agR;
    }
    public void setAgR(String agR) {
        this.agR = agR;
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

    private String cL = "No coat length available"; //coat length
    public String getCL() {
        return cL;
    }
    public void setCL(String cl) {
        this.cL = cl;
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

    private int dt = 0; //distance (only used locally)
    public int getDt() {
        return dt;
    }
    public void setDt(int dt) {
        this.dt = dt;
    }

    private boolean fv = false; //favorite flag (only used locally)
    public boolean getFv() {
        return fv;
    }
    public void setFv(boolean fv) {
        this.fv = fv;
    }

    private Map<String, String> vet = new HashMap<>(); //Veterinary events
    public Map<String, String> getVet() {
        return vet;
    }
    public void setVet(Map<String, String> vet) {
        this.vet = vet;
    }

    private Map<String, String> fam = new HashMap<>(); //Fostering families
    public Map<String, String> getFam() {
        return fam;
    }
    public void setFam(Map<String, String> fam) {
        this.fam = fam;
    }

    //region Comparator interface
    @Override public int compareTo(@NonNull Pet pet) {
        //see: https://www.mkyong.com/java/java-object-sorting-example-comparable-and-comparator/

        int compareDistance = pet.getDt();

        //ascending order
        return this.dt - compareDistance;

        //descending order
        //return compareDistance - this.dt;
    }
    public static Comparator<Pet> PetNameComparator = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            String petName1 = pet1.getNm().toUpperCase();
            String petName2 = pet2.getNm().toUpperCase();

            //ascending order
            return petName1.compareTo(petName2);

            //descending order
            //return petName2.compareTo(petName1);
        }

    };
    public static Comparator<Pet> PetDistanceComparatorAscending = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            int petDistance1 = pet1.getDt();
            int petDistance2 = pet2.getDt();
            return petDistance1 - petDistance2;
        }

    };
    public static Comparator<Pet> PetDistanceComparatorDescending = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            int petDistance1 = pet1.getDt();
            int petDistance2 = pet2.getDt();
            return petDistance2 - petDistance1;
        }

    };
    public static Comparator<Pet> PetAgeComparatorAscending = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            int petAge1 = pet1.getAg();
            int petAge2 = pet2.getAg();
            return petAge1 - petAge2;
        }

    };
    public static Comparator<Pet> PetAgeComparatorDescending = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            int petAge1 = pet1.getAg();
            int petAge2 = pet2.getAg();
            return petAge2 - petAge1;
        }

    };
    public static Comparator<Pet> PetBreedComparatorAscending = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            String petBreed1 = pet1.getRc().toUpperCase();
            String petBreed2 = pet2.getRc().toUpperCase();
            return petBreed1.compareTo(petBreed2);
        }

    };
    public static Comparator<Pet> PetBreedComparatorDescending = new Comparator<Pet>() {

        public int compare(Pet pet1, Pet pet2) {

            String petBreed1 = pet1.getRc().toUpperCase();
            String petBreed2 = pet2.getRc().toUpperCase();
            return petBreed2.compareTo(petBreed1);
        }

    };
    //endregion

    //Parcelable interface
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nm);
        parcel.writeString(nmL);
        parcel.writeString(tp);
        parcel.writeString(uI);
        parcel.writeString(oI);
        parcel.writeString(cn);
        parcel.writeString(cnL);
        parcel.writeString(ct);
        parcel.writeString(ctL);
        parcel.writeString(se);
        parcel.writeString(seL);
        parcel.writeString(st);
        parcel.writeString(stL);
        parcel.writeString(stN);
        parcel.writeString(gac);
        parcel.writeStringList(vU);
        parcel.writeString(agR);
        parcel.writeInt(ag);
        parcel.writeString(sz);
        parcel.writeString(rc);
        parcel.writeString(gn);
        parcel.writeString(cL);
        parcel.writeByte((byte) (gk ? 1 : 0));
        parcel.writeByte((byte) (gc ? 1 : 0));
        parcel.writeByte((byte) (gd ? 1 : 0));
        parcel.writeByte((byte) (cs ? 1 : 0));
        parcel.writeByte((byte) (hT ? 1 : 0));
        parcel.writeByte((byte) (sn ? 1 : 0));
        parcel.writeString(hs);
        parcel.writeStringList(iUT);
        parcel.writeInt(dt);
        parcel.writeByte((byte) (fv ? 1 : 0));

        parcel.writeDouble(geo.getLatitude());
        parcel.writeDouble(geo.getLongitude());
        parcel.writeMap(vet);
        parcel.writeMap(fam);
    }




}
