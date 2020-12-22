/*
 * Title:mini PIC12F 8 bit bootloader
 * Author: Nandumar Ganesh Dhavalikar
// Memory Map
//   -----------------
//   |    0x0000     |   Reset vector
//   |               |
//   |    0x0004     |   Interrupt vector  jump to 2A4
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
//   |    0x7FF     |
//   -----------------
//
*/
#include"common.h"

//-----------------------------------------------Other  Constants -------------------------------------------------------

#define PORT_DIR     0b00101000       // 0 : output 1: input
#define ADC_INPUT    0b00000000       // 0: Digital 1: ADC


volatile union Un_Bt_Data  Bt_Data @ 0x68 = {0};

        
uint8_t *ptr,lent;
void Bt_ComSendData(void);
#define ApiComSendData(ptrD,lenD) ptr=ptrD;lent=lenD; Bt_ComSendData();

void   Bt_UnlockSeq(void);       
void   Bt_FlashWriteBlock(void); // @  0x00C5;
void   Bt_ReadData(void);       //  @ 0x0166;   
void   Bt_WriteEep(void);       //  @ 0x012A;
inline void BlCheckDefResetSeq(void);

void interrupt serrvice_isr()  // all interrupt will jump to this def address but we are making it to jump some differant address
{
	asm ("pagesel  " str (NEW_INTERRUPT_VECTOR));
    asm ("goto   " str (NEW_INTERRUPT_VECTOR));
/*
    #asm
		GOTO	0x204;
	#endasm
*/
}
#define MAXRXBUF 69
#define FLAH_CMD_LEN 68                          // 1 bytecommand , 2 byte address ,64 byte data ,1 byte crc

uint8 frame[MAXRXBUF]; // one for command , 2 for address , 64  for data240
uint16 count; // used for delay
//0xAA is appp valid else invalid

#if (DEBUG_MODE == 1)
const unsigned char appvalid @ (EEP_APP_VALID|0xF000) = 0xFF;  //This is to make APP invalid while we debug(flash bootloader ))
#else
const unsigned char appvalid @ (EEP_APP_VALID|0xF000) = 0xAA; 
#endif

uint8  dat_cnt;     // stores no of bytes
uint16 data_checksum;  // checksum data
uint8  stAppvalid;   // app valid status
uint8  tx_len;       //tx length

uint16 del1_add;
const uint8 BlVer @ (APP_START_ADD -1) = {BL_VERSION};

void set_delay(uint8 del) 
{
    while(del)
    {
        del1_add = 0xFFFF;
        while(del1_add--);
        del--;
    }
}

uint8 frame_chk;
uint8 getFrmCrc(uint8 *data, uint8 len)
{
    frame_chk=0xAB;
    while(len)
    {
        len--;
        frame_chk += data[len];
    }
    return frame_chk;
}

/*
void memclr(uint8 *data, uint8 len)
{
	while(len)
    {
        len--;
        data[len]= 0x00;
    }
}
 */
