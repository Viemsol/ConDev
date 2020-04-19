#ifndef _INCL_SYSTEM_CONFIG
#define _INCL_SYSTEM_CONFIG
#include "type_def.h"
/*
// Memory Map
//   -----------------
//   |    0x0000     |   Reset vector
//   |               |
//   |    0x0004     |   Interrupt vector   ( jump to 2A4)
//   |               |
//   |               |
//   |  Boot Block   |   (this program)
//   |               |
//   |    0x02A0     |   Re-mapped Reset Vector
//   |    0x02A4     |   Re-mapped High Priority Interrupt Vector
//   |               |
//   |       |       |
//   |               |
//   |  Code Space   |   User program space
//   |               |
//   |       |       |
//   |               |
//   |    0x3FFF     |
//   -----------------
//
*/

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

#define ADC_MODULE NOT_REQUIRE

#define PWM_0 NOT_REQUIRE
#define PWM_1 NOT_REQUIRE
#define PWM_2 NOT_REQUIRE

#define  FLASH_LIB  NOT_REQUIRE

#define ENABLE_ENCRIPTION TRUE

#define DEBUG_MSG NOT_REQUIRE //REQUIRE // NOT_REQUIRE
 // one for command , 2 for address , 64  for data
// The bootloader code does not use any interrupts.
// However, the downloaded code may use interrupts.
// The interrupt vector on a PIC16F1937 is located at 
// address 0x0004. The following function will be located 
// at the interrupt vector and will contain a jump to
// 0x0204
#define  NEW_RESET_VECTOR        0x2A0
#define  NEW_INTERRUPT_VECTOR    NEW_RESET_VECTOR + 0x04

#define _str(x)  #x
#define str(x)  _str(x)

#define BOOTLOADABLE  TRUE //TRUE //FALSE




// configure system pins i/O adc pwm , RS232
/*---------PIN TYPE CONFIGURATION----------------------*/
	/*This IC have 8 pins out of which two are vcc and ground*/
	/*-remaining 6 pin configuration is given below-*/

/*							---------------
	(vcc)	 HW_PIN1-------|			   |--------- HW_PIN8 (ground)
	(I/O,)	 HW_PIN2-------|			   |--------- HW_PIN7 (I/O,ADC)
	(I/O,ADC)HW_PIN3-------|  PIC12F683	   |--------- HW_PIN6 (I/O,ADC)
	(I)	 	 HW_PIN4-------|			   |--------- HW_PIN5 (I/O,ADC,CCP)
							---------------                       */
#define PORT_DIR     0b00101000  // 0 : output 1: input (port directions for LED,SWITCH,TX RX set in Bootloader)
#define PULL_UP_BITS 0x39  // 0: no pullup , 1: pullup ((port directions for LED,SWITCH,TX RX set in Bootloader))

#if(ADC_MODULE == REQUIRE)
#define ADC_INPUT    0b00100111  // 0: Digital 1: ADC
#define ADC_CH0 0x00
#define ADC_CH1 0x01
#define ADC_CH2 0x02
#define ADC_CH3 0x03
#define ADC_CH4 0x04
#define ADC_CH5 0x05
#define ADC_TEMP 0x3D
#define ADC_SEQ {ADC_CH0,ADC_CH1,ADC_CH2,ADC_CH5,ADC_TEMP}  // in this sequence the adc scallin will occure and data is trored in adc.data[]
#define ADC_COUNT 5                 // total no of adc channes to bescanned

#else
#define ADC_INPUT    0b00000000  // 0: Digital 1: ADC
#endif

#define PIN_INIT     0b00111000  // initial value of port pins


#define MAX_EVENT 4
enum
{
    EEP_PW0,     // PW
    EEP_PW1,     // PW
    EEP_PW2,     // PW
    EEP_PW3,     // PW
    EEP_PWC,     // PW CSUM used for commisioning encryption
    EEP_DAY_RTC_HRS_START24_DUR_30,   // first 5 bits will contain hrs of day 0 - 23) (24 to 31 values un used) (next two bits contain 0:0 min(disable) 1:30 min 2:1hrs 3: 2 hrs) last one bit is action 0 is off 1 is on (this mode can be interrupted/ this mode retain pervious value if not interrupted on completion)
    EEP_DAY_IN_USE_LSB,
    EEP_DAY_IN_USE_MSB,
    EEP_APP_VALID,
    EEP_APP_CHKSM,
    EEP_APP_VERSION,
    EEP_APP_MAC_OK,
    EEP_APP_MAC0,
    EEP_APP_MAC1,
    EEP_APP_MAC2,
    EEP_APP_MAC3,
    EEP_APP_LID0,
    EEP_APP_LID1,
    EEP_APP_LID2,
    EEP_APP_LID3,
    EEP_APP_DEVTYP,
    EEP_FD,
    EEP_BOOT_VERSION,
    EEP_FECTORY_OK,
    MAX_EEP_IDX
};
// un changed EEPROM  location or updated in fectory command 
// location   meaning                  value  
// 0xFF       fectory data present     0x01   // when this bit is set application can not program keys OR locations from 0xF8 to 0xFF
// 0xFE       Bl version               0x01
// 0xFD       reserved                 0xXX
// 0xFC       device type              0xXX   // 0x01 : DEvmo device , 0x02 : wall switch
// 0xFB       LID  3                   0xXX...// unique lock ID  UN/PW
// 0xFA       LID  2                   0xXX...// unique lock ID  UN/PW
// 0xF9       LID  1                   0xXX...// unique lock ID  UN/PW
// 0xF8       LID  0                   0xXX...// unique lock ID  UN/PW 

