package com.example.cduser;

public class AuditDb
{
    public String Time;     // Holds Device version
    public String Action; // this is Master user db ID , used by cred user to update audit

    public AuditDb()
    {
        // empty contructor

    }

    public AuditDb( String Time,String Action) {
        this.Time = Time;
        this.Action = Action;
    }
}
