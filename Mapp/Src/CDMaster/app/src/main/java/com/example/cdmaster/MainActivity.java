package com.example.cdmaster;

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
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.grpc.Context;

import static android.app.PendingIntent.getActivity;
import static com.example.cdmaster.BleLib.App_Ba;
import static com.example.cdmaster.BleLib.bluetoothSocket;
import static com.example.cdmaster.BleLib.col_macAddressParts;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_COMMISION;


import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FLASH;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FUCTION_GET_GPIO;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLE_UART_FUCTION_SET_GPIO;
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
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_GET_VERSION_SUCCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.BLUETOOTH_SUCESS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_CONNECT_COMMISION;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_CRED;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DEV_FLASH;
import static com.example.cdmaster.GLOBAL_CONSTANTS.CMD_BLUETOOTH_DIV_DISCOVERY_5SEC;
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
import static com.example.cdmaster.ServLib.isEmailValid;
import static com.google.common.collect.ComparisonChain.start;


public class MainActivity extends AppCompatActivity {

    private LoopThread _Thread_LoopHandThread = new LoopThread("Looper",this);
    public static volatile Handler _Handler_MainHandler;
    public static  Handler ActionUiHandler;
    public int Uniqe_Id=0;
    final private String TAG = "TAG_MAIN_Thread";


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
    FirebaseStorage mFirebaseStorage;
    StorageReference mStorageRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    DatabaseReference TempRef;
    public static ArrayList<DeviceDb> Device_List =new ArrayList<>();

    private Button App_ButAddDev, App_ButHelp, App_ButQuit,App_ButCancel,App_ButLogout;
    private TextView App_TxtVPercentage,TxtV_ProgInfo;
    RelativeLayout App_loadingPanel;
    ListView Lw_BleDev_temp;
    //Action vindow
    Button But_typ1;
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
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TempRef = FirebaseDatabase.getInstance().getReference("User");


        mStorageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null)
        {
            Log.d(TAG,"Signed in sucess , setting welcome title...");
            // Name, email address etc
            String name = user.getDisplayName().split("%")[0];
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
            case BLUETOOTH_COMMISION_FAIL:
                App_UI_Call(CMD_DISPLAY_SHORT_ALEART," Comission Failed!");
                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                break;
            case BLUETOOTH_CRED_GET_STATUS_SUCESS:
            case BLUETOOTH_CRED_SUCESS:
                if(((BleLibCmd) msg.obj).Dev_Typ == 1)
                {

                    if ((((BleLibCmd) msg.obj).Function_Data) != 0) {

                        Button_typ1.setImageResource(R.drawable.img_on);
                        Button_typ1.setTag("img_on");
                        App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " Device is On ");
                    } else {

                        Button_typ1.setImageResource(R.drawable.img_off);
                        Button_typ1.setTag("img_off");
                        App_UI_Call(CMD_DISPLAY_SHORT_ALEART, " Device is  Off");
                    }
                }

                lay_loadingPanel.setVisibility(View.INVISIBLE);
                lay_ActionPanel.setVisibility(View.VISIBLE);

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
            case CMD_SET_ACTION_DIALOG_STATUS:
                TxtV_ActionInfoTemp.setText((String) msg.obj);
                break;
            case CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT:
                TxtV_ActionPercentage.setText(((int)msg.obj) + " %");
                break;
            case CMD_SET_MAIN_PROGRESS_PERCENT:
                App_TxtVPercentage.setText(((int)msg.obj) + " %");
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
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
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
                // Alert user and take input
                String dev_type = "Unknown";
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Device Info :");
                if((Device_List.get(position)).Dev_Typ == 1)
                {
                    dev_type = "Wireless Switch";
                }

                alertDialogBuilder.setMessage("Device ID: " +(Device_List.get(position)).Dev_ID + "\n\nDevice Type: "+ dev_type + "\n\nFirmware Version: " + (Device_List.get(position)).Version);
                alertDialogBuilder.setNegativeButton("Ok",new DialogInterface.OnClickListener()
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
            public void onCredClick(final int position)
            {

                byte cred_valid = 0;

                final boolean is_master = (Device_List.get(position)).Is_Master;
                if (!is_master) // its user cread
                {
                    String User_UID = (Device_List.get(position)).User_Cred;
                    if (User_UID.equals(Uniqe_Id))
                    {
                        cred_valid = 1;
                    }
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
                final String MastName =  FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("CredSent");
                TempRef.addListenerForSingleValueEvent(new ValueEventListener() // lisen only once
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG,"User Credential Dataset changed");
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                        //builderSingle.setIcon(R.drawable.ic_launcher);
                        builderSingle.setTitle("Credential Users ");
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                        Log.d(TAG,"Cred Users read !!!");
                        final ArrayList<CredUserProfile> CredUser_tempval_list = new ArrayList();
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
                                final String Cred_PhoneUid = CredUser_tempval_list.get(user_idx).PhoneId;
                                // create user options

                                builderInner.setItems(new CharSequence[] {"\tSend Credential to User", "\tDisable This User", "\tDelete This User", "\tCancel","\tUpdate user Credential"},
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                // The 'which' argument contains the index position
                                                // of the selected item
                                                final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                switch (which) {
                                                    case 0:
                                                        // update the credential validity
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                        LayoutInflater inflater = getLayoutInflater();
                                                        builder.setTitle("Enter User Infos");
                                                        builder.setView(R.layout.send_cred);
//In case it gives you an error for setView(View) try
                                                        builder.setView(inflater.inflate(R.layout.send_cred, null));

                                                        builder.setPositiveButton("Ok", null);
                                                        builder.setNegativeButton("Cancel", null);

                                                        final AlertDialog alertDialog = builder.create();

                                                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                            @Override
                                                            public void onShow(final DialogInterface dialog)
                                                            {
                                                                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                                                positiveButton.setOnClickListener(new View.OnClickListener()
                                                                {
                                                                    @Override
                                                                    public void onClick(View v)
                                                                    {
                                                                        EditText DateStrt  = (EditText)alertDialog.findViewById(R.id.EtDateStrt);
                                                                        EditText DateExp = (EditText)alertDialog.findViewById(R.id.EtDateExp);
                                                                        EditText TimeAct = (EditText)alertDialog.findViewById(R.id.EtTimeAct);
                                                                        EditText TimeExp = (EditText)alertDialog.findViewById(R.id.EtTimeExp);
                                                                        CheckBox OneTimeAccess = (CheckBox)alertDialog.findViewById(R.id.CbOneTimeAccess);

                                                                        String DateStrt_txt  = DateStrt.getText().toString();
                                                                        String DateExp_txt  = DateExp.getText().toString();
                                                                        String TimeAct_txt  = TimeAct.getText().toString();
                                                                        String TimeExp_txt  = TimeExp.getText().toString();


                                                                        if(OneTimeAccess.isChecked() || ((!DateStrt_txt.isEmpty())&&(DateStrt_txt.length()>=8)&&(DateStrt_txt.contains("/"))&&(!DateExp_txt.isEmpty())&&(DateExp_txt.length()>=8)&&(DateExp_txt.contains("/"))
                                                                        &&(!TimeAct_txt.isEmpty())&&(TimeAct_txt.length()>=3)&&(TimeAct_txt.contains(":"))&&(!TimeExp_txt.isEmpty())&&(TimeExp_txt.length()>=3)&&(TimeExp_txt.contains(":"))))
                                                                        {
                                                                            // OK
                                                                            // update credentil to user account
                                                                            // creade user cred db
                                                                            DeviceDb Device_Db_temp;
                                                                            try {
                                                                                Device_Db_temp = (DeviceDb)((Device_List.get(position))).clone() ;

                                                                                if(OneTimeAccess.isChecked())
                                                                                {
                                                                                    Device_Db_temp.ActivationDate = "";
                                                                                    Device_Db_temp.ExpiryDate = "";
                                                                                    Device_Db_temp.StartTime = "";
                                                                                    Device_Db_temp.EndTime = "";
                                                                                    Device_Db_temp.OneTimeAccess = 1;
                                                                                }
                                                                                else
                                                                                {
                                                                                    Device_Db_temp.ActivationDate = DateStrt_txt;
                                                                                    Device_Db_temp.ExpiryDate = DateExp_txt;
                                                                                    Device_Db_temp.StartTime = TimeAct_txt;
                                                                                    Device_Db_temp.EndTime = TimeExp_txt;
                                                                                    Device_Db_temp.OneTimeAccess = 0;
                                                                                }


                                                                                Device_Db_temp.Is_Master = false;
                                                                                Device_Db_temp.User_Cred = Cred_PhoneUid;
                                                                                TempRef = FirebaseDatabase.getInstance().getReference("User").child(Cred_Uid).child("CredReceived").child(Device_List.get(position).Dev_ID);
                                                                                //TODO : Update dtatabse layout

                                                                                TempRef.setValue(Device_Db_temp).addOnCompleteListener(new OnCompleteListener<Void>() {

                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (!task.isSuccessful()) {

                                                                                            try {
                                                                                                throw task.getException();
                                                                                            } catch (FirebaseAuthUserCollisionException e) {
                                                                                                // log error here
                                                                                                Log.d(TAG, "" + e);

                                                                                            } catch (FirebaseNetworkException e) {
                                                                                                // log error here
                                                                                                Log.d(TAG, "" + e);
                                                                                            } catch (Exception e) {
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
                                                                                                    String message_temp = "Hi " + CredUser_tempval_list.get(user_idx).UserName + "! Welcome to Connected Devices.\n" + MastName.split("%")[0] +" ("+MastName.split("%")[1]+ ") want to grant you access to " + Device_List.get(position).Dev_ID + " Device.\nYour Credential are updated to your account .Kindly login to CDUser App.\nIf Do not have App, Download 'CDUser' App from Google Play Store.\nThank you";
                                                                                                    SmsManager smsManager = SmsManager.getDefault();
                                                                                                    ArrayList<String> parts = smsManager.divideMessage(message_temp);

                                                                                                    smsManager.sendMultipartTextMessage(CredUser_tempval_list.get(user_idx).PhoneNo, null, parts, null, null);

                                                                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Sent Credential to " + CredUser_tempval_list.get(user_idx).UserName + " :" + CredUser_tempval_list.get(user_idx).PhoneNo);
                                                                                                    Log.d(TAG, " SMS Send success!");
                                                                                                    // Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();

                                                                                                } catch (Exception e) {
                                                                                                    // Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                                                                                                    Log.d(TAG, " SMS Send Fail! " + e.toString());
                                                                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Failed Sending Credential to " + CredUser_tempval_list.get(user_idx).UserName);

                                                                                                }
                                                                                            }
                                                                                            else
                                                                                            {
                                                                                                App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Failed Sending Credential to " + CredUser_tempval_list.get(user_idx).UserName + "\nAllow permissions to Send SMS ");

                                                                                            }

                                                                                            // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                                                                            // device will be added automatically by event

                                                                                        }
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


                                                        break;
                                                    case 1:
                                                        //Step1: Delete Credential from   CredUser  first

                                                        // delete credential from USER location
                                                        TempRef = FirebaseDatabase.getInstance().getReference("User").child(CredUser_tempval_list.get(user_idx).UserId).child("CredReceived").child(Device_List.get(position).Dev_ID);
                                                        TempRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {

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
                                                                    Log.d(TAG,"Failed to delete User");
                                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Failed To delete user");

                                                                }
                                                                else
                                                                {

                                                                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"User Disabled !");
                                                                    // RowList.add(new RowCls(R.drawable.test,"1.1",DeviceDb_temp.Dev_ID,""));
                                                                    // device will be added automatically by event

                                                                }
                                                            }
                                                        });
                                                        App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                                                        break;
                                                    case 2:
                                                           //Step1: Delete Credential from   CredUser  first

                                                        // delete credential from USER location
                                                        TempRef = FirebaseDatabase.getInstance().getReference("User").child(CredUser_tempval_list.get(user_idx).UserId).child("CredReceived").child(Device_List.get(position).Dev_ID);
                                                        TempRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {

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
                                                        // delete the user data stored
                                                        TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(Device_List.get(position).Dev_ID).child("CredSent").child(CredUser_tempval_list.get(user_idx).UserId);
                                                        //TODO : Update dtatabse layout
                                                        TempRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {

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
                                                        App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_NORMAL);
                                                        break;
                                                    case 3:
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
                                                EditText CredName  =(EditText)alertDialog.findViewById(R.id.EtCredName);
                                                EditText CredEmail = (EditText)alertDialog.findViewById(R.id.EtCredEmail);
                                                EditText CredPhoneNo = (EditText)alertDialog.findViewById(R.id.EtCredPhoneNo);
                                                EditText Credential = (EditText)alertDialog.findViewById(R.id.EtCredential);
                                                String CredName_txt  = CredName.getText().toString();
                                                String CredEmail_txt  = CredEmail.getText().toString();
                                                String CredPhoneNo_txt  = CredPhoneNo.getText().toString();
                                                String Credential_txt  = Credential.getText().toString();

                                                if((CredPhoneNo_txt.length()>9)&&(!CredName_txt.isEmpty())&&((!CredEmail_txt.isEmpty()) && isEmailValid(CredEmail_txt))&&(!Credential_txt.isEmpty())&&(Credential_txt.contains("CRED_INFO :"))&&(Credential_txt.contains(",")))
                                                {
                                                    Log.d(TAG,Credential_txt);
                                                    boolean cred_ok = false;
                                                    String[] arrOfStr = Credential_txt.split("CRED_INFO :");
                                                    String[] arrOfStr_getcode = arrOfStr[1].split(",");
                                                    if((arrOfStr_getcode.length == 2)&&(arrOfStr_getcode[1].length()== 17)&&(arrOfStr_getcode[0].length()>= 28))
                                                    {
                                                        cred_ok = true;
                                                        Log.d(TAG,"Cred OK");
                                                    }

                                                    //TODO: // get encripted string from user
                                                            // user app use device type key to encript
                                                    // parse cred text and update db
                                                    if(cred_ok)
                                                    {
                                                        CredUserProfile CreduserTemp = new CredUserProfile(arrOfStr_getcode[0], CredName_txt, CredPhoneNo_txt, CredEmail_txt, arrOfStr_getcode[1]);
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
                                                                    } catch (FirebaseAuthUserCollisionException e) {
                                                                        // log error here
                                                                        Log.d(TAG, "" + e);

                                                                    } catch (FirebaseNetworkException e) {
                                                                        // log error here
                                                                        Log.d(TAG, "" + e);
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
                                                        dialog.dismiss();
                                                    }
                                                    else
                                                    {
                                                        App_UI_Call(CMD_DISPLAY_SHORT_ALEART,"Invalid Input");
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

                if (!is_master) // its user cread
                {
                    String User_UID = (Device_List.get(position)).User_Cred;
                    if (User_UID.equals(Uniqe_Id ))
                    {
                        cred_valid = 1;
                    }
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


                TempRef = FirebaseDatabase.getInstance().getReference("FirmwareMeta").child("DevTyp_"+DevType).child("Image");
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

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                        final ArrayAdapter<String> arrayAdapterInfo = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);

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
                                                int function = BLE_UART_FLASH;       // ON
                                                int function_data = 0x00; // NA
                                                App_UI_Call(CMD_SET_DISPLY_VISIBILITY,SCREEN_WAIT_INPROGRESS);;
                                                BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_FLASH, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, function, function_data, DeviceName,bytes,position);

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
            public void onActionClick(final int position)
            {

                Log.d(TAG,"Action clicked");
                byte cred_valid = 0;
                // get bluetooth device
                final BluetoothDevice device = App_Ba.getRemoteDevice((Device_List.get(position).Dev_Mac));
                final int Rand = (Device_List.get(position)).Dev_Rand;
                final int UniqueMast = (Device_List.get(position)).Mast_Id;
                final byte DevType = (byte) (Device_List.get(position)).Dev_Typ;
                final boolean is_master = (Device_List.get(position)).Is_Master;

                final String DeviceID = (Device_List.get(position)).Dev_ID;

                if (!is_master) // its user cread
                {
                    String User_UID = (Device_List.get(position)).User_Cred;
                    if ((User_UID).equals(Uniqe_Id )) {
                        cred_valid = 1;
                    }
                            /*
                            // convert hex string to byte values only 4 bytes are considerd from MAC
                            for (int i = 0; i < 4; i++) {
                                Integer hex = Integer.parseInt(macAddressParts[i + 2], 16);
                                CmdCredCommision[i + 3] = hex.byteValue();
                            }
                           */
                }
                else
                {
                    cred_valid = 1;
                }
                // Alert user and take input
                if (cred_valid == 1)
                {
                    // setup custom view to LOADING
                    Log.d(TAG,"Clicked 1");
                    if(action_builder == null) {
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


                    Log.d(TAG,"Clicked 2");

                    // Your dialog code.
                    Action_alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
                    {
                        @Override
                        public void onShow(final DialogInterface dialog)
                        {
                            Log.d(TAG,"Reading Device Status");

                            lay_loadingPanel.setVisibility(View.VISIBLE);
                            lay_ActionPanel.setVisibility(View.INVISIBLE);
                            switch(DevType)
                            {
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

                            int function = BLE_UART_FUCTION_GET_GPIO;       // ON
                            BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_CRED, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, function, 0, DeviceID, null, position);
                            APP_SendCmdToLooper(CMD_BLUETOOTH_DEV_CRED, temp_Cmd);

                            Button_typ1.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    int function_data;
                                    lay_loadingPanel.setVisibility(View.VISIBLE);


                                    if (Button_typ1.getTag().equals("img_off")) // if device is alredy on
                                    {
                                        function_data = 0x01; // NA
                                    } else // device is off
                                    {
                                        function_data = 0x00; // NA
                                    }

                                    int function = BLE_UART_FUCTION_SET_GPIO;       // ON
                                    BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_CRED, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, function, function_data, DeviceID, null, position);
                                    APP_SendCmdToLooper(CMD_BLUETOOTH_DEV_CRED, temp_Cmd);

                                }
                            });

                        }
                    });
                    Action_alertDialog.show();

                }
                else
                {
                    // invalid cred
                    App_UI_Call(CMD_DISPLAY_SHORT_ALEART, "Credential Do not belongs to User!");
                    // TODO: delete credential from db
                }

                /*
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("Command to Device ?");
                alertDialogBuilder.setPositiveButton("Turn ON", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        int function = BLE_UART_FUCTION_SET_GPIO;       // ON
                        int function_data = 0x01; // NA
                        BleLibCmd temp_Cmd = new BleLibCmd(CMD_BLUETOOTH_DEV_CRED, (byte) BLE_UART_COMMISION, device, Rand, UniqueMast, DevType, function, function_data, DeviceName,null,position);
                        APP_SendCmdToLooper(CMD_BLUETOOTH_DEV_CRED, temp_Cmd);
                    }
                });
                alertDialogBuilder.setNegativeButton("Turn Off", new DialogInterface.OnClickListener()
                {
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
                } */

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

                String Dev_ID_db;
                if((pairedDeviceArrayList_ble_copy.get((int)position).getName()).length() == 6) // device ID is present and valid
                {
                    Dev_ID_db = (pairedDeviceArrayList_ble_copy.get((int)position).getName());
                }
                else
                {
                    Dev_ID_db="";
                }


                int Unique_MASTER_ID = (int) UID_byte_array[0] | (((int) UID_byte_array[1])<<8) | (((int) UID_byte_array[2])<<16) |(((int) UID_byte_array[3])<<24);
                BleLibCmd Temp_cmd = new BleLibCmd(CMD_BLUETOOTH_CONNECT_COMMISION,(byte)BLE_UART_COMMISION,device,0x00,Unique_MASTER_ID,0x00,0x00,0x00,Dev_ID_db,null,position);
                APP_SendCmdToLooper(CMD_BLUETOOTH_CONNECT_COMMISION,Temp_cmd);




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
        TempRef.child(UID).child("Devices").child(Device_List.get(position).Dev_ID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {

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
                            Device_List.add(DeviceDb_tempval);
                            // set appropriate image

                            if(DeviceDb_tempval.Dev_Typ == 0x01) // its switch
                            {
                                imag_idx = R.drawable.typ_1;
                            }
                            else
                            {
                                imag_idx = R.drawable.test;
                            }
                            RowList.add(new RowCls(imag_idx,DeviceDb_tempval.Dev_Name));
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

                    } catch (FirebaseNetworkException e) {
                        // log error here
                        Log.d(TAG,""+e);
                    } catch (Exception e) {
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

        TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("Audits").child("User_Audit");
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
                    temp_msg = "User Id : " + UserId.substring(16) + "\nUser Name : " + Audits_temp.UsrName + "\nTime : "+ Audits_temp.Time + "\nAction : " +Audits_temp.Action;
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

        TempRef.child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("DeviceInfo").setValue(DeviceDb_temp).addOnCompleteListener(new OnCompleteListener<Void>() {

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
                    final String UsrName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split("%")[0];;
                    final String UsrEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    final String UsrPhone = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split("%")[1];;

                    TempRef = FirebaseDatabase.getInstance().getReference("User").child(UID).child("Devices").child(DeviceDb_temp.Dev_ID).child("CredSent").child(UID);
                    //TODO : Update dtatabse layout
                    CredUserProfile Temp_CredUserProfileDb = new CredUserProfile(UID,UsrName,UsrPhone,UsrEmail,col_macAddressParts);
                    TempRef.setValue(Temp_CredUserProfileDb).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {

                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    // log error here
                                    Log.d(TAG, "" + e);

                                } catch (FirebaseNetworkException e) {
                                    // log error here
                                    Log.d(TAG, "" + e);
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
}
