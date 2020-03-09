#ifndef _INCL_SRVLB
#define _INCL_SRVLB
// get bit
#define GetBit(base,pos)               (((base)>>pos)&0x01)
//set bit
#define SetBit(base,pos)               ((base) |= (1u<<(pos)))
//clear bit
#define ClrBit(base,pos)               ((base) &= ~(1u<<(pos)))
//toggel bit
#define TogBit(base,pos)               ((base) ^= ((1) << (pos)))
//putbit
#define PutBit(base,pos,bit)           base ^= ((-bit ^ base) & (1 << pos)); 

#define ascii_to_hex(ascii_dat)        ((ascii_dat<='9')?(ascii_dat-'0'):(ascii_dat-'A'))

#define hexnib_to_ascii(hex_dat)        ((hex_dat<=9)?(hex_dat+'0'):(hex_dat+'A'))
#endif