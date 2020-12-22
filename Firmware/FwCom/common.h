#ifndef common
#define common
#include "pic16f18313.h"
#include "xc.h"

// CONFIG1
//#pragma config FEXTOSC = OFF    // FEXTOSC External Oscillator mode Selection bits (Oscillator not enabled)
//#pragma config RSTOSC = HFINT32 // Power-up default value for COSC bits (HFINTOSC with 2x PLL (32MHz))
#pragma config CLKOUTEN = OFF   // Clock Out Enable bit (CLKOUT function is disabled; I/O or oscillator function on OSC2)
#pragma config CSWEN = ON       // Clock Switch Enable bit (Writing to NOSC and NDIV is allowed)
#pragma config FCMEN = ON       // Fail-Safe Clock Monitor Enable (Fail-Safe Clock Monitor is enabled)
#pragma config FEXTOSC = OFF    // RA5 functional as input
// CONFIG2
#pragma config MCLRE = OFF       // Master Clear Enable bit (MCLR/VPP pin function is MCLR; Weak pull-up enabled )
#pragma config PWRTE = OFF      // Power-up Timer Enable bit (PWRT disabled)
#pragma config WDTE = OFF       // Watchdog Timer Enable bits (WDT disabled; SWDTEN is ignored)
#pragma config LPBOREN = ON     // Low-power BOR enable bit (ULPBOR enabled)
#pragma config BOREN = SBOREN   // Brown-out Reset Enable bits (Brown-out Reset enabled according to SBOREN)
#pragma config BORV = LOW       // Brown-out Reset Voltage selection bit (Brown-out voltage (Vbor) set to 2.45V)
#pragma config PPS1WAY = ON     // PPSLOCK bit One-Way Set Enable bit (The PPSLOCK bit can be cleared and set only once; PPS registers remain locked after one clear/set cycle)
#pragma config STVREN = ON      // Stack Overflow/Underflow Reset Enable bit (Stack Overflow or Underflow will cause a Reset)
#pragma config DEBUG = OFF      // Debugger enable bit (Background debugger disabled)

// CONFIG3
#pragma config WRT = OFF        // User NVM self-write protection bits (Write protection off)
#pragma config LVP = OFF        // Low Voltage Programming Enable bit (HV on MCLR/VPP must be used for programming.)

// CONFIG4
#pragma config CP = OFF         // User NVM Program Memory Code Protection bit (User NVM code protection disabled)
#pragma config CPD = OFF        // Data NVM Memory Code Protection bit (Data NVM code protection disabled)

typedef unsigned char 		uint8;
typedef unsigned char 		uint8_t;
typedef short unsigned int 	uint16;
typedef short unsigned int 	uint16_t;
typedef long unsigned int 	uint32;
typedef long unsigned int 	uint32_t;

// use to stringify the things
#define _str(x)  #x
#define str(x)  _str(x)  

// The bootloader code does not use any interrupts.
// However, the downloaded code may use interrupts.
// The interrupt vector on a PIC16F1937 is located at 
// address 0x0004. The following function will be located 
// at the interrupt vector and will contain a jump to
// 0x0204
//-----------------------------------------------Memory MAP -------------------------------------------------------
#define APP_START_ADD            0x2A0  // bootloader start from 0x00 and end at 0x2A0
#define APP_CRC_START            0x7DE
#define START_KEY_FLASH          0x7E0  // App starts at 2A0 and end at 0x7CF
#define END_FLASH                0x7FF

#define  NEW_RESET_VECTOR        APP_START_ADD
#define  NEW_INTERRUPT_VECTOR    APP_START_ADD + 4 // 0x2A4

#define BL_VERSION 0x01
struct FormwareMap
{
    uint8 Bootloader[APP_START_ADD];                // Bootloader resides here 
	
	uint8 App[START_KEY_FLASH - APP_START_ADD - 2]; // Application resides here
	uint16 AppCrc;                                  // Application CRC
	
	// Factory data / keys resides here total 32 bytes from 7E0 to 0xFFF
	uint8 KeysCID[8];  // connection ID "CDXXXXXX" connection ID
	uint8 KeysPW1 [8];  // connection password "XXXXXX"
    uint8 KeysPW2 [8];  // connection password "XXXXXX"
	uint8 KeysModel[8]; // model key cmmon accross all models ..not used to be used to MAAP and device and genuinity,OTA  
};

const extern char sn[8]         @ 0x7E0;
const extern char pw1[8]        @ 0x7E8;
const extern char modelKey[8]   @ 0x7F0;
const extern char pw2[8]        @ 0x7F8;
const extern volatile uint16 appCrc      @ APP_CRC_START;

//-----------------------------------------------EEPROM MEMORY MAP-------------------------------------------------------

