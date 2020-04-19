package com.example.cdmaster;

//Author: Nandkumar G Dhavalikar Date 02/05/2019
// TLV8 is format to store binary data in structured form into memory constraint devices.
// This TLV code is applicable for 8 bit controller only else segmentation fault may occur
// TLV8 Format
//TLV8Header,Tag,Length,Value,Tag,Length,Value...Checksum
//- TLV8Header :2 byte (TLV Type,TagCount) ...type TLV8:0 ,TagCount: 1- 250
//- [TAG] 1 byte (0-250 valid values) 255:deleted/unused tag , 254:
//- [LEN] 1 byte , Indicate length of data bytes
//- [VALUE] 1 byte
//- example TLV8(2): 0x00[TLV8],0x02[TagCount],0xFF[TAG],0x01[LEN],0x33[VAL],0x06[TAG],0x02[LEN],0x00[VAL],0x01[VAL],0xCC[CHK]
//- There  are 2 tags [TagCount]
//- 1st tag is deleted one and other new tag with same length can be written
//- CHK is Checksum [CHK]

import java.util.ArrayList;
import java.util.List;

public class TLV8lib
{

    List<Byte> TLVFile= new ArrayList<>();
    public TLV8lib()
    {
        TLVFile.add((byte)0x00); // add TLV8
        TLVFile.add((byte)0x00); // add TLV8 length of zero
    }
}
