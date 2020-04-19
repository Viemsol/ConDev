#ifndef _INCL_SYSTEM
#define _INCL_SYSTEM

#include <xc.h>

#define DisISR INTCONbits.GIE=0
#define EnIsr INTCONbits.GIE=1

#define CRITICAL_EN DisISR
#define CRITICAL_DIS EnIsr

#define DisPEISR INTCONbits.PEIE=0
#define EnPEISR INTCONbits.PEIE=1

#define TIMER_0_1MS 31 //224
#define Start_Timer_0   T0CON0bits.T0EN=1
#define Stop_Timer_0   T0CON0bits.T0EN=0

#define TXISR_EN (TXIE = 1)
#define TXISR_DIS (TXIE = 0)

#define TX_DIS (TXEN = 0)
#define TX_EN (TXEN = 1)

#define RXISR_EN (RCIE = 1)
#define RXISR_DIS (RCIE = 0)

#define TX_BYTE TX1REG
#define RX_BYTE RC1REG


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


#define set_pin_5 LATAbits.LATA5=1
#define get_pin_5 PORTAbits.RA5
#define clear_pin_5 LATAbits.LATA5=0
#define toggle_pin_5 LATAbits.LATA5^=1

#define WRITE_FLASH_BLOCKSIZE    32
#define ERASE_FLASH_BLOCKSIZE    32
#define END_FLASH                2048


enum SYS_EVENT
{
    NO_EVENT=0x00,
    SW0_RELEASE,
    SW0_PRESS,
    ADC0_DATA,
    FDR
};
union typ_addr
{
  uint16 addr;
  struct
  {
      uint8 addrh;
      uint8 addrl;  
  }; 
   
};
typedef union typ_addr Addr_Typ;

#if(ADC_MODULE == REQUIRE)
struct str_adc_mgr
{
    uint16 data[ADC_COUNT]; // adc data values   
    uint8 ch_idx;
};
void adc_ini(void);
void adc_mgr(void); // called when ADC convertion is complte from ISR
uint16 get_adc_data(uint8 adc_idx);
#endif

#define MAX_EEP_ENTRY 0
#define SELECT_FLASH NVMCON1bits.NVMREGS=0 // prohram memory
#define SELECT_EEPROM NVMCON1bits.NVMREGS=1 // eeprom

struct str_eep_mgr
{
    uint8 *ram_data_ptr;  // start address of ram
    uint8 eep_idx;        // start address of eep 
    uint8 lnt;            // lengt of variable
};

enum st_eep_mgr 
{
    EEP_WRI_IDLE,
    EEP_WRITE_REQ,
    EEP_WRITE_SEQ1,
    EEP_WRITE_SEQ2
};




inline void Sys_Ini(void);
inline void Ini_Oscillator(void);
inline void Ini_Dio(void);
inline void Ini_Timer_0(void);

#if((PWM_0 == REQUIRE)||(PWM_1 == REQUIRE)||(PWM_2 == REQUIRE))
void pwm_ini(void);
void pwm_set_period(uint8 period);

#if(PWM_0 == REQUIRE)
void pwm_0_start_stop(uint8 cmd);
void pwm_0_set_duty(uint16 duty);
#endif

#if(PWM_1 == REQUIRE)
void pwm_1_start_stop(uint8 cmd);
void pwm_1_set_duty(uint16 duty);
#endif

#endif

//void eep_mgr_write(void);
//void eep_mgr_read(void);
//uint8 get_eep_st(void);
uint16 read_mem(uint16 mem_addr,uint8 typ); // reads eep: typ:1 and flash typ:0
uint16 FLASH_ReadWord(uint16 flashAddr);

void led0_task(void);
extern uint8 Led_st_cnt;
#define led0_blink(cnt) Led_st_cnt=Led_st_cnt + cnt;

struct Str_Com
{
    uint8 *ptr;
    uint8 lent;
};
struct Str_ReadMem
{
    uint16 add;
    uint16 result;
    uint8 typ;
    
};
struct Str_Flh
{
    uint16  *flashWordArray;
    uint16  writeAddr;
    uint8 result;
};
struct Str_EepWr
{
    uint8 add;
    uint8 eep_data;
    
};
union Un_Bt_Data
{
    struct Str_Com Com;
    struct Str_EepWr EepWr;
    struct Str_Flh Flh;
    struct Str_ReadMem  ReadMem;
};
void eep_write_char(uint8 add, uint8 data);

extern volatile union Un_Bt_Data Bt_Data @  0x68;

extern void   Bt_ComSendData(void)       @  0x00238;
extern void   Bt_UnlockSeq(void)         @  0x00276;
extern void   Bt_WriteEep(void)          @  0x0024F;
extern void   Bt_FlashWriteBlock(void)   @  0x001B1;

extern void   Bt_ReadData(void)          @  0x0021D;

extern volatile uint8 RxDataReceived;
#endif