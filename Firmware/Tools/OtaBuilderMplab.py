#this script is run on post buid 
#this script generates OTA file from hex file
#!/usr/bin/python
import sys
import os
import string
sys.path.append(os.path.realpath('..'))
# ROM ADDRESSSES
FLASH_SECTOR_SIZE = 0x20  # minimum size which can be eraced 32 bytes

APP_START_ADD = 0x2A0
APP_END_ADD	  = 0x800

#EEPROM ADDRESSES
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
EEP_WR_CMD	= 0x02
FLSH_RD_CMD = 0x03
EEP_RD_CMD	= 0x04
PING_CMD	= 0x05
RESET_CMD	= 0x06
FLASH_CMD	= 0x07
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

def Write_Hex_to_String_64(file_path,Device_Type_temp):
	global temp
	bl_frame_64_data = ""
	bl_frame_64_add =0
	eep_frame=[]
	ext_add=0
	image_checksum = 0
	byt_cnt =0
	frame_cnt=1
	# 1 command , 2 address ,64 data bytes
	bl_frame_64_new_add_prv=0
	length_prv=0

	try:
		f = open(file_path,'r')
		for line in f:
			length = int(line[1:3],16)/2			# data length in bytes# 1st two chrecter are length	 # 1:3 mease extrect 1 and 2nd charecter from line #int(line[1:2], 16)
			address = int(line[3:7],16) | (ext_add<<16)		# hex file address	# 3rd 4th 5th and 6th char are address 
			dat_type =	int(line[7:9],16)
			line_lnt=len(line)
			if (line_lnt > 3):
				data=line[9:(line_lnt-3)]  # data field
			else:
				data=""
				print("\n invalid line lnth")
				break
			checksum = int(line[(line_lnt-3):(line_lnt-1)],16) #checksum of line
			bl_frame_64_new_add=(address/2)
			if(length == 0): # end of hex file
				#print(eep_frame)
				break
			elif(dat_type == 4) : # if it extended addrss update the same
					ext_add = int(data,16)
					#bl_frame_64_data = bl_frame_64_data + "00"*((32-((bl_frame_64_new_add_prv +length_prv)%32))*2)
			elif(ext_add!=0): # its eeprom data or config data
				if(bl_frame_64_new_add>=EEP_START_ADD):# its eeprom data  
					temp_data=list(split_by_n(data,4)) # eeprom data have some extra byte to be ignored in hex file
					temp_add=bl_frame_64_new_add
					for index in range(len(temp_data)):
						eep_frame.append(EEP_WR_CMD)
						eep_frame.append(temp_add&0xFF)
						eep_frame.append((temp_add>>8)&0xFF)
						eep_frame.append(((int(temp_data[index], 16))>>8)&0xFF)
						temp_add  = temp_add +1
			else: # its program data			
				if(bl_frame_64_new_add_prv !=0):
					if((bl_frame_64_new_add_prv + length_prv) != bl_frame_64_new_add):
						bl_frame_64_data = bl_frame_64_data + "00"*((bl_frame_64_new_add - (bl_frame_64_new_add_prv + length_prv))*2)
					bl_frame_64_data = bl_frame_64_data + str(data)
				else:
					bl_frame_64_data = bl_frame_64_data + str(data) # add new ata
			length_prv=length
			bl_frame_64_new_add_prv=bl_frame_64_new_add
	finally:
		  f.close()
	list_b=list(split_by_n(bl_frame_64_data,128)) # split each byte
	temp_add = APP_START_ADD
	bl_data_64_farme = []
	New_Hex_File_Path=os.path.splitext(file_path)[0]
	New_Hex_File_Path=New_Hex_File_Path + ".OTA"
	thefile = open(New_Hex_File_Path, 'w')
	
	thefile.write(str(DUMMY_DEF_CMD) + " 0 0 0 0\n") # insert dummy command for delay to recover
	thefile.write(str(DUMMY_DEF_CMD) + " 0 0 0 0\n") # insert dummy command for delay to recover
	thefile.write(str(DUMMY_DEF_CMD) + " 0 0 0 0\n") # insert dummy command for delay to recover
	thefile.write(str(DUMMY_DEF_CMD) + " 0 0 0 0\n") # insert dummy command for delay to recover
	thefile.write( str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_VALID) +" 240 " + str(DEF_EEP_DATA) + " 0\n") # make application Invalid basically erases device type stored in FDR 
	thefile.write(str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_VERSN) +" 240 "+str(DEF_EEP_DATA) + " 0\n") # make application version 255 
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
		temp_data=str(bl_data_64_farme)
		temp_data=temp_data.replace("[", "")
		temp_data=temp_data.replace(",", "")
		temp_data=temp_data.replace("]", "")
		thefile.write(temp_data + "\n")
		temp_add = temp_add + FLASH_SECTOR_SIZE # FLASH_SECTOR_SIZE 0x20
		del bl_data_64_farme[:]
	eep_frame=list(split_array(eep_frame,4)) # 16 cherecters in eep write command
	if(len(eep_frame)>1):
		print("writing	EEPROM data")
		#print len(eep_frame)
		for index in range(len(eep_frame)):
			#print (eep_frame[index]),
			temp_data=str(eep_frame[index])
			temp_data=temp_data.replace("[", "")
			temp_data=temp_data.replace(",", "")
			temp_data=temp_data.replace("]", "")
			thefile.write(temp_data + " 0\n")
	image_checksum = image_checksum & 0xFF
	print "Image checksum : " + str(image_checksum) + ", data lnt: " + str(byt_cnt)
	thefile.write(str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_CHKSM) + " 240 " + str(image_checksum) +" 0\n") # write image_checksum
	thefile.write(str(EEP_WR_CMD) + " "+ str(EEP_FDR_APP_VALID) + " 240 " + str(Device_Type_temp) + " 0\n") # make application valid
	thefile.write("5 0 0 0 0\n") # insert ping command to read version and app sucess (wait for 3 sec befor sending this command!!!!!!!!!!!!!)
	thefile.close()
	print "Success !!!"

def main():
	print "\nLog:\nTotal argument passed: " + str(len(sys.argv))
	for arg in sys.argv[1:]:
			print "Argument :" + arg
	if(len(sys.argv)>=3):
		Device_Typ = int(sys.argv[2])
		print "\n"
		dirname = os.path.dirname(__file__)
		fin_name = os.path.join(dirname, sys.argv[1])
		print("Hex File Entered : " + fin_name)
		Write_Hex_to_String_64(fin_name,Device_Typ)
	else:
		print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()