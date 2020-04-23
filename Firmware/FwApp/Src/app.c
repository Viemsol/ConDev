#include "common.h"

uint8 tmr_1ses,tmr_temp_key;

uint8 cmd_res_data[MAX_CMD_FRAME_LEN];
uint8 temp_key = 0xFF; // temp key session key

uint8 event_read;

uint8 sw0_st_prev;
uint8 lock_id[24]; // holds eep read for lock commisioning

struct Dev_On_Time
{
    uint16 days;    // 0-((2^16)-2)
    uint8 hrs;      // 0-23
    uint8 min;      // 0-59
    uint8 sec;      // 0-59
};
struct Dev_On_Time OnTym;
#define MAX_REPLY_ATTACK_COUNT 5 // 5 continuous error frames 
#define MAX_REPLY_ATTACK_TIME 30 // lock down for 30 seconds
uint8 Replay_Attack = 0;
uint8 Replay_Attack_Frame = 0;

inline void app_ini(void)
{
    uint16 temp_add,dest_add;

    // load back EEP DATA
    temp_add = APP_EEP_START_ADDRESS;
    dest_add = 0;
    while(temp_add <= FECT_DAT_VALID_TYP_ADDRESS)
    {
        lock_id[dest_add] = read_mem(temp_add,1);
        dest_add++;
        temp_add++;
    }
	    // test LED
    // Test Switch
    sw0_st_prev = ! (get_pin_5);
    OS_Init_Tmr(tmr_1ses);
#if(ENABLE_ENCRIPTION == TRUE) 
    OS_Init_Tmr(tmr_temp_key);
#endif
    // Test Communication
    // test Operatig system
    
   // com_send_dat(lock_id,10);
}
// set temp key to 0xFF after 5 second 
// use temp keun in both command

