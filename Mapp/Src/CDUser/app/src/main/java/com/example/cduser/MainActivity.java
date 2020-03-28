package com.example.cduser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.PendingIntent.getActivity;
import static com.example.cduser.BleLib.App_Ba;
import static com.example.cduser.BleLib.bluetoothSocket;
import static com.example.cduser.BleLib.col_macAddressParts;
import static com.example.cduser.GLOBAL_CONSTANTS.BLE_UART_COMMISION;


import static com.example.cduser.GLOBAL_CONSTANTS.BLE_UART_FUCTION_SET_GPIO;
import static com.example.cduser.GLOBAL_CONSTANTS.BLE_UART_GET_VERSION;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_COMMISION_FAIL;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_COMMISION_PASS;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_CRED_FAIL;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_CRED_SUCESS;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_DIV_DISCOVERY_COMPLETE;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_ERROR;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_FLASH_SUCESS;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_GET_VERSION_SUCCESS;
import static com.example.cduser.GLOBAL_CONSTANTS.BLUETOOTH_SUCESS;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_BLUETOOTH_CONNECT_COMMISION;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_CRED;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_FLASH;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DIV_DISCOVERY_5SEC;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_BLUETOOTH_GET_VERSION;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_BLUETOOTH_INIT;

import static com.example.cduser.GLOBAL_CONSTANTS.CMD_DISPLAY_SHORT_ALEART;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_PAIR_DEVICE;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_SET_DISPLY_VISIBILITY;
import static com.example.cduser.GLOBAL_CONSTANTS.CMD_SET_PROGRESS_PERCENT;
import static com.example.cduser.GLOBAL_CONSTANTS.EVENT_TIMER;
import static com.example.cduser.GLOBAL_CONSTANTS.SCREEN_LIST_NEARBY_DEVICE;
import static com.example.cduser.GLOBAL_CONSTANTS.SCREEN_NORMAL;
import static com.example.cduser.GLOBAL_CONSTANTS.SCREEN_WAIT_INPROGRESS;
import static com.example.cduser.GLOBAL_CONSTANTS.TIMER_RX_TIMEOUT;
import static com.example.cduser.ServLib.isEmailValid;


public class MainActivity extends AppCompatActivity {

    private LoopThread _Thread_LoopHandThread = new LoopThread("Looper",this);
    public static volatile Handler _Handler_MainHandler;

    final private String TAG = "TAG_MAIN_Thread";

    // Variable View
    RecyclerView recyclerView;
    RowAdaptor adaptor;
    List<RowCls> RowList;   // holds the list to be displayed

    //Db
    FirebaseAuth mFirebaseAuth;
    FirebaseStorage mFirebaseStorage;
    StorageReference mStorageRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    DatabaseReference TempRef;
    public static ArrayList<DeviceDb> Device_List =new ArrayList<>();

    private Button App_ButAddDev, App_ButHelp, App_ButQuit,App_ButCancel,App_ButLogout;
    private TextView App_TxtVPercentage;
    RelativeLayout App_loadingPanel;
    ListView Lw_BleDev_temp;
    public static ArrayList<DeviceDb> Device_Image_List =new ArrayList<>();
    public static ArrayList<String> pairedDeviceArrayList = new ArrayList<>();;  // list of paired devices
    public static ArrayList<BluetoothDevice> pairedDeviceArrayList_ble_copy = new ArrayList<>(); // hold copy
    ArrayAdapter<String> pairedDeviceAdapter ;//= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,pairedDeviceArrayList);