void main(void)
{
    
    //OSCILLATOR
    OSCCON1bits.NOSC=0;         // HFINTOSC + 2x PLL = 32MHz {MUST}
    OSCCON1bits.NDIV=0;         // no clock divider          {MUST} 
    while(!OSCCON3bits.ORDY);
   
    
    //DIO
    TRISA = PORT_DIR;       // set pin dir sen=t in systam config // 0: for output , 1 for input
    ANSELA = ADC_INPUT;     // Initialise ADC pins 
    WPUA = 0x39;            // tx pin need pullup and led and switch
    
    RXPPS = 0x03;   //PPS   // rx pps RA3
    RA4PPS = 0x14;  //PPS    //tx pps RA4
    
    //UART
    TX1STA=0x24;  //  transmitter config
    BRG16 = 0;    // set baud rate at 32 MZ internal ocillator
    
    RC1STA=0x90;  // enable seial port
    BlCheckDefResetSeq();
    
    // set the baud

    SPBRG=34u;    // 34: 57.6k , 103:19.3k , 207:9600

    Bt_Data.ReadMem.Dptr = 0x7FF; // just to force compiler to use 16 bit pointer
    Bt_Data.EepWr.Dptr = 0x7FF; // just to force compiler to use 16 bit pointer
    while(1)
    { 
        if(RCIF) //if any byte received
        {
            if(dat_cnt >= MAXRXBUF)
            {
                dat_cnt = 0;
            }
            frame[dat_cnt] = RC1REG;
			dat_cnt++;
            RCIF=0;
            count=0;  
        }
        else
        {
            count++;
            if(count == 0xFFFF) // break detected
            { // process data 

                tx_len = 0;
                if((dat_cnt == 0)) //no data is received then check if app valid
                {
#if DEBUG_MODE
                    goto  JumpApp;
#endif
                    // Read app valid flag is set by OTA
					ApiReadEEP(EEP_APP_VALID,&stAppvalid,1); // make this and flash read multi byte
                    if(stAppvalid == 0xAA) //0xAA is appp valid else invalid // application valid flag is true, // NVM adress starts from F000h to F0FFh last byte
                     {
                         // verify app
						data_checksum = 0xabcd;
						del1_add = APP_START_ADD; // app address
                        
                        while(del1_add < (START_KEY_FLASH - 2)) //0x2A0 to 7DD
						{
                             ApiReadFlash(del1_add, &count,2);   // Bt_Data.ReadMem.typ = 0; Bt_ReadData(); Bt_Data.ReadMem.result;
							            
							 data_checksum += count;
							 if(data_checksum&0x8000)
							 {
								dat_cnt = 1;
							 }
							 data_checksum = (data_checksum<<1) + dat_cnt;
                             dat_cnt =0;
							 del1_add++;
						} 
                        if(data_checksum == appCrc) // valid application , in debug mode bypass checksum
                        {
                   
                           // clear_pin_0; alredy done by application on init
                          JumpApp:
                            STKPTR = 0x1F;
                            asm ("pagesel " str(NEW_RESET_VECTOR));
                            asm ("goto  "  str(NEW_RESET_VECTOR));
                            
                        }
                        else
                        {
                           // make application invalid
                           ApiClrEEP(EEP_APP_VALID,1);
                        }
                     }
                     
                }
                else 
                {
                    if(getFrmCrc(frame,dat_cnt-1) == frame[dat_cnt-1])
                    {
                        tx_len = RESP_LEN;
                        if((frame[0] == WR_FLH)) //write flash request 
                        {
                            if((dat_cnt==FLAH_CMD_LEN) && ((*((uint16*)&frame[1u])) >= NEW_RESET_VECTOR)) //prevent application to write bootloader memory
                            {   //TODO: keys are not write protected
                                 ApiWriteFlash(*((uint16*)&frame[1u]) , &frame[3u] );                                
                            }
                        }
                        else if(frame[0] == WR_EEP) // write eeprom
                        {
                            ApiWritEEP((*(uint16*)&frame[1u]), &frame[4u], frame[3u]); // make command for variable length
                        }
                        else if(frame[0] == RD_EEP) // read eep or flash
                        {
                            ApiReadEEP((*((uint16*)&frame[1u])),&frame[4u],frame[3u]);
                        }
                        /*
                        else if ((frame[0] == RSTCMD) )
                        {
                            asm("RESET"); // reset
                        }  
                        */
                        else if ((frame[0] == PING) ) // awake signal , sand awake back
                        {
                           
                            //Send data as it is Need to Send SN
                        }
                        else
                        {
                            // un supported command received
                            tx_len = 0; // DONOT respond
                        }
                    }
                    else
                    {
                      // frame[0] = 0xAA; //CRC error
                      // tx_len = RESP_LEN; 
                    }
                }
                frame[(RESP_LEN-1)] = getFrmCrc(frame,(RESP_LEN-1)); // last byte crc
				ApiComSendData(frame,tx_len);// all responces are of 5 byte long
                frame[0] = DEF; // this is to make sure on bus idle default is executed
                dat_cnt = 0;
                toggle_pin_0;
            }
        }   
    }
}
void Bt_ComSendData(void) 
{
    while(lent) 
    {
        TX1REG = *ptr;
        ptr++;
        lent--;
        while(TXIF==0); // wait for buffer to empty
    }
    while(!TRMT); // wait till all bits are trasmitted
}
asm("global _Bt_FlashWriteBlock"); // this will remove optimization for below function , code will be generated for ame
void  Bt_FlashWriteBlock(void)  //@ 0x00CF
{
    NVMCON1bits.WRERR = 0;      // clear WRERR bit at power up
    NVMCON1bits.NVMREGS=0;
    
    while(WR);
    // Flash write must start at the beginning of a row
    //-------------------- Block erase sequence address alredy updated in API

    // Block erase sequence
    NVMCON1bits.FREE = 1;    // Specify an erase operation
    NVMCON1bits.WREN = 1;    // Allows erase cycles

    // Start of required sequence to initiate erase
    Bt_UnlockSeq();

    //---------------------------- Block write sequence
    NVMCON1bits.LWLO = 1;    // Only load write latches
 
    while(Bt_Data.Flh.idx)
    {
        // Load data in current address
        NVMDAT = *Bt_Data.Flh.flashWordArray;
        if(Bt_Data.Flh.idx == 1)
        {
            // Start Flash program memory write
            NVMCON1bits.LWLO = 0;
        }
        Bt_UnlockSeq();
		NVMADR++;  // point to next address // two bytes at a time , its pointer to uint16
		Bt_Data.Flh.flashWordArray++;
		Bt_Data.Flh.idx--;
    }
    NVMCON1bits.WREN = 0;       // Disable writes
}