/*private function inclusion*/
void app_test_1ms(void)
{
   // toggle_pin_1;    

//-----Handle RX Tx Pkt------------------------------------------
	uint8 i=0, resp_len = 0,MacKeyTemp = 0;  
	uint8 pktreceived=0;
	
	CRITICAL_EN;
	if(RxDataReceived) // if byte is received
	{
	   RxDataReceived=RxDataReceived+1; // addressing
	   if(RxDataReceived>=3)  //this ensures responce in 3 OS tick = 3 ms TX is idel its pkt
	   {
			RxDataReceived=0;
			pktreceived=1;
	     // packet Received
	   }
	}
	CRITICAL_DIS;
	
    if(pktreceived) // buffer have some data
    {     
		MacKeyTemp = lock_id[EEP_APP_MAC_OK];  // commisiond
		if( MacKeyTemp == 0xFF ) // check if un commisiond
		{
			MacKeyTemp = lock_id[EEP_PWC];  // uncommisiond
		}

        // read command
        cmd_res_data[0] = 0xFF;
        while(com_get_rx_buf_lnt() && (i < MAX_CMD_FRAME_LEN)) // read all data
        {
            cmd_res_data[i]=com_rx_read_char(); 
            i++;
        }

        // process command
        if( cmd_res_data[0] == PING )  // default supported services
        {
            cmd_res_data[0] = PING_APP_RESP;
#if(ENABLE_ENCRIPTION == TRUE)       

            temp_key = cmd_res_data[1]; // random key sent by app
            // calucate the shared secret key
            cmd_res_data[1] = get_temp_key(); // random key to  app
            // calculate encription key           
            temp_key ^= cmd_res_data[1];
            temp_key ^=  lock_id[EEP_PWC]; // fist chr of UID ie CD5678 then 5 is to be used
            
            OS_Start_Tmr(tmr_temp_key); // temp key expier timer
            
#endif           
            led0_blink(2);
            resp_len = 5;
        }
        // below command are for master and user 
        else if((i ==  MAX_CMD_FRAME_LEN) && (Replay_Attack == 0)) // bothe commands are of maximum length
        {
            //cmd_1,enc(not_used)_1,mac(rand)_1,mac_4,dev_id_4,dev_typ_1,func_1,data_2,CRC_1 // 16 bytes
            
            //enc [NOT_USED]= upper byte challege of device rand // lowe challge of app rand
#if(ENABLE_ENCRIPTION == TRUE)
        // 1st byte is command , other decrypt// decrypt here
           enc_dec(&cmd_res_data[1], ENCRIPTION_LEN ,temp_key);
#endif
            if(CRC_Chk(&cmd_res_data[0],ENCRIPTION_LEN,0,MacKeyTemp))// validate integrity
            {

                if(cmd_res_data[0] == COMMISION) // commision by master
                {
                     resp_len = 16;
                   // com_send_dat('M',1);
                    if( lock_id[EEP_APP_MAC_OK] == 0xFF ) // not commitioned so comission it
                    {
                        // write MASTER MAC
                       uint8 temp_add_or_rand = FDR_DAT_MASTER_MAC_START_ADD_LSB;
                       uint8 dat_idx_offset = EEP_APP_MAC0;
                       uint8 cmd_idx_offset = 3;  //cmd_res_data[3] is for mac start
                       
                       while(temp_add_or_rand <= FDR_DAT_MASTER_MAC_END_ADD_LSB)
                       {
                           eep_write_char(temp_add_or_rand,cmd_res_data[cmd_idx_offset]); // write EEP write RAND
                           lock_id[dat_idx_offset] = cmd_res_data[cmd_idx_offset]; // also update MAC in  lock_id[X]
                           dat_idx_offset++;
                           cmd_idx_offset++;
                           temp_add_or_rand++; 
                       }
                       
                       // write RANDOM VALUE
                       lock_id[EEP_APP_MAC_OK] =  get_temp_key();
                       
                       eep_write_char(FDR_DAT_MASTER_FREEZ_ADD_LSB,lock_id[EEP_APP_MAC_OK]); // write EEP write RAND


                       // sand back data ....command and1 , enc1 and mac4is unchanged
                       cmd_res_data[2] =  lock_id[EEP_APP_MAC_OK];
                       cmd_res_data[7] =  lock_id[EEP_APP_LID0];  // Device ID
                       cmd_res_data[8] =  lock_id[EEP_APP_LID1];
                       cmd_res_data[9] =  lock_id[EEP_APP_LID2];
                       cmd_res_data[10] = lock_id[EEP_APP_LID3];        
                       cmd_res_data[11] = lock_id[EEP_APP_DEVTYP];  // Device type
                       cmd_res_data[12] = GET_VERSION;  // GET Version
                       cmd_res_data[13] = lock_id[EEP_BOOT_VERSION];  // BL VERSION
                       cmd_res_data[14] = lock_id[EEP_APP_VERSION];   // APP version
                   }
                   else if(!mem_compare(&cmd_res_data[3],&lock_id[EEP_APP_MAC0],9)) // validate mac ,device id, and type  , keep amc ok  = 0xFF and its secret 
                   {
                       // TODO: DONOT EXPOCE MASTER MAC AND RAND in CRAD, ENCRYPT devid with mac and send
                 
                       // execute functions authenticated
                        /*
                        if( cmd_res_data[0] == RSTCMD )
                        {
                            asm("RESET");
                        }
                        */
                       if(cmd_res_data[12] == GET_VERSION )
                        {
                            cmd_res_data[13] = lock_id[EEP_BOOT_VERSION];  // BL VERSION
                            cmd_res_data[14] = lock_id[EEP_APP_VERSION];   // APP version
                        }
  						else if(cmd_res_data[12] == GET_DIN )
                        {
                            cmd_res_data[13] = lock_id[EEP_DAY_IN_USE_LSB];  //  days in use
                            cmd_res_data[14] = lock_id[EEP_DAY_IN_USE_MSB];   // days in use
                        }
						else if(cmd_res_data[12] == GET_CUR_ON_TYM )
                        {					 
                             cmd_res_data[14] = *(((char*)&OnTym.days)+ cmd_res_data[13]);    
					    }                     
                        else if( cmd_res_data[12] == FLASH )
                        {
                            eep_write_char(FDR_DAT_APP_VALID_ADDRESS_LSB,0xFF);// make application invalid
                            asm("RESET");// reset the device
                        }
                        
                        /*
                        else if (cmd_res_data[12] ==  READ_RAM)  // its multi byte  command, ther may be possiblity that only command is received
                        {
                            *((uint16*)(&cmd_res_data[13])) = *(uint16*)(*((uint16*)&cmd_res_data[13u])); // read two bytes in ram
                        }
                        
                        else if( cmd_res_data[0] == WR_EEP )
                        {
                            eep_write_char(cmd_res_data[1],cmd_res_data[3u]);
                        }
                        */
                        else if( (cmd_res_data[12] == RD_EEP) )
                        {
                              cmd_res_data[13] = read_mem(*((uint16 *)&cmd_res_data[13]),1); // cmd,add,data
                              cmd_res_data[14] = 0; 
                        }
                        else if(cmd_res_data[12] == SET_GPIO_0)
                        {
                            if(cmd_res_data[13])
                            {
                                led0_blink(10);
                                set_pin_2;
                            }
                            else
                            {
                                led0_blink(2);
                                clear_pin_2;
                            }
                           
                        }
                        else if(cmd_res_data[12] == READ_GPIO_0)
                        {
                            cmd_res_data[13] = get_pin_2;  // contain DIO status
                            cmd_res_data[14] = 0;
                        }
                        else
                        {
                            resp_len = 0;// not supported command
                               //     cmd_res_data[0] = ERROR_FRAME;
                      // cmd_res_data[1] =  UNSUPPOERED_FUNCTION;
 
                        } 
                   }
                   else
                   {
                       resp_len = 0;// not supported / invalid data
                       // we can disable rx for some period brot force attak
                      // cmd_res_data[0] = ERROR_FRAME;
                       // cmd_res_data[1] =  AUTH_ERROR;
     
                   }
    
                }
                
                CRC_Chk(&cmd_res_data[0],ENCRIPTION_LEN,1,MacKeyTemp);// update CRC
                Replay_Attack_Frame=0;
#if(ENABLE_ENCRIPTION == TRUE)
                // encript the data
                // 1st byte is command , other decrypt// decrypt here
                enc_dec(&cmd_res_data[1], ENCRIPTION_LEN ,temp_key);
#endif
            }
            else
            {
			   Replay_Attack_Frame++;
			   if(Replay_Attack_Frame> MAX_REPLY_ATTACK_COUNT)
			   {
			        Replay_Attack_Frame=0;
					Replay_Attack=MAX_REPLY_ATTACK_TIME;
			   }
              //  cmd_res_data[0] = ERROR_FRAME;
              //  cmd_res_data[1] = CRC_ERROR;
            }
            
        }
        else
        {
           //  cmd_res_data[3] = cmd_res_data[0];
           // cmd_res_data[0] = ERROR_FRAME;
           // cmd_res_data[1] = INVALID_CMD_LENFTH;
            // cmd_res_data[2] = i;
          
        }
        //-------------------------SEND RESPONCE-------------------------------
        if (resp_len != 0) // check for multi frame responce
        {
            // calculate and update checksum and send responce at byte 6 if needed
            com_send_dat(cmd_res_data,resp_len);
        }
        else
        {
		   
           //  resp_len = 8;
           //  com_send_dat(cmd_res_data,resp_len);
        }
    }   
}
void app_test_10ms(void)
{
   
}