// below is FDR data (0xF0 to F7))
// 0xF7       master_mac_3                   0xXX // Only first 4 bytes from mac are considerd as MAC 
// 0xF6       master_mac_2                   0xXX
// 0xF5       master_mac_1                   0xXX
// 0xF4       master_mac_0                   0xXX
// 0xF3       Mac OK                         0xXX    // 0xFF means not written , else written, and this random value is access link key
// 0xF2       app version                    0xXX
// 0xF1       app checksum                   0xXX
// 0xF0       app valid                      0xXX    // written by OTA at end ,if matches with device type 0xFC jump to crc validation 

// below is other Application specific data
  
// 0xEF       DAY IN USE MSB		   0x01   // this is incremented every 24hrs , updated to value 00 in factory
// 0xEE       DAY IN USE LSB           0x01   // this is incremented every 24hrs , updated to value 00 in factory
// 0xED       DAY_RTC_HRS_START        0xXX   // Timed event value stored for action (0-23)
// 0xEC       DAY_RTC_HRS_STOP         0xXX   // Timed event value stored for action (0-59)
// 0xEB       DAY_RTC_ACTION               0xXX   //(MSB:1=enable, LSB : Status in Start stop duration :ON/OFF:0/1) device take previous state after event, or event get cancelled for a day on override 
// 0xEA       NA                       0xXX   // Timed event value stored for action (0-59)
// 0xE9       NA                       0xXX   //(MSB:1=enable, LSB : Status in Start stop duration :ON/OFF:0/1) device take previous state after event, or event get cancelled for a day on override 
// 0xE8       NA                       0xXX   // unique lock ID  UN/PW 

#define FDR_DAT_START_ADDRESS_LSB 0xF0
#define FDR_DAT_APP_VALID_ADDRESS_LSB 0xF0   // eep write address are 1 byte
#define FDR_DAT_APP_VALID_ADDRESS 0x70F0
#define FDR_DAT_CHECKSUM_ADDRESS 0x70F1
#define FDR_DAT_CHECKSUM_ADDRESS_LSB F1
#define FDR_DAT_MASTER_FREEZ_ADD 0x70F3
#define FDR_DAT_MASTER_FREEZ_ADD_LSB 0xF3
#define FDR_DAT_MASTER_MAC_START_ADD 0x70F4  // read addresses are alwys two bytes
#define FDR_DAT_MASTER_MAC_START_ADD_LSB 0xF4
#define FDR_DAT_MASTER_MAC_END_ADD 0x70F7
#define FDR_DAT_MASTER_MAC_END_ADD_LSB 0xF7

#define FECT_DAT_START_ADDRESS 0x70F8
#define FECT_DAT_LID_START_ADDRESS 0x70F8
#define LID_LEN 4u

#define FECT_DAT_START_ADDRESS_LSB 0xF8

#define FECT_DAT_DEV_TYP_ADDRESS 0x70FC  // this data (device id) indicates app is valid and can be jmed to app after CRC validation
#define FECT_DAT_VALID_TYP_ADDRESS 0x70FF

#define DAY_IN_USE_ADD_HIGH_LSB 0xEF
#define DAY_IN_USE_ADD_LOW_LSB  0xEE
#define DAY_RTC_ACTION_START_TYM_ADD_LSB 0xED
#define DAY_RTC_ACTION_STOP_TYM_ADD_LSB 0xEC
#define DAY_RTC_ACTION_ADD_LSB 0xEB
#define EEP_DEF_VAL 0xFF

#define APP_EEP_START_ADDRESS_LSB 0xE8   // eep write address are 1 byte
#define APP_EEP_START_ADDRESS  0x70E8

#define SYSTEM_VER 0x02 // give info about version number 
#define DEVICE_TYP 0x01 // give info about product (01 is demo test product) {1: DEMO ; 2 :Wall switch ...}

const unsigned char sys_ver @ 0xF0F2 = SYSTEM_VER; 
//const unsigned char sys_typ @ 0xF0FC = DEVICE_TYP; // Not this data is updated in EEP in combined image by parsing #define DEVICE_TYP

#endif