void Bt_UnlockSeq(void)
{
    NVMCON2 = 0x55;
    NVMCON2 = 0xAA;
    NVMCON1bits.WR = 1;      // Set WR bit to begin erase
    NOP();
    NOP();
}
asm("global _Bt_ReadData"); // this will remove optimization for below function , code will be generated for ame
void Bt_ReadData(void) //@ 0x0122
{
	if(Bt_Data.ReadMem.eepFlshAdd < 0xF000)
	{
		NVMCON1bits.NVMREGS = 0;
	}
	else
	{
		NVMCON1bits.NVMREGS = 1;
	}
     
    while(Bt_Data.ReadMem.len)
    { 
		NVMADR = Bt_Data.ReadMem.eepFlshAdd;
		NVMCON1bits.RD = 1;      // Initiate Read
        NOP();
        NOP();
        *Bt_Data.ReadMem.Dptr = NVMDATL;
        Bt_Data.ReadMem.Dptr++;
        Bt_Data.ReadMem.len--;
        
        if(NVMCON1bits.NVMREGS == 0)
        {
            *Bt_Data.ReadMem.Dptr = NVMDATH;
			Bt_Data.ReadMem.Dptr++;
			Bt_Data.ReadMem.len--;
        }
		Bt_Data.ReadMem.eepFlshAdd++;
    }
}

asm("global _Bt_WriteEep"); // this will remove optimization for below function , code will be generated for ame
void Bt_WriteEep(void) // @ 0x0150
{   
	while(Bt_Data.EepWr.len)
	{
		NVMCON1bits.WRERR = 0;      // clear WRERR bit at power up
		NVMCON1bits.NVMREGS = 1;
		WREN = 1;
		NVMDATL=0xFF; // erase
		if(Bt_Data.EepWr.Dptr)
		{
			NVMDATL = *Bt_Data.EepWr.Dptr;
		}
		Bt_UnlockSeq();
		while(NVMCON1bits.WR);
		WREN = 0;
		
		Bt_Data.EepWr.len--;
		Bt_Data.EepWr.Dptr++;
		NVMADR++;// NVM adress starts from F000h to F0FFh
	}
}
inline void BlCheckDefResetSeq(void)
{
	if(get_pin_5 == 0)  // if switch is pressed on power up
    { 
		set_delay(60); // switch hold for enough time 
		if(get_pin_5 != 0)
		{
            while(get_pin_5 == 0);// wait for switch to release
            set_pin_0;// disply LED indication saying Switch sequence is passed


            SPBRG=207u;   // set baud 9600 as its basic baud of HC-06

            ApiComSendData("AT+BAUD7",8); // do not send end string  // set 57k baud  // but this command is sent at 9600 baud
            set_delay(10);  

            // set the def baud
            SPBRG=34u;    // 34: 57.6k , 103:19.3k , 207:9600    
            // set default UN 
            ApiComSendData("AT+NAME",7);
            ApiComSendData(sn,8);//  do not send end of string , 5 byte UID "AT+NAMEXXXXXXXX" written in fectory
            set_delay(10);      
            // set PW
            ApiComSendData("AT+PIN",6);
            ApiComSendData(pw1,8);  // 8 byte password "AT+PINXXXXXXXX" writen in fectory
            // erase data which is FDR erasable
            ApiClrEEP(EEP_APP_VALID,17); //  delete 16 byte keys and 1 byte app invalid
            asm("RESET"); //  reset the device after FDR (this was  done because uart receiver was not working after fdr)        
        }	
    }
    
}
