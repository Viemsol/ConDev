//#define BOOTLOADABLE  FALSE //TRUE //FALSE
/*---------PIN TYPE CONFIGURATION----------------------*/
	/*This IC have 8 pins out of which two are vcc and ground*/
	/*-remaining 6 pin configuration is given below-*/

/*							---------------
	(vcc)	 HW_PIN1-------|			   |--------- HW_PIN8 (ground)
	(I/O,)	 HW_PIN2-------|			   |--------- HW_PIN7 (I/O,ADC)
	(I/O,ADC)HW_PIN3-------|  PIC12F683	   |--------- HW_PIN6 (I/O,ADC)
	(I)	 	 HW_PIN4-------|			   |--------- HW_PIN5 (I/O,ADC,CCP)
							---------------                       */
#define PORT_DIR     0b00101000  // 0 : output 1: input (port directions for LED,SWITCH,TX RX set in Bootloader)
#define PULL_UP_BITS 0x39  // 0: no pullup , 1: pullup ((port directions for LED,SWITCH,TX RX set in Bootloader))
#define PIN_INIT     0b00111000  // initial value of port pins
#define ADC_INPUT    0b00000000  // 0: Digital 1: ADC

#define DEBUG 1
//#-> bypass crc by hardcoding AAAA