    // Variable View END
    @Override
    public  void onBackPressed()
    {
        // Alert user and take input
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage("Want to Exit ?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

               // Intent i =  MainActivity.this.getIntent();
                // clear flag
                MainActivity.this.finish();
                finishAffinity();
                System.exit(0);
            }
        });
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // send command
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @Override
    public  void onPause()
    {
        super.onPause();
        Log.d(TAG,"Pausing Main Activity");
       // MainActivity.this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // PERMISSIONS ------------------------------------------------------
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is to avoid restart of activiry/app on orientation change

         final int PERMISSION_REQUEST_CODE = 1;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }


        RowList = new ArrayList<>(); // hold all current rows
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Cross heck if not loged in
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");


        mStorageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null)
        {
            Log.d(TAG,"Signed in sucess , setting welcome title...");
            // Name, email address etc
            String name = user.getDisplayName();
            String email = user.getEmail();
            getSupportActionBar().setSubtitle("Welcome " + name);
            //getSupportActionBar().setTitle(getSupportActionBar().getTitle() + );
        }
        mAuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser == null)
                {
                    // go back to Login
                    Log.d(TAG,"Please sign in");
                    Intent i = new Intent(MainActivity.this, Login.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    MainActivity.this.finish();
                }
                else
                {
                    // on chnge listener update the devices from firebase
                }
            }
        };
        _Handler_MainHandler = new Handler(getMainLooper()) // handle message received from looper
        {
            @Override
            public void handleMessage(@NonNull Message msg)
            {
                super.handleMessage(msg);
                App_HandleLooperMsg(msg);
            }
        };

        _Thread_LoopHandThread.start(); // Start Looper thread : to handle non UI task
        SystemClock.sleep(10); // wait for looper thread to start


        // check if BLE is supported and initialize BLE

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        APP_SendCmdToLooper(CMD_BLUETOOTH_INIT,"CMD_BLUETOOTH_INIT");

        // Create all buttons
        App_Create_Buttons();  //  TODO change all buttons to images and handle click refer https://www.youtube.com/watch?v=HMjI7cLsyfw&list=PLrnPJCHvNZuBtTYUuc5Pyo4V7xZ2HNtf4&index=5

        App_Create_List(); // TODO remove this once app is ready

        App_Build_Recycler_View();
        App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_WAIT_INPROGRESS);


    }

    public void APP_SendCmdToLooper(int command, Object SubCommand)   //  This function to be used to pass command to looper thread
    {
        Message msg = Message.obtain();
        Log.d(TAG,"Send Cmd :" + command);
        if(msg != null)
        {
            msg.what = command;
            msg.obj = SubCommand;
            _Thread_LoopHandThread.getHandler().sendMessage(msg);// place in looper queue
        }
        else
        {
            Log.d(TAG,"Null looper Handler");
        }
    }

    void App_HandleLooperMsg(Message msg) // all messages from other threads are handled in this
    {
        Log.d(TAG,"Received Cmd : " + msg.what + " obj "+ msg.obj);
        switch(msg.what)
        {
            case EVENT_TIMER:
            if(msg.arg1 == TIMER_RX_TIMEOUT) // Bluettooth read task is stuck as no data is received
            {
                switch (((BleLibCmd)(msg.obj)).Command)
                {
                    case CMD_BLUETOOTH_DEV_FLASH:
                    case CMD_BLUETOOTH_DEV_CRED:
                    case CMD_BLUETOOTH_CONNECT_COMMISION:
                        Log.d(TAG,"No Responce received ");
                        try
                        {
                            bluetoothSocket.close(); // Start Looper thread : to handle non UI task
                        }catch(IOException e1)
                        {
                            e1.printStackTrace();
                        }
                    break;


                }
            }
            break;
            case CMD_SET_PROGRESS_PERCENT:
                App_TxtVPercentage.setText(((int)msg.obj) + " %");
                break;
            case CMD_DISPLAY_SHORT_ALEART:
                Toast.makeText(this, (String)msg.obj, Toast.LENGTH_SHORT).show();
                break;
            case CMD_SET_DISPLY_VISIBILITY:
                switch ((int)msg.obj)
                {
                    case SCREEN_NORMAL:
                        App_ButAddDev.setClickable(true);
                        recyclerView.setVisibility(View.VISIBLE);
                        App_ButCancel.setVisibility(View.GONE);
                        Lw_BleDev_temp.setVisibility(View.GONE);
                        App_loadingPanel.setVisibility(View.GONE);
                        App_TxtVPercentage.setVisibility(View.GONE);
                        pairedDeviceAdapter.notifyDataSetChanged();
                       // adaptor.notify();
                        break;
                    case SCREEN_LIST_NEARBY_DEVICE:

                        App_ButAddDev.setClickable(true);
                        recyclerView.setVisibility(View.GONE);
                        App_ButCancel.setVisibility(View.VISIBLE);
                        Lw_BleDev_temp.setVisibility(View.VISIBLE);
                        App_loadingPanel.setVisibility(View.GONE);
                        App_TxtVPercentage.setVisibility(View.GONE);
                        pairedDeviceAdapter.notifyDataSetChanged();
                        break;
                    case SCREEN_WAIT_INPROGRESS: // any thing in progress
                        App_ButAddDev.setClickable(false);
                        recyclerView.setVisibility(View.GONE);
                        App_ButCancel.setVisibility(View.GONE);
                        Lw_BleDev_temp.setVisibility(View.GONE);
                        App_loadingPanel.setVisibility(View.VISIBLE);
                        App_TxtVPercentage.setVisibility(View.VISIBLE);
                        pairedDeviceAdapter.notifyDataSetChanged();
                        break;
                }
                break;
            case BLUETOOTH_ERROR:
                MainActivity.this.finish();
                break;
            case BLUETOOTH_SUCESS:

                break;
            case BLUETOOTH_DIV_DISCOVERY_COMPLETE:

                pairedDeviceAdapter.notifyDataSetChanged(); // update list
                break;

            case BLUETOOTH_COMMISION_FAIL:
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART," Comission Failed!");
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                break;
            case BLUETOOTH_CRED_SUCESS:
                // TODO change function_data with specific values
                String Action;
                if((((BleLibCmd) msg.obj).Function_Data) != 0) {
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " Device Turned On ");
                    Action = "Device Turned On";

                }
                else {
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " Device Turned Off");
                    Action = "Device Turned Off";
                }

                // generate audit
                // TODO: PROMPT USER FOR MORE INPUTS
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String Mast_UID = Device_List.get(((BleLibCmd) msg.obj).position).DeviceBelongsTo;
                TempRef = FirebaseDatabase.getInstance().getReference("User");
                AuditDb AuditDb_temp = new AuditDb(currentDateandTime,Action);
                TempRef.child(Mast_UID).child("Devices").child(Device_List.get(((BleLibCmd) msg.obj).position).Dev_ID).child("Audits").child("User_Audit").child(UID).setValue(AuditDb_temp).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful())
                        {

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                // log error here
                                Log.d(TAG,""+e);

                            } catch (FirebaseNetworkException e) {
                                // log error here
                                Log.d(TAG,""+e);
                            } catch (Exception e) {
                                // log error here
                                Log.d(TAG,""+e);
                            }
                            Log.d(TAG,"Failed Adding Audit");

                        }

                        App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                    }
                });

                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                break;

            case BLUETOOTH_CRED_FAIL:
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " No communication  with device");
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                break;
            case CMD_PAIR_DEVICE:
                BluetoothDevice device = (BluetoothDevice) msg.obj;
                device.createBond();
                break;
            case BLUETOOTH_FLASH_SUCESS:
                 // update version
                ((BleLibCmd) msg.obj).Function = BLE_UART_GET_VERSION;
                ((BleLibCmd) msg.obj).Command = CMD_BLUETOOTH_GET_VERSION;
                APP_SendCmdToLooper(CMD_BLUETOOTH_GET_VERSION,  msg.obj);
                break;

            default:
                Log.d(TAG,"COMMAND NOT FOUND!!");
                break;

        }
    }

    public void App_Build_Recycler_View()
    {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptor = new RowAdaptor(this ,RowList);
        recyclerView.setAdapter(adaptor);
        adaptor.setOnItemClickListener(new RowAdaptor.OnItemClickListener()
        {
            @Override
            public void onItemClick(int position)
            {

                //App_RowCliked(position);
            }

            @Override
            public void onDeleteClick(final int position)
            {
                // Alert user and take input
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage(" Want to Remove Device ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        App_DeleteRow(position); // delete the device
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }


            @Override
            public void onActionClick(final int position)
            {
                byte cred_valid = 0;
                // get bluetooth device
                final BluetoothDevice device = App_Ba.getRemoteDevice((Device_List.get(position).Dev_Mac));
                final int Rand = (Device_List.get(position)).Dev_Rand;
                final int UniqueMast = (Device_List.get(position)).Mast_Id;
                final byte DevType = (byte) (Device_List.get(position)).Dev_Typ;
                final boolean is_master = (Device_List.get(position)).Is_Master;

                final String DeviceName = (Device_List.get(position)).Dev_Name;

                if (!is_master) // its user cread
                {
                    String User_UID = (Device_List.get(position)).User_Cred;
                    Log.d(TAG, "Expected" + User_UID + "found"+ col_macAddressParts );
                    if (User_UID.equals(col_macAddressParts)) {

                        cred_valid = 1;
                    }
                            /*
                            // convert hex string to byte values only 4 bytes are considerd from MAC
                            for (int i = 0; i < 4; i++) {
                                Integer hex = Integer.parseInt(macAddressParts[i + 2], 16);
                                CmdCredCommision[i + 3] = hex.byteValue();
                            }
                           */
                } else {
                    cred_valid = 1;
                }
                // Alert user and take input
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("Command to Device ?");
                alertDialogBuilder.setPositiveButton("Turn ON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        int function = BLE_UART_FUCTION_SET_GPIO;       // ON
                        int function_data = 0x01; // NA
                        BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_CRED, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, function, function_data, DeviceName,null,position);
                        APP_SendCmdToLooper(CMD_BLUETOOTH_DEV_CRED, temp_Cmd);
                    }
                });
                alertDialogBuilder.setNegativeButton("Turn Off", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // send command
                        int function = BLE_UART_FUCTION_SET_GPIO;       // OFF
                        int function_data = 0x00; // NA
                        BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_CRED, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, function, function_data, DeviceName,null,position);
                        APP_SendCmdToLooper(CMD_BLUETOOTH_DEV_CRED, temp_Cmd);
                    }
                });
                //Button Three : Neutral
                alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

                if (cred_valid == 1)
                {
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else
                {
                    // invalid cred
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Credential Do not belongs to User!");
                    // TODO: delete credential from db
                }

            }

        });
    }
    public void App_Create_Buttons()
    {
        App_ButAddDev = findViewById(R.id.ButAddDev);  // all main activity buttons
        App_ButHelp = findViewById(R.id.ButHelp);
        App_ButQuit = findViewById(R.id.ButQuit);
        App_ButLogout = findViewById(R.id.ButLogout);

        App_ButCancel = findViewById(R.id.ButCancel);

        Lw_BleDev_temp = (ListView) findViewById(R.id.Lw_BleDev); // list to display devices
        pairedDeviceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,pairedDeviceArrayList);

        Lw_BleDev_temp.setAdapter(pairedDeviceAdapter);

        App_loadingPanel = findViewById(R.id.loadingPanel);
        App_TxtVPercentage = findViewById(R.id.TxtVPercentage);
        // set on click event functions
        App_ButAddDev.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // request credential
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                builder.setTitle("Request Device Credential");
                // builder.setMessage("AlertDialog");
                builder.setView(R.layout.cred_user);
                //In case it gives you an error for setView(View) try
                builder.setView(inflater.inflate(R.layout.cred_user, null));

                builder.setPositiveButton("Send Request", null);
                builder.setNegativeButton("Cancel", null);

                final AlertDialog alertDialog = builder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditText CredName  =(EditText)alertDialog.findViewById(R.id.EtCredName);
                                EditText CredEmail = (EditText)alertDialog.findViewById(R.id.EtCredEmail);
                                EditText CredPhoneNo = (EditText)alertDialog.findViewById(R.id.EtCredPhoneNo);
                                EditText Request = (EditText)alertDialog.findViewById(R.id.EtRequest);
                                String CredName_txt  = CredName.getText().toString();
                                String CredEmail_txt  = CredEmail.getText().toString();
                                String CredPhoneNo_txt  = CredPhoneNo.getText().toString();
                                String Credential_txt  = Request.getText().toString();

                                if((CredPhoneNo_txt.length()>9)&&(!CredName_txt.isEmpty())&&((!CredEmail_txt.isEmpty()) && isEmailValid(CredEmail_txt))&&(!Credential_txt.isEmpty()))
                                {
                                    // form the request and send
                                    if(isSmsPermissionGranted())
                                    {
                                        try {
                                            // more then 160 char SMS need sendMultipartTextMessage to be called, check if dual sim need saperate handling
                                            // String message_temp = "Hi Prachi, Conneced Device nok nok ";
                                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                            String name = "";
                                            if (user != null) {
                                                name = user.getDisplayName();
                                            }

                                            String CELLID = col_macAddressParts;
                                            String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            String message_temp = "Hi, This is " + name + ",\n Request you to send me credential for respective device. CRED_INFO :"+ UID + "," + CELLID;
                                            SmsManager smsManager = SmsManager.getDefault();
                                            ArrayList<String> parts = smsManager.divideMessage(message_temp);

                                            smsManager.sendMultipartTextMessage(CredPhoneNo_txt, null, parts, null, null);

                                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Sent Credential Request to :" + CredPhoneNo_txt);
                                            Log.d(TAG, " SMS Send success!");
                                            // Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();

                                        }
                                        catch (Exception e)
                                        {
                                            // Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, " SMS Send Fail! " + e.toString());
                                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Failed Sending Credential to :" + CredPhoneNo_txt);

                                        }
                                    }
                                    else
                                    {
                                        App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Failed Sending Credential to :" + CredPhoneNo_txt + "\nAllow permissions to Send SMS ");

                                    }
                                    dialog.dismiss();
                                }
                                else
                                {
                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Invalid Input");
                                }

                            }
                        });

                        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_LONG).show();
                                //CLOSE THE DIALOG
                                dialog.dismiss();
                            }
                        });
                    }
                });

                alertDialog.show();

                // return builder.create();


            }
        });

        App_ButHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("* + : Add new Devices\n* CRED : Send Credential\n* OTA : Upgrade Device\n* Support : Connected Devices");

                alertDialogBuilder.setPositiveButton("Write to Us", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {

                        MainActivity.this.finish();
                        finishAffinity();
                        System.exit(0);
                    }
                });

                alertDialogBuilder.setNegativeButton("Ok",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Do nothing
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        App_ButQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Alert user and take input
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage(" Want to Exit ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        MainActivity.this.finish();
                        finishAffinity();
                        System.exit(0);
                    }
                });
                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Do nothing
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        App_ButLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Alert user and take input
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage(" Logging Out ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // log out
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(MainActivity.this,Login.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        MainActivity.this.finish();
                    }
                });
                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Do nothing
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        App_ButCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
            }
        });

        // register on item click listener
        // register on item click listener

        Lw_BleDev_temp.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                BluetoothDevice device = (BluetoothDevice) pairedDeviceArrayList_ble_copy.get((int)position);
                String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                byte[] UID_byte_array = UID.getBytes();

                int Unique_MASTER_ID = (int) UID_byte_array[0] | (((int) UID_byte_array[1])<<8) | (((int) UID_byte_array[2])<<16) |(((int) UID_byte_array[3])<<24);
                BleLibCmd Temp_cmd = new BleLibCmd(CMD_BLUETOOTH_CONNECT_COMMISION,(byte)BLE_UART_COMMISION,device,0x00,Unique_MASTER_ID,0x00,0x00,0x00,"",null,position);
                APP_SendCmdToLooper(CMD_BLUETOOTH_CONNECT_COMMISION,Temp_cmd);




            }
        });
    }

    public void App_DeleteRow(int position)
    {
        //update online database
        final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");
        TempRef.child(UID).child("CredReceived").child(Device_List.get(position).Dev_ID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful())
                {

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthUserCollisionException e) {
                        // log error here
                        Log.d(TAG,""+e);

                    } catch (FirebaseNetworkException e) {
                        // log error here
                        Log.d(TAG,""+e);
                    } catch (Exception e) {
                        // log error here
                        Log.d(TAG,""+e);
                    }
                    Log.d(TAG,"Failed to delete Device");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed to delete Device!\n Check internet connectivity.");

                }
                else
                {

                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Device Deleted !");
                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                    // device will be added automatically by event

                }

                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
            }
        });
        // TODO: update offline database
       // adaptor.notifyItemRemoved(position);// tell adapter to reflash
    }
    public void App_Create_List()
    {
        // read data from database and update the list
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");
        TempRef.child(UID).child("CredReceived")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        RowList.clear();
                        Device_List.clear();
                        Log.d(TAG,"Data set Changed !!!");
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DeviceDb DeviceDb_tempval = snapshot.getValue(DeviceDb.class);
                            Device_List.add(DeviceDb_tempval);

                            RowList.add(new RowCls(R.drawable.test,DeviceDb_tempval.Dev_ID));
                        }
                        App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);;
                        adaptor.notifyDataSetChanged(); // just to notify if sucess
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
     //   RowList.add(new RowCls(R.drawable.test,"1.1","Fan","")); // Note Manually Copy Pste The Images in drawable folder
     //   RowList.add(new RowCls(R.drawable.test,"1.1","Bulb",""));
    }

    void App_Show_Alert_decision(String AlertMessage)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(AlertMessage);
        alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener()
        {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                //Actrion  on OK
                            }
                        });
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
               // finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void App_ShowAlertSimple(String AlertMessage)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(AlertMessage);
        alertDialogBuilder.setPositiveButton("Got It", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                //Actrion  on OK
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    void App_UI_Call(int Commad, Object SubCommand)
    {
        Message Temp_Message = new Message();
        Temp_Message.what = Commad;
        Temp_Message.obj =SubCommand;
        App_HandleLooperMsg(Temp_Message);

    }

    public boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

}
