/*ISR routines*/
#include "include.h"
volatile union Un_Bt_Data Bt_Data @ 0x68 ={0};
void interrupt isr(void) // ISR this controller has only one ISR Vector
{
    //NOTE : Itwas observed that if we call funtion in ISR its global veriables not getting updated
  // timer 0 overflow
  if(PIR0bits.TMR0IF)
  {   
    // TMR0 = TIMER_0_1MS;
    PIR0bits.TMR0IF=0;
    putNextEvent(handleTimer,0); //process pending timer events
  }  
  if(PIR1bits.RCIF)
  {
	  setTmr(RX_BREAK,2); //  reset timer to new value when received when overflow execute frame
      byte_received();
      // RCIF=0;  //read onl set reset by hardware
  }
  else if(PIR1bits.TXIF)
  { 
      if(TXIE)
      {
        byte_transmit();
      }
      //TXIF=0; //this is set / clear by hardware
      
  }
  if(TMR2IF)
  {
      TMR2IF=0;
  }
  if(ADIF) // ADC converstion complete , this flag is clerd by ADC manager
  {
     ADIF=0;
  }
  
  if(IOCAF5)
  {
     if(get_pin_5)
     {
        setTmr(FDR_SW,0); // stop timer
        fdrCancel();
     }
     else
     {
         setTmr(FDR_SW,4); // start timer
     }
     IOCAF5=0;
  }
  
}

inline void Sys_Ini(void)
{
    uint16 count=1;  
    Ini_Oscillator();
    Ini_Dio(); 
    com_ini();
    //DELAY
    while((count++));  // very important check if can be removed
    
    // low power options
    CPUDOZE=0x80; // only cpu and memory disable when sleep, but run clock and peripherals
    
    //--------timer0 interrut init----------------
    SET_10MS_TMR; // OS timer
    //app_ini();     // this is to make sure APP variable get initialize before OS
  
    Start_Timer_0;
    T2CON = 0x04; // used for random number
    IOCAN5 = 1; // rising edge detection for switch
    IOCAP5 = 1; // falling edge for switch
    
    IOCIE = 1; // IOC interrupt enable
    EnPEISR;
    EnIsr;
}

inline void Ini_Oscillator(void) // reviewed
{
    // initializes osillator
#if !defined(BOOTLOADABLE)
    OSCCON1bits.NOSC=0;
    OSCCON1bits.NDIV=0;
    while(!OSCCON3bits.ORDY);
#endif
}

inline void Ini_Dio(void)
{
    // initializes DIO pin purpose , Digital,input,digital ouput, analog input, analog,output
    /*set proper system configuration file for this to take affect*/
    TRISA = PORT_DIR;       // 0: for output , 1 for input (alredy set in BL code for sw,led tx rx)
    ANSELA = ADC_INPUT;     // Initialise ADC pins 
    WPUA =  PULL_UP_BITS;	//1: for pull up, 0 for no pull up (alredy set in BL code for sw,led tx rx)
    //  ODCONA=0; // push pull  
  
    //PPS
#if !defined(BOOTLOADABLE)
    RXPPS = 0x03;     // rx pps RA3
    RA4PPS = 0x14;     //tx pps RA4
#endif    
    LATA  = PIN_INIT; 		//init  all pins
}

