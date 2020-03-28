package com.example.cduser;

public class UserProfileDb
{

    public String UserId; // unique user database id at time of creating account
    public String UserName; // user name of user
    public String Email; // user name of user
    public String Password; // user name of user
    public String PhoneNo; // user name of user

    public UserProfileDb()
    {
        // empty contructor

    }

    public UserProfileDb( String UserId,String UserName,String Email,String Password,String PhoneNo)
    {
        this.UserId = UserId;
        this.UserName = UserName;
        this.Email = Email;
        this.Password = Password;
        this.PhoneNo = PhoneNo;
    }

}
