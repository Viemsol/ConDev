package com.example.cduser;

public class AuditDb
{
    public String Time;     // Holds Device version
    public String Action; // this is Master user db ID , used by cred user to update audit
    public String UsrName; // user Name
    public AuditDb()
    {
        // empty contructor

    }

    public AuditDb( String UsrName, String Time,String Action) {
        this.UsrName = UsrName; // user name % Phone % User ID
        this.Time = Time;
        this.Action = Action;
    }
}
