#ifndef _INCL_APP
#define _INCL_APP
	extern void app_test_1ms(void);
	extern void app_test_10ms(void);
	extern void app_test_100ms(void);
	extern void app_test_1000ms(void);
    extern void app_test_BG(void);
    extern inline void app_ini(void);
     extern inline void Do_Every_Day(void);
	 extern inline void Do_Every_Hr(void);
    uint8 mem_compare(uint8 *dest,uint8 *sour, uint8 len);
 
    uint8 CRC_Chk(uint8 *dbuf,uint8 len,uint8 flg,uint8 MacKey); // CRC in last bte
    void enc_dec(uint8 *data, uint8 len , uint8 temp_key_0);
    
     //  service supported in bootloader , so to update flash of eep jump to bootloader update make application valid, reset done
    #define WR_FLH 1 
    #define WR_EEP (WR_FLH+1)
    #define RD_FLH 3      
    #define RD_EEP (RD_FLH +1)

     //  service supported in application code
    #define PING 5
    #define RSTCMD 6
    #define FLASH 7  // command supported by application , to jump bootloader , amkes application invalid
    #define PING_APP_RESP 8 // on PING command Application send 8 as responce while BL send 5 as responce

     //  service supported in bootloader
    #define READ_RAM 0x0A
    #define CRED_AUTH 0x0B   //user attempt function
    #define COMMISION 0x0C  // master load encripted mac befor to configure
    
    // test service supported for application
    #define CLR_GPIO_0 0x10         // Relay 0 // PWM 
	#define SET_GPIO_0 0x11
	#define READ_GPIO_0 0x12
	
	#define CLR_GPIO_1 0x20        // Relay 1 // ADC
	#define SET_GPIO_1 0x21
	#define READ_GPIO_1 0x22
	
	#define CLR_GPIO_2 0x30         // FDR button
	#define SET_GPIO_2 0x31
	#define READ_GPIO_2 0x32
	
	#define CLR_GPIO_3 0x40         // Status LED
	#define SET_GPIO_3 0x41
	#define READ_GPIO_3 0x42
    

	
   #define  FCTORY_RESET 0x50
   #define GET_VERSION 0x51
   #define GET_DIN 0x52
   #define GET_CUR_ON_TYM 0x53
	// remaining are tx and rx for BLE
	#define FIVE_SEC 5
    #define TEN_SEC 10
    #define FIFTEEN_SEC 15
    #define TWENTY_SEC 20
    #define MAX_CMD_FRAME_LEN 16 
    #define ENCRIPTION_LEN (MAX_CMD_FRAME_LEN -1)

   #define ERROR_FRAME 0x88
   #define AUTH_ERROR 0x01
   #define CRC_ERROR 0x02
   #define UNSUPPOERED_FUNCTION 0x03
   #define INVALID_CMD_LENFTH 0x04
#endif