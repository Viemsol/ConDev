#ifndef _INCL_OS
#define _INCL_OS
#include "common.h"


#define MAX_PRO 5u
#define BAG_PRO_IDX (MAX_PRO-1u)
#define SET_BG_PRO_BIT (1u<<BAG_PRO_IDX)

#define MAX_EVENT_IDX (MAX_EVENT - 1)

typedef void (*Fun_Ptr)(void);
struct STR_OS
{
	Fun_Ptr pro_ptr [BAG_PRO_IDX];
    uint32 os_time_ms; // time from system on in ms
    uint16 os_time_sec;
	uint8 time_keeper [BAG_PRO_IDX];
};
struct EVENT_STR
{
    uint8 head;
    uint8 tale;
    uint8 queue[MAX_EVENT];
    uint8 overflow;
};

extern struct STR_OS os;
#define karnel_tick (os.time_keeper[0] = 10)
#define OS_Init_Tmr(TMR_ID) (TMR_ID = 0xFF)
#define OS_Start_Tmr(TMR_ID) (TMR_ID = 0)
#define OS_Stop_Tmr(TMR_ID)  (TMR_ID = 0xFF) // ff is timer stoopd do not operate
#define OS_Run_Tmr(TMR_ID)   if(TMR_ID < 0xFE){TMR_ID++; } // running timer can hva max valid vaue xfe
#define OS_Read_Tmr(TMR_ID)  ((TMR_ID < 0xFF)?TMR_ID:0) // alwys read timer before stopping

void Run_Os(void);
uint32 os_get_sys_tim(void);
void Run_Container(Fun_Ptr *F_Ptr);
void os_insert_task(uint8 pocess_id, Fun_Ptr F_Ptr);
uint8 os_remove_task(uint8 pocess_id,Fun_Ptr F_Ptr);

void event_push(uint8 Event_ID);
uint8 event_read(void);
uint8 get_temp_key(void);

#endif