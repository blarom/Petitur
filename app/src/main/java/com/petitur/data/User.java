package com.petitur.data;

import com.petitur.resources.Utilities;

public class User {

    public User() {}

    public User(String uniqueIdentifier) {
        this.uI = uniqueIdentifier;
    }

    private String nm = ""; //name
    public String getNm() {
        return nm;
    }
    public void setNm(String nm) {
        this.nm = nm;
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

}
