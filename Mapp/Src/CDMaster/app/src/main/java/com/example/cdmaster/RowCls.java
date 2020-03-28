package com.example.cdmaster;

public class RowCls {

    private int DevImage;
    private String  DevName;


    public RowCls(int devImage, String devName) {
        DevImage = devImage;

        DevName = devName;

    }


    public int getDevImage() {
        return DevImage;
    }

    public String getDevName() {
        return DevName;
    }



}
