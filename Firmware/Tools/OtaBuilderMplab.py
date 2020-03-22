#this script is run on post buid 
#this script generates OTA file from hex file
#!/usr/bin/python
import sys
import os
import string
import subprocess
FLASH_DATA = 11 # just to differantiat its tlv command tag
tlv8db= [0,0] #its tlv 8 db header tag_count is 0
def TLV8_AddTag(tlv8List,TagLenVal):
    tlv8List[1] = tlv8List[1] + 1 #increment the tag_count
    tlv8List.extend(TagLenVal) #append new tag
    return tlv8List
def TLV8_Finish(tlv8List):
    Checksum = sum(tlv8List) #calculate checksum
    tlv8List.append((Checksum&0xFF)) #append checksum
    return tlv8List
def TLV8_Verify(tlv8List): # varifyes if tlv structure is valid
    if(tlv8List[-1:][0] == sum(tlv8List[:-1])&0xFF):
        return 1
    return 0
DEV_TYP_PETTERN='#define DEVICE_TYP'
sys.path.append(os.path.realpath('..'))
# ROM ADDRESSSES
FLASH_SECTOR_SIZE = 0x20  # minimum size which can be eraced 32 bytes

APP_START_ADD = 0x2A0
APP_END_ADD   = 0x800

#EEPROM ADDRESSES
CONFIG_START_ADD = 0x8007
EEP_START_ADD = 0xF000
DEF_EEP_DATA = 0xFF
#--------Fectory 0xF0 is added by default ----------
EEP_FECT_DATA_VALID = 255
EEP_FECT_BL_VER = 254
EEP_FECT_DEV_TYP = 252   
EEP_FECT_LID_3 = 251
EEP_FECT_LID_2 = 250
EEP_FECT_LID_1 = 249
EEP_FECT_LID_0 = 248

#-----------FDR------------
EEP_FDR_APP_MAC03 = 247
EEP_FDR_APP_MAC02 = 246
EEP_FDR_APP_MAC01 = 245
EEP_FDR_APP_MAC00 = 244
EEP_FDR_APP_MACOK = 243
EEP_FDR_APP_VERSN = 242
EEP_FDR_APP_CHKSM = 241
EEP_FDR_APP_VALID = 240

#-----------Command-------
FLSH_WR_CMD = 0x01
EEP_WR_CMD  = 0x02
FLSH_RD_CMD = 0x03
EEP_RD_CMD  = 0x04
PING_CMD    = 0x05
RESET_CMD   = 0x06
FLASH_CMD   = 0x07
DUMMY_DEF_CMD = 0xFF

def split_by_n( seq, n ):
    """A generator to divide a sequence into chunks of n units."""
    while seq:
        yield seq[:n]
        seq = seq[n:]

def split_array(arr, size):
     arrs = []
     while len(arr) > size:
         pice = arr[:size]
         arrs.append(pice)
         arr   = arr[size:]
     arrs.append(arr)
     return arrs

