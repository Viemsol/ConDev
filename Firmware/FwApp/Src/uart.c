#include"common.h"

struct STR_UART_MGR com;

void com_send_dat(char *ptr, uint8 len)
{
    CRITICAL_EN;
    while(len)
    {
        com.tx_buf[com.tx_head]=*ptr; // its user respocibility to check head tale postion befor colling function
        ptr++;
        len--;
        INC_TX_HEAD;
        if(TX_HEAD_TALE_EQUAL)
        {
          com.tx_ovrfl = 1;
          //TX_EN;
		  TXISR_EN;
          CRITICAL_DIS;
          while(com.tx_ovrfl); // let overflow condition go off
          CRITICAL_EN;
        }

    }
    //TX_EN;
    TXISR_EN;// enable transmit isr
	CRITICAL_DIS;
}

void com_send_string(char *ptr)
{
    uint8 strlen=0;
    uint8 *temp_ptr=ptr;
    while(*temp_ptr)
    {
        temp_ptr++;
        strlen++;
    }
    strlen++;
    com_send_dat(ptr, strlen);	
}

uint8 com_get_tx_buf_lnt(void)
{
	uint8 temp_dif;
	CRITICAL_EN;
	if(com.tx_head > com.tx_tale)
    {
        temp_dif = ((COM_TX_BUF - com.tx_head) + com.tx_tale);
    }
    else
    {
        temp_dif = (com.tx_tale - com.tx_head);
    }
	CRITICAL_DIS;
	return(temp_dif);
}
uint8 com_get_rx_buf_lnt(void) // gives the content length of rx buffer
{
	uint8 temp_dif;
	CRITICAL_EN;
	if(com.rx_head >= com.rx_tale)
    {
        temp_dif = (com.rx_head - com.rx_tale);
    }
    else
    {
        temp_dif = ((COM_TX_BUF-com.rx_tale) + com.rx_head);
    }
	CRITICAL_DIS;
	return(temp_dif);
}

void com_send_Dat(char val)
{
	uint8 temp_st;
  
    CRITICAL_EN;
   
    com.tx_buf[com.tx_head] = val;
    INC_TX_HEAD;

    if(TX_HEAD_TALE_EQUAL)
    {
       com.tx_ovrfl = 1u;
       //temp_st=1;
    }
    
   // TX_EN;
    TXISR_EN;// enable transmit isr
	CRITICAL_DIS;
    while(com.tx_ovrfl); // let overflow go 
}
/*
uint8 com_tx_hex(uint8 *ptr,uint8 lnt)
{
	const uint8 hex2chr[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    uint8 temp_res=1,tmp_value,nib=0u;   
    CRITICAL_EN;
	
    while(lnt)
    {
        if(nib)
        {
            nib=0u;
            tmp_value=(((*ptr)>>(4u)) & 0x0Fu);
            ptr++;
            lnt--;
        }
        else
        {	
            tmp_value=(*ptr)& 0x0Fu;
            nib=1u;
        }
        com.tx_buf[com.tx_head] = hex2chr[tmp_value]; // its user responsibility to check head tale postion befor colling function
        INC_TX_HEAD;
        if(TX_HEAD_TALE_EQUAL)
        {
         // TX_EN;
		  TXISR_EN;
          CRITICAL_DIS;
          while(com.tx_ovrfl); // let overflow condition go off
          CRITICAL_EN;
         
        }

    }
   // TX_EN;
    TXISR_EN;// enable transmit isr
    
	CRITICAL_DIS;
    return temp_res;
}
*/
uint8 com_rx_read_char(void)
{
    uint8 temp_val=0;
	
	CRITICAL_EN;
	temp_val=com.rx_buf[com.rx_tale];
	INC_RX_TALE;
	CRITICAL_DIS;
	
	return temp_val;
}
inline void com_ini(void) // reviewed
{

// set pin dir sen=t in systam config
// set baud rate
#if(BOOTLOADABLE == FALSE)
    TX1STA=0x04;
    BRG16 = 0;
    SPBRG=34u;// 34: 57.6k , 103:19.3k , 207:9600
    // enable transmitter
    TX_EN;
   
#else
  // evey thing set in bootloader
#endif

// enable receive isr
    RXISR_EN; //TODO check if buffer is empty
// enable seial port
    #if(BOOTLOADABLE == FALSE)
    RC1STA=0x90; // enable seial port  and receiver  
    #endif
}
// device driver
inline void byte_received(void)
{
    uint8 temp_data;
    temp_data = RX_BYTE;
    if(!((com.rx_ovrfl)&& (RX_HEAD_TALE_EQUAL)))
	{
		com.rx_ovrfl = 0;
		com.rx_buf[com.rx_head] = temp_data;
	    INC_RX_HEAD;
		if(RX_HEAD_TALE_EQUAL)
	    {
			  com.rx_ovrfl = 1;
	    }
	}
    //TODO: handle overrun condition
}

inline void byte_transmit(void)
{
	if((com.tx_tale != com.tx_head) || com.tx_ovrfl ) //  trsmit only when ther is a gap or over flow
	{
		TX_BYTE = com.tx_buf[com.tx_tale];
		INC_TX_TALE;
        if(TX_HEAD_TALE_EQUAL)
        {
            TXISR_DIS;   
        }
        if(com.tx_ovrfl) //  trasmission was due to overflow
        {
            com.tx_ovrfl=0;
        }
	}
	else // should not come here
	{
        TXISR_DIS;   
	}
     
}

uint8 com_if_received(char data)
{
    uint8 tale,lnt,st_ret=0;
    tale=com.rx_tale;
    lnt=com_get_rx_buf_lnt();
    while(lnt)
    {
        if(com.rx_buf[tale]==data)
        {
            st_ret=1;
            break;
        }
        lnt--;
        if(tale>=COM_RX_BUF_IDX){ tale=0u;}else{tale++;}
    }
    return(st_ret);
}
