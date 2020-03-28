package com.example.cdmaster;

public class GLOBAL_CONSTANTS
{
// Protocol Level COMMAND
// Generic device commands

// Message queue level Commands

    // MAIN UI COMMANDS
    public static final int CMD_DISPLAY_SHORT_ALEART = 10; // displays short alert infor to user 3 sec

    public static final int CMD_SET_DISPLY_VISIBILITY = 11; // sets UI content visibilty as per the mode
        public static final int SCREEN_NORMAL = 110;
        public static final int SCREEN_LIST_NEARBY_DEVICE = 111;
        public static final int SCREEN_WAIT_INPROGRESS = 112;

    public static final int CMD_SET_MAIN_PROGRESS_PERCENT = 12;
    public static final int CMD_SET_ACTION_VIEW = 13;
           public static final int SCREEN_ACTION_TYP_1 = 130;

    public static final int CMD_SET_MAIN_DIALOG_STATUS  = 14 ;
    public static final int CMD_SET_ACTION_DIALOG_STATUS = 15;
    public static final int CMD_SET_ACTION_DIALOG_PROGRESS_PERCENT  = 16;

    // LOOPER COMMANDS
    public static final int CMD_BLUETOOTH_INIT = 20;
        public static final int BLUETOOTH_ERROR = 200;
        public static final int BLUETOOTH_SUCESS = 201;

    public static final int CMD_BLUETOOTH_GET_VERSION = 21;
        public static final int BLUETOOTH_GET_VERSION_SUCCESS = 210;


    public static final int CMD_BLUETOOTH_DIV_DISCOVERY_5SEC = 22; // discover nearby devices
        public static final int BLUETOOTH_DIV_DISCOVERY_COMPLETE = 220; //

    public static final int CMD_BLUETOOTH_CONNECT_COMMISION = 23;
        public static final int BLUETOOTH_COMMISION_FAIL = 230; //
        public static final int BLUETOOTH_COMMISION_PASS = 231; //
        public static final int BLUETOOTH_BONDING_PASS = 233; //

    public static final int CMD_BLUETOOTH_DEV_PING = 24;

    public static final int CMD_BLUETOOTH_DEV_CRED = 25;
        public static final int BLUETOOTH_CRED_FAIL = 250;
        public static final int BLUETOOTH_CRED_SUCESS = 251;

    public static final int CMD_BLUETOOTH_DEV_CRED_STATUS = 26;
        public static final int BLUETOOTH_CRED_GET_STATUS_FAIL = 260;
        public static final int BLUETOOTH_CRED_GET_STATUS_SUCESS = 261;

    public static final int CMD_BLUETOOTH_DEV_FLASH = 27;
        public static final int BLUETOOTH_FLASH_FAIL = 270;
        public static final int BLUETOOTH_FLASH_SUCESS = 271;

    public static final int  CMD_PAIR_DEVICE = 27;

    public static final int EVENT_TIMER = 90;
    public static final int TIMER_RX_TIMEOUT = 900;
    public static final int TIMER_PING_TIMEOUT = 901;
    public static final int TIMER_PAIR_TIMEOUT = 902;

    public static final int DEVICE_PAIR_CONNECT = 2; //
    public static final int DEVICE_SEND_RCV_DATA = 9; //
    public static final int DEVICE_COMMISION = 4; //
    public static final int DEVICE_SEND_DATA = 5; //
    public static final int DEVICE_OTA = 6; //
    public static final int UPDATE_DB =7;
    public static final int DISPLAY_MESSAGE =8;


    public static final int MAX_TIMERS = 5;

    public static final int TIMER_ONESHOT = 0;
    public static final int TIMER_PERIODIC = 1;
    public static final int TIMER_INACTIVE = 0xFF;

    public static final int CMD_BLE_TX_RX = 70;

    public static final int TX_RXSOCKET_ERROR = 700;
        public static final int RX_TIMEOUT = 701;
        public static final int TX_CMD_ERR= 702;
        public static final int RX_CMD_SUCESS= 704;
        public static final int TX_CMD_SUCESS_NO_RESP= 705;

        public static final int TX_BLUETOOTH_PING= 705;
        public static final int TX_BLUETOOTH_COMMISION= 706;

    //Sleep time of OS in ms
    public static final int     TIMEOUT_BLUETOOTH_DISCOVORY = 7000;

    // Timeout values for Timer event task, in 100ms resolution ie 80 = 8000ms
    public static final int  TIMEOUT_COMMISON_FRAME = 80;
    public static final int     TIMEOUT_BLUETOOTH_CONNECT_PAIR = 200;
    public static final int TIMEOUT_FLSHING_FRAME = 700;
// BLE UART CMD
    public static final int MAX_CMD_RESP_LEN_APP = (byte)16;
    public static final int BLE_UART_PING_APP = (byte)0x05;
    public static final int BLE_UART_PING_BL_RESP = (byte)0x05;
    public static final int BLE_UART_RESET = 0x06;
    public static final int BLE_UART_FLASH = 0x07;
    public static final int BLE_UART_PING_APP_RESP = (byte)0x08;
    public static final int BLE_UART_GET_VERSION = 0x51;

    public static final int BLE_UART_COMMISION = 0x0C;

    public static final int BLE_UART_FUCTION_SET_GPIO = 0x11;
    public static final int BLE_UART_FUCTION_GET_GPIO = 0x12;

// BUSINESS PERAMETERS
    public static final int MAX_CRED_FOR_USE_FREE = 2;  // meximum credential master user can distribute
// Security perameter
    public static final int ENCRIPTION_EN = 1;
}