def Write_Hex_to_String_64(file_pathapp,Device_Type_temp):
    global temp
    bl_frame_64_data = ""
    bl_frame_64_add =0
    global eep_frame
    eep_frame =[]
    ext_add=0
    image_checksum = 0
    byt_cnt =0
    frame_cnt=1
    # 1 command , 2 address ,64 data bytes
    bl_frame_64_new_add_prv=0
    length_prv=0

    try:
        f = open(file_pathapp,'r')#read App.Hex and parse hex file
        for line in f:
            length = int(line[1:3],16)/2                    # :0A123400XXXX...CC , 1:3 mease extrect 1 and 2nd charecter(0A) which is data length (each word occupy  2 bytes one is data , other is dummy)
            address = int(line[3:7],16) | (ext_add<<16)     #    1234 hex data address  # 3rd 4th 5th and 6th char are address 
            dat_type =  int(line[7:9],16)                   #        00
            line_lnt=len(line)
            if (line_lnt > 3):
                data=line[9:(line_lnt-3)]                   #           XXXX...Data Field
            else:
                data=""
                print("\n invalid line length")
                break
            checksum = int(line[(line_lnt-3):(line_lnt-1)],16) #               CC checksum of line
            bl_frame_64_new_add=(address/2)                     # physical address is / 2 for PIC16f as word is of two bytes 
            if(length == 0): # end of hex file
                break
            elif(dat_type == 4): # if it extended addrss update the same , from now onwards in hex file all addresses will be added with this value
                    ext_add = int(data,16)
            elif(bl_frame_64_new_add==CONFIG_START_ADD):
                    print"Excluding Config data from OTA file"
            elif(bl_frame_64_new_add>=EEP_START_ADD):# its eeprom data
                    temp_data=list(split_by_n(data,4)) # eeprom data have some extra byte to be ignored in hex file
                    print"Adding Application EEP data to OTA file"
                    print temp_data
                    temp_add=bl_frame_64_new_add
                    for index in range(len(temp_data)):
                        eep_frame.append(EEP_WR_CMD)
                        eep_frame.append(temp_add&0xFF)
                        eep_frame.append((temp_add>>8)&0xFF)
                        eep_frame.append(((int(temp_data[index], 16))>>8)&0xFF)
                        temp_add  = temp_add +1   
            else:#its programming data
                if(((bl_frame_64_new_add_prv + length_prv) != bl_frame_64_new_add)and(bl_frame_64_new_add_prv!=0)):#new address is not continuous 
                     bl_frame_64_data = bl_frame_64_data + "00"*((bl_frame_64_new_add - (bl_frame_64_new_add_prv + length_prv))*2) #fill gap with bytes with 00
                bl_frame_64_data = bl_frame_64_data + str(data) # add new ata 
            length_prv=length
            bl_frame_64_new_add_prv=bl_frame_64_new_add
    finally:
          f.close()
    list_b=list(split_by_n(bl_frame_64_data,128)) # split each byte
    temp_add = APP_START_ADD
    bl_data_64_farme = []
    New_Hex_File_Path=os.path.splitext(file_pathapp)[0]
    New_Tlv8_File_Path=New_Hex_File_Path + ".TLV8"
    New_Hex_File_Path=New_Hex_File_Path + ".OTA"
    
    global tlv8db
    try:
        thefile = open(New_Hex_File_Path, 'w')
        tlv8file = open(New_Tlv8_File_Path, 'w')
        #start writing OTA file
        for x in range(4):#write 4 times
            thefile.write(str(DUMMY_DEF_CMD) + " 0 0 0 0\n") # insert dummy command for initial delay
            tlvtest = [FLASH_DATA,5,DUMMY_DEF_CMD,0,0,0,0]
            tlv8db = TLV8_AddTag(tlv8db,tlvtest)
        
        thefile.write( str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_VALID) +" 240 " + str(DEF_EEP_DATA) + " 0\n") # make application Invalid basically erases device type stored in FDR 
        tlvtest = [FLASH_DATA,5,EEP_WR_CMD,EEP_FDR_APP_VALID,240,DEF_EEP_DATA,0]
        tlv8db = TLV8_AddTag(tlv8db,tlvtest)
        
        thefile.write(str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_VERSN) +" 240 "+str(DEF_EEP_DATA) + " 0\n") # make application version 255 invalid
        tlvtest = [FLASH_DATA,5,EEP_WR_CMD,EEP_FDR_APP_VERSN,240,DEF_EEP_DATA,0]
        tlv8db = TLV8_AddTag(tlv8db,tlvtest)
        for index in range(len(list_b)):
            list_Temp = list_b[index]
            list_Temp = list(split_by_n(list_Temp,2))
            #print(list_Temp)
            bl_data_64_farme.append(FLSH_WR_CMD)
            bl_data_64_farme.append(temp_add & 0xFF)
            bl_data_64_farme.append((temp_add >> 8)&0xFF)
            for index in range(len(list_Temp)):
                image_checksum = image_checksum + ((int(list_Temp[index], 16)) & 0xFF) # flash mem checksum
                byt_cnt = byt_cnt + 1
                bl_data_64_farme.append(int(list_Temp[index], 16))
            #print(bl_data_64_farme)
            #print("\n")
            tlvtest = [FLASH_DATA,67]
            tlvtest.extend(bl_data_64_farme)
            tlv8db = TLV8_AddTag(tlv8db,tlvtest)
            temp_data=str(bl_data_64_farme)
            temp_data=temp_data.replace("[", "")
            temp_data=temp_data.replace(",", "")
            temp_data=temp_data.replace("]", "")
            thefile.write(temp_data + "\n")
            temp_add = temp_add + FLASH_SECTOR_SIZE # FLASH_SECTOR_SIZE 0x20
            del bl_data_64_farme[:]
        eep_frame=list(split_array(eep_frame,4)) # 16 cherecters in eep write command
        if(len(eep_frame)!=0):
            for index in range(len(eep_frame)):
                tlvtest = [FLASH_DATA,5]
                tlvtest.extend(eep_frame[index])
                tlvtest.append(0)
                tlv8db = TLV8_AddTag(tlv8db,tlvtest)
                temp_data=str(eep_frame[index])
                temp_data=temp_data.replace("[", "")
                temp_data=temp_data.replace(",", "")
                temp_data=temp_data.replace("]", "")
                thefile.write(temp_data + " 0\n")
        image_checksum = image_checksum & 0xFF
        print "Image checksum : " + str(image_checksum)
        thefile.write(str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_CHKSM) + " 240 " + str(image_checksum) +" 0\n") # write image_checksum
        thefile.write(str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_VALID) + " 240 " + str(Device_Type_temp) + " 0\n") # make application valid
        thefile.write("5 0 0 0 0\n") # insert ping command to read version and app sucess (wait for 3 sec befor sending this command!!!!!!!!!!!!!)
        
        tlvtest = [FLASH_DATA,5,EEP_WR_CMD,EEP_FDR_APP_CHKSM,240,image_checksum,0]
        tlv8db = TLV8_AddTag(tlv8db,tlvtest)
        
        tlvtest = [FLASH_DATA,5,EEP_WR_CMD,EEP_FDR_APP_VALID,240,Device_Type_temp,0]
        tlv8db = TLV8_AddTag(tlv8db,tlvtest)
        
        tlvtest = [FLASH_DATA,5,5,0,0,0,0]
        tlv8db = TLV8_AddTag(tlv8db,tlvtest)
        tlv8db = TLV8_Finish(tlv8db) # freeze TLV8
        newFileByteArray = bytearray(tlv8db)
        print "TLV8 file Validity :" + str(TLV8_Verify(tlv8db))
        tlv8file.write(newFileByteArray)
        print "Success !!!"
    finally:
        thefile.close
        tlv8file.close
def main():
    print "--------------Creating  App.OTA -------------\nLog:\nTotal argument passed: " + str(len(sys.argv))
    for arg in sys.argv[1:]:
        print "Argument :" + arg
    if(len(sys.argv)>=2):
        #get devicetype from file
        dirname = os.path.dirname(__file__)
        config_fin_name = os.path.join(dirname, sys.argv[1] + "\Src\system_config.h")
        print "Reading Device Type  from :" + config_fin_name
    
        Device_Typ = 0
        try:
            f = open(config_fin_name,'r')
            for line in f:
                if DEV_TYP_PETTERN in line:
                    Device_Typ = int((line.split()[2]),0)
                    print "Device type Found : " + str(Device_Typ)
                    f.close();
                    break;
        finally:
            f.close()
        dirname = os.path.dirname(__file__)
        fin_name = os.path.join(dirname, sys.argv[1] + "\Build\dist\default\production\Build.production.hex")
        print("App Hex File Path : " + fin_name)
        dirname = os.path.dirname(__file__)
        if(Device_Typ!=0):
            Write_Hex_to_String_64(fin_name,Device_Typ)
        else:
            print "Device type incorrect"
    else:
        print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()