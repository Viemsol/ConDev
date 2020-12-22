#ifndef _INCL_OS
#define _INCL_OS

#define printfD(str) com_send_data(str,sizeof(str));
void printfB(uint8 *data , uint8 len);
enum sysEvents
{
    MAXEVENT    = 8,         // MAX event can be stored in ring buffer
    MAXMSTIMER  = 4,         // MAX ten milli sec resolution timers 
    MAXSTIMER   = 4,         // MAX one Sec resolution timers
};                                                      

struct simpleScheduler
{
    void (*fptr[MAXEVENT])(void*);            // holds function to be executed for events
    unsigned char *data[MAXEVENT];            // holds data pointer corresponding to event
    unsigned char msecTimers[MAXMSTIMER];     // 8 bit timer software (10ms sec resolution), if non zero value will decrement it
    unsigned char secTimers[MAXSTIMER];       // 8 bit timer (1 sec resolution) ,if non zero value will decrement it  
    unsigned char eventH;                     // hade of event
    unsigned char eventT;                     // tale of the event
};
extern volatile struct simpleScheduler strScheduler;
extern uint8_t time_sys;
// make sure valid index is provided
#define setTmr(idx,val) strScheduler.msecTimers[idx] = val
#define getTmr(idx) strScheduler.msecTimers[idx]

//Scheduler functions
void putNextEvent(void* fPtr, void* data);   // push event to execute queue 
unsigned char getNextEvent(void);            // get next event index to execute
unsigned char getEventDepth(void);           // get event dept 
bool canSleep(void);                         // check if sytem can sleep
//sw timer callback function
bool allTimerDeactive(void);
void handleTimer(void* data);                // timer callback every 10ms (push this event in timer isr)
void swTimerOverflow(unsigned char tmrIdx); // callback on any timer overflow

//ms timer 
#define RX_BREAK   0  // 10ms resolution
#define LED_BLINK  1

#define FDR_SW     4  // one sec resolution

#endif
