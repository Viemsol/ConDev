/* 
 * File:   type_def.h
 * Author: acer
 *
 * Created on November 30, 2014, 6:11 PM
 */

#ifndef TYPE_DEF_H
#define	TYPE_DEF_H

typedef unsigned char _UBYTE;
typedef unsigned char _UINT8;
typedef unsigned char uint8;

typedef signed char _SBYTE;
typedef signed char _SINT8;

typedef unsigned int _UINT16;
typedef unsigned int uint16;

typedef signed int _SINT16;

typedef unsigned long int _UINT32;
typedef unsigned long int uint32;

typedef signed long int _SINT32;
typedef signed long int sint32;
#define NULL 0

//I_O_STATUS
#define I 0
#define O 1
#define ADC 2
#define PWM 3
#define PM 4
#define  EXT_INTR 5
#define  UART_TX 6
#define  UART_RX 7
#define  UART_IDLE 8

//COMPLEX_FUNC_STATUS
#define REQUIRE 0
#define NOT_REQUIRE 1

#define FALSE 0x00
#define NO 0x00


#define ON 0x01
#define ONE 0x01
#define TRUE 0x01
#define YES 0x01
#define OK 0x01

/*Global constants*/
#define MAX_UINT8   0xFF
#define MIN_UINT8   0x00
#define MAX_SINT8   0x7F
#define MIN_SINT8   0x80
#define MAX_UINT16  0xFFFF
#define MIN_UINT16  0x0000
#define MAX_SINT16  0x7FFF
#define MIN_SINT16  0x8000
#define MAX_UINT32  0xFFFFFFFF
#define MIN_UINT32  0x00000000
#define MAX_SINT32  0x7FFFFFFF
#define MIN_SINT32  0x80000000






#ifdef	__cplusplus
extern "C" {
#endif




#ifdef	__cplusplus
}
#endif

#endif	/* TYPE_DEF_H */

