// Simple Sigle Page scheduler
// By Nandkumar Ganesh Dhavalikar
// Events can be pushed from ISR or from application
// Timer Events
// Expandable timers and Event Buffers
// sleep condition check
#include "include.h"

volatile struct simpleScheduler strScheduler;

void main(void)
{
    static uint8_t eventVal;
    Sys_Ini();
    printfD(" Boot..CD V" str(APPVERSION) "\n\r"); // start with space alwys

    while(1)
    {
        eventVal = getNextEvent();
        if(eventVal<MAXEVENT)
        {
            strScheduler.fptr[eventVal](strScheduler.fptr[eventVal]); // execute event
        }
        else // no events are active 
        {
            if(canSleep())  // check other condition to sleep
            {
                // all stpes before going to sleep
                Stop_Timer_0;// disable 10 ms timer as its of no use

                SLEEP();
                NOP();
                NOP();
                
                // all stesps on waking up

               Start_Timer_0;// enable 10 ms timer again

            }
        }
    }
}

bool canSleep(void)
{
    return(allTimerDeactive()); // incase of tx or rx frame one timer can be kept active
}

static bool stOverflow = false; // event que buffer overflow
/*This Function Must be called from ISR*/
void putNextEvent(void * fptr, void * data)
{
    if(!stOverflow)
    {
        strScheduler.fptr[strScheduler.eventH] = fptr;
        strScheduler.data[strScheduler.eventH] = data;
        strScheduler.eventH++;
        if(strScheduler.eventH >= MAXEVENT)
        {
           strScheduler.eventH = 0; 
        }
    }
    stOverflow = false;  // eventually when event get executed tall will increment and event will be added then
    if(strScheduler.eventT == strScheduler.eventH)
    {
        stOverflow = true;
    }
}

unsigned char getNextEvent(void)
{
    unsigned char tempEvent = 0xFF;
    DISABLE_GLOBAL_ISR;
    if(strScheduler.eventH != strScheduler.eventT)
    {
        tempEvent = strScheduler.eventT;
        strScheduler.eventT++;
        if(strScheduler.eventT >= MAXEVENT)
        {
           strScheduler.eventT = 0; 
        }
    }
    ENABLE_GLOBAL_ISR;
    if(stOverflow)
    {
        printfD(" Overflow\n\t");
    }
    return tempEvent;
}

bool allTimerDeactive(void)
{
    uint8_t i = 0;
    while(i < (MAXSTIMER + MAXMSTIMER))
    {
      if(strScheduler.msecTimers[i])
      {
          i = 0;
          break;
      }
      i++;
    }
    return i; 
}

void handleTimer(void* data) // to be called as a callback in 10 ms isr
{
    static unsigned char tmrH = 0;
    unsigned char i = 0;

    while(i < (MAXMSTIMER + MAXSTIMER))
    {
        if(strScheduler.msecTimers[i])
        {
            strScheduler.msecTimers[i]--;
            if(!strScheduler.msecTimers[i]) // overflow :if timer is zero after decrementing
            {
                swTimerOverflow(i);// timer overflow repot it by timer index
            }
        }
        i++;
        if((i == MAXMSTIMER)) // check second timer
        {
            if(tmrH >= 100) // its 1 second
            {
                tmrH = 0;
            }
            else
            {
                 i = (MAXMSTIMER + MAXSTIMER); // come out of look no timer active
            }
        }
    }
    tmrH++;
}

void swTimerOverflow(unsigned char tmrIdx)  // tmrIdx is pointer to timer index which coused overflow
{
    // add case for handling respective timer overflows
    if(tmrIdx == FDR_SW) // second timer index 0 
    {
        fdr();
        setTmr(FDR_SW,4); // start timer
    }
    
    if(tmrIdx == LED_BLINK)
    {
        LedUpdate();
    }
    
    if(tmrIdx == RX_BREAK)
    {
    	Com();
    }
}
