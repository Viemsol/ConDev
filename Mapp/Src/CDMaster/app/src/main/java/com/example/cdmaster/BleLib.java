package com.example.cdmaster;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_PING_APP;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_PING_APP_RESP;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_BONDING_PASS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_ERROR;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_SUCESS;

import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_DISPLAY_SHORT_ALEART;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_PAIR_DEVICE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_MAIN_PROGRESS_PERCENT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.RX_CMD_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMEOUT_BLUETOOTH_DISCOVORY;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TX_CMD_SUCESS_NO_RESP;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TX_RXSOCKET_ERROR;
import static com.example.cdmaster.LoopThread.LoopHandler;
import static com.example.cdmaster.MainActivity._Handler_MainHandler;
import static com.example.cdmaster.MainActivity.globalCurrentPairingKey;
import static com.example.cdmaster.MainActivity.pairedDeviceArrayList;
import static com.example.cdmaster.MainActivity.pairedDeviceArrayList_ble_copy;


public class BleLib {

    public static volatile BluetoothSocket bluetoothSocket = null;
    // all BLE connection and Handling
    public static final String TAG = "TAG_BleLib";

    // ALLdevice Level BLE COMMAND

    // ALL BLE Higl LEVEL COMMAND

    // Variables
    public static BluetoothAdapter App_Ba;

    // list of paired devices
    IntentFilter  filter = new IntentFilter();

    public UUID myUUID;
    public final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    private static final int REQUEST_ENABLE_BT = 1;

    //initialise BLE
    public int Ble_Init(Context context) {
        // Handle
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        context.registerReceiver(mReceiver, filter);
        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Log.d(TAG, "Bluetooth is not supported on this device");
            return (BLUETOOTH_ERROR);
        }

