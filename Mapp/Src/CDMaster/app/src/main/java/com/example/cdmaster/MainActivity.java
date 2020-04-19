package com.example.cdmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.cdmaster.BleLib.App_Ba;
import static com.example.cdmaster.BleLib.bluetoothSocket;
import static com.example.cdmaster.BleLib.mReceiver;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_COMMISION;


import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FLASH;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FUCTION_GET_GPIO;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FUCTION_SET_GPIO;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_GET_DIN;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_GET_ON_TIME;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_GET_VERSION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_COMMISION_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_COMMISION_PASS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_GET_STATUS_FAIL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_GET_STATUS_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_CRED_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_DIV_DISCOVERY_COMPLETE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_ERROR;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_FLASH_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_GET_DEV_ON_TIME_SUCCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_GET_DIN_SUCCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_GET_VERSION_SUCCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_CONNECT_COMMISION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_CRED;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_FLASH;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DIV_DISCOVERY_5SEC;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_GET_DEV_ON_TIME;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_GET_DIN;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_GET_VERSION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_INIT;

import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_DISPLAY_SHORT_ALEART;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_PAIR_DEVICE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_ACTION_DIALOG_STATUS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_DISPLY_VISIBILITY;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_MAIN_DIALOG_STATUS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_SET_MAIN_PROGRESS_PERCENT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.EVENT_TIMER;
import static com.example.cdmaster.GLOBAL_CONSTANTS.SCREEN_LIST_NEARBY_DEVICE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.SCREEN_NORMAL;
import static com.example.cdmaster.GLOBAL_CONSTANTS.SCREEN_WAIT_INPROGRESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_RX_TIMEOUT;
import static com.example.cdmaster.LoopThread.globaltempDIN;
import static com.example.cdmaster.LoopThread.globaltempOnTime;
import static com.example.cdmaster.Security.CRC_Chk;
import static com.example.cdmaster.Security.DecryptString;
import static com.example.cdmaster.ServLib.GetDate;
import static com.example.cdmaster.ServLib.GetTime;
import static com.example.cdmaster.ServLib.generateMaapUUID;


public class MainActivity extends AppCompatActivity {

    private LoopThread _Thread_LoopHandThread = new LoopThread("Looper",this);
    public static volatile Handler _Handler_MainHandler;
    public int Uniqe_Id=0;
    final private String TAG = "TAG_MAIN_Thread";

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    TextView DateStrt;
    TextView DateExp;
    TextView TimeAct;
    TextView TimeExp;

    // Variable View
    RecyclerView recyclerView;
    RowAdaptor adaptor;
    List<RowCls> RowList;   // holds the list to be displayed

    //Action variable v
    public AlertDialog.Builder action_builder = null;
    public AlertDialog Action_alertDialog = null;
    public LayoutInflater actioninflater =null ;
    public View ActionContent;
     public ImageButton Button_typ1;
     public  TextView TxtV_ActionPercentage ;
     public  ProgressBar PerBar_ActionPercentage ;
    public  TextView TxtV_ActionInfoTemp;
     public  RelativeLayout lay_loadingPanel ;
     public  RelativeLayout lay_ActionPanel;
    public  RelativeLayout lay_ActionPanelTyp1;
    public  RelativeLayout lay_ActionPanelTyp2;

    // APP shared variable (variables data used by dialog to get results )
    //Db
    FirebaseAuth mFirebaseAuth;
    StorageReference mStorageRef;
    DatabaseReference TempRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static ArrayList<DeviceDb> Device_List =new ArrayList<>();

    private Button App_ButAddDev, App_ButHelp, App_ButQuit,App_ButCancel,App_ButLogout;
    private TextView App_TxtVPercentage,TxtV_ProgInfo;
    RelativeLayout App_loadingPanel;
    ListView Lw_BleDev_temp;
    //Action vindow

    // Global pairing key holder
    public static String globalCurrentPairingKey;
    public static ArrayList<String> pairedDeviceArrayList = new ArrayList<>();  // list of paired devices
    public static ArrayList<BluetoothDevice> pairedDeviceArrayList_ble_copy = new ArrayList<>(); // hold copy
    ArrayAdapter<String> pairedDeviceAdapter ;//= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,pairedDeviceArrayList);

    public static  AlertDialog alertDialogLogOut;
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
        if(alertDialogLogOut!=null)
        {
            Log.d(TAG,"Destroying Dialoge");
            alertDialogLogOut.dismiss();
            alertDialogLogOut=null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

    }
    @Override
    public  void onDestroy()
    {
        super.onDestroy();

        Log.d(TAG,"Destroying Main Activity");

        // MainActivity.this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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

        TempRef = FirebaseDatabase.getInstance().getReference("User");


        mStorageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null)
        {
            Log.d(TAG,"Signed in sucess , setting welcome title...");
            // Name, email address etc
            String name = user.getDisplayName().split("%")[0];
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
                    Log.d(TAG,"Signing Out from Main Activity");
                    Intent i = new Intent(MainActivity.this, Login.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
        App_UI_Call(CMD_SET_MAIN_DIALOG_STATUS,"Loading your Device's");
        App_UI_Call(CMD_SET_MAIN_PROGRESS_PERCENT,0);

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
                    case CMD_BLUETOOTH_DEV_CRED:
                    case CMD_BLUETOOTH_DEV_FLASH:
                    case CMD_BLUETOOTH_CONNECT_COMMISION:

                    break;


                }
                Log.d(TAG,"No Responce received ");
                try
                {
                    bluetoothSocket.close(); // Start Looper thread : to handle non UI task
                }catch(IOException e1)
                {
                    e1.printStackTrace();
                }
            }
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
                        pairedDeviceAdapter.notifyDataSetChanged();
                       // adaptor.notify();
                        break;
                    case SCREEN_LIST_NEARBY_DEVICE:

                        App_ButAddDev.setClickable(true);
                        recyclerView.setVisibility(View.GONE);
                        App_ButCancel.setVisibility(View.VISIBLE);
                        Lw_BleDev_temp.setVisibility(View.VISIBLE);
                        App_loadingPanel.setVisibility(View.GONE);
                        pairedDeviceAdapter.notifyDataSetChanged();
                        break;
                    case SCREEN_WAIT_INPROGRESS: // any thing in progress
                        App_ButAddDev.setClickable(false);
                        recyclerView.setVisibility(View.GONE);
                        App_ButCancel.setVisibility(View.GONE);
                        Lw_BleDev_temp.setVisibility(View.GONE);
                        App_loadingPanel.setVisibility(View.VISIBLE);
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

