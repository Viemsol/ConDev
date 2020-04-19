package com.example.cdmaster;

// EveryTime user is linked to device this user is added to lock DB
public class CredUserProfile
{
    public String UserId;
    public String UserName; // user name of user
    public String PhoneNo; // user name of user
    public String UserCred;
    public String Validity;
    public CredUserProfile()
    {
        // empty contructor
    }

    public CredUserProfile( String UserId,String UserName,String PhoneNo,String UserCred,String Validity)
    {
        this.UserId   = UserId;
        this.UserName = UserName;
        this.PhoneNo  = PhoneNo;
        this.UserCred = UserCred;
        this.Validity = Validity;
    }


}
