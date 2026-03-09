package com.example.cablejournal;

public class Equipment {
    private int _id;
    private String name;
    private String inv;

    Equipment(int _id, String name, String inv) {
        this._id = _id;
        this.name = name;
        this.inv = inv;
    }

    public void setInv(String inv) {
        this.inv = inv;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId()
    {
        return _id;
    }

    public String getInv() {
        return inv;
    }

    public String getName() {
        return name;
    }

}