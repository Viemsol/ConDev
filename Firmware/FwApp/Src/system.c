#include "common.h"


#define _str(x)  #x
#define str(x)  _str(x)
//UART_VARIABLES

#if(ADC_MODULE == REQUIRE)
const uint8 ADC_Ch[ADC_COUNT]=ADC_SEQ;
struct str_adc_mgr adc;
#endif
// Force variables into Unbanked for 1-cycle accessibility 

//static uint8 eep_state;
//uint16 app_data= 0xabcd;
//uint8 test2= 0xef;
//const struct str_eep_mgr eep[MAX_EEP_ENTRY]={{&app_data,0,2}/*,{&test2,2,1}*/};  // variable address,eep index ,length in bytes

uint8 Led_st_cnt;
/*ISR routines*/
void interrupt isr(void) // 10ms timer    os timer
{
    //NOTE : Itwas observed that if we call funtion in ISR its global veriables not getting updated
  // timer 0 overflow
  if(PIR0bits.TMR0IF)
  {    
    karnel_tick;
    // TMR0 = TIMER_0_1MS;
    PIR0bits.TMR0IF=0;

    
  }  
  if(PIR1bits.RCIF)
  {
     // toggle_pin_0;
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
      #if(ADC_MODULE == REQUIRE)
     
      #endif
     ADIF=0;
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
#if(BOOTLOADABLE == TRUE)   // supported only if BL present
    //eep_mgr_read();
#endif
    
    // low power options
    CPUDOZE=0x80; // only cpu and memory disable when sleep, but run clock and peripherals
    
    Ini_Timer_0(); // OS timer
    app_ini();     // this is to make sure APP variable get initialize before OS
  
    Start_Timer_0;
    EnPEISR;
    EnIsr;
}
inline void Ini_Oscillator(void) // reviewed
{
    // initializes osillator
    #if(BOOTLOADABLE == FALSE)
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
#if(ADC_MODULE == REQUIRE)
        adc_ini();
#endif

#if((PWM_0 == REQUIRE)||(PWM_1 == REQUIRE)||(PWM_2 == REQUIRE))
        pwm_ini();
#endif   
    //PPS
#if(BOOTLOADABLE == FALSE)
    RXPPS = 0x03;     // rx pps RA3
    RA4PPS = 0x14;     //tx pps RA4
#endif
#if((PWM_0 == REQUIRE))
    RA1PPS = 0x03;     // PWM6 OR PWM 1 to RA1
#endif
#if((PWM_1 == REQUIRE))
    RA2PPS = 0x02;     // PWM5 OR PWM 0 to RA2
#endif
    
    LATA  = PIN_INIT; 		//init  all pins
   
    
}

inline void Ini_Timer_0(void)
{
    //--------timer0 interrut init----------------
    // select the oscillator register
    T0CON1bits.T0CS0=0;
    T0CON1bits.T0CS1=0;
    T0CON1bits.T0CS2=1;//Select LF internal oscillator
    
    // 8bit auto reload mode
    TMR0L=0;
	TMR0H=TIMER_0_1MS; // value to get 1 ms delay // Timer T0 counts from//256-126 =131
    PIR0bits.TMR0IF = 0; // clear timer0 interrupt flag 
	PIE0bits.TMR0IE =1;     // Enable interrupt TMR0       OR T0IE =1 // timer 0 interrupt enale with globeel interrupt bit 1
}

#if((ADC_MODULE == REQUIRE))
void adc_ini(void)
{
    // Pin directions are set in system_config.h
    //ADC conversion clock is default FOS/2
    //voltage reference is default 5V
    //ADC input channel by default zero
    ADCON0=((ADC_Ch[0u]<<2u)|0x01u); // select channel and turn on adc
    ADIF=0u; // Clear ADC interupt flag
   // ADIE=1; // ADC interrupt
    //ADACT = 0x03; // autoconvertion on every 1ms / os timer
    //The Auto-conversion Trigger allows periodic ADC measurements without software intervention.
    //When arising edge of the selected source occurs, the GO/DONE bit is set by hardware.
    
    //An advantage of left-justified results (on processors that support it) is that you can take just the most-significant byte of the register, giving you 8-bits of precision instead of the native precision.
}

void adc_mgr(void) // called every 1ms
{
    uint8 temp_data;
    if(ADIF)  // conversion complete
    {
        ADIF=0;    
        
        temp_data = ADRESL;     // select adc channel  and wait for signal aquesition time
        adc.data[adc.ch_idx]=ADRESH;
        
        temp_data = temp_data >> 6u;
        adc.data[adc.ch_idx]= ((adc.data[adc.ch_idx])<<2u);
        adc.data[adc.ch_idx]|= temp_data;
        
        adc.ch_idx =(adc.ch_idx < (ADC_COUNT-1u)) ? (adc.ch_idx + 1u) : 0u;
        ADCON0bits.CHS = ADC_Ch[adc.ch_idx];
            
    }
    else
    {
        GO=1; // start convertion
    }
}

uint16 get_adc_data(uint8 adc_idx)
{
    return(adc.data[adc_idx]); // make sure ADC index within range
}

#endif

#if((PWM_0 == REQUIRE)||(PWM_1 == REQUIRE)||(PWM_2 == REQUIRE))
// maximum 3 pwm are possible , time base ie period is same , but duty cycle is configurable
void pwm_ini(void)
{
	// SET TRISIO to 0
    
    TMR2IF = 0u;
    T2CON = 3u;  // pre-scalar is 64, turn on timer
    PR2 = 255u;
    
    #if((PWM_0 == REQUIRE))
        pwm_0_set_duty(0); 
    #endif

    #if((PWM_1 == REQUIRE))
        pwm_1_set_duty(0);
    #endif
    TMR2ON=1;
    
}
void pwm_set_period(uint8 period) //PWM Period = (PR2 + 1) * 4 * TOSC *(TMR2 Prescale Value)
{ 
    // period = 125us*(PR2+1)*(TMR2 Prescale Value))
    // minimum period is 8us 
    // maximum period is 2ms
    
    PR2=period; // 255 ~ 2ms period , 8ms period ~ 
}
#endif

#if(PWM_0 == REQUIRE)
void pwm_0_start_stop(uint8 cmd)
{
    PWM5EN = cmd;
}

void pwm_0_set_duty(uint16 duty)
{
    PWM5DCH = ((duty>>2)& 0xFF);
    PWM5DCL = (uint8)(duty<<6);
}
#endif

#if(PWM_1 == REQUIRE)
void pwm_1_start_stop(uint8 cmd)
{
    PWM6EN = cmd;
}

void pwm_1_set_duty(uint16 duty)
{
    PWM6DCH = ((duty >> 2u)& 0xFF);
    PWM6DCL = (uint8)(duty << 6u);
}
#endif

/*
void eep_mgr_read(void) // reads eep variable data and updates the variables
{
    
    uint8 i,j;
    for(i=0;i<MAX_EEP_ENTRY;i++)
    {
        j=0;
        while(j < eep[i].lnt)
        {
            *(eep[i].ram_data_ptr + j) = read_mem(( eep[i].eep_idx + j ),1u);
            //com_tx_hex((eep[i].ram_data_ptr + j),1);
            j++;
        }
    }
}

void eep_mgr_write(void) // reads eep variable data and updates the variables
{
    static uint8 eep_mgr_idx,eep_data_idx;
    switch(eep_state)
    {
        case EEP_WRI_IDLE:
        break;
        case EEP_WRITE_REQ : 
        SELECT_EEPROM;
        eep_mgr_idx=0;
        eep_data_idx=0;
        WREN = 1;
        NVMADRH=0x70;   // NVM adress starts from F000h to F0FFh 
        case EEP_WRITE_SEQ1:  // initiate write
            if(eep_mgr_idx<MAX_EEP_ENTRY)
            {
                if(eep_data_idx < (eep[eep_mgr_idx].lnt))   
                {
                    NVMADRL = (eep[eep_mgr_idx].eep_idx) + eep_data_idx ;
                    NVMDATL=(*(eep[eep_mgr_idx].ram_data_ptr + eep_data_idx));
                    CRITICAL_EN;
                    Bt_UnlockSeq();
                    CRITICAL_DIS;
                    eep_state = EEP_WRITE_SEQ2;
                }
                else
                {
                    eep_data_idx=0;
                    eep_mgr_idx++;// increment the eep mgr index
                }
            }
            else
            { 
                WREN = 0;
                eep_state = EEP_WRI_IDLE;  // all write operations finished
                // kill task
                os_remove_task(0,eep_mgr_write);
            }
        break;
        case EEP_WRITE_SEQ2 :
            if(!(NVMCON1bits.WR))  // debounce the flag if not clear
            {
                eep_data_idx++;
                eep_state = EEP_WRITE_SEQ1;  
            }
        break;
    }
    
    
}
uint8 get_eep_st(void)
{
    return(eep_state);
}
void start_eep_write(void)
{
    if(eep_state==EEP_WRI_IDLE)
    {
        os_insert_task(0,eep_mgr_write);
        eep_state=EEP_WRITE_REQ;
    }
}
*/
uint16 read_mem(uint16 mem_addr,uint8 typ)
{
    Bt_Data.ReadMem.typ = typ; 
    Bt_Data.ReadMem.add = mem_addr;
    Bt_ReadData(); // read eeprom,flash
    return(Bt_Data.ReadMem.result);
}
void eep_write_char(uint8 add, uint8 data)
{
    Bt_Data.EepWr.add=add;
    Bt_Data.EepWr.eep_data=data;
    CRITICAL_EN;
    Bt_WriteEep();
    CRITICAL_DIS;
}

void led0_task(void) // 500 ms task , give even counts so periviust state of led is not alterd
{
    
    if(Led_st_cnt)
    {
        Led_st_cnt--;
        toggle_pin_0;
    }
}

/*
// make sure these absolute locations are @ end of the bank or section else it will lead to fregmentation
int test_var @ 0x10
const int test_ROM @ 0x100
void test_func(void) @ 0x1000
{

}
*/
