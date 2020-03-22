'''
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
'''

import string
tlv8db= [0,0] #its tlv 8 db header tag_count is 0
def TLV8_AddTag(tlv8List,TagLenVal):
	tlv8List[1] = tlv8List[1] + 1 #increment the tag_count
	tlv8List.extend(TagLenVal)
	return tlv8List
def TLV8_Finish(tlv8List):
    Checksum = sum(tlv8List) #calculate checksum
    tlv8List.append((Checksum&0xFF)) #append checksum
    return tlv8List
def TLV8_Verify(tlv8List): # varifyes if tlv structure is valid
    if(tlv8List[-1:][0] == sum(tlv8List[:-1])&0xFF):
        return 1
    return 0
def TLV8_GetTagCount(tlv8List):
    if((len(tlv8List))>1):
        return(tlv8List[1:][0])
    return 0
def TLV8_GetTagAtIndex(tlv8List,Index):
    if(((len(tlv8List))>1) and (TLV8_GetTagCount(tlv8List) > Index)):
        Tag_idx = 2 #first tag starts from this
        for x in range(Index+1):
            if(Index == x):
                return tlv8List[Tag_idx:(Tag_idx + 2 +(tlv8List[Tag_idx + 1]) )]
            Tag_idx = (Tag_idx + 2 +(tlv8List[Tag_idx + 1])) #next tag
        return(0)
    return 0
def TLV8_GetTagValueAtIndex(tlv8List,Index):
    if(((len(tlv8List))>1) and (TLV8_GetTagCount(tlv8List) > Index)):
        Tag_idx = 2 #first tag starts from this
        for x in range(Index+1):
            if(Index == x):
                return tlv8List[Tag_idx + 2:(Tag_idx + 2 +(tlv8List[Tag_idx + 1]) )]
            Tag_idx = (Tag_idx + 2 +(tlv8List[Tag_idx + 1])) #next tag
        return(0)
    return 0
def TLV8_GetValueAtTag(tlv8List,Tag):
    if(((len(tlv8List))>1)):
        Tag_idx = 2 #first tag starts from this
        for x in range(TLV8_GetTagCount(tlv8List)):
            if(Tag == tlv8List[Tag_idx]):
                return tlv8List[Tag_idx + 2:(Tag_idx + 2 +(tlv8List[Tag_idx + 1]) )]
            Tag_idx = (Tag_idx + 2 +(tlv8List[Tag_idx + 1])) #next tag
        return(0)
    return 0
def TLV8_GetLengthOfTag(tlv8List,Tag):
    if(((len(tlv8List))>1)):
        Tag_idx = 2 #first tag starts from this
        for x in range(TLV8_GetTagCount(tlv8List)):
            if(Tag == tlv8List[Tag_idx]):
                return tlv8List[Tag_idx + 1]
            Tag_idx = (Tag_idx + 2 +(tlv8List[Tag_idx + 1])) #next tag
        return(0)
    return 0
tlvtest = [1,2,12,133]
tlvtest2 = [55,5,0,3,4,12,133]
tlv8db = TLV8_AddTag(tlv8db,tlvtest) # add one tag
tlv8db = TLV8_AddTag(tlv8db,tlvtest2)  # add one more  tag
tlv8db = TLV8_Finish(tlv8db) # freeze TLV8
print (tlv8db)
print (TLV8_Verify(tlv8db))
print (TLV8_GetTagCount(tlv8db))
print (TLV8_GetTagAtIndex(tlv8db,1))
print (TLV8_GetTagValueAtIndex(tlv8db,0))
print (TLV8_GetValueAtTag(tlv8db,55))
print (TLV8_GetLengthOfTag(tlv8db,0))