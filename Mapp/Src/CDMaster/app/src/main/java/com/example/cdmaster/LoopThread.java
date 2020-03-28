package com.example.cdmaster;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_COMMISION;

import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FLASH;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FUCTION_GET_GPIO;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FUCTION_SET_GPIO;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_PING_APP;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_PING_APP_RESP;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_BONDING_PASS;

import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_COMMISION_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_COMMISION_PASS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_GET_STATUS_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_GET_STATUS_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_DIV_DISCOVERY_COMPLETE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_ERROR;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_FLASH_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_FLASH_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_GET_VERSION_SUCCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_CONNECT_COMMISION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_CRED;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_CRED_STATUS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_FLASH;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_PING;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DIV_DISCOVERY_5SEC;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_GET_VERSION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_INIT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_DISPLAY_SHORT_ALEART;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_ACTION_DIALOG_STATUS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_DISPLY_VISIBILITY;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_MAIN_DIALOG_STATUS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_MAIN_PROGRESS_PERCENT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.DEVICE_COMMISION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.DEVICE_OTA;
import static com.example.cdmaster.GLOBAL_CONSTANTS.DEVICE_PAIR_CONNECT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.DEVICE_SEND_DATA;
import static com.example.cdmaster.GLOBAL_CONSTANTS.DEVICE_SEND_RCV_DATA;

import static com.example.cdmaster.GLOBAL_CONSTANTS.ENCRIPTION_EN;
import static com.example.cdmaster.GLOBAL_CONSTANTS.EVENT_TIMER;
import static com.example.cdmaster.GLOBAL_CONSTANTS.MAX_CMD_RESP_LEN_APP;
import static com.example.cdmaster.GLOBAL_CONSTANTS.RX_CMD_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.SCREEN_LIST_NEARBY_DEVICE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.SCREEN_NORMAL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.SCREEN_WAIT_INPROGRESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMEOUT_BLUETOOTH_CONNECT_PAIR;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMEOUT_COMMISON_FRAME;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMEOUT_FLSHING_FRAME;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_ONESHOT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_PAIR_TIMEOUT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_RX_TIMEOUT;
import static com.example.cdmaster.MainActivity._Handler_MainHandler;
import static com.example.cdmaster.Security.nen_enc_dec;


public class LoopThread extends Thread {
    public Context ctx;
    public LoopThread(String name,Context context)
    {
        super(name);
        this.ctx = context;
    }
    // this handles all beground activitys
    private static final String TAG = "TAG_LooperThread";
    public static Handler LoopHandler;
    public BleLib bleLib = new BleLib();
    public static volatile TimerScheduler  Timer_Sched= new TimerScheduler();

    @Override
    public void run()
    {

        Log.d(TAG,"Starting looper Thread");
        Timer_Sched.start();
        Looper.prepare();
        LoopHandler = new  Handler(){
            @Override
            public void handleMessage(Message msg) {
                Handle_Message(msg);
            }
        };
        Looper.loop();    // wait for task to perform from UI/Main Thread Disovery,Socket,etc

        Log.d(TAG,"Ending of Looper Thread");
    }
    public Handler getHandler()
    {
        return LoopHandler;
    }