enum EEP
{
    EEP_DAY_RTC_HRS_START24_DUR_30 = 0xF000,   // first 5 bits will contain hrs of day 0 - 23) (24 to 31 values un used) (next two bits contain 0:0 min(disable) 1:30 min 2:1hrs 3: 2 hrs) last one bit is action 0 is off 1 is on (this mode can be interrupted/ this mode retain pervious value if not interrupted on completion)
    EEP_DAY_IN_USE_LSB,
    EEP_DAY_IN_USE_MSB,
	
    EEP_APP_VALID,  // contains if app is valid or not 
    EEP_APP_CKEY0,  // commision key
    EEP_APP_CKEY1,
    EEP_APP_CKEY2,
    EEP_APP_CKEY3,
    EEP_APP_CKEY4,
    EEP_APP_CKEY5,
    EEP_APP_CKEY6,
    EEP_APP_CKEY7,
	
	EEP_APP_MKEY0,  // master key
    EEP_APP_MKEY1,
    EEP_APP_MKEY2,
    EEP_APP_MKEY3,
    EEP_APP_MKEY4,
    EEP_APP_MKEY5,
    EEP_APP_MKEY6,
    EEP_APP_MKEY7,
    EEP_APP_FDR, //0xAA is in FDR state
    MAX_EEP_IDX
};
//-----------------------------------------COM command
enum ComCommand
{
    PING=1,         // used as ping command and also responce if in bootloader
    PING_IN_APP, // APP
    PING_IN_COMMISION, // alredy commision and in app
	WR_FLH,
	WR_EEP,
	RD_FLH,
	RD_EEP,
	RSTCMD,
	FLASH,	        // 0x07, command supported by application , to jump bootloader , amkes application invalid
	PING_APP_RESP,  // 0x08, on PING command Application send 8 as responce while BL send 5 as responce
    COMMISION,
    CONFIG,
    FDR,
    ACTION,
    READ,
    DEBUG = 0x20, // begum message start with space can have any length and not encryped
	DEF = 0xFF,
};

#define     RESP_LEN 18
#define     DEBUG_MODE 0
struct Str_ReadMem
{
    volatile uint16 eepFlshAdd;
    volatile uint8* Dptr;
    volatile uint8 len;
};
struct Str_Flh
{
    volatile uint16  *flashWordArray;
	volatile uint8 idx;
};
struct Str_EepWr
{
    volatile uint16 eepAdd;   // F000 to F0ff
    volatile uint8 *Dptr;    // pointer to data
	volatile uint8 len;      // length
};

union Un_Bt_Data
{
    struct Str_EepWr EepWr;
    struct Str_Flh Flh;
    struct Str_ReadMem  ReadMem;
};

//LED
#define set_pin_0 LATAbits.LATA0=1
#define get_pin_0 LATAbits.LATA0
#define clear_pin_0 LATAbits.LATA0=0
#define toggle_pin_0 LATAbits.LATA0^=1

#define set_pin_1 LATAbits.LATA1=1
#define get_pin_1 LATAbits.LATA1
#define clear_pin_1 LATAbits.LATA1=0
#define toggle_pin_1 LATAbits.LATA1^=1

#define set_pin_2 LATAbits.LATA2=1
#define get_pin_2 LATAbits.LATA2
#define clear_pin_2 LATAbits.LATA2=0
#define toggle_pin_2 LATAbits.LATA2^=1

#define set_pin_4 LATAbits.LATA4=1
#define get_pin_4 LATAbits.LATA4
#define clear_pin_4 LATAbits.LATA4=0
#define toggle_pin_4 LATAbits.LATA4^=1

//SWITCH
#define set_pin_5 LATAbits.LATA5=1
#define get_pin_5 PORTAbits.RA5
#define clear_pin_5 LATAbits.LATA5=0
#define toggle_pin_5 LATAbits.LATA5^=1


#define WRITE_FLASH_BLOCKSIZE    32
#define ERASE_FLASH_BLOCKSIZE    32
#define START_WRITE_FLASH_BLOCKSIZE (WRITE_FLASH_BLOCKSIZE - 1)

#define ApiClrEEP(addEep,datlen) Bt_Data.EepWr.Dptr = 0; NVMADR=addEep; Bt_Data.EepWr.len = datlen; Bt_WriteEep()
#define ApiWritEEP(addEep,dataPtr,datlen) Bt_Data.EepWr.Dptr = dataPtr; NVMADR = addEep; Bt_Data.EepWr.len = datlen; Bt_WriteEep()

#define ApiReadEEP(addEep,dataPtr,datlen)    Bt_Data.ReadMem.Dptr = dataPtr; Bt_Data.ReadMem.eepFlshAdd = addEep; Bt_Data.ReadMem.len = datlen;  Bt_ReadData()  // read eeprom
#define ApiReadFlash ApiReadEEP
#define ApiWriteFlash(flshAdd, dataPtr) NVMADR = flshAdd; Bt_Data.Flh.idx = WRITE_FLASH_BLOCKSIZE;Bt_Data.Flh.flashWordArray = dataPtr; Bt_FlashWriteBlock()              

#endif