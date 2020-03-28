package com.example.cduser;

// EveryTime user is linked to device this user is added to lock DB
public class CredUserProfile
{
    public String UserId; // unique user database id (unique ID of User Personal phone)
    public String UserName; // user name of user
    public String PhoneNo; // user name of user
    public String email; // user name of user
    public String userCred;
    public CredUserProfile()
    {
        // empty contructor
    }

    public CredUserProfile( String UserId,String UserName,String PhoneNo,String email,String userCred)
    {
        this.UserId   = UserId;
        this.UserName = UserName;
        this.PhoneNo  = PhoneNo;
        this.email = email;
        this.userCred = userCred;
    }
}