    public void APP_SendCmdToMainUI(final int command , Object Obj)   //  This function to be used to pass command to looper thread
    {
        Message msg = Message.obtain();
        Log.d(TAG, "Sending message to UI, Command " + command + " , Obj :"+ Obj);
        if (msg != null) {
            msg.what = command;
            msg.obj = Obj;
            _Handler_MainHandler.sendMessage(msg);// place in main queue
        } else {
            Log.d(TAG, "Null looper Handler");
        }
    }
    public void Lpr_SendCmdToLooper(int command, Object SubCommand)   //  This function to be used to pass command to looper thread
    {
        Message msg = Message.obtain();
        Log.d(TAG,"Send Cmd :" + command);
        if(msg != null)
        {
            msg.what = command;
            msg.obj = SubCommand;
            LoopHandler.sendMessage(msg);// place in looper queue
        }
        else
        {
            Log.d(TAG,"Null looper Handler");
        }
    }
    public void Handle_Message(Message msg)
    {
        int Temp_Progress ;
        Temp_Progress = 0;
        byte Session_key = 0,MacKey = 0;
        Log.d(TAG,"executing Command " + msg.what);
        switch(msg.what)
        {
            case CMD_BLUETOOTH_INIT:
                if( BLUETOOTH_ERROR == bleLib.Ble_Init(ctx))
                {
                    APP_SendCmdToMainUI(BLUETOOTH_ERROR,"Ble Error");
                }
                else
                {
                    APP_SendCmdToMainUI(BLUETOOTH_SUCESS,"Ble Sucess");
                }
                break;
            case CMD_BLUETOOTH_DIV_DISCOVERY_5SEC:

                APP_SendCmdToMainUI(CMD_SET_MAIN_DIALOG_STATUS,"Discovering Devices");
                APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,Temp_Progress);
                APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_STATUS,"Discovering Devices");
                APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT,Temp_Progress);

                APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_WAIT_INPROGRESS);
                bleLib.Ble_Start_Discovery(ctx); // TODO check if object can be returnd this way

                APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_LIST_NEARBY_DEVICE);
                APP_SendCmdToMainUI(BLUETOOTH_DIV_DISCOVERY_COMPLETE,"Ok");

                APP_SendCmdToMainUI(CMD_SET_MAIN_DIALOG_STATUS,"Device Discovery Complete");
                APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,Temp_Progress);
                APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_STATUS,"Discovering Devices");
                APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT,Temp_Progress);

            break;
            case CMD_BLUETOOTH_GET_VERSION:
            case CMD_BLUETOOTH_DEV_FLASH:
            case CMD_BLUETOOTH_DEV_PING:
            case CMD_BLUETOOTH_DEV_CRED:
            case CMD_BLUETOOTH_CONNECT_COMMISION:
                Temp_Progress+=20;
                APP_SendCmdToMainUI(CMD_SET_MAIN_DIALOG_STATUS,"Connecting to Device");
                APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,Temp_Progress);
                APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_STATUS,"Connecting to Device");
                APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT,Temp_Progress);


                APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_WAIT_INPROGRESS);
                Timer_Sched.Start_Timer( TIMER_PAIR_TIMEOUT,TIMEOUT_BLUETOOTH_CONNECT_PAIR,TIMER_ONESHOT,msg.obj);



                bleLib.Ble_Try_Connection((BleLibCmd) msg.obj,ctx);
            break;

            case EVENT_TIMER:
               if(msg.arg1 == TIMER_PAIR_TIMEOUT)
               {
                   Lpr_SendCmdToLooper(BLUETOOTH_BONDING_PASS, msg.obj); //Todo : this condition ts not tested
               }
           break;

            case BLUETOOTH_BONDING_PASS: // send commision command

                Timer_Sched.Stop_Time(TIMER_PAIR_TIMEOUT);
                BluetoothDevice Temp_BleDev = ((BleLibCmd) msg.obj).Device;
                int Temp_OriginalCmd = ((BleLibCmd) msg.obj).Command;
                int random = new Random().nextInt(256) + 1;

                byte CmdPing[] = new byte[]{BLE_UART_PING_APP,(byte) random,0x00,0x00,0x00};
                byte CmdCredCommision[] = new byte[]{BLE_UART_COMMISION, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                        // CMD              //ENC        // RAND   // MAC      //MAC       //MAC       //MAC    //DEVID
                        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
                        //DEVID       // DEVID          //DEVID    // DEVTYP    //FUNCTION   // DATA  //DATA     //CRC
                }; // 16
                byte[] RespPing = new byte[5];
                byte[] RespCredCommision = new byte[MAX_CMD_RESP_LEN_APP];
                // get command
                CmdCredCommision[0] = (((BleLibCmd) msg.obj).uart_commad);
                // get random no
                CmdCredCommision[2] = (byte)(((BleLibCmd) msg.obj).Dev_Rand);

                MacKey =   CmdCredCommision[2];  // MAC key as rendom byte
                //get Uniue master number
                CmdCredCommision[3] = (byte)((((BleLibCmd) msg.obj).Master_MAC_4)& 0xFF);
                CmdCredCommision[4] = (byte)(((((BleLibCmd) msg.obj).Master_MAC_4)>>8)& 0xFF);
                CmdCredCommision[5] = (byte)(((((BleLibCmd) msg.obj).Master_MAC_4)>>16)& 0xFF);
                CmdCredCommision[6] = (byte)(((((BleLibCmd) msg.obj).Master_MAC_4)>>24)& 0xFF);

                 // get Dev type
                CmdCredCommision[11] = (byte)(((BleLibCmd) msg.obj).Dev_Typ);

                // get function
                CmdCredCommision[12] = (byte)(((BleLibCmd) msg.obj).Function);

                // get function data
                CmdCredCommision[13] = (byte)(((BleLibCmd) msg.obj).Function_Data);
                CmdCredCommision[14] = (byte)(((BleLibCmd) msg.obj).Function_Data>>8);

                if(((BleLibCmd) msg.obj).Dev_IDName.length() == 6) // device ID is present and valid
                {
                    byte[] Dev_ID_db = (((BleLibCmd) msg.obj).Dev_IDName).getBytes();
                    // update device ID
                    CmdCredCommision[7] = Dev_ID_db[2];
                    CmdCredCommision[8] = Dev_ID_db[3];
                    CmdCredCommision[9] = Dev_ID_db[4];
                    CmdCredCommision[10] = Dev_ID_db[5];
                }

                if(bleLib.Ble_CheckIfBondingSuces(Temp_BleDev))
                {


                    // device is already bonded try connecting and send command
                    int tmp_status;
                    Log.d(TAG, "Pairing OK !!");
                    Temp_Progress+=20;
                    APP_SendCmdToMainUI(CMD_SET_MAIN_DIALOG_STATUS,"Sending Command to Device");
                    APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,Temp_Progress);
                    APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_STATUS,"Sending Command to Device");
                    APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT,Temp_Progress);
                    switch (Temp_OriginalCmd)
                    {
                        case CMD_BLUETOOTH_DEV_PING:
                            Timer_Sched.Start_Timer(TIMER_RX_TIMEOUT, TIMEOUT_COMMISON_FRAME, TIMER_ONESHOT, msg.obj);
                            tmp_status = bleLib.Send_Receive(Temp_BleDev, CmdPing, 1, RespPing);
                            Timer_Sched.Stop_Time(TIMER_RX_TIMEOUT);
                        break;
                        case CMD_BLUETOOTH_CONNECT_COMMISION:
                            MacKey = (((BleLibCmd) msg.obj).Dev_IDName).getBytes()[2];
                        case CMD_BLUETOOTH_GET_VERSION:
                        case  CMD_BLUETOOTH_DEV_FLASH:
                            //1) send commision frame with all data
                            // 2) wait and start flashing sequence

                        case  CMD_BLUETOOTH_DEV_CRED:
                            // send credential frame
                        case   CMD_BLUETOOTH_DEV_CRED_STATUS:
                            // send credential get status frame


                            CmdCredCommision[15] = CRC_Chk(CmdCredCommision, MAX_CMD_RESP_LEN_APP, 1,MacKey);

                            if(ENCRIPTION_EN == 1)
                            {
                                Timer_Sched.Start_Timer(TIMER_RX_TIMEOUT, TIMEOUT_COMMISON_FRAME, TIMER_ONESHOT, msg.obj);
                                tmp_status = bleLib.Send_Receive(Temp_BleDev, CmdPing, 0, RespPing);
                                Timer_Sched.Stop_Time(TIMER_RX_TIMEOUT);
                                if ((tmp_status == RX_CMD_SUCESS))
                                {
                                    // get key
                                    RespPing[1] ^= CmdPing[1];
                                    RespPing[1] ^=  CmdCredCommision[7];
                                    // calculate session key
                                    Session_key = RespPing[1];
                                    // encrypt the data befor sending
                                    Log.d(TAG, "Raw Data" + Arrays.toString(CmdCredCommision) );
                                    Log.d(TAG,"MAC Key:" + MacKey);
                                            Log.d(TAG,"Session key :" + Session_key);
                                    nen_enc_dec(CmdCredCommision,Session_key);
                                }

                            }

                            // SEND COMMISION FRAME
                            Timer_Sched.Start_Timer(TIMER_RX_TIMEOUT, TIMEOUT_COMMISON_FRAME, TIMER_ONESHOT, msg.obj);

                            if(((BleLibCmd) msg.obj).Function == BLE_UART_FLASH)  // reset will happen and no responce will be received
                            {
                                tmp_status = bleLib.Send_Receive(Temp_BleDev, CmdCredCommision, 1, RespCredCommision);
                            }
                            else
                            {
                                tmp_status = bleLib.Send_Receive(Temp_BleDev, CmdCredCommision, 0, RespCredCommision);

                            }
                            Timer_Sched.Stop_Time(TIMER_RX_TIMEOUT);

                            if(ENCRIPTION_EN == 1 && (tmp_status == RX_CMD_SUCESS) ) {
                                nen_enc_dec(RespCredCommision, Session_key);
                            }
                            if ((tmp_status == RX_CMD_SUCESS) && (1 == CRC_Chk(RespCredCommision, MAX_CMD_RESP_LEN_APP, 0, MacKey)))
                            {

                                switch (Temp_OriginalCmd)
                                {
                                    case  CMD_BLUETOOTH_DEV_FLASH:
                                        Temp_Progress=0;
                                        APP_SendCmdToMainUI(CMD_SET_MAIN_DIALOG_STATUS,"Flashing Device");
                                        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,Temp_Progress);
                                        APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_STATUS,"Flashing Device");
                                        APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT,Temp_Progress);

                                        // commision sucess send flash frames
                                        Timer_Sched.Start_Timer(TIMER_RX_TIMEOUT, TIMEOUT_FLSHING_FRAME, TIMER_ONESHOT, msg.obj);
                                        tmp_status = bleLib.Send_Receive_Stream_Flash(Temp_BleDev, (BleLibCmd) msg.obj, CmdPing);
                                        Timer_Sched.Stop_Time(TIMER_RX_TIMEOUT);

                                        if (tmp_status == RX_CMD_SUCESS)
                                        {
                                            APP_SendCmdToMainUI(CMD_DISPLAY_SHORT_ALEART, "Device Flashed. Success !!");
                                            APP_SendCmdToMainUI(BLUETOOTH_FLASH_SUCESS, msg.obj);
                                        }
                                        else
                                        {
                                            APP_SendCmdToMainUI(CMD_DISPLAY_SHORT_ALEART, "Device Flashing Fail !!");
                                            APP_SendCmdToMainUI(BLUETOOTH_FLASH_FAIL, msg.obj);
                                        }
                                        APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);

                                        break;

                                    case CMD_BLUETOOTH_DEV_CRED:


                                        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,100);

                                        if( (RespCredCommision[0] == BLE_UART_COMMISION) &&  ((RespCredCommision[12] ==  BLE_UART_FUCTION_SET_GPIO) || (RespCredCommision[12] ==  BLE_UART_FUCTION_GET_GPIO)) )
                                        {
                                             (((BleLibCmd) msg.obj).Function_Data) = RespCredCommision[13];
                                            (((BleLibCmd) msg.obj).Function_Data) |= ((int)RespCredCommision[14]<<8);
                                            Log.d(TAG, "Credential pass Data" + ((BleLibCmd) msg.obj).Function_Data);



                                            APP_SendCmdToMainUI(BLUETOOTH_CRED_SUCESS, msg.obj);
                                        }
                                        else
                                        {
                                            Log.d(TAG, "Credential Failed");
                                            APP_SendCmdToMainUI(BLUETOOTH_CRED_FAIL, Temp_BleDev);
                                        }
                                        break;

                                    case CMD_BLUETOOTH_CONNECT_COMMISION:
                                        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,100);
                                        if (RespCredCommision[0] == BLE_UART_COMMISION)
                                        {
                                            // device is captured now read the responce data and store it
                                            String Dev_ID = (Temp_BleDev).getName();      // Unique device serial number Alpha numaric ie CD5678
                                            int Dev_Typ = RespCredCommision[11];        // Device type 1 for demo ,
                                            int BL_version = RespCredCommision[13];
                                            int App_version = RespCredCommision[14];
                                            int Dev_Rand = RespCredCommision[2];       // commision link number generated by lock on commision

                                            int Mast_Id = ((int) RespCredCommision[3] | ((int) RespCredCommision[4] << 8) | ((int) RespCredCommision[5] << 16) | ((int) RespCredCommision[6] << 24));

                                            Log.d(TAG, "Mast_Id" + Mast_Id); // Master ID after commision of uniue id of master
                                            String Dev_Mac = (Temp_BleDev).getAddress();
                                            String Dev_Name = Dev_ID;    // user given name for device
                                            String Dev_Img = "NA";     // image for device user selected
                                            String User_Cred = "";      // for master its same,  this is linked to phone user and canot useit on other phone
                                            boolean Is_Master = true;  // 1 is master else user chredential
                                            String Dev_Version = "Ver " + (Dev_Typ) + "." + (BL_version) + "." + (App_version);
                                            Log.d(TAG,"Version" + Dev_Version);
                                            DeviceDb DeviceDb_temp = new DeviceDb(Dev_ID, Dev_Typ, Dev_Rand, Dev_Mac, Mast_Id, Dev_Name, Dev_Img, User_Cred, Is_Master,Dev_Version,"","","","","",0);
                                            msg.obj = DeviceDb_temp;

                                            APP_SendCmdToMainUI(BLUETOOTH_COMMISION_PASS, msg.obj);
                                        }
                                        else
                                        {
                                            APP_SendCmdToMainUI(BLUETOOTH_COMMISION_FAIL, Temp_BleDev);
                                        }
                                        break;
                                    case CMD_BLUETOOTH_GET_VERSION:
                                        String Dev_ID = (Temp_BleDev).getName();      // Unique device serial number Alpha numaric ie CD5678
                                        int BL_version = RespCredCommision[13];
                                        int App_version = RespCredCommision[14];
                                        int Dev_Typ = RespCredCommision[11];        // Device type 1 for demo ,
                                        String Dev_Version = "Ver " + (Dev_Typ) + "." + (BL_version) + "." + (App_version);
                                        Log.d(TAG,"Version" + Dev_Version);

                                        DeviceDb DeviceDb_temp = new DeviceDb(Dev_ID, 0, 0, "", 0, "", "", "", true,Dev_Version,"","","","","",0);
                                        msg.obj = DeviceDb_temp;

                                        APP_SendCmdToMainUI(BLUETOOTH_GET_VERSION_SUCCESS, msg.obj);

                                        break;
                                }
                            }
                            else  // Device is in bootloader mode : Botloader commands
                            {
                                switch (Temp_OriginalCmd)
                                {
                                    case CMD_BLUETOOTH_DEV_FLASH:
                                        // commision faild so might be in bootloader
                                        Temp_Progress=0;
                                        APP_SendCmdToMainUI(CMD_SET_MAIN_DIALOG_STATUS,"Flashing Device");
                                        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,Temp_Progress);
                                        APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_STATUS,"Flashing Device");
                                        APP_SendCmdToMainUI(CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT,Temp_Progress);

                                        Timer_Sched.Start_Timer(TIMER_RX_TIMEOUT, TIMEOUT_FLSHING_FRAME, TIMER_ONESHOT, msg.obj);
                                        tmp_status = bleLib.Send_Receive_Stream_Flash(Temp_BleDev, (BleLibCmd) msg.obj, CmdPing);
                                        Timer_Sched.Stop_Time(TIMER_RX_TIMEOUT);

                                        if (tmp_status == RX_CMD_SUCESS)
                                        {
                                            APP_SendCmdToMainUI(CMD_DISPLAY_SHORT_ALEART, "Device Flashed. Success !!");

                                            APP_SendCmdToMainUI(BLUETOOTH_FLASH_SUCESS, msg.obj);
                                        }
                                        else
                                        {
                                            APP_SendCmdToMainUI(CMD_DISPLAY_SHORT_ALEART, "Device Flashing Fail !!");
                                            APP_SendCmdToMainUI(BLUETOOTH_FLASH_FAIL, msg.obj);
                                        }
                                        APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                                        break;
                                    case CMD_BLUETOOTH_CONNECT_COMMISION:
                                        Log.d(TAG, "Bonding Failed");
                                        APP_SendCmdToMainUI(BLUETOOTH_COMMISION_FAIL, Temp_BleDev);
                                        break;
                                    case CMD_BLUETOOTH_DEV_CRED:
                                        Log.d(TAG, "Credential Failed");
                                        APP_SendCmdToMainUI(BLUETOOTH_CRED_FAIL, Temp_BleDev);
                                        break;
                                    case CMD_BLUETOOTH_DEV_CRED_STATUS:
                                        Log.d(TAG, "Credential Status Read Failed");
                                        APP_SendCmdToMainUI(BLUETOOTH_CRED_GET_STATUS_FAIL, Temp_BleDev);
                                        break;
                                    case CMD_BLUETOOTH_GET_VERSION:
                                        Log.d(TAG, "Version read fail Failed");

                                        break;
                                }
                                APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                            }
                        break;
                        default:
                            //invalid command
                        break;
                    }
                }
                else
                {
                    switch (Temp_OriginalCmd)
                    {
                        case CMD_BLUETOOTH_CONNECT_COMMISION:
                        Log.d(TAG, "Bonding Failed");
                        APP_SendCmdToMainUI(BLUETOOTH_COMMISION_FAIL, Temp_BleDev);
                        break;
                        case CMD_BLUETOOTH_DEV_CRED:
                        Log.d(TAG, "Credential Failed");
                        APP_SendCmdToMainUI(BLUETOOTH_CRED_FAIL, Temp_BleDev);
                        break;
                        case CMD_BLUETOOTH_DEV_FLASH:
                            Log.d(TAG, "Flashing Failed");
                            APP_SendCmdToMainUI(CMD_DISPLAY_SHORT_ALEART,"Device Flashing Failed");
                            APP_SendCmdToMainUI(BLUETOOTH_FLASH_FAIL, msg.obj);
                            break;
                    }
                    APP_SendCmdToMainUI(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                }
                break;
            case DEVICE_PAIR_CONNECT:
                Log.d(TAG,"executing DEVICE_PAIR_CONNECT");
                break;
            case DEVICE_SEND_RCV_DATA:
                Log.d(TAG,"executing DEVICE_SEND_RCV_DATA");
                break;
            case DEVICE_COMMISION:
                Log.d(TAG,"executing DEVICE_COMMISION");
                break;
            case DEVICE_SEND_DATA:
                Log.d(TAG,"executing DEVICE_SEND_DATA");
                break;
            case DEVICE_OTA:
                Log.d(TAG,"executing DEVICE_OTA");
                break;
                default:
                    Log.d(TAG,"COMMAND NOT FOUND!!");
                    break;
        }
    }

    byte CRC_Chk(byte data[], int len , int cmd_crc_calc,byte mac_key)
    {
        len = len -1;
        byte st=0;
        int sum = 0,i=0;
        while(len>0)
        {
            sum+=data[i];
            i++;
            len--;
        }
        if(ENCRIPTION_EN == 1)
        {
            sum ^= mac_key;
        }
        if(cmd_crc_calc == 1) // CRC calculate
        {
            Log.d(TAG,"Checksum :" + (byte)sum);
            return((byte)sum);
        }

        if(((byte)sum) == data[i])
        {
            st =1;
            Log.d(TAG,"Checksum matching ");
        }
        Log.d(TAG,"Checksum" + sum);
        return(st);
    }
}
