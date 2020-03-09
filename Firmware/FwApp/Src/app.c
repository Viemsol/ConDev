#include "common.h"

uint8 tmr_1ses,tmr_temp_key;

uint8 cmd_res_data[MAX_CMD_FRAME_LEN];
uint8 temp_key = 0xFF; // temp key session key

uint8 sw0_st_prev;
uint8 lock_id[16]; // holds eep read

inline void app_ini(void)
{
    uint16 temp_add,dest_add;
    // test LED
    // Test Switch
    sw0_st_prev = ! (get_pin_5);
    OS_Init_Tmr(tmr_1ses);
#if(ENABLE_ENCRIPTION == TRUE) 
    OS_Init_Tmr(tmr_temp_key);
#endif
    // Test Communication
    // test Operatig system
    
    // load back EEP DATA
    temp_add = FDR_DAT_APP_VALID_ADDRESS;
    dest_add = 0;
    while(temp_add <= FECT_DAT_VALID_TYP_ADDRESS)
    {
        lock_id[dest_add] = read_mem(temp_add,1);
        dest_add++;
        temp_add++;
    }
   // com_send_dat(lock_id,10);
}
// set temp key to 0xFF after 5 second 
// use temp keun in both command

/*private function inclusion*/
void app_test_1ms(void)
{
   // toggle_pin_1;     
}
void app_test_10ms(void)
{
   
}

void app_test_100ms(void)
{
    uint8 i=0, resp_len = 0,MacKeyTemp = 0;
    volatile uint8 sw0_st;
    if( lock_id[3] == 0xFF )
    {
        MacKeyTemp = lock_id[8];  // uncommisiond
    }
    else
    {
        MacKeyTemp = lock_id[3];  // commisiond
    }
    
    if(com_get_rx_buf_lnt()) // buffer have some data
    {     
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
            temp_key ^=  lock_id[8]; // fist chr of UID ie CD5678 then 5 is to be used
            
            OS_Start_Tmr(tmr_temp_key); // temp key expier timer
            
#endif           
            led0_blink(2);
            resp_len = 5;
        }
        // below command are for master and user 
        else if((i ==  MAX_CMD_FRAME_LEN)) // bothe commands are of maximum length
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
                    if( lock_id[3] == 0xFF ) // not commitioned so comission it
                    {
                        // write MASTER MAC
                       uint8 temp_add_or_rand = FDR_DAT_MASTER_MAC_START_ADD_LSB;
                       uint8 dat_idx_offset = 4;
                       uint8 cmd_idx_offset = 3;  //cmd_res_data[3] is for mac start
                       
                       while(temp_add_or_rand <= FDR_DAT_MASTER_MAC_END_ADD_LSB)
                       {
                           eep_write_char(temp_add_or_rand,cmd_res_data[cmd_idx_offset]); // write EEP write RAND
                           lock_id[dat_idx_offset] = cmd_res_data[cmd_idx_offset]; // also update MAC in  lock_id[X]
                           dat_idx_offset++;
                           cmd_idx_offset++;
                           temp_add_or_rand++; 
                       }
                       
                       // write RAND
                       lock_id[3] =  get_temp_key();
                      // lock_id[3] = 0xAA;  // TODO: remove this this is for test purpose
                       
                       eep_write_char(FDR_DAT_MASTER_FREEZ_ADD_LSB,lock_id[3]); // write EEP write RAND


                       // sand back data ....command and1 , enc1 and mac4is unchanged
                       cmd_res_data[2] =  lock_id[3];
                       cmd_res_data[7] =  lock_id[8];  // Device ID
                       cmd_res_data[8] =  lock_id[9];
                       cmd_res_data[9] =  lock_id[10];
                       cmd_res_data[10] = lock_id[11];        
                       cmd_res_data[11] = lock_id[12];  // Device type
                       cmd_res_data[12] = GET_VERSION;  // GET Version
                       cmd_res_data[13] = lock_id[14];  // BL VERSION
                       cmd_res_data[14] = lock_id[2];   // APP version
                   }
                   else if(!mem_compare(&cmd_res_data[2],&lock_id[3],10)) // validate random no and mac
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
                            cmd_res_data[13] = lock_id[14];  // BL VERSION
                            cmd_res_data[14] = lock_id[2];   // APP version
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
                
#if(ENABLE_ENCRIPTION == TRUE)
                // encript the data
                // 1st byte is command , other decrypt// decrypt here
                enc_dec(&cmd_res_data[1], ENCRIPTION_LEN ,temp_key);
#endif
            }
            else
            {
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
           // resp_len = 6;
            // com_send_dat(cmd_res_data,resp_len);
        }
    }
    
   //------------switch event---------------
  
    sw0_st = get_pin_5;  
    if(sw0_st_prev!=sw0_st)
    {
       // event_push(FDR);
        if(sw0_st)
        {    
            event_push(SW0_RELEASE);     
        }
        else
        {
            event_push(SW0_PRESS);         
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

    os.os_time_sec++;
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
    temp_time = OS_Read_Tmr(tmr_1ses);
     
    if((temp_time >= TEN_SEC) && (temp_time <= FIFTEEN_SEC))
    {
        led0_blink(4); 
    }

}
void app_test_BG(void)
{
    uint8 temp_time;
     
    switch(event_read())
    {
        case SW0_PRESS :
            OS_Start_Tmr(tmr_1ses);
            led0_blink(2);  
        break;
        case SW0_RELEASE :
            temp_time = OS_Read_Tmr(tmr_1ses); // alwys read timer before stopping
            OS_Stop_Tmr(tmr_1ses);
            
            if((temp_time <= FIFTEEN_SEC))
            {
                if(temp_time >= TEN_SEC)
                {
                  event_push(FDR);
                }
            }   
            
        break;
        
        case FDR :
        eep_write_char(FDR_DAT_MASTER_FREEZ_ADD_LSB,0xFF); // uncommision the device
        lock_id[3]=0xFF; // device not commision
        //TODO: below code can be remove after cosistancy check of erase , by verifieng LED
        led0_blink(40); // odd no is to compenste for FDR   
        // else FDR fail
        break;   
    }
     
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