            case BLUETOOTH_COMMISION_PASS:
                // update Device in lisr
                App_AddNewCommsiondDevice((DeviceDb) msg.obj);

                break;

            case BLUETOOTH_CRED_GET_STATUS_SUCESS:
            case BLUETOOTH_CRED_SUCESS:
                // TODO change function_data with specific values
                String Action = "";
				   if(((BleLibCmd) msg.obj).Dev_Typ == 1)
                {

                    if ((((BleLibCmd) msg.obj).Function_Data) != 0) {

                        Button_typ1.setImageResource(R.drawable.img_on);
                        Button_typ1.setTag("img_on");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " Device Is On ");
                    Action = "Device Turned On";

                }
                else {
				 Button_typ1.setImageResource(R.drawable.img_off);
				 Button_typ1.setTag("img_off");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " Device Is Off");
                    Action = "Device Turned Off";
                }
				}
                lay_loadingPanel.setVisibility(View.INVISIBLE);
                lay_ActionPanel.setVisibility(View.VISIBLE);
                // generate audit
                // TODO: PROMPT USER FOR MORE INPUTS
                //String Mast_UID = Device_List.get(((BleLibCmd) msg.obj).position).DeviceBelongsTo;
                //String Dev_ID = Device_List.get(((BleLibCmd) msg.obj).position).Dev_ID;

                //AppSaveAudit(Mast_UID, Dev_ID,Action);

                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                break;
            case BLUETOOTH_COMMISION_FAIL:
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART," Comission Failed!");
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                break;
            case BLUETOOTH_CRED_GET_STATUS_FAIL:
            case BLUETOOTH_CRED_FAIL:
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " No communication  with device");
                lay_loadingPanel.setVisibility(View.INVISIBLE);
                lay_ActionPanel.setVisibility(View.VISIBLE);
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
            case BLUETOOTH_GET_VERSION_SUCCESS:
                App_UpdaeDeviceVersion((DeviceDb)msg.obj);
                break;
            case BLUETOOTH_GET_DEV_ON_TIME_SUCCESS:
               // App_UpdaeOnTime((DeviceDb)msg.obj);
                //show status
                int position = ((BleLibCmd)(msg.obj)).position;
                String dev_type="";
                if((Device_List.get(position).Dev_Typ == 1))
                {
                    dev_type = "Wireless Switch";
                }
                String onTim = "\n\nOn Time: Days "+((globaltempOnTime[0]&0xFF) | ((globaltempOnTime[1]&0xFF)<<8))+", "+(globaltempOnTime[2]&0xFF)+" : "+(globaltempOnTime[3]&0xFF)+" : "+(globaltempOnTime[4]&0xFF);

                String Msg =  "Device ID: " +(Device_List.get(position)).Dev_ID + "\n\nDevice Type: "+ dev_type + "\n\nFirmware Version: " + (Device_List.get(position)).Version +onTim+"\n\nDays In Use:"+globaltempDIN;
                App_ShowAlertSimple("Device Info:",Msg);


                break;
            case BLUETOOTH_GET_DIN_SUCCESS:
               // App_UpdaeDIN((DeviceDb)msg.obj);
                //App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                // request On time
                SendBleCommand((((BleLibCmd)(msg.obj)).position),CMD_BLUETOOTH_GET_DEV_ON_TIME,BLE_UART_GET_ON_TIME,0x00);
                SendBleCommand((((BleLibCmd)(msg.obj)).position),CMD_BLUETOOTH_GET_DEV_ON_TIME,BLE_UART_GET_ON_TIME,0x01);
                SendBleCommand((((BleLibCmd)(msg.obj)).position),CMD_BLUETOOTH_GET_DEV_ON_TIME,BLE_UART_GET_ON_TIME,0x02);
                SendBleCommand((((BleLibCmd)(msg.obj)).position),CMD_BLUETOOTH_GET_DEV_ON_TIME,BLE_UART_GET_ON_TIME,0x03);
                SendBleCommand((((BleLibCmd)(msg.obj)).position),CMD_BLUETOOTH_GET_DEV_ON_TIME,BLE_UART_GET_ON_TIME,0x04);

                break;
            case CMD_SET_ACTION_DIALOG_STATUS:
                TxtV_ActionInfoTemp.setText((String) msg.obj);
                break;
            case CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT:
                String Tmp=((int)msg.obj) + " %";
                TxtV_ActionPercentage.setText(Tmp);
                break;
            case CMD_SET_MAIN_PROGRESS_PERCENT:
                 Tmp=((int)msg.obj) + " %";
                App_TxtVPercentage.setText(Tmp);
                break;
            case CMD_SET_MAIN_DIALOG_STATUS:
                TxtV_ProgInfo.setText((String) msg.obj);
                break;
            default:
                Log.d(TAG,"COMMAND NOT FOUND!!");
                break;

        }
    }

    public void App_Build_Recycler_View()
    {
        recyclerView =  findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptor = new RowAdaptor(this ,RowList);
        recyclerView.setAdapter(adaptor);

        adaptor.setOnItemClickListener(new RowAdaptor.OnItemClickListener()
        {
            @Override
            public void onAuditClick(int position)
            {
                App_DisplayAudit(Device_List.get(position));
            }
            @Override
            public void onItemClick(int position)
            {

                //App_RowCliked(position);
            }
            @Override
            public void onImageClick(final int position)
            {
                if(AppValidateCred(Device_List.get(position)))
                {

                    HandleDevTypAction(position);
                }
                else
                {
                    // invalid cred
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Credential Do not belongs to User!");
                    // TODO: delete credential from db
                }

            }


            @Override
            public void onCredClick(final int position)
            {

                byte cred_valid = 0;

                final boolean is_master = (Device_List.get(position)).Is_Master;
                if (!is_master) // its user cread
                {

                        cred_valid = 0;
                }
                else
                {
                    cred_valid = 1;
                }
                // Alert user and take input

                if (cred_valid != 1)
                {
                    // invalid cred
                    // TODO: delete credential from db
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Credential Do not belongs to User!");
                    return;

                }
                Log.d(TAG,"Clicked User Cred");
                final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("CredSent");
                TempRef.addListenerForSingleValueEvent(new ValueEventListener() // lisen only once
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG,"User Credential Dataset changed");
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                        //builderSingle.setIcon(R.drawable.ic_launcher);
                        builderSingle.setTitle("People using this Device:");
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                        Log.d(TAG,"Cred Users read !!!");
                        final ArrayList<CredUserProfile> CredUser_tempval_list = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            CredUserProfile CredUser_tempval = snapshot.getValue(CredUserProfile.class);
                            CredUser_tempval_list.add(CredUser_tempval);
                            if(CredUser_tempval != null )
                            {
                                arrayAdapter.add(CredUser_tempval.UserName);
                                Log.d(TAG, "User name " + CredUser_tempval.UserName);
                            }
                        }
                        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, final int user_idx)
                            {
                                Log.d(TAG,"Index Selected :" + user_idx);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);

                                builderInner.setTitle("Credential User :" +CredUser_tempval_list.get(user_idx).UserName);
                                final String Cred_Uid = CredUser_tempval_list.get(user_idx).UserId;
                                final String Cred_Validity = CredUser_tempval_list.get(user_idx).Validity;
                                // create user options

                                builderInner.setItems(new CharSequence[] {"\tSend Credential to User", "\tDisable This User", "\tDelete This User",	"\tView Credential", "\tCancel"},
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                // The 'which' argument contains the index position
                                                // of the selected item
                                                final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                final DatabaseReference UserCredRef = FirebaseDatabase.getInstance().getReference("User").child(CredUser_tempval_list.get(user_idx).UserId).child("CredReceived").child(Device_List.get(position).Dev_ID);
                                                // delete the user data stored
                                                final DatabaseReference MasterCredRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("CredSent").child(CredUser_tempval_list.get(user_idx).UserId);
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                                AlertDialog alertDialog_user;

                                                if(Cred_Uid.equals(mFirebaseAuth.getUid()))
                                                {
                                                    // its master credential selected
                                                    if((which==1) || (which==2))
                                                    {
                                                        which =4;
                                                        App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Master data can not be deleted");
                                                    }
                                                }
                                                switch (which) {
                                                    case 0:
                                                        AppUsrCredUpdate(CredUser_tempval_list.get(user_idx),position, TempRef);
                                                        break;
                                                    case 1:
                                                        alertDialogBuilder.setMessage("Disable This User from Accessing Device?");
                                                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface arg0, int arg1) {

                                                                AppDeleteCredUserLoc(UserCredRef);
                                                                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
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
                                                        alertDialog_user = alertDialogBuilder.create();
                                                        alertDialog_user.show();
                                                        break;
                                                    case 2:
                                                        // Alert user and take input

                                                        alertDialogBuilder.setMessage("Delete credential permanently?");
                                                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface arg0, int arg1) {

                                                                AppDeleteCredPermenently(UserCredRef,MasterCredRef);
                                                                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
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
                                                        alertDialog_user = alertDialogBuilder.create();
                                                        alertDialog_user.show();
                                                        break;
                                                    case 3:
                                                        // view Credential

                                                        alertDialogBuilder.setTitle("Credential Validity:");
                                                        alertDialogBuilder.setMessage(Cred_Validity);
                                                        alertDialogBuilder.setNegativeButton("OK",new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {
                                                                // send command
                                                            }
                                                        });
                                                        alertDialog_user = alertDialogBuilder.create();
                                                        alertDialog_user.show();
                                                        break;
                                                    case 4:
                                                        // cancelbotton
                                                        break;
                                                }
                                            }
                                        });


                               builderInner.create().show();
                            }

                        });
                        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                        builderSingle.setPositiveButton("Add New User", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                AppAddNewCred(position);
                               // return builder.create();
                            }
                        });
                       // AlertDialog alertDialog = builderSingle.create();
                        builderSingle.show();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG,"Image read error");
                    }
                });
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
            public void onDeviceNameClick(final int position)
            {
                final EditText input = new EditText(MainActivity.this);

                // Alert user and take input
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                alertDialogBuilder.setView(input);

                alertDialogBuilder.setMessage("Set Name to Device");
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String temp_input = input.getText().toString();
                        if(!temp_input.isEmpty() && temp_input.length()<=7)
                        {
                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Updating Device Name");
                            Device_List.get(position).Dev_Name = temp_input;
                            App_UpdaeDeviceName(Device_List.get(position));
                        }
                        else
                        {
                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Device name Invalid");
                        }

                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
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
            public void onOtaClick(final int position)
            {
                // Alert user and take input

                byte cred_valid = 0;
                // get bluetooth device
                final BluetoothDevice device = App_Ba.getRemoteDevice((Device_List.get(position).Dev_Mac));
                final int Rand = (Device_List.get(position)).Dev_Rand;
                final int UniqueMast = (Device_List.get(position)).Mast_Id;
                final byte DevType = (byte) (Device_List.get(position)).Dev_Typ;
                final boolean is_master = (Device_List.get(position)).Is_Master;

                final String DeviceName = (Device_List.get(position)).Dev_ID;
                globalCurrentPairingKey = (Device_List.get(position)).PairingPw;
                if (!is_master) // its user cread
                {

                        cred_valid = 0;

                }
                else
                {
                    cred_valid = 1;
                }
                // Alert user and take input

                if (cred_valid != 1)
                {
                    // invalid cred
                    // TODO: delete credential from db
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Credential Do not belongs to User!");
                    return;

                }


                TempRef = FirebaseDatabase.getInstance().getReference("FirmwareMeta").child("DevTyp"+DevType).child("Image");
                Log.d(TAG,"Clicked Ota" + TempRef);
                TempRef.addListenerForSingleValueEvent(new ValueEventListener() // lisen only once
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG,"Image Dataset changed");
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                        //builderSingle.setIcon(R.drawable.ic_launcher);
                        builderSingle.setTitle("Select OTA Image:-");

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                        final ArrayAdapter<String> arrayAdapterInfo = new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_singlechoice);

                        Log.d(TAG,"Image name read !!!");
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            String ImageDb_tempval = snapshot.getValue(String.class);
                            arrayAdapter.add(ImageDb_tempval.split("%")[0]);
                            arrayAdapterInfo.add(ImageDb_tempval.split("%")[1]);
                            Log.d(TAG,"Image Name " + ImageDb_tempval.split("%")[0]);
                        }
                        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.d(TAG,"Index Selected :" + which);
                                final String ImagestrName = arrayAdapter.getItem(which);
                                final String ImagestrInfo = arrayAdapterInfo.getItem(which);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                                builderInner.setMessage("Download Image '" + ImagestrName + "' to Device?\n\nImage Info:" + ImagestrInfo +"\n\nNote:\n*OTA takes 60 seconds to complete\n*Do not Close the App.");
                                builderInner.setTitle("OTA Upgrade Device");

                                builderInner.setPositiveButton("Update ", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,int which) {
                                        // Doenload Image

                                        StorageReference ImgStorageReference = mStorageRef.child("cdmasterStorage").child("FwImages").child(ImagestrName);
                                        Log.d(TAG,"Image Ref " + ImgStorageReference);
                                        final long TWENTY_KB = 1024 * 50; // maximum size of OTA image 20 k

                                        ImgStorageReference.getBytes(TWENTY_KB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                // Data for "images/island.jpg" is returns, use this as needed
                                                Log.d(TAG ,"download Success, flashing the device!!!");
                                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Image downloaded , Flashing the device" + bytes[0] );

                                                int function_data = 0x00; // NA
                                                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_WAIT_INPROGRESS);
                                                BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_FLASH, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, BLE_UART_FLASH, function_data, DeviceName,bytes,position);

                                                APP_SendCmdToLooper(CMD_BLUETOOTH_DEV_FLASH, temp_Cmd);

                                            }

                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Log.d(TAG,"Error downloading image :" + exception.getMessage());
                                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed to download image!");
                                            }
                                        });


                                        // then OTA the image
                                    }
                                });
                                builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.show();
                            }

                        });
                        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                        builderSingle.show();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG,"Image read error");
                    }
                });
            }

            @Override
            public void onInfoClick(final int position)
            {
                // Alert user and take input
                String dev_type = "Unknown";
                // Request days in used
                SendBleCommand(position,CMD_BLUETOOTH_GET_DIN,BLE_UART_GET_DIN,0x00);
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

        Lw_BleDev_temp =  findViewById(R.id.Lw_BleDev); // list to display devices
        pairedDeviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,pairedDeviceArrayList);

        Lw_BleDev_temp.setAdapter(pairedDeviceAdapter);

        App_loadingPanel = findViewById(R.id.loadingPanel);
        App_TxtVPercentage = findViewById(R.id.TxtVPercentage);
        TxtV_ProgInfo = findViewById(R.id.TxtVProgInfo);

        // set on click event functions


        App_ButAddDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : add commisionin if master / Add credential data  if user
                APP_SendCmdToLooper(CMD_BLUETOOTH_DIV_DISCOVERY_5SEC,"CMD_BLUETOOTH_DIV_DISCOVERY_5SEC");
            }
        });

        App_ButHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("* + : Add new Devices\n* CRED : Send Credential\n* OTA : Upgrade Device\n* Support : Connected Devices");


                alertDialogBuilder.setNegativeButton("Got it",new DialogInterface.OnClickListener()
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
                        Intent i = new Intent(MainActivity.this, Login.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        // listener shuld take you out o activity
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

                alertDialogLogOut = alertDialogBuilder.create();
                alertDialogLogOut.show();

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
                BluetoothDevice device = pairedDeviceArrayList_ble_copy.get(position);
                String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                byte[] UID_byte_array = UID.getBytes();

                String Dev_ID_db;
                if((pairedDeviceArrayList_ble_copy.get(position).getName()).length() == 6) // device ID is present and valid
                {
                    Dev_ID_db = (pairedDeviceArrayList_ble_copy.get(position).getName());
                    int Unique_MASTER_ID = (int) UID_byte_array[0] | (((int) UID_byte_array[1])<<8) | (((int) UID_byte_array[2])<<16) |(((int) UID_byte_array[3])<<24);
                    final BleLibCmd Temp_cmd = new BleLibCmd(CMD_BLUETOOTH_CONNECT_COMMISION,(byte)BLE_UART_COMMISION,device,0x00,Unique_MASTER_ID,0x00,0x00,0x00,Dev_ID_db,null,position);

                    //Load Global Pairing bytes
                    Log.d(TAG,"ID:"+Dev_ID_db);

                    TempRef = FirebaseDatabase.getInstance().getReference("FirmwareMeta").child("DevKey").child(Dev_ID_db);
                    TempRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {


                       globalCurrentPairingKey = dataSnapshot.getValue(String.class).substring(0,4);
                       Log.d(TAG,"Passwor:"+globalCurrentPairingKey);
                        APP_SendCmdToLooper(CMD_BLUETOOTH_CONNECT_COMMISION,Temp_cmd);


                    }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Device Invalid");
                            App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                        }

                    });
                }
                else
                {
                   App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Device Invalid");
                   App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                }

            }
        });

        App_SetActionDialogView();


    }

    public void App_DeleteRow(final int position)
    {
        //update online database
        final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // before deleting device delete each user sent credential

        TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("CredSent");
        TempRef.addListenerForSingleValueEvent(new ValueEventListener() // lisen only once
        {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot)
               {
                   Log.d(TAG, "User Credential Dataset changed , dleting user credentils sent from user account");

                   for (DataSnapshot snapshot : dataSnapshot.getChildren())
                   {
                       // get cred user profile
                       CredUserProfile CredUser_tempval = snapshot.getValue(CredUserProfile.class);
                       // delete credential from USER location
                       TempRef = FirebaseDatabase.getInstance().getReference("User").child(CredUser_tempval.UserId).child("CredReceived").child(Device_List.get(position).Dev_ID);
                       TempRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                       {

                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(!task.isSuccessful())
                               {

                                   try {
                                       throw task.getException();
                                   }   catch (Exception e) {
                                       // log error here
                                       Log.d(TAG,""+e);
                                   }
                                   Log.d(TAG,"Failed to delete User");
                                   App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed To delete user");

                               }
                               else
                               {

                                   App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"User Deleted !");
                                   // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                   // device will be added automatically by event

                               }
                           }
                       });
                   }
               }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG,"User cred Read error for deleting");
            }
       });
                    // then delte device
        TempRef = FirebaseDatabase.getInstance().getReference("User");
        TempRef.child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("DeviceInfo").child("Deleted").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {

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
        TempRef.child(UID).child("Devices")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        RowList.clear();
                        Device_List.clear();
                        Log.d(TAG,"Data set Changed !!!");
                        int imag_idx;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DeviceDb DeviceDb_tempval = snapshot.child("DeviceInfo").getValue(DeviceDb.class);

                            // set appropriate image
                            if((DeviceDb_tempval!=null)&&(!DeviceDb_tempval.Deleted)) // device is not deleted
                            {
                                Device_List.add(DeviceDb_tempval);
                                if (DeviceDb_tempval.Dev_Typ == 0x01) // its switch
                                {
                                    imag_idx = R.drawable.typ_1;
                                } else {
                                    imag_idx = R.drawable.typ_1;
                                }
                                RowList.add(new RowCls(imag_idx, DeviceDb_tempval.Dev_Name));
                            }
                        }
                        App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
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
        alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener()
        {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                //Actrion  on OK
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void App_ShowAlertSimple(String Title,String AlertMessage)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(Title);
        alertDialogBuilder.setMessage(AlertMessage);
        alertDialogBuilder.setPositiveButton("Got It", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
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
    void App_UpdaeDeviceVersion(final DeviceDb DeviceDb_temp)
    {
        // TODO: PROMPT USER FOR MORE INPUTS
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");
        //TODO : Update dtatabse layout

        TempRef.child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("DeviceInfo").child("Version").setValue(DeviceDb_temp.Version).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful())
                {

                    try {
                        throw task.getException();
                    }  catch (Exception e) {
                        // log error here
                        Log.d(TAG,""+e);
                    }
                    Log.d(TAG,"Version Update failed ");

                }
                else
                {
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Updated Device Version!");
                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                    // device will be added automatically by event

                }
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
            }
        });

        //connect/get bluettoth device using MAC as a string
        // BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // TODO update DB

        // UPDATE LIST dummy list

    }
    void App_UpdaeDeviceName(final DeviceDb DeviceDb_temp)
    {
        // TODO: PROMPT USER FOR MORE INPUTS
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");
        //TODO : Update dtatabse layout

        TempRef.child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("DeviceInfo").child("Dev_Name").setValue(DeviceDb_temp.Dev_Name).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful())
                {

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthUserCollisionException e) {
                        // log error here
                        Log.d(TAG,""+e);

                    }  catch (Exception e) {
                        // log error here
                        Log.d(TAG,""+e);
                    }
                    Log.d(TAG,"Version Update failed ");

                }
                else
                {
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Updated Device Name");
                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                    // device will be added automatically by event

                }
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
            }
        });

    }
    void  App_DisplayAudit( final DeviceDb DeviceDb_temp)
    {

        //update online database
        final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // before deleting device delete each user sent credential

        TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("Audits").child("UserAudit");
        TempRef.addListenerForSingleValueEvent(new ValueEventListener() // lisen only once
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "User Credential DeviceDb_temp changed ");
                AuditDb Audits_temp =null;
                String UserId =null;
                String temp_msg;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // get cred user profile
                    UserId = snapshot.getKey();
                    Audits_temp = snapshot.getValue(AuditDb.class);

                }

                // Alert user and take input

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Recent Device Audits");
                if(UserId != null && Audits_temp!=null)
                {
                    temp_msg = "User Id : " + UserId.substring(16) + "\nUser Name : " + Audits_temp.UsrName.split("%")[0]+ "\nPhone : " + Audits_temp.UsrName.split("%")[1] + "\nTime : "+ Audits_temp.Time + "\nAction : " +Audits_temp.Action;
                }
                else
                {
                    temp_msg = "No Audits";
                }
                alertDialogBuilder.setMessage(temp_msg);
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {


                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG,"User cred Read error for deleting");
            }
        });
    }
    void App_AddNewCommsiondDevice( final DeviceDb DeviceDb_temp)
    {
        //TODO : Update dtatabse layout
        final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");

        DeviceDb_temp.DeviceBelongsTo = UID;
        DeviceDb_temp.Deleted = false;
        TempRef.child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("DeviceInfo").setValue(DeviceDb_temp).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful())
                {

                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        // log error here
                        Log.d(TAG,""+e);
                    }
                    Log.d(TAG,"Failed Adding Device , Default reset the Device and try again ");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed Adding Device!\nFactory Default the device and try again.");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Commission Fail!");

                }
                else
                {
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Commission Success: Device added !");
                   // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                   // device will be added automatically by event

                    // Add self credential

                    // update sent credentials
                    final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final String UsrName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split("%")[0];
                    final String UsrPhone = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split("%")[1];

                    TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("CredSent").child(UID);
                    //TODO : Update dtatabse layout
                    String MaapUUID = GetLocDbData("USERUID");
                    if(MaapUUID.length() == 0)
                    {
                        MaapUUID = generateMaapUUID();
                        Log.d(TAG,"New UUID generated:"+MaapUUID);
                        AddLocDbData("USERUID",MaapUUID);
                    }
                    CredUserProfile Temp_CredUserProfileDb = new CredUserProfile(UID,UsrName,UsrPhone,MaapUUID,"NOT Updated");
                    TempRef.setValue(Temp_CredUserProfileDb).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {

                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    // log error here
                                    Log.d(TAG, "" + e);
                                }
                                Log.d(TAG, "Failed Adding Cred user ");
                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Adding User Failed!");

                            } else {
                                Log.d(TAG, " Adding Cred user Success ");

                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Adding User Success !");
                                // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                // device will be added automatically by event

                            }
                            App_UI_Call(CMD_SET_DISPLY_VISIBILITY, SCREEN_NORMAL);
                        }
                    });
                    TempRef = FirebaseDatabase.getInstance().getReference("User");

                    // update  received credential
                    TempRef.child(UID).child("CredReceived").child(DeviceDb_temp.Dev_ID).setValue(DeviceDb_temp).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful())
                            {

                                try {
                                    throw task.getException();
                                }  catch (Exception e) {
                                    // log error here
                                    Log.d(TAG,""+e);
                                }
                                Log.d(TAG,"Failed adding self credential ");

                            }
                            else
                            {
                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Added self credential !");
                                // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                // device will be added automatically by event

                            }

                        }
                    });

                }
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
            }
        });

        //connect/get bluettoth device using MAC as a string
        // BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // TODO update DB

        // UPDATE LIST dummy list

    }
    public boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    void App_SetActionDialogView()
    {
        actioninflater = getLayoutInflater();
        ActionContent = actioninflater.inflate(R.layout.dev_typ_action_view, null);

        lay_loadingPanel = ActionContent.findViewById(R.id.loadingPanel);
        TxtV_ActionPercentage  = ActionContent.findViewById(R.id.TxtVActionPercentage);
        PerBar_ActionPercentage  = ActionContent.findViewById(R.id.PerBarActionPercentage);
        TxtV_ActionInfoTemp= ActionContent.findViewById(R.id.TxtVActionInfoTemp);

        Button_typ1  = ActionContent.findViewById(R.id.ButTyp1);

        lay_ActionPanel = ActionContent.findViewById(R.id.ActionPanel);
        lay_ActionPanelTyp1 = ActionContent.findViewById(R.id.ActionPanelTyp1);
        lay_ActionPanelTyp2= ActionContent.findViewById(R.id.ActionPanelTyp2);


        // select action based on device type
        // get device type
    }
    public void AppDeleteCredUserLoc(DatabaseReference TempRefCredUsrLoc)
    {
        TempRefCredUsrLoc.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful())
                {

                    try {
                        throw task.getException();
                    }  catch (Exception e) {
                        // log error here
                        Log.d(TAG,""+e);
                    }
                    Log.d(TAG,"Failed to delete User");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed To delete user");

                }
                else
                {

                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"User Deleted !");
                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                    // device will be added automatically by event

                }
            }
        });
    }
    public void AppDeleteCredMasterLoc(DatabaseReference TempRefCredMasterLoc)
    {
        //TODO : Update dtatabse layout
        TempRefCredMasterLoc.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful())
                {

                    try {
                        throw task.getException();
                    }  catch (Exception e) {
                        // log error here
                        Log.d(TAG,""+e);
                    }
                    Log.d(TAG,"Failed to delete User");
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed To delete user");

                }
                else
                {

                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"User Deleted !");
                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                    // device will be added automatically by event

                }
            }
        });

    }
    public void AppDeleteCredPermenently(DatabaseReference TempRefCredUsrLoc , DatabaseReference TempRefCredMasterLoc)
    {
        AppDeleteCredUserLoc(TempRefCredUsrLoc);
        AppDeleteCredMasterLoc(TempRefCredMasterLoc);
    }

    public void AppAddNewCred(final int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Enter User Infos");
        // builder.setMessage("AlertDialog");
        builder.setView(R.layout.cred_user);
        //In case it gives you an error for setView(View) try
        builder.setView(inflater.inflate(R.layout.cred_user, null));

        builder.setPositiveButton("Add User", null);
        builder.setNegativeButton("Cancel", null);
        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(final DialogInterface dialog)
            {
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText Credential = alertDialog.findViewById(R.id.EtCredential);
                        String Credential_txt  = Credential.getText().toString();

                        if((!Credential_txt.isEmpty())&&(Credential_txt.contains("CRED_INFO:"))&&(Credential_txt.contains("HEAD:"))&&(Credential_txt.contains(","))&&(Credential_txt.contains("CHK:")))
                        {
                            // now extrect credential data

                            Log.d(TAG,Credential_txt);

                            // calculate check value and encrypt it

                            String KeyUid = ((Credential_txt.split("HEAD:"))[1].split("CRED_INFO:"))[0];
                            Log.d(TAG,KeyUid);
                            String CredStr =  ((Credential_txt.split("CRED_INFO:"))[1].split("CHK:")[0]);
                            Log.d(TAG,CredStr);
                            CredStr = DecryptString(CredStr,KeyUid);
                            Log.d(TAG,CredStr);
                            String Credchk =  (Credential_txt.split("CRED_INFO:"))[1].split("CHK:")[1];
                            final String [] tempCred =CredStr.split(",");
                            String tmp_UID = tempCred[0];
                            byte MacKey = CRC_Chk(tmp_UID.getBytes(),tmp_UID.getBytes().length,1,(byte)0);
                            byte Tmp_CHECK = (CRC_Chk(CredStr.getBytes(),CredStr.getBytes().length,1,MacKey));
                            //TODO: decrypt with AES
                            if(tmp_UID.equals((KeyUid))&&(tempCred.length == 4)&&(tempCred[3].length()== 17)&&(tempCred[0].length()>= 28)&&Byte.toString(Tmp_CHECK).equals(Credchk))
                            {
                                // Alert user and take input
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                alertDialogBuilder.setTitle("Verify User Details");
                                alertDialogBuilder.setMessage("User:" +tempCred[1] + "\n"+"Phone:"+tempCred[2]+"\n");

                                alertDialogBuilder.setPositiveButton("Add User", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        //TODO: // get encripted string from user
                                        // user app use device type key to encript
                                        // parse cred text and update db
                                        Log.d(TAG,"Cred OK");
                                        CredUserProfile CreduserTemp = new CredUserProfile(tempCred[0], tempCred[1], tempCred[2], tempCred[3],"Not Updated");
                                        // TODO: PROMPT USER FOR MORE INPUTS
                                        final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("CredSent").child(CreduserTemp.UserId);
                                        //TODO : Update dtatabse layout
                                        TempRef.setValue(CreduserTemp).addOnCompleteListener(new OnCompleteListener<Void>() {

                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {

                                                    try {
                                                        throw task.getException();
                                                    }  catch (Exception e) {
                                                        // log error here
                                                        Log.d(TAG, "" + e);
                                                    }
                                                    Log.d(TAG, "Failed Adding Cred user ");
                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Adding User Failed!");

                                                } else {
                                                    Log.d(TAG, " Adding Cred user Success ");

                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Adding User Success !");
                                                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                                    // device will be added automatically by event

                                                }
                                                App_UI_Call(CMD_SET_DISPLY_VISIBILITY, SCREEN_NORMAL);
                                            }
                                        });
                                        dialog.dismiss();
                                    }
                                });
                                alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {

                                    }
                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                            }
                        }
                        else
                        {
                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Invalid Input");
                        }
                        //  dialog.dismiss();
                        //tvM.setText("An entry for this day already exist!");
                        //DO NOT CLOSE DIALOG
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

    }
    void AppUsrCredUpdate(final CredUserProfile tempUserProf , final int position,final DatabaseReference MasterCrdSentRef)
    {
        final String MastName =  FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        final String Cred_User= tempUserProf.UserCred;
        final String Cred_Uid = tempUserProf.UserId;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // update the credential validity
        LayoutInflater inflater = getLayoutInflater();
        alertDialogBuilder.setTitle("User Credential Validity");
        alertDialogBuilder.setView(R.layout.send_cred);
        //In case it gives you an error for setView(View) try
        alertDialogBuilder.setView(inflater.inflate(R.layout.send_cred, null));

        alertDialogBuilder.setPositiveButton("Update", null);
        alertDialogBuilder.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog)
            {
                 DateStrt  = alertDialog.findViewById(R.id.EtDateStrt);
                 DateExp   = alertDialog.findViewById(R.id.EtDateExp);
                 TimeAct   = alertDialog.findViewById(R.id.EtTimeAct);
                 TimeExp   = alertDialog.findViewById(R.id.EtTimeExp);

                final CheckBox OneTimeAccess = alertDialog.findViewById(R.id.CbOneTimeAccess);
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                //set start data
                DateStrt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // calender class's instance and get current date , month and year from calender
                        final Calendar c = Calendar.getInstance();
                        int mYear = c.get(Calendar.YEAR); // current year
                        int mMonth = c.get(Calendar.MONTH); // current month
                        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                        // date picker dialog
                        datePickerDialog = new DatePickerDialog(MainActivity.this,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        // set day of month , month and year value in the edit text
                                       String Temp =dayOfMonth + "/"
                                               + (monthOfYear + 1) + "/" + year;
                                        DateStrt.setText(Temp);
                                        DateExp.setText("");
                                    }
                                }, mYear, mMonth, mDay);
                        datePickerDialog.show();
                    }
                });
                //set end date
                DateExp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // calender class's instance and get current date , month and year from calender
                        final Calendar c = Calendar.getInstance();
                        int mYear = c.get(Calendar.YEAR); // current year
                        int mMonth = c.get(Calendar.MONTH); // current month
                        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                        // date picker dialog
                        datePickerDialog = new DatePickerDialog(MainActivity.this,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        // set day of month , month and year value in the edit text
                                        String Temp =dayOfMonth + "/"
                                                + (monthOfYear + 1) + "/" + year;
                                        if(ServLib.cmpDate(DateStrt.getText().toString(),Temp)>=0)
                                        {
                                            DateExp.setText(Temp);
                                        }
                                        else
                                        {
                                            DateExp.setText("");
                                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"End Date less then Start Date");
                                        }
                                    }
                                }, mYear, mMonth, mDay);
                        datePickerDialog.show();
                    }
                });
                //set start time
                TimeAct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // calender class's instance and get current date , month and year from calender
                        // Get Current Time
                        final Calendar c = Calendar.getInstance();
                        int mHour = c.get(Calendar.HOUR_OF_DAY);
                        int  mMinute = c.get(Calendar.MINUTE);
                        // date picker dialog
                        // Launch Time Picker Dialog
                        timePickerDialog = new TimePickerDialog(MainActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        String Temp =hourOfDay + ":" + minute;
                                        TimeAct.setText(Temp);
                                        TimeExp.setText("");
                                    }
                                }, mHour, mMinute, false);
                        timePickerDialog.show();
                    }
                });
                //set end time
                TimeExp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // calender class's instance and get current date , month and year from calender
                        // Get Current Time
                        final Calendar c = Calendar.getInstance();
                        int mHour = c.get(Calendar.HOUR_OF_DAY);
                        int  mMinute = c.get(Calendar.MINUTE);
                        // date picker dialog
                        // Launch Time Picker Dialog
                        timePickerDialog = new TimePickerDialog(MainActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        String Temp =hourOfDay + ":" + minute;
                                        if(ServLib.cmpTime(TimeAct.getText().toString(),Temp)>0)
                                        {
                                            TimeExp.setText(Temp);
                                        }
                                        else
                                        {
                                            TimeExp.setText("");
                                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"End Time less then Start Date");
                                        }

                                    }
                                }, mHour, mMinute, false);
                        timePickerDialog.show();
                    }
                });
                positiveButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        String DateStrt_txt  = DateStrt.getText().toString();
                        String DateExp_txt  = DateExp.getText().toString();
                        String TimeAct_txt  = TimeAct.getText().toString();
                        String TimeExp_txt  = TimeExp.getText().toString();


                        if(((!DateStrt_txt.isEmpty()))&&(!DateExp_txt.isEmpty())
                                &&(!TimeAct_txt.isEmpty())&&(!TimeExp_txt.isEmpty()))
                        {
                            // OK
                            // update credentil to user account
                            // creade user cred db
                            DeviceDb Device_Db_temp;
                            try {
                                Device_Db_temp = (DeviceDb)((Device_List.get(position))).clone() ;
                                Device_Db_temp.ActivationDate = DateStrt_txt;
                                Device_Db_temp.ExpiryDate = DateExp_txt;
                                Device_Db_temp.StartTime = TimeAct_txt;
                                Device_Db_temp.EndTime = TimeExp_txt;
                                Device_Db_temp.Deleted = false;
                                if(OneTimeAccess.isChecked())
                                {
                                    Device_Db_temp.OneTimeAccess = 1;
                                }
                                else
                                {
                                    Device_Db_temp.OneTimeAccess = 0;
                                }


                                Device_Db_temp.Is_Master = false;
                                Device_Db_temp.User_Cred = Cred_User;
                                //TODO: check if user exist
                                //update validity in master database
                                String CredValidity =  "Date:"+Device_Db_temp.ActivationDate + "-"+Device_Db_temp.ExpiryDate+"\n"+"Time:"+Device_Db_temp.StartTime+"-"+Device_Db_temp.EndTime+"\nOne Time Access:"+(Device_Db_temp.OneTimeAccess);
                                MasterCrdSentRef.child(Cred_Uid).child("Validity").setValue(CredValidity);

                                TempRef = FirebaseDatabase.getInstance().getReference("User").child(Cred_Uid).child("CredReceived").child(Device_List.get(position).Dev_ID);
                                //TODO : Update dtatabse layout
                                TempRef.setValue(Device_Db_temp).addOnCompleteListener(new OnCompleteListener<Void>() {

                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {

                                            try {
                                                throw task.getException();
                                            }  catch (Exception e) {
                                                // log error here
                                                Log.d(TAG, "" + e);
                                            }
                                            Log.d(TAG, "Failed Adding Cred user ");

                                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Adding User Failed!");

                                        } else {
                                            Log.d(TAG, " Adding Cred user Success ");

                                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Adding Cred user Success !");


                                            if(isSmsPermissionGranted() )
                                            {
                                                try {
                                                    // more then 160 char SMS need sendMultipartTextMessage to be called, check if dual sim need saperate handling
                                                    // String message_temp = "Hi Prachi, Conneced Device nok nok ";
                                                    String message_temp = "Hi " + tempUserProf.UserName + "! Welcome to Connected Devices.\n" + MastName.split("%")[0] +" ("+MastName.split("%")[1]+ ") want to grant you access to " + Device_List.get(position).Dev_ID + " Device.\nYour Credential are updated to your account .Kindly login to CDUser App.\nIf Do not have App, Download 'CDUser' App from Google Play Store.\nThank you";
                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    ArrayList<String> parts = smsManager.divideMessage(message_temp);

                                                    smsManager.sendMultipartTextMessage(tempUserProf.PhoneNo, null, parts, null, null);

                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Sent Credential to " + tempUserProf.UserName + " :" + tempUserProf.PhoneNo);
                                                    Log.d(TAG, " SMS Send success!");
                                                    // Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();

                                                } catch (Exception e) {
                                                    // Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, " SMS Send Fail! " + e.toString());
                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Failed Sending Credential to " + tempUserProf.UserName);

                                                }
                                            }
                                            else
                                            {
                                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Failed Sending Credential to " + tempUserProf.UserName + "\nAllow permissions to Send SMS ");

                                            }
                                            // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                            // device will be added automatically by event

                                        }
                                        dialog.dismiss();
                                        App_UI_Call(CMD_SET_DISPLY_VISIBILITY, SCREEN_NORMAL);
                                    }
                                });
                            }catch (CloneNotSupportedException e)
                            {

                                Log.d(TAG,"Clone not supported");
                            }
                        }
                        else
                        {
                            App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Inavlid date and time format");
                            // discard
                        }
                    }
                });
                Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //CLOSE THE DIALOG
                        dialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void AddLocDbData(String key, String Val)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("CDMASTER",MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putString(key,Val); // can put differant values string, int etc
        editor.apply();
    }
    public String GetLocDbData(String key)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("CDMASTER",MODE_PRIVATE);
        return(sharedPreferences.getString(key,"")); // if value not present "" will be returned
    }
    boolean AppValidateCred(DeviceDb DeviceDbTmp)
    {
        // get bluetooth device
        boolean credValid = false;
        final boolean is_master = DeviceDbTmp.Is_Master;
        if (!is_master) // its user cread
        {
            /*
            String curDate = GetDate();
            String curTime = GetTime();
            Log.d(TAG, "Cur Time " + curTime);
            Log.d(TAG, "Cur Date" + curDate);
            Log.d(TAG, "Exp Time " + DeviceDbTmp.StartTime + "," + DeviceDbTmp.EndTime);
            Log.d(TAG, "Exp Date" + DeviceDbTmp.ActivationDate + "," + DeviceDbTmp.ExpiryDate);
            String User_UID = DeviceDbTmp.User_Cred;
            String UID = FirebaseAuth.getInstance().getUid();
            Log.d(TAG, "MAC Expected:" + User_UID + ",found:" + UID.substring(UID.length()-17));
            String PastActionDate = GetLocDbData(LAST_ACTION_DATE);
            Log.d(TAG,"Past Action Date:"+ PastActionDate);

            if (User_UID.equals(UID.substring(UID.length()-17)))
            {
                Log.d(TAG, "Mac valid");
                if ((PastActionDate.length() == 0) || ServLib.cmpDate(PastActionDate, curDate) >= 0)  // last action date shuld be <= current date , past time attack protection
                {
                    Log.d(TAG, "Android APP Date Valid");
                    if (ServLib.cmpDate(DeviceDbTmp.ActivationDate, curDate) >= 0) // cueent date is > ActivationDate
                    {

                        Log.d(TAG, "Date Valid 1");
                        if (ServLib.cmpDate(curDate, DeviceDbTmp.ExpiryDate) >= 0) // cueent date is < ExpDate
                        {
                            Log.d(TAG, "Date Valid 2");
                            if (ServLib.cmpTime(DeviceDbTmp.StartTime, curTime) >= 0) // cueent time is > Activation Time
                            {
                                Log.d(TAG, "Time Valid 1");
                                if (ServLib.cmpTime(curTime, DeviceDbTmp.EndTime) >= 0) // cueent time is < Exp Time
                                {
                                    Log.d(TAG, "Cred valid");
                                    credValid = true;
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Credential Do not belongs to User!");
                // TODO: delete credential from db
            }
            if(!credValid)
            {
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "This Credential is Expired !");
            } */
        }
        // Alert user and take input
        else
        {
            credValid = true;
        }
        return credValid;
    }

    void HandleDevTypAction(final int position) {
        // setup custom view to LOADING

        if (action_builder == null) // this variable is declared global because buttun image get updated by rx frame
        {
            action_builder = new AlertDialog.Builder(MainActivity.this);
            action_builder.setView(ActionContent);
            action_builder.setTitle("User Action");
            action_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Log.d(TAG, "Clicked Cancel");
                    Action_alertDialog.dismiss();

                }
            });
            Action_alertDialog = action_builder.create();
            Action_alertDialog.setCancelable(true);
        }
        // Your dialog code.
        Action_alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                // get currest status
                Log.d(TAG, "Reading Device Status");

                SendBleCommand(position,CMD_BLUETOOTH_DEV_CRED,BLE_UART_FUCTION_GET_GPIO,0);
                lay_loadingPanel.setVisibility(View.VISIBLE);
                lay_ActionPanel.setVisibility(View.INVISIBLE);

                switch ((Device_List.get(position)).Dev_Typ) {
                    case 1:
                        lay_ActionPanelTyp1.setVisibility(View.VISIBLE);
                        lay_ActionPanelTyp2.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        lay_ActionPanelTyp1.setVisibility(View.INVISIBLE);
                        lay_ActionPanelTyp2.setVisibility(View.VISIBLE);
                        break;
                }

                // READ Button status and update image

                Button_typ1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int function_data;
                        lay_loadingPanel.setVisibility(View.VISIBLE);

                        if (Button_typ1.getTag().equals("img_off")) // if device is alredy on
                        {
                            function_data = 0x01; // NA
                        } else // device is off
                        {
                            function_data = 0x00; // NA
                        }
                        SendBleCommand(position,CMD_BLUETOOTH_DEV_CRED,BLE_UART_FUCTION_SET_GPIO,function_data);

                    }
                });

            }
        });
        Action_alertDialog.show();
    }
    void SendBleCommand(int position,int AppCmd,int Uartfunc,int UartFuncData)
    {

        int Rand = (Device_List.get(position)).Dev_Rand;
        int UniqueMast = (Device_List.get(position)).Mast_Id;
        byte DevType = (byte) (Device_List.get(position)).Dev_Typ;
        String DeviceID = (Device_List.get(position)).Dev_ID;
        BluetoothDevice device = App_Ba.getRemoteDevice((Device_List.get(position).Dev_Mac));
        globalCurrentPairingKey = (Device_List.get(position)).PairingPw;
        BleLibCmd temp_Cmd = new BleLibCmd(AppCmd, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, Uartfunc, UartFuncData, DeviceID, null, position);
        APP_SendCmdToLooper(AppCmd,  temp_Cmd);
    }
}
