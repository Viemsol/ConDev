#include  "include.h"

uint8 Led_st_cnt,BlinkPriod;
const uint8 AppVer @ (APP_CRC_START -1) = {APPVERSION};
const uint16 AppCrc @ (APP_CRC_START) = {0xAAAA};

void Com(void)
{
	static uint8 CommKeys[16];        // contains current keys session key calculated on same
	static uint8 comData[RESP_LEN];   // contain recived data[ command ,'16 byte command',CRC]
	static uint8 TempKey[8];          // contain session key
    uint8_t crc = 0xab;
    uint8 valLen = RESP_LEN;  // contain tx rx frame length length
   
	if(com_get_rx_buf_lnt() == RESP_LEN) // compete frame is received
	{
        com_rx_read_data(comData, RESP_LEN);
   
#ifdef BOOTLOADABLE
         ApiReadEEP(EEP_APP_CKEY0, CommKeys, 16);          // in case of FDR these keys = Factory key
#endif
 
#ifdef ENCRIPTION  	
        if(comData[0] > PING_IN_COMMISION)
        {
            ddpSimp(TempKey,CommKeys, 8, ENC);              // this is user encryption key
            if(comData[0] < ACTION)
            {
                ddpSimp(TempKey,(CommKeys+8),8,ENC);        // master key all commands below action master have previlage
            }
            ddpSimp(&comData[1], TempKey, 17, DEC);
        }
#endif
        ddpSimp(&crc,&comData[0],17,CRC);
        
        if(crc != comData[17])
        {
            printfD(" CrcErr");
            return;
        }
	}
	else
	{
        RX_FLUSH; //Flush data
		return;
	}
    
	if(comData[0] == PING) // PING, RAND[8],EMPTY[8] CRC[]
	{  
        // no encryption
		ddpSimp(TempKey,&comData[9],8,RAND);                 // create random number
		// this key to be used for rand key = rnadom num encripted with commition key           
        // share this key with peer
        comData[0] = PING + 1;                                // we are in application
        if(ddpSimp(CommKeys,pw1,8,MEMCMP))    // we are in FDR state
        {
            comData[0]++;                                   // FDR state
        }

        ddpSimp(&comData[1], sn, 8, MEMCPY);  // share sn with peere
        ddpSimp(&comData[9], TempKey, 8, MEMCPY);   
	}
	else
	{
        if(comData[0] == COMMISION)//COMMISION
        {
             // device send some random16 bytes in this frame encryptrd with default commition key
             // default commiton key = 6 bytes of PW and 3 bytes of model key
             // update keys after commision

             DisISR;
             //// user key and // master key updated this key shuld be random and sent by Mobile device
             // 8 byte  of commition keys to be set to factory keys
             // 8 byte  of master  keys to be set to factory keys
             ApiWritEEP(EEP_APP_CKEY0, &comData[1], 16);
             EnIsr;
        }
        else if(comData[0] == FLASH)
        {
            DisISR;
            // invalidate application and reset
            ApiClrEEP(EEP_APP_VALID,1);
            asm("RESET"); // reset
        }
        else if(comData[0] == CONFIG)
        {
            // update time , update other settings
        }
        else if(comData[0] == ACTION)
        {
            // user actions on of
            led0_blink(10,5);
        }
        else if(comData[0] == READ)
        {
            // user  read device satus
        }
        else if(comData[0] == RD_EEP) // read eep or flash (for reading APP and BL version)
        {
            ApiReadEEP((*((uint16*)&comData[1u])),&comData[4u],comData[3u]);
        }
        else if(comData[0] == FDR) // read eep or flash (for reading APP and BL version)
        {
            fdr(); // to be called 3 times with gap of 1 sec
        }
	}
    // CRC
    comData[17] = 0xab;
    ddpSimp(&comData[17],&comData[0],17,CRC);
#ifdef ENCRIPTION
    if(comData[0] > PING_IN_COMMISION)
    {
        // ENCRYPTION
        ddpSimp(&comData[1],TempKey,17,ENC);
    }
#endif
	com_send_data(comData,valLen);
    // ping -> command -> responce -> ping -> command -> responce// session key can not be reused
}

static uint8_t _itr = 0; // three iteration in fdr
void fdrCancel(void)
{
    _itr = 0;
}
void fdr(void)
{ 
    if(_itr == 0)
    {
        // set default UN 
        com_send_data("AT+NAME",7);
        com_send_data(sn,8);//  do not send end of string , 5 byte UID "AT+NAMEXXXXXXXX" written in fectory
        _itr = 1;
    }
    else if(_itr == 1)
    {
        // set PW
        com_send_data("AT+PIN",6);
        com_send_data(pw1,8);  // 6 byte password "AT+PINXXXXXXXX" writen in fectory
        _itr = 2;
    }
    else
    {
        DisISR;
        // 6 bytes of password and 3 bytes of model key is used as reset keys
        // this is done to make sure each device on FDR will have unique commision key
       
        ApiWritEEP(EEP_APP_CKEY0,pw1,16); //   16 byte  of commition keys to be set to factory keys

        set_pin_0;
        while(!(get_pin_5)); // wait for switch to depress
        asm("RESET"); // reset
    }
}

void LedUpdate(void)
{
	Led_st_cnt--;
	toggle_pin_0;
	if(Led_st_cnt)
	{
		setTmr(LED_BLINK,BlinkPriod); // 250 ms
	}
}

void led0_blink(uint8 cnt, uint8 Priod)
{
    Led_st_cnt = Led_st_cnt + (cnt<<1);
    BlinkPriod =  Priod;
    if(!getTmr(LED_BLINK))
    {
        setTmr(LED_BLINK,BlinkPriod);
    }
}

uint8_t ddpSimp(uint8_t *dest,uint8_t *src, uint8 len,uint8 cmd)
{
	uint8_t st = 1, idx = 0;
    while(len)
    {
        if(cmd == MEMCPY)
        {
            *dest = *src;
        }

        else if(cmd == MEMCMP)
        {
        	if(*dest != *src)
        	{
               st = 0;
        	}
        }

/*
        else if(cmd == XOR)
        {
            *dest ^= *src;
        }
        else if(cmd == ADD)
        {
            *dest += *src;
        }
*/
        else if(cmd == RAND)
        {
           // *(dest) = 0xAA;
            *(dest) += TMR2;
            *(dest) ^=(*(src));
        }
        else if(cmd == ENC)
        {
           *dest += 0xab;
           *dest ^= len;
           *dest ^=  *src;
           idx++;
           if(idx == 8)
           {
               src -= 8; // roll key back , 8 is used and not 7 because src++
               idx=0;
           }
           // make key back to origine
           
        }
        else if(cmd == DEC)
        {
            *dest ^=  *src;
            *dest ^= len;
            *dest -= 0xab;
            idx++; 
            if(idx == 8)
            {
                src -= 8; // roll key back,8 is used and not 7 because src++
                idx=0;
            }
        }
        else if(cmd == CRC)
        {
           *dest += *src; // make sure sorce is assigned value 0xab
           dest--;        // keep  dest same 
        }
        dest++;
        src++;
        len--;
    }
    return st;
}

void printfB(uint8 *data , uint8 len)
{
    uint8 dat,flg = 4;
    while(len)
    {
        dat = ((*data)>>flg)&0xFF + 0x30;
        if(dat > 0x39)
        {
            dat +=7;
        }
        if(flg)
        {
            flg = 0;
        }
        else
        {
            flg = 4;
            data++;
            len--;
        }
        com_send_data(&dat,1);
    }
}