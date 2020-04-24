package com.example.cdmaster;

import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import static com.example.cdmaster.GLOBAL_CONSTANTS.EVENT_TIMER;
import static com.example.cdmaster.GLOBAL_CONSTANTS.MAX_TIMERS;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_INACTIVE;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_ONESHOT;
import static com.example.cdmaster.GLOBAL_CONSTANTS.TIMER_RX_TIMEOUT;
import static com.example.cdmaster.LoopThread.LoopHandler;
import static com.example.cdmaster.MainActivity._Handler_MainHandler;

class TimerScheduler extends Thread {
    final static  String TAG = "TAG_Timer";
    private static int[] timerTyp = new int[MAX_TIMERS];// 00 is one shot RX , 01 is periodic else stop ,02 Pering Timeout
    private static Object[] timerObj = new Object[MAX_TIMERS];
    private static int[] timer = new int[MAX_TIMERS];  // hold timer
    private static int[] timerTemp = new int[MAX_TIMERS]; // hold reload value
    int i;  // index  // 00is rx timeout  , 0x01 is ping blink

    TimerScheduler() {

        for(i=0;i<MAX_TIMERS;i++) {
            this.timerTyp[i] = 0xFF;
        }
    }
    @Override
    public void run() {
        Log.d(TAG,"Running"); // 100 ms resolution timer
        //noinspection InfiniteLoopStatement : error suppression comment
        while (true) {
            SystemClock.sleep(100);
            for (i = 0; i < MAX_TIMERS; i++) {
                if (timerTyp[i] != TIMER_INACTIVE) // timer service active
                {
                    // rais event
                    if (timer[i] > 0) {
                        timer[i]--;
                    } else {
                        if (timerTyp[i] == TIMER_ONESHOT) // one shot
                        {
                            timerTyp[i] = TIMER_INACTIVE;
                            Log.d(TAG,"Stoping Timer Index:"+ i);
                        } else {
                            timer[i] = timerTemp[i]; // periodic
                        }
                        Message temp_msg_loop = new Message();
                        Message temp_msg_main = new Message();

                        temp_msg_loop.what = EVENT_TIMER;
                        temp_msg_loop.obj = timerObj[i];
                        temp_msg_loop.arg1 = 900+i;

                        temp_msg_main.what =  temp_msg_loop.what;
                        temp_msg_main.obj = temp_msg_loop.obj;
                        temp_msg_main.arg1 = temp_msg_loop.arg1;

                        _Handler_MainHandler.sendMessage(temp_msg_main);// place in looper queue
                        LoopHandler.sendMessage(temp_msg_loop); // send to looper
                    }

                }
            }
        }

    }

    //TODO : Make this function variables thread safe
    public void Start_Timer(int Index , int timerVal_100ms , int type , Object obj)
    {
        Index = Index % TIMER_RX_TIMEOUT;
        if(Index < MAX_TIMERS)
        {
            timer[Index] = timerVal_100ms;
            timerTemp[Index] = timerVal_100ms;
            timerTyp[Index] = type;
            timerObj[Index] = obj;
            Log.d(TAG,"Starting Timer Index:"+ Index);
        }

    }
    public void Stop_Time(int Index)
    {
        Index = Index % TIMER_RX_TIMEOUT;
        timerTyp[Index] = TIMER_INACTIVE;
        Log.d(TAG,"Stoping Timer Index:"+ Index);
    }
}