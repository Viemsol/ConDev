#ifndef _INCL_UART
#define _INCL_UART
/*
description:
pers1:
para2:
return: success:1 /fail:0
eg:
*/
#define COM_TX_BUF 24u
#define COM_TX_BUF_IDX (COM_TX_BUF - 1u)
#define COM_RX_BUF 24u
#define COM_RX_BUF_IDX (COM_RX_BUF - 1u)

struct STR_UART_MGR
{
  char tx_buf[COM_TX_BUF]; // ring bufer which will store things to be trasmitted
  uint8 tx_head;
  uint8 tx_tale;
  uint8 tx_ovrfl;
  char rx_buf[COM_RX_BUF]; // ring bufer which will store things to be trasmitted
  uint8 rx_head;
  uint8 rx_tale;
  uint8 rx_ovrfl;
};
extern struct STR_UART_MGR com;

#define INC_TX_HEAD if(com.tx_head>=COM_TX_BUF_IDX){com.tx_head = 0;}else{com.tx_head++;}
#define INC_TX_TALE if(com.tx_tale>=COM_TX_BUF_IDX){ com.tx_tale = 0u;}else{com.tx_tale++;}
#define INC_RX_HEAD if(com.rx_head>=COM_RX_BUF_IDX){com.rx_head =  0u;}else{com.rx_head++;}
#define INC_RX_TALE if(com.rx_tale>=COM_RX_BUF_IDX){com.rx_tale =  0u;}else{com.rx_tale++;}
#define TX_HEAD_TALE_EQUAL (com.tx_tale == com.tx_head)
#define RX_HEAD_TALE_EQUAL (com.rx_tale == com.rx_head)
#define RX_FLUSH  com.rx_tale = com.rx_head

//void com_send_Dat(char val);  // send charecter
//#define SEND_EOL com_send_Dat(0x00)
//#define SEND_NUL com_send_Dat(0x0A)


//uint8 com_get_tx_buf_lnt(void); // will return nuber of empty bytes
uint8 com_get_rx_buf_lnt(void); // will return nuber of received bytes

/*
description:
send out the string on uart
on detection of null it terminate
pers1:
para2:
return: sucess:1 else fail:0
*/

//void com_send_string(char *ptr);  // send sting


/*
description:
pers1: pointer to aray stucture or anything
para2: lnt of bytes to be convrted into ascii and sent.
return: success:1/ fail:0
eg com_make_string_eol_hex(&(0x1F124518),4); //  '1F124518' will be sent
*/
void com_send_data(char *ptr, uint8 len);

void com_rx_read_data(uint8* dataPtr, uint8 len);
//uint8 com_tx_hex(uint8 *ptr,uint8 lnt); // send data
inline void com_ini(void);
//uint8 com_if_received(char data);
inline void byte_received(void);
inline void byte_transmit(void);

//uint8 com_rx_read_char(void);


#endif
