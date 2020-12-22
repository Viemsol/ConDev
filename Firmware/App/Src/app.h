#ifndef _INCL_APP
#define _INCL_APP
#define ENCRIPTION 

#define APPVERSION 1
enum ddpFunc
{
    MEMCPY,
    MEMCMP,
    XOR,
    ADD,
    RAND,
    ENC,
    DEC,
    CRC
};

uint8_t ddpSimp(uint8_t *dest,uint8_t *src, uint8 len,uint8 cmd);
void fdr(void);
void fdrCancel(void);
void Com(void);

void LedUpdate(void);
void led0_blink(uint8 cnt,uint8 Period);

extern uint8 Led_st_cnt,BlinkPriod;

#endif
