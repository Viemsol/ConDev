package com.example.cduser;

import static com.example.cduser.GLOBAL_CONSTANTS.MAX_CRED_FOR_USE_FREE;

public class DeviceDb implements Cloneable
{
     // do not change the sequence of variables of device db they they are ixed to values any new variable add below
    public String Dev_ID;      // Unique device NAME ie CD5678
    public int Dev_Typ;        // Device type 1 for demo ,
    public int Dev_Rand;       // commision link Random number
    public String Dev_Mac;     // Mac addrees of Lock
    public int Mast_Id;        // its first 4 cherector of UID in database
    public String Dev_Name;    // Name of the device for display
    public String Dev_Img;     // Image of the device for display
    public String User_Cred;      // for USER : first 4 byte of Mobile MAC ,MASTER: First 4 byte of  UID
    public boolean Is_Master;  // 1 is master else users credential
    public int MAX_CRED;       // meximum credential master user can distribute
    public int NUM_CRED_USED;  // credential master have distributed
    public String Version;     // Holds Device version
    public boolean Deleted;     // Holds Device version

    // fields used by Cred user only
    public String DeviceBelongsTo; // this is Master user db ID , used by cred user to update audit

    public String ActivationDate; // (DD/MM/YYYY)
    public String ExpiryDate;     // (DD/MM/YYYY)
    public String StartTime;      // (HH/MM)
    public String EndTime;         // (HH/MM)
    public  int OneTimeAccess;

    public String PairingPw;
    public DeviceDb()
    {
        // empty contructor

    }
    public DeviceDb( String Dev_ID,int Dev_Typ,int Dev_Rand,String Dev_Mac,int Mast_Id,String Dev_Name,String Dev_Img,String User_Cred,boolean Is_Master,String Version,String DeviceBelongsTo,String ActivationDate,String ExpiryDate,String StartTime,String EndTime,int OneTimeAccess,String PairingPw)
    {
        this.Dev_ID = Dev_ID;
        this.Dev_Typ = Dev_Typ;
        this.Dev_Rand = Dev_Rand;
        this.Dev_Mac = Dev_Mac;
        this.Mast_Id = Mast_Id;
        this.Dev_Name = Dev_Name;
        this.Dev_Img = Dev_Img;
        this.User_Cred = User_Cred;
        this.Is_Master = Is_Master;
        this.MAX_CRED = MAX_CRED_FOR_USE_FREE;
        this.NUM_CRED_USED = 0;
        this.Version = Version;
        this.DeviceBelongsTo = DeviceBelongsTo;

        this.ActivationDate = ActivationDate; // (DD/MM/YYYY)
        this.ExpiryDate = ExpiryDate;     // (DD/MM/YYYY)
        this.StartTime = StartTime;      // (HH/MM)
        this.EndTime  = EndTime;         // (HH/MM)
        this.OneTimeAccess = OneTimeAccess;
        this.PairingPw = PairingPw;
    }

    @Override
    public Object clone()throws CloneNotSupportedException
    {
        return super.clone();
    }

}

