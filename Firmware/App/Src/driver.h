#ifndef _INCL_DRIVER
#define _INCL_DRIVER

#define DisISR INTCONbits.GIE=0
#define EnIsr INTCONbits.GIE=1

#define CRITICAL_EN DisISR
#define CRITICAL_DIS EnIsr

#define DisPEISR INTCONbits.PEIE=0
#define EnPEISR INTCONbits.PEIE=1

#define TIMER_0_1MS 31 //224

#define SET_1MS_TMR T0CON1 = 0b10000000;TMR0L=0;TMR0H=TIMER_0_1MS;PIR0bits.TMR0IF = 0// 1ms //Select LF internal oscillator // Timer T0 counts from//256-126 =131
#define SET_10MS_TMR T0CON1 = 0b10000101;TMR0L=0;TMR0H=10;PIR0bits.TMR0IF = 0         // 10 ms
   
#define Start_Timer_0   T0CON0bits.T0EN=1;PIE0bits.TMR0IE =1
#define Stop_Timer_0   T0CON0bits.T0EN=0;PIE0bits.TMR0IE =0

//add critical sections as per microcontroller ASM code
#define DISABLE_GLOBAL_ISR DisISR
#define ENABLE_GLOBAL_ISR EnIsr

#define TXISR_EN (TXIE = 1)
#define TXISR_DIS (TXIE = 0)

#define TX_DIS (TXEN = 0)
#define TX_EN (TXEN = 1)

#define RXISR_EN (RCIE = 1)
#define RXISR_DIS (RCIE = 0)

#define TX_BYTE TX1REG
#define RX_BYTE RC1REG


inline void Sys_Ini(void);
inline void Ini_Oscillator(void);
inline void Ini_Dio(void);
inline void Ini_Timer_0(void);

extern volatile union Un_Bt_Data Bt_Data @  0x68;

extern void   Bt_WriteEep(void)          @  0x001FB;
extern void   Bt_FlashWriteBlock(void)   @  0x001D2;

extern void   Bt_ReadData(void)          @  0x00195;

#endif