void app_test_100ms(void)
{
   //------------switch event---------------
    volatile uint8 sw0_st;
    sw0_st = get_pin_5;  
    if(sw0_st_prev!=sw0_st)
    {
       // event_push(FDR);
        if(sw0_st)
        {    
            event_read = SW0_RELEASE;     
        }
        else
        {
            event_read = SW0_PRESS;         
        }        
        sw0_st_prev=sw0_st;
    }
    
    // LED 
    led0_task();
}
void app_test_1000ms(void)
{
    uint8 temp_time;
    
  //  toggle_pin_2;  
    OnTym.sec++;// increment second
    if(OnTym.sec==60) // its one second
    {
        //do something every min
        OnTym.sec=0;
        OnTym.min++;
        if(OnTym.min==60)
        {
            //do something every hours
            Do_Every_Hr();
            //Do_Every_Hour();
            OnTym.min=0;
            OnTym.hrs++;
            if(OnTym.hrs==24) // its one hrs cycle
            {
                //do something every day
                Do_Every_Day();
                OnTym.hrs=0;
                OnTym.days++;
            }
        }
    }
    
    OS_Run_Tmr(tmr_1ses); 
#if(ENABLE_ENCRIPTION == TRUE)
    OS_Run_Tmr(tmr_temp_key);
    temp_time = OS_Read_Tmr(tmr_temp_key);
    if(temp_time == FIFTEEN_SEC)
    {
         OS_Stop_Tmr(tmr_temp_key); // key expier
         temp_key = ~temp_key; // key expier
    }
#endif
    if(Replay_Attack)
	{
		Replay_Attack--;
	}
    temp_time = OS_Read_Tmr(tmr_1ses);
     
    if((temp_time >= TEN_SEC) && (temp_time <= FIFTEEN_SEC))
    {
        led0_blink(4); 
    }
	else if((temp_time == TWENTY_SEC)) //Set UN
	{
	    //Set the NAME to BLE chip
	    com_send_dat("AT+NAMECD",9);
	    com_send_dat(&lock_id[EEP_APP_LID0],4);
		led0_blink(10); 
	}

}
void app_test_BG(void)
{
    uint8 temp_time;
    if(event_read == SW0_PRESS)
    {
        OS_Start_Tmr(tmr_1ses);
        led0_blink(2);
    }
    else if(event_read == SW0_RELEASE)
    {
         temp_time = OS_Read_Tmr(tmr_1ses); // alwys read timer before stopping
            OS_Stop_Tmr(tmr_1ses);
            
            if((temp_time <= FIFTEEN_SEC))
            {
                if(temp_time >= TEN_SEC)
                {
                  //-------------FDR-------------- CODE
                  led0_blink(40); // odd no is to compenste for FDR   
                  eep_write_char(FDR_DAT_MASTER_FREEZ_ADD_LSB,0xFF); // uncommision the device
                  lock_id[EEP_APP_MAC_OK]=0xFF; // device not commision
				  clear_pin_2;// REQ:FDR:turn off the output on FDR
                }
            }
            else
            {
				//Set PW to BLE chip
				com_send_dat("AT+PIN",6);
				com_send_dat(&lock_id[EEP_PW0],4);
				led0_blink(10);
			}		
    }
    else
    {
        // do not do any thing
    }
    event_read= 0xFF;
	
}
uint8 mem_compare(uint8 *dest,uint8 *sour, uint8 len)
{
    uint8 status = 0;
    while(len)
    {  
        if(*dest != *sour)
        {
             status=1;
             len = 1;  //come out    
        }

        len--;
        sour++;
        dest++; 
    }
    return status;
}

