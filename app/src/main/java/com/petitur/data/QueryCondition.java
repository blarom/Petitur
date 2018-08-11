package com.petitur.data;

public class QueryCondition {

    private String operation = "equals";
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
