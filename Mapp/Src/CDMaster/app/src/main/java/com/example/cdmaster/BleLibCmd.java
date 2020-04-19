package com.example.cdmaster;

import android.bluetooth.BluetoothDevice;

public class BleLibCmd
{
   BluetoothDevice Device;
    byte uart_command;
   int Command;

  int Dev_Rand;   // random number at linking
  int Master_MAC_4;  // its unique number of master account strored in device
   int Dev_Typ;    // 0x01 boolean or demo, 0x02
   int Function;
   int Function_Data;
   String Dev_IDName;
   Object flashfileobject;

    int position; // basical used for audit, comed from which device list


    public BleLibCmd( int Command, byte uart_command,BluetoothDevice Device, int Dev_Rand,int Master_MAC_4,int Dev_Typ,int Function,int Function_Data,String Dev_IDName,Object flashfileobject,int position)
    {
        this.Command = Command;     //command to app
        this.uart_command =uart_command; // command to chip
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