        App_Ba = BluetoothAdapter.getDefaultAdapter();
        if (App_Ba == null) {
            Log.d(TAG, "Bluetooth is not supported on this hardware platform");
            return (BLUETOOTH_ERROR);
        }
        Ble_Start(context); // try Starting Bluetooth
        return BLUETOOTH_SUCESS;
    }

    public static BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {

            String action = intent.getAction();

            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                //discovery starts, we can show progress dialog or perform other tasks
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                //discovery finishes, dismis progress dialog
            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                   // SF_ShowTempMsg("Bonded");
                    Log.d(TAG,"Bonded !!!");
                }
                if(device.getBondState() == BluetoothDevice.BOND_NONE)
                {
                    //SF_ShowTempMsg("Not Bonded");
                }
                if(device.getBondState() == BluetoothDevice.BOND_BONDING)
                {
                    //SF_ShowTempMsg("Bonding");
                }
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //bluetooth device found
                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add this device to list
                //add to device lis ony if it starts with CD and length is 6 charecters

                if(device.getName() != null)
                {
                    if((device.getName().length()==6 )&&(device.getName().substring(0,2).equals("CD")) )
                    {
                        pairedDeviceArrayList.add(device.getName() + " ( " + device.getAddress() + "  )");
                        // also copy item to list view
                        pairedDeviceArrayList_ble_copy.add(device);
                    }
                }


            }
            if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction()))
            {
                try
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int varient = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, -1);
                    // hab
                    switch (varient)
                    {
                        case BluetoothDevice.PAIRING_VARIANT_PIN:
                            //TODO: extrect key from device name and pair
                            if(device.getName() != null)
                            {
                                if ((device.getName().length() == 6) && (device.getName().indexOf("CD") == 0)) {
                                    Log.d(TAG,"Started Pairing Device");
                                    device.setPin(globalCurrentPairingKey.getBytes());

                                }

                            }
                            //device.setPairingConfirmation(true);
                           // APP_SendCmdToLooper(BLUETOOTH_BONDING_PASS,device);

                            break;
                        case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                            Log.d(TAG,"Pairedd!!");
                            //App_Sreeen_Visibility(SCREEN_NORMAL);
                            break;

                    }
                }
                catch (Exception e)
                {
                    Log.d(TAG,"Failed Pairing !!!");
                    e.printStackTrace();
                }
            }
        }
    };

    public void Ble_Start(Context ctx)
    {
        //Turn ON BlueTooth if it is OFF
        if (!App_Ba.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)ctx).startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    void Ble_Start_Discovery(Context context)
    {
        context.registerReceiver(mReceiver, filter);

        if (App_Ba.isDiscovering())  // if alredy discovering cancel it
        {
            App_Ba.cancelDiscovery();
        }
        pairedDeviceArrayList.clear();
        pairedDeviceArrayList_ble_copy.clear();

        // No need to update all paired devices
        /*
        Set<BluetoothDevice> pairedDevices = App_Ba.getBondedDevices(); // get list of paird devices

        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)  // format devices to be listed (name MAC)
            {
                pairedDeviceArrayList.add(device.getName() + " ( " + device.getAddress() + "  )");

                // also copy item to list view
                pairedDeviceArrayList_ble_copy.add(device);
            }
        }

         */
        App_Ba.startDiscovery();  // discovering nearby devices , wait for discovry to complte
        // discovry time 1 and half second
        SystemClock.sleep(TIMEOUT_BLUETOOTH_DISCOVORY/3);
        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,30);
        SystemClock.sleep(TIMEOUT_BLUETOOTH_DISCOVORY/3);
        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,60);
        SystemClock.sleep(TIMEOUT_BLUETOOTH_DISCOVORY/3);
        APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,100);
        App_Ba.cancelDiscovery();

        Log.d(TAG,"Nearby devices found :" + pairedDeviceArrayList.size() );
    }

    public void Ble_Try_Connection(BleLibCmd BleCmd)
    {
        BluetoothDevice device =  BleCmd.Device;
        if (App_Ba.isDiscovering())  // if alredy discovering cancel it
        {
            App_Ba.cancelDiscovery();
        }

        String TempText = "Name: " + device.getName() + "\n"
                + "Address: " + device.getAddress() + "\n"
                + "BondState: " + device.getBondState() + "\n"
                + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                + "Class: " + device.getClass();

        Log.d(TAG,TempText);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Device Already Paired ");
                APP_SendCmdToLooper(BLUETOOTH_BONDING_PASS, BleCmd);

            } else {
                Log.d(TAG, "Device Not Paired pairig device... ");
                APP_SendCmdToMainUI(CMD_PAIR_DEVICE, BleCmd.Device);
            }
    }



    public void APP_SendCmdToMainUI(final int command , Object Obj)   //  This function to be used to pass command to looper thread
    {
        Message msg = Message.obtain();

        if (msg != null) {
            msg.what = command;
            msg.obj = Obj;
            _Handler_MainHandler.sendMessage(msg);// place in looper queue
        } else {
            Log.d(TAG, "Null looper Handler");
        }
    }

    public void APP_SendCmdToLooper(int command, Object SubCommand)   //  This function to be used to pass command to looper thread
    {
        Message msg = Message.obtain();
        Log.d(TAG,"Sending to Looper :" + command);
        if(msg != null)
        {
            msg.what = command;
            msg.obj = SubCommand;
            LoopHandler.sendMessage(msg); // send to looper
        }
        else
        {
            Log.d(TAG,"Null looper Handler");
        }
    }

    public boolean Ble_CheckIfBondingSuces(BluetoothDevice device)
    {
        return(device.getBondState() == BluetoothDevice.BOND_BONDED);
    }

    int Send_Receive( BluetoothDevice BleDevice, byte[] cmd, int ReceivFlg , byte[] resp)
    {
       bluetoothSocket = null;
       InputStream connectedInputStream;
       OutputStream connectedOutputStream;
       InputStream in = null;
       OutputStream out = null;

        int bytes_len = 0;
        int temp_st =0;
        int retry=0;
        Log.d(TAG,"Enterd loop");
        while(true) {
            try {
                bluetoothSocket = BleDevice.createRfcommSocketToServiceRecord(myUUID);
                bluetoothSocket.connect();
                in = bluetoothSocket.getInputStream();
                out = bluetoothSocket.getOutputStream();
                connectedInputStream = in;
                connectedOutputStream = out;
                break;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Socket Connection Typ 1 Failed.. Retrying ");
                try {
                    bluetoothSocket = (BluetoothSocket) BleDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(BleDevice, 1);
                    bluetoothSocket.connect();
                    in = bluetoothSocket.getInputStream();
                    out = bluetoothSocket.getOutputStream();
                    connectedInputStream = in;
                    connectedOutputStream = out;
                    break;
                } catch (Exception e2) {
                    retry++;
                    Log.d(TAG, "Socket Connection Typ 2 Failed.. Retrying "+ retry);
                    // close the socket and retry
                    try{
                        bluetoothSocket.close();
                    }
                    catch (IOException t)
                    {

                    }
                    if (retry >= 2) {
                        return (TX_RXSOCKET_ERROR);
                    }
                }
            }
        }
        Log.d(TAG,"Socket Connected");
        try
        {
            connectedOutputStream.write(cmd);
            Log.d(TAG,"Socket Data Sent : " + Arrays.toString(cmd));
            if(ReceivFlg == 1)
            {
                bluetoothSocket.close();
                return(TX_CMD_SUCESS_NO_RESP);
            }
            SystemClock.sleep(150);
            while(bytes_len < resp.length)
            {
                temp_st = connectedInputStream.read(resp,bytes_len,(resp.length - bytes_len));
                if(temp_st>=0)
                {
                    Log.d(TAG, "received Data of length  "+ temp_st);
                    bytes_len +=temp_st;
                }

            }
            if(bytes_len == resp.length)
            {
                Log.d(TAG, "Socket Data Received : " + Arrays.toString(resp));
                bluetoothSocket.close();
                return(RX_CMD_SUCESS);
            }
            else
            {
                bluetoothSocket.close();
                return(TX_RXSOCKET_ERROR);
            }
        }
        catch (IOException e)
        {
            Log.d(TAG,"Socket Read write Failed");
            e.printStackTrace();
            try
            {
                bluetoothSocket.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            return(TX_RXSOCKET_ERROR);
        }
    }
    int Send_Receive_Stream_Flash( BluetoothDevice BleDevice, BleLibCmd Temp_BleLibCmd , byte[] resp)
    {
        bluetoothSocket = null;
        InputStream connectedInputStream;
        OutputStream connectedOutputStream;
        InputStream in = null;
        OutputStream out = null;

        try {
            bluetoothSocket = BleDevice.createRfcommSocketToServiceRecord(myUUID);
            // bluetoothSocket = BleDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
            bluetoothSocket.connect();
            in = bluetoothSocket.getInputStream();
            out = bluetoothSocket.getOutputStream();
            connectedInputStream = in;
            connectedOutputStream = out;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.d(TAG,"Socket Connection ...Failed Retrying(Flash) ");
            try {
                bluetoothSocket = (BluetoothSocket) BleDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(BleDevice, 1);
                bluetoothSocket.connect();
                in = bluetoothSocket.getInputStream();
                out = bluetoothSocket.getOutputStream();
                connectedInputStream = in;
                connectedOutputStream = out;
            }
            catch (Exception e2) {
                Log.e("", "Couldn't establish Bluetooth connection!");
                return(TX_RXSOCKET_ERROR);
            }

        }
       // ------------------------------------Connected with Device Start Flshing ----------------------
        Log.d(TAG,"Socket Connected");
        InputStream is = null;
        BufferedReader bfReader = null;
        try
        {
            is = new ByteArrayInputStream((byte[])( Temp_BleLibCmd ).flashfileobject);
            bfReader = new BufferedReader(new InputStreamReader(is));
            String temp = null;
            String[] strNums;
            int file_size = ((byte[]) ( Temp_BleLibCmd ).flashfileobject).length + 1; // (Not Critial data)1 is added to avoid divide by zero exception
            Log.d(TAG,"Flash File Size :"+file_size);
            int file_read = 0;
            byte[]  flsh_byt;
            while((temp = bfReader.readLine()) != null)
            {
                int bytes_len = 0;
                int temp_st =0;
                file_read += temp.length();

                strNums = temp.split("\\s");
                int length = strNums.length;

                if(((length != 5) && (length != 67) )) //  (length ==0 ) ||
                {
                    // invalid length
                    Log.d(TAG,"invalid length");
                    break;
                }
                flsh_byt = new byte[length];

                for(int i=0; i<strNums.length; i++) // convert string to bytes
                {
                    flsh_byt[i] = (byte)(Integer.parseInt(strNums[i]));
                }

                // send next line
                if(flsh_byt[0] == BLE_UART_PING_APP)
                {
                    // wait for app to reboot
                    SystemClock.sleep(3000);
                }
                connectedOutputStream.write(flsh_byt);
                // check for the responce if applicable
                Log.d(TAG,"Socket Data Sent : " + Arrays.toString(flsh_byt));
                SystemClock.sleep(1000);
                Log.d(TAG,"Flash File read :"+file_read);
                APP_SendCmdToMainUI(CMD_SET_MAIN_PROGRESS_PERCENT,((file_read*100)/file_size)); // display progress

                if((flsh_byt[0] ==7) || (flsh_byt[0] == -1)) // its dummy byte or reflash command which do not respond
                {
                    continue;
                }
                while(bytes_len < resp.length)
                {
                    temp_st = connectedInputStream.read(resp,bytes_len,(resp.length - bytes_len));
                    if(temp_st>=0)
                    {
                        Log.d(TAG, "received Data of length  "+ temp_st + ": " + Arrays.toString(resp));
                        bytes_len +=temp_st;
                    }

                }
                if((bytes_len == resp.length) && (flsh_byt[0] == resp[0])) // RESPONCE SHULD BE OF LENGTH 5 AND  SHULD MATCH COMMAD
                {
                    Log.d(TAG, "Socket Data Received : " + Arrays.toString(resp));
                    // send next data
                }
                else if((bytes_len == resp.length) && (flsh_byt[0] == BLE_UART_PING_APP)) // its last frame to check if application is flashed
                {
                    if(resp[0] == BLE_UART_PING_APP_RESP)
                    {
                        // sucess fult flashed
                        // TODO: verify Version
                        bluetoothSocket.close();
                        return (RX_CMD_SUCESS);
                    }
                    else
                    {
                         break;
                    }
                }
                else
                {
                   break;

                }
            }
            APP_SendCmdToMainUI(CMD_DISPLAY_SHORT_ALEART, "Error while flashing device !!");
            bluetoothSocket.close();
            return(TX_RXSOCKET_ERROR);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {

                if(is != null) is.close();
            }
            catch (Exception ex)
            {

            }
        }

        return(TX_RXSOCKET_ERROR);
    }
    /*
    String BleGetMac(Context ctx)
    {
        String macAddress;
        macAddress = App_Ba.getAddress();
        if (macAddress.equals("02:00:00:00:00:00")) {
            ContentResolver mContentResolver = ctx.getContentResolver();
            macAddress = Settings.Secure.getString(mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);

        }
        Log.d(TAG,macAddress);
        return(macAddress);

    }*/
    boolean BleLibisBleEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }
}