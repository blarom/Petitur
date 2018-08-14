package com.petitur.data;

public class QueryCondition {

    public QueryCondition() { }

    public QueryCondition(String operation, String key, String valueString, boolean valueBoolean, int valueInteger) {
        this.operation = operation;
        this.key = key.toLowerCase();
        this.valueString = valueString;
        this.valueBoolean = valueBoolean;
        this.valueInteger = valueInteger;
    }

    private String operation = "default";
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }

    private String key = "";
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    private String valueString = "";
    public String getValueString() {
        return valueString;
    }
    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    private boolean valueBoolean = true;
    public boolean getValueBoolean() {
        return valueBoolean;
    }
    public void setValueBoolean(boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    private int valueInteger = 0;
    public int getValueInteger() {
        return valueInteger;
    }
    public void setValueInteger(int valueInteger) {
        this.valueInteger = valueInteger;
    }
}