uint8 CRC_Chk(uint8 *dbuf,uint8 len,uint8 flg,uint8 MacKey) // CRC in last bte
{
    uint8  sum = 0,st=0;
    while(len)
    {
        sum +=*dbuf;
        dbuf++;
        len--;
    }
    if(flg)
    {
       *dbuf = sum^MacKey;
    }
    if((sum^MacKey) == *dbuf)
    {
        st = 1;
    }
    return st;
}
#if(ENABLE_ENCRIPTION == TRUE)
void enc_dec(uint8 *data, uint8 len , uint8 temp_key_0)
{
    while(len)
    {
        len--;
        data[len] ^= temp_key_0;
        temp_key_0 ^= len;
    }
}
#endif
inline void Do_Every_Day(void)
{

}
inline void Do_Every_Hr(void)
{
    if(lock_id[EEP_DAY_IN_USE_MSB] == EEP_DEF_VAL) // on time is valid
    {
		lock_id[EEP_DAY_IN_USE_MSB] =0;
		lock_id[EEP_DAY_IN_USE_LSB] =0;
	}
	(*(uint16 *)(&lock_id[EEP_DAY_IN_USE_LSB]))++;
	eep_write_char(DAY_IN_USE_ADD_LOW_LSB,lock_id[EEP_DAY_IN_USE_LSB]);
	eep_write_char(DAY_IN_USE_ADD_HIGH_LSB,lock_id[EEP_DAY_IN_USE_MSB]);
}
/********************************************
REQ:FDR:on FDR reset the DIO pin
********************************************/