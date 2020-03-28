package com.example.cduser;

import android.bluetooth.BluetoothDevice;

public class BleLibCmd
{
   public  BluetoothDevice Device;
    byte uart_commad;
   public int Command;

   public int Dev_Rand;   // random number at linking
   public int Master_MAC_4;  // its unique number of master account strored in device
   public int Dev_Typ;    // 0x01 boolean or demo, 0x02
   public int Function;
   public int Function_Data;
   public String Dev_IDName;
   public Object flashfileobject;

   public int position; // basical used for audit, comed from which device list

    public BleLibCmd( int Command, byte uart_commad,BluetoothDevice Device, int Dev_Rand,int Master_MAC_4,int Dev_Typ,int Function,int Function_Data,String Dev_IDName,Object flashfileobject,int position)
    {
        this.Command = Command;     //command to app
        this.uart_commad =uart_commad; // command to chip
        this.Device = Device;       // remote device MAC
        this.Dev_Rand = Dev_Rand;
        this.Master_MAC_4 = Master_MAC_4;
        this.Dev_Typ = Dev_Typ;
        this.Function = Function;
        this.Function_Data = Function_Data;
        this.Dev_IDName =Dev_IDName;
        this.flashfileobject = flashfileobject;
        this.position = position;
    }
}
