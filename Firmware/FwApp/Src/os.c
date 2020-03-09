#include "os.h"
// note: make below task assignment const if you want to save some ram, adding and removing task function will not support if const
uint8 j_1;
struct EVENT_STR str_event;
struct STR_OS os =
{
	{app_test_1ms,app_test_10ms,app_test_100ms,app_test_1000ms}, // pro_ptr
    {0},							// sys timer in ms
    {0},                            // sys timer in seconds , 18 hrs max
	{0,0,0,0} // time keeper
};
void main(void)
{
    //OSCILLATOR
    Sys_Ini();  
    #if(DEBUG_MSG == REQUIRE)
    com_send_string("Init...\n");
    #endif
    
    Run_Os();
    //while(1);
    
 }
void Run_Os(void)
{
	while(1)
	{     
        if(os.time_keeper[0]) // karnal tick happend
        {
            
            for(j_1=0u;j_1<(BAG_PRO_IDX);j_1++) // scall all process , exclude baground process
            {
                if(os.time_keeper[j_1]>=10u)
                {
                  os.pro_ptr[j_1]();// execute currunt task 
                    
                  os.time_keeper[j_1+1u]++;    // increment next task time keeper   
                  os.time_keeper[j_1]=0u;      // make current task time keeper zero
                }
            }
            os.os_time_ms++; // overflow after 50 days of operation
        }
        app_test_BG();
        // go to sleep , only periperal are active CPU in sleep, wake up by any Interrupt
        while(com_get_tx_buf_lnt());
        SLEEP();
        NOP();
        NOP();   
    }
}


uint32 os_get_sys_tim(void)
{
	uint32 temp_tim;
    
	CRITICAL_EN;
	temp_tim = os.os_time_ms;
	CRITICAL_DIS;
    
	return (temp_tim);
}
/*
void Run_Container(Fun_Ptr *F_Ptr)
{
    while(*F_Ptr)
    {
        (*F_Ptr)();
        F_Ptr++;
    }
}
*/ 

/*
uint8 os_remove_task(uint8 pocess_id,Fun_Ptr F_Ptr) // remove perticular task from procees
{
    // scan through task
    uint8 i=0, reap=0;
    while((*(os.pro_ptr[pocess_id] + i))!= 0)
    {
        if((*(os.pro_ptr[pocess_id] + i)) == F_Ptr)
        {
            while((*(os.pro_ptr[pocess_id] + i))!= 0)
            {
              (*(os.pro_ptr[pocess_id] + i))=(*(os.pro_ptr[pocess_id] + (i+1)));  // shift the array
              reap=1;
              i++;
            }
            break;
        }
        i++;
    }
    return reap;
}

void os_insert_task(uint8 pocess_id, Fun_Ptr F_Ptr) // // add perticular task at end of process
{
    // scan through task
    uint8 i=0;
    while((*(os.pro_ptr[pocess_id] + i))!=0)
    {
        i++;
    }
    (*(os.pro_ptr[pocess_id] + i))=F_Ptr; // make sure task array have enough space to incert the new task
    (*(os.pro_ptr[pocess_id] + (i+1)))=0;

} 
*/
void event_push(uint8 Event_ID)
{
    if(!str_event.overflow)  // check if there is space for event in queue
    {
        str_event.queue[str_event.head] = Event_ID;
        if(str_event.head<MAX_EVENT_IDX){str_event.head++;}else {str_event.head = 0;} 
        
        if(str_event.head == str_event.tale)
        {
            str_event.overflow=1;
        }
    }
    else
    {
        // no more event can be queued
    }
}
uint8 event_read(void)
{
    // returns 0 if no event in queue, wlse retuen event ID
    uint8 temp_event_id=0;
    if((str_event.head != str_event.tale)||str_event.overflow) // something is present in queue
    {
        temp_event_id = str_event.queue[str_event.tale];
        if(str_event.tale<MAX_EVENT_IDX){str_event.tale ++;}else{str_event.tale = 0;} 
        str_event.overflow=0;
    }
    return (temp_event_id);
}

uint8 get_temp_key(void)
{   
    uint8 temp_key;
    temp_key = os_get_sys_tim();
    if(temp_key == 0xFF)
    {
        temp_key = 0xAA;
    }
    return temp_key;
}