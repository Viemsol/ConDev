from MyOnlineLib.PythonDevLib.libAll.libDisp import *
from MyOnlineLib.PythonDevLib.libAll.libRun import *
from MyOnlineLib.PythonDevLib.libAll.libPicKit3 import *
from MyOnlineLib.PythonDevLib.libAll.libFirebase import *
from MyOnlineLib.PythonDevLib.libAll.libConvert  import *
from MyOnlineLib.PythonDevLib.libAll.libSerial import *
from MyOnlineLib.PythonDevLib.libAll.libFile import *

from memoryMap import *
from intelhex import IntelHex
from itertools import chain


import sys
import os
import subprocess
import time
import random
import string
from subprocess import Popen
import argparse
import configparser

import serial

RESPONCE_BYTE_LEN = 18 #Responce bytes length
BREAK  = 0.1
devconIdUsbCom = "USB\VID_0403&PID_6001\FTHC1GUK"
devconIdPiKit3 = "USB\VID_04D8&PID_900A\DEFAULT_PK3_"

PING   = 1             # in bootloader
PING_IN_APP = 2        # in app commisioned
PING_IN_COMMISION = 3  # not commisiond
WR_FLH = 4
WR_EEP = 5
RD_FLH = 6
RD_EEP = 7

RSTCMD = 8
FLASH  = 9
PING_APP_RESP = 10
COMMISION = 11
CONFIG = 12
FDR = 13
ACTION = 14 
READ = 15
DEBUG_CMD = 0x20
DEF = 0xFF

ser = 0
Debug = 1  # debug print
FactTest = 1 # run self test
Fb_UN = ""
Fb_PW = ""
user = ""
firebase = ""
currentDevPath = ""
APP = 0
BOOT = 1

def EncData(data,key):
	lenD = len(data)
	temp = 0
	KeyIteration = 0
	outData= bytes()
	for iteration, value in enumerate(data):
		temp = data[iteration]
		temp = temp + 0xab
		temp = temp ^ lenD
		temp = temp ^ key[KeyIteration]
		lenD = lenD - 1
		outData += bytes([(temp&0xFF)])
		KeyIteration +=1
		if(KeyIteration>=8):
			KeyIteration = 0
	#printD("DEC",DecData(outData,key))
	#printD("ENC",outData)
	return outData

def DecData(data,key):
	lenD = len(data)
	temp = 0
	KeyIteration = 0
	outData= bytes()
	for iteration, value in enumerate(data):
		temp = data[iteration] ^ key[KeyIteration]
		temp = temp ^ lenD
		temp = temp - 0xab		
		lenD = lenD - 1
		outData += bytes([(temp&0xFF)])
		KeyIteration +=1
		if(KeyIteration>=8):
			KeyIteration = 0
	#printI(outData)
	return outData

def insetStringBetweenEverChar(stringDat,stringDatIns):
	return stringDatIns.join(stringDat)
def get_random_password_string(length):
    password_characters = string.ascii_letters + string.digits + "#@$&*!"
    password = ''.join(random.choice(password_characters) for i in range(length))
    return password
def checksum16(data):#data in bytes
	lent = int(len(data)/2)
	idx = 0
	data_checksum = 0xabcd;
	dat_cnt = 0
	while(idx < lent):
		dataR = (int.from_bytes(data[(idx*2):(2*(idx+1))], byteorder='little'))
		data_checksum += dataR
		data_checksum = data_checksum & 0xFFFF
		if(data_checksum & 0x8000):
			dat_cnt = 1
		data_checksum = ((data_checksum<<1) + dat_cnt)&0xFFFF;
		dat_cnt = 0;
		idx = idx+1
	return data_checksum
def createNewModel():
	#it creates new device stucture in database
    return true	
def provisionDevice(model): # create New Device Account , create backupHex file, Flash the device
	FactoryDevHex             = IntelHex("BootAppConfig.hex")
	keySectionApp             = IntelHex()             # contain key  section of hex file
	#update key section 
	#keys A4T4 is stored for "AT" in eeprom
	if(not fbLoginSucess()):
		printE("Login to provision")
		return
	#register new device and load SN
	maxSn = fbReadDb("FirmwareMeta/"+model+"/MaxSn")
	fbWriteDb("FirmwareMeta/"+model+"/MaxSn",maxSn+1)
	
	DevSn = base36encode(maxSn,6)
	userid = model+DevSn # 8 bytes 
	passkey =  get_random_password_string(8) #pass key 8 bytes
	
	#get model key From Server, makeure for any new device modle key is generated manually 
	ModelKey = fbReadDb("FirmwareMeta/"+model+"/ModelKey") # "MODEL"+model+"0" #8 bytes

	fbWriteDb("FirmwareMeta/"+model+"/DevSnKeys/"+userid,passkey)
	#mearge hex file with keys and generate device hex file

	keysection = userid+passkey+ModelKey+passkey  # last passkey is just filler
	eepKeySection =  insertByteAfterOne(bytes(passkey+ModelKey,'utf-8'),0) # this keys are also loaded into EEP so that user need not have to to FDR
	keysectionFlash = insertByteAfterOne(bytes(keysection,'utf-8'),0x34) # 34 in msb signify flash constant
	keySectionApp.puts(KEY_START_ADD*FLASH_WORD_SIZE,keysectionFlash)
	keySectionApp.puts(EEP_KEY_START_ADD*FLASH_WORD_SIZE,eepKeySection)
	FactoryDevHex.merge(keySectionApp)
	FactoryDevHex.write_hex_file("Factory"+model+DevSn+".hex", False) # don't write start address
	
	currentDevPath = "Factory"+model+DevSn+".hex"
	return currentDevPath

def picFlashDevice(picPartNo,hexpath=currentDevPath,picProgVoltage=4.5):
	#path = "C:\Program Files\Microchip\MPLABX\v4.05\mplab_ipe\pk3cmd.exe" #add this to env variable
	if(os.path.isfile(hexpath)):
		printApp("Flashing "+hexpath)
		#cliEx("pk3cmd.exe -P16F18313 -V3.3 -B -C")
		
		if(cliEx("pk3cmd.exe -P"+picPartNo+" -F"+hexpath+" -V"+str(picProgVoltage)+" -M -Y -H")):
			return 1
		else:
			if(cliEx("pk3cmd.exe -P"+picPartNo+" -V"+str(picProgVoltage)+" -L")):#release from reset
				return 1
			else:
				return 0
	else:
		printE("Hex File Not Found")
		return 1
def picEraseDevice(picPartNo,picProgVoltage=4.5):
	status = cliEx("pk3cmd.exe -P"+picPartNo+" -V"+str(picProgVoltage)+" -E")
	print("Pic Status :"+str(status))
	if(status):
		return 1
	else:
		return 0
def picFlashSaveKey(picPartNo,hexpath=currentDevPath,picProgVoltage=4.5):
	#status = cliEx("pk3cmd.exe -P"+picPartNo+" -F"+hexpath+" -V"+str(picProgVoltage)+" -MP -N"+hex(APP_START_ADD)+","+hex(APP_END_ADD))
	status = cliEx("pk3cmd.exe -P16F18313 -Ftest.hex -V4.3 -MP0x2A0,0x7DF")
	print("Pic Status :"+str(status))
	if(status):
		return 1
	else:
		return 0
def generateFactFiles(model):
	print("Memory MAP")
	print("BOOT SIZE  : "+ hex(BL_SIZE) + " ADDRESS: "+ hex(BL_START_ADD) + " to " + hex(BL_END_ADD))
	print("APP SIZE   : "+ hex(APP_SIZE) + " ADDRESS: "+ hex(APP_START_ADD) + " to " + hex(APP_END_ADD) )
	print("KEY SIZE   : "+ hex(KEY_SIZE) + "  ADDRESS: "+ hex(KEY_START_ADD) + " to " + hex(KEY_END_ADD)) 
	print("CONFIG SIZE: "+ hex(CONFIG_SIZE)+ "   ADDRESS: "+ hex(CONFIG_START_ADD) + " to " + hex(CONFIG_END_ADD) )
	#read app and boot files
	bootHex                = IntelHex(bootPath)
	apphex                 = IntelHex(appPath)    
	bootSection            = IntelHex()       # contain Boot application section of hex file
	appSection             = IntelHex()        # contain application section of hex file
	configSectionBoot      = IntelHex()          # contain config  section of hex file
	eepromSectionboot      = IntelHex()          # contain config  section of hex file
	configSectionApp       = IntelHex()          # contain config  section of hex file
	eepromSectionApp       = IntelHex()          # contain config  section of hex file
	keySectionApp          = IntelHex()             # contain key  section of hex file

	# create factory hex file
	bootSection            = bootHex[BL_START_ADD*FLASH_WORD_SIZE:(BL_END_ADD+1)*FLASH_WORD_SIZE]
	configSectionBoot      = bootHex[CONFIG_START_ADD*FLASH_WORD_SIZE:(CONFIG_END_ADD+1)*FLASH_WORD_SIZE] # create config hex file
	eepromSectionBoot      = bootHex[EEP_START_ADD*FLASH_WORD_SIZE:(EEP_END_ADD+1)*FLASH_WORD_SIZE]
	appSection             = apphex[APP_START_ADD*FLASH_WORD_SIZE:(APP_END_ADD+1)*FLASH_WORD_SIZE]
	configSectionApp       = apphex[CONFIG_START_ADD*FLASH_WORD_SIZE:(CONFIG_END_ADD+1)*FLASH_WORD_SIZE]
	eepromSectionApp       = apphex[EEP_START_ADD*FLASH_WORD_SIZE:(EEP_END_ADD+1)*FLASH_WORD_SIZE]
	
	eepAppBytes     =   eepromSectionApp.tobinarray()
	appBytes        =   appSection.tobinarray()
	
	#Update CRC to APP hex
	# creating factory file (factory hex file contain, Bootloader,Application,keys,EEP,CONFIG all combined together)
	temp = checksum16(appBytes[0:-2*FLASH_WORD_SIZE])
	appSection[APP_CRC_ADDRESS*FLASH_WORD_SIZE]     = temp&0xFF
	appSection[APP_CRC_ADDRESS*FLASH_WORD_SIZE + 1] = 0x34
	appSection[APP_CRC_ADDRESS*FLASH_WORD_SIZE + 2] = (temp>>8)&0xFF 
	appSection[APP_CRC_ADDRESS*FLASH_WORD_SIZE + 3] = 0x34
	appBytes        =   appSection.tobinarray()                         # App binary with CRC
	
	#OTA IMAGE
	f = open(model+'appOta.bin', 'w+b')
	f.write(bytearray(appBytes))
	f.close()
	FactoryHex = bootSection
	#create reamining file to be used for creating factory image
	FactoryHex.merge(appSection)
	FactoryHex.merge(eepromSectionApp)
	FactoryHex.merge(eepromSectionBoot)
	FactoryHex.merge(configSectionBoot)
	FactoryHex.write_hex_file('BootAppConfig.hex', False) # don't write start address
def updateOTAImage(model,OtaVersionComment):  #update OTA image to FB Server , OTA image to be located in the script folder
	if(not fbLoginSucess()):
		printE("Login Credential not set")
		return

	db_key = "FirmwareMeta/"+model+"/FwImages/Ver_0"
	db_val = model+"_AppOta.bin%"+OtaVersionComment #image name + image comment
	fbWriteDb(db_key,db_val)

	Strpath = "cdmasterStorage/FwImages/"+model+"_AppOta.bin"
	fbwriteStorage(Strpath,model+"appOta.bin")
	printS("Success")
	return 0
def printApp(printDat):
	if(Debug == 1):
		print(printDat)
# this function ping device and return session key for next command,0: fail . 1 : in bootloader ,else session key

#define Update these variable with persistant storage file ini file, on successfully commission
DevSn = 0
userKey = 0
masterKey = 0
userKeySession = 0 
masterKeySession = 0
#####
# ping the device and geenrate session key
# return 0: error
#        1: in bootloader
#        2: in Application
####
def getSessionKey(model):
	global DevSn
	global userKey
	global masterKey
	global userKeySession
	global masterKeySession
	
	pingRes = pingDevice()
	if(isinstance(pingRes, bytes)):
		if(pingRes[0] == PING):# in bootloader
			masterKeySession = 0
			userKeySession = 0
			return 1
		elif(pingRes[0] == 2): # already commission
			# get commition key
			printI("Device is commissioned")
			if(isinstance(userKey, int)):#if keys present
				#read from stored file
				f = open(DevSn +'CommissionKey.bin', 'rb')
				byteData = f.read()
				f.close()
				userKey = byteData[:8]
				masterKey =  byteData[8:]
				if(userKey == 0):
					printI("Session key not present")
					masterKeySession = 0
					userKeySession = 0
					return 0
		else:# not commotion
			printI("Device is not commissioned")
			if(fbLoginSucess()):
				printS('Login Success !!')
			else:
				printE('login Fail !!')
				return 0
			#get temporary encryption key from server
			DevSn = bytesToAscii(pingRes[1:9])
			printI("Reading Key from " + "FirmwareMeta/"+model+"/DevSnKeys/"+bytesToAscii(pingRes[1:9]))
			devKey = fbReadDb("FirmwareMeta/"+model+"/DevSnKeys/"+bytesToAscii(pingRes[1:9]))
			printD("DevKey",devKey,1)
			userKey = strlen2ByteArray(devKey)
			
			masterKey = strlen2ByteArray("MODELCD0")
		#user key
		userKeySession = EncData(list2Bytes(pingRes[9:-1]),userKey)
		#master key
		masterKeySession = EncData(userKeySession,masterKey)
		#printD("usrS_Key:",userKeySession,2)
		#printD("mstS_Key:",masterKeySession,2)
		return 2
	else: # no responce
		return 0
############
# respond with Lis else 0, 1st byte of list is status
############
def pingDevice():
	global CommitionKey
	global DevSn
	list_ping = [PING, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] # only fill random number at index 9 to 16 , other keep 0
	pingResp = 0
	listResp = send_data(list_ping,APP)
	if(listResp and (listResp[0] <= PING_IN_COMMISION)):
		printI("Ping Success :" + str(listResp[0]))
		# compute session key
		DevSn = bytesToAscii(listResp[1:9])
		#printD("SN",listResp[1:9],1)
		#printD("Rand",listResp[9:-1],2)
		pingResp = listResp
		return pingResp   	
	else:
		printI("Ping Fail")
		return 0
######
# update commution keys to device
# save Commition keys locally and device
#####
def commission():
	# session key calculated erlier
	global DevSn
	global userKey
	global masterKey

	comm_data = [COMMISION, 1, 2, 0, 0, 0, 0, 0, 0, 3, 4, 0, 0, 0, 0, 0, 0]
	listResp = send_data(comm_data,APP)
	if(listResp):
		printS("commision Sucess")
		userKey = listResp[1:9]
		masterKey = listResp[9:17]
		
		#store commition keys to file
		f = open(DevSn +'CommissionKey.bin', 'w+b')
		f.write(bytearray(listResp[1:17]))
		f.close()
		
		#printD("New_UK",userKey,2)
		#printD("New_MK",masterKey,2)
		return listResp[0]		
	else:
		printI("Action Fail")
		return 0
def sendAction():
	list_action = [ACTION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
	listResp = send_data(list_action,APP)
	if(listResp):
		printI("Action Sucess")
		return listResp[0]		
	else:
		printI("Action Fail")
		return 0
def sendFdr():
	list_action = [FDR, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
	listResp = send_data(list_action,APP)
	if(listResp):
		printI("FDR Sucess")
		return listResp[0]		
	else:
		printI("FDR Fail")
		return 0
def otaDevice():
	list_flash = [FLASH, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
	listResp = send_data(list_flash,APP)
	if(isinstance(listResp, bytes)):
		if(listResp[0] == PING):
			printI("Device in FLASH Mode...\n Flashing Device")
			return(otaDeviceInBoot())		
	printE("Ota Fail")
	return 0
def makeAppValid():
	eepByte = [0xAA]
	if( writeMem(EEP_APP_VALID_ADD, eepByte, 1) ):
		printI("App Valid Sucess")
		return 1
	else:
		return 0
def otaDeviceInBoot():
	count = 0
	with open("CDappOta.bin", "rb") as f:
		byteData = f.read()
		otaDataLen = len(byteData)
		printI("Flashing OTA image of size : " + str(otaDataLen/1024) + " KB")
		
		st = writeMem(APP_START_ADD,bytes2List(byteData),otaDataLen) # flash OTA file
		if(st):
			printI("FLASING Success .. Validating application")
			st = makeAppValid()
			if(st):
				time.sleep(1) # wait for device to reset and boot
				if(0 != CmdSend("getBlVersion")):
					if(0 != CmdSend("getAppVersion")):
						printI("Reading Version...Success")
				else:
					printE("Error reading version")
	if(st == 0):
		printE("OTA fail")
	return st
def getBlVersion():
	ReadVer = [RD_EEP, (BL_VER_ADD&0xFF), (BL_VER_ADD>>8), 2,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
	listResp = 	send_data(ReadVer,APP)
	if(listResp):
		printI("Boot version : " + str(listResp[4]))
		return listResp[4]
	else:
		printI("Version Read Fail")
		return 0

def getAppVersion():
	ReadVer = [RD_EEP, (APP_VER_ADDRESS&0xFF), (APP_VER_ADDRESS>>8), 2,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
	listResp = 	send_data(ReadVer,APP)
	if(listResp):
		printI("App version : " + str(listResp[4]))
		return listResp[4]
	else:
		printI("Version Read Fail")
		return 0
def resetDevice(add,len):
	resetCmdLst = [RSTCMD]
	printI("Resetting  Device")

def readMem(add,len):
	bytsRespFin = bytes(0)
	if((add <= FLASH_END_ADD) and( (len>0) and ((len%2) == 0) and (len <= (FLASH_SIZE))) or (((add>=EEP_START_ADD) and (add<=EEP_END_ADD)) and (len>0 and len <=EEP_SIZE))):
		#printI("Reading Memory, From  Address :" + hex(add) + ", Length: " + str(len)+"\n[Address] [length] [data]")
		while(len):
			templen = len
			if(len>=8):
				templen = 8
			len = len - templen 
			readMemCmd = [RD_EEP,(add & 0xFF),(add >> 8),templen]
			bytesResp = 	send_data(readMemCmd,BOOT)
			if(bytesResp):
				bytsRespFin = bytsRespFin + bytesResp[4:4+templen]
				#printI("["+hex(add) +"] "+ "["+hex(templen) +"] "+(bytesResp[4:4+templen]).hex())
			else:
				printE("Eep Read fail")
				return 0
			if(add <= FLASH_END_ADD):
				add = add + 4
			else:
				add = add + 8
		return bytsRespFin
	else:
		return 0
FLASH_BLOCK_SIZE = 64
def writeMem(add,data,len):
	if(((add <= FLASH_END_ADD) and (len%FLASH_BLOCK_SIZE==0) and (len <= (FLASH_SIZE))) or (((add>=EEP_START_ADD) and (add<=EEP_END_ADD)) and (len>0 and len <=EEP_SIZE))):
		cmd = WR_EEP
		lenSent = 0
		blkLen = 8
		if(add <= FLASH_END_ADD): # flash write
			#printI("\n[Address] [length] [data]")
			cmd = WR_FLH
			blkLen = 32
		#else:
		#printI("Writing EEPROM memory from Address :"+hex(add)+", Len:"+str(len)+"\n[Address] [length] [data]")
		while(len):
			templen = len
			if(len>=(blkLen*2)):
				templen = blkLen*2
			len = len - templen
			printI("Writing flash memory at address :"+hex(add)+", Len:"+str(templen))
			if(add <= FLASH_END_ADD):
				writeMemCmd = [cmd,(add & 0xFF),(add >> 8)] + data[lenSent:(lenSent+templen)]
			else:
				writeMemCmd = [cmd,(add & 0xFF),(add >> 8),templen] + data[lenSent:(lenSent+templen)]
			bytesResp = send_data(writeMemCmd,BOOT)
			if(bytesResp == 0):
				printE("Memory write fail")
				return 0
			#else:
			#printI("["+hex(add) +"] "+ "["+hex(templen) +"] "+ "".join(["{:02x}".format(x) for x in data[lenSent:(lenSent+templen)]]))	
			add = add + blkLen
			lenSent = lenSent + templen
		return 1
	else:
		return 0
def wirteModelAndKey(data):
	if(writeMem(KEY_START_ADD,data,64)):
		printI("FLash write Success")
	flashRead = readMem(KEY_START_ADD,64)
	if(flashRead):
		printI("Flash Read Success")
		printApp(flashRead.hex())
	else:
		printE("Flash write Fail")
def send_data(list_dat_orig,BL_APP):
	global ser
	global masterKeySession
	global userKeySession
	bArray_out = bytes()
	devResp = 0
	list_dat = list_dat_orig.copy()
	if(len(list_dat)):
		crc = (sum(list_dat) + 0xab) & 0xFF #[cmd][data][crc] , crc = 0x1b + sum of cmd and data
		list_dat.append(crc)
		bArray = bytes(list_dat)
		bArray_out += bytes([bArray[0]&0xFF])
		if( (bArray[0] < ACTION) and (bArray[0] > PING_IN_COMMISION) and (BL_APP == APP) ): #master
			tmpOut = EncData(bArray[1:],masterKeySession)
		elif(bArray[0] >= ACTION and (BL_APP == APP)): #user, ping then do not encrypt
			tmpOut = EncData(bArray[1:],userKeySession)
		else:
			tmpOut = bArray[1:] #donot encryypt if PING command or System in bootloader
		bArray_out += tmpOut
		#printD("Sending Data :",bArray_out,2)
		if((bArray[0] == FLASH) or (bArray[0] == FDR)): # no responce is expected
			serSend(ser,bArray_out)
			time.sleep(1) # wait for device to reset OR FDR
			list_ping = [PING, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0xac]
			devResp = SendAndRead(ser,list_ping, RESPONCE_BYTE_LEN)
		else:
			ser.timeout = 0.5 # this is needed as flashing takes time
			devResp = SendAndRead(ser,bArray_out, RESPONCE_BYTE_LEN)
			devResp = reponse(devResp,BL_APP)
	return devResp
		
def reponse(devResp,BL_APP):
	bArray_out = bytes()
	if (len(devResp) != 0):
		#printD("Resp :",devResp)
		bArray_out += bytes([devResp[0]&0xFF])
		if(devResp[0] == DEBUG_CMD):
			#its debug print
			printD("MSG:",devResp,1)
			tmpOut = devResp[1:]
			bArray_out += tmpOut
		else:
			if( (devResp[0] < ACTION) and (devResp[0] > PING_IN_COMMISION) and (BL_APP == APP)): #master
				tmpOut = DecData(devResp[1:],masterKeySession)
			elif(devResp[0] >= ACTION and (BL_APP == APP)): #user
				tmpOut = DecData(devResp[1:],userKeySession)
			else:
				tmpOut = devResp[1:]
			bArray_out += tmpOut
			crc = (sum(bArray_out[:-1]) + 0xab) & 0xFF
			if(crc != bArray_out[len(devResp) -1]):
				printE("CRC Error, Exp CRC "+ str(crc) +", Received "+str(bArray_out[len(devResp) -1]))
				bArray_out = 0
	else:
		printE("No Response received")
		bArray_out = 0
	return bArray_out
def writeLog(Log):
    with open('pythLog.txt', 'a') as fh:
        fh.write(Log + '\n')
        fh.close()
    
def writeOut(Key,Value):
    with open('pythOut.txt', 'a') as fh:
        fh.write(Key + " " + Value)
        fh.close()
def CmdSend(cmd):
	if(username and password):
		fbLogin(username,password)
		if(fbLoginSucess() == 0):
			printE('login Fail !!')
			return 0
		#printS('Login Success !!')
	else:
		printE('User name Password needed')
		return 0
	stkey = getSessionKey(deviceModel)
	if(stkey != 0):
		if(stkey>1): #in application
			printS("We are in application:Session key generated!!")
			return(getattr(sys.modules[__name__], cmd)())	
		else: #in bootloader
			printE("Faild to generate session keys")
			if(cmd == "otaDevice"):#in bootloader
				cmd = "otaDeviceInBoot"
				return(getattr(sys.modules[__name__], cmd)())
			return 0		
	else: # no responce
		return 0
if(FactTest):
	'''	
	#####################-------------------------COM Test-------------------------######################
	setComPort("COM24",57600,0.1)

	#####################-------------------------Ping Test-------------------------######################
	pingDevice()
	sendAction()

	#####################-------------------------Boot Version Test-------------------------######################
	temp = getBlAppVersion()
	if(temp):
		printApp("BL Version:" + str(temp))
	#####################-------------------------Flash Read Test-------------------------######################
	flashRead = readMem(KEY_START_ADD,64)
	if(flashRead):
		printApp("Flash Read Success")
		printApp(flashRead.hex())
	#####################-------------------------Flash write test-------------------------######################
	flagKeyByte = [0xAA,0x00,0xBB,0x00,0xCC,0x00,0xCC,0x00,0xDD,0x00,0xDD,0x00,0xCC,0x00,0xCC,0x00,0xAA,0x00,0xBB,0x00,0xCC,0x00,0xCC,0x00,0xDD,0x00,0xDD,0x00,0xCC,0x00,0xCC,0x00,0xAA,0x00,0xBB,0x00,0xCC,0x00,0xCC,0x00,0xDD,0x00,0xDD,0x00,0xCC,0x00,0xCC,0x00,0xAA,0x00,0xBB,0x00,0xCC,0x00,0xCC,0x00,0xDD,0x00,0xDD,0x00,0xCC,0x00,0xCC,0x00]
	if(writeMem(KEY_START_ADD,flagKeyByte,64)):
		printApp("FLash write Success")
	flashRead = readMem(KEY_START_ADD,64)
	if(flashRead):
		printApp("Flash Read Success")
		printApp(flashRead.hex())
	#####################-------------------------EEp read test-------------------------######################
	eepRead = readMem(EEP_START_ADD+24,24)
	if(eepRead):
		printApp("EEP Read Success")
		printApp(eepRead.hex())
	eepByte = [0xAA,0xAB,0xCC,0xAA,0xAB,0xCC,0xAA,0xAB,0xCC,0xAA,0xAB,0xCC]
	#####################-------------------------EEP write Test-------------------------######################
	if(writeMem(EEP_START_ADD+24,eepByte,12)):
		printApp("EEP write Su22ccess")
	eepRead = readMem(EEP_START_ADD+24,12)
	if(eepRead):
		printApp("EEP Read Success")
		printApp(eepRead.hex())

	#####################-------------------------Login and write keys to firebase and flash fectory image to device------------------------------#######################

	login("nan1ban@yahoo.com","123456")
	generateFactFiles()
	updateOTAImage(1)
	file = provisionDevice(1)

	picFlashDevice(1,file)

	#eepByte = [0xAA]
	#if( writeMem(0x7003, eepByte, 1) ):
		#printApp("EEP write Success")

#####################-------------------------OTA Test------------------------------#######################
#SEND PING, Check if its APP
#If APP...Send encrypted flash command....(if device is not FDR then key from EEPROM is used, else pass key from NVM to be used)
#UPDATE APP BIN FILE UPDATE
#####################-------------------------Commission Test-------------------------################

#####################-------------------------GetAccessTest-------------------------##################

#####################-------------------------Decommission Test-------------------------###############


#####################-------------------------Manual FDR Test-------------------------################
'''
# Create the parser
#With the prog keyword, you specify the name of the program that will be used in the help
#By default, the library uses the value of the sys.argv[0] element to set the name of the program, 
#which as you probably already know is the name of the Python script you have executed. However,
#you can specify the name of your program just by using the prog
my_parser = argparse.ArgumentParser(prog='tool',
									usage='%(prog)s command [option]',
									prefix_chars='-',
                                    description='CLI tool for Factory programming Connected Devices Version 1.0')

# Add the arguments
my_parser.add_argument("cmd", nargs='?', default="EMPTY",help=': fact_build, provision, post_img,all..\n e.g. arg.py factBuild -un useradmin@gmail.com -pw adminpassword -vc "Version Info"')
my_parser.add_argument("-pathProv", nargs='?', default="EMPTY",help='path of application image (device alredy provisiond) ')
my_parser.add_argument("-N", nargs='?', default="1",help='Number of est Cycles')
#my_parser.add_argument("-bootPath", nargs='?', default="EMPTY",help='path of boot image image.current directory if not provided')
#my_parser.add_argument("-devType", nargs='?', default="EMPTY",help='set Device type')
#my_parser.add_argument("-un", nargs='?',help='user name e.g. arg.py login -un useradmin@gmail.com -pw adminpassword')
#my_parser.add_argument("-pw", nargs='?',help='password')
#my_parser.add_argument("-vc", nargs='?',help='version comment')

listToStr = ' '.join(map(str, sys.argv))
writeLog(listToStr)

args = my_parser.parse_args()

writeLog("settings.ini")
config = configparser.ConfigParser()
config.sections()

# read values from a section
#string_val = config.get('section_a', 'string_val')
#bool_val = config.getboolean('section_a', 'bool_val')
#int_val = config.getint('section_a', 'int_val')
#float_val = config.getfloat('section_a', 'pi_val')
config.read('cred.ini')
username = config.get('Cred', 'username')
password = config.get('Cred', 'password')
config.read('settings.ini')
otaImageComment = config.get('Product', 'otaImageComment')
appPath = config.get('Product', 'appPath')
bootPath = config.get('Product', 'bootPath')
deviceModel = config.get('Product', 'deviceModel')
if(len(deviceModel)!=2):
	sys.exit(1)
pushOTAImageToServer = config.getint('Product', 'pushOTAImageToServer')
comport = config.get('Other', 'comport')
baudRate = config.getint('Other', 'baudRate')
timeout = config.getfloat('Other', 'timeout')
picProgVoltage = config.getfloat('Other', 'picProgVoltage')
picPartNo = config.get('Other', 'picPartNo')
cmdStatus = 1
writeLog("Executing Command "+  args.cmd)
if args.cmd == 'EMPTY':
    print('NO COMMAND')
elif args.cmd == 'factBuild': # build finial (combine APP and BL )image to be flashed
	if(ifFileExist(appPath) and ifFileExist(bootPath)):
		printI('building factory image...')
		generateFactFiles(deviceModel)
		if(0!=pushOTAImageToServer):
			if(username and password):
				fbLogin(username,password)
				if(fbLoginSucess()):
					printS('Login Success !!')
				else:
					printE('login Fail !!')
			else:
					printE('User name Password needed')
			if(fbLoginSucess() and otaImageComment):
				printI('Posting OTA image to server...')
				cmdStatus = pushOTAImageToServer(deviceModel,otaImageComment)
			else:
				printE('login to post image / version comment')
		else:
			cmdStatus = 0
	else:
		printE('boot.hex / app.hex do not exist')		
elif args.cmd == 'provision':      #flashes image to device and flashes SN
	if(ifFileExist("BootAppConfig.hex")):
		if(username and password):
			fbLogin(username,password)
			if(fbLoginSucess()):
				printS('Login Success !!')
			else:
				printE('login Fail !!')
				
		else:
			printE('User name Password needed')
		if(fbLoginSucess()):
			printI('provisioning device...')
			if(0==picEraseDevice(picPartNo,picProgVoltage)):
				file = provisionDevice(deviceModel)
				cmdStatus = picFlashDevice(picPartNo,file,picProgVoltage)
		else:
			printE('login to provision device')
	else:
		printE('BootAppConfig.hex do not exist')
elif args.cmd == 'provisionLocal':
	if(args.pathProv):
		cmdStatus = picFlashDevice(picPartNo,args.pathProv,picProgVoltage)
elif args.cmd == 'picFlashSaveKey': #only program program memory other data preserved
	if(args.pathProv):
		cmdStatus = picFlashSaveKey(picPartNo,args.pathProv,picProgVoltage)
elif ((args.cmd == 'commission') or (args.cmd == 'runTest')):      # capture and commition the device
	ser = setComPort(comport,baudRate,timeout)
	cycle = int(args.N)
	curCycl = 1
	stTest = 1;
	while(cycle>=curCycl):
		printH("Running Cycle Test, Iteration "+ str(curCycl) + " of " + args.N)
		stTest &= (0!=CmdSend('commission'))
		stTest &= (0!=CmdSend("sendAction"))
		stTest &= (0!=CmdSend("otaDevice"))  # no need to commition if we need to OTA the device
		stTest &= (0!=CmdSend("sendFdr"))
		time.sleep(2)
		stTest &= (0!=CmdSend("sendFdr"))
		time.sleep(2)
		stTest &= (0!=CmdSend("sendFdr"))
		time.sleep(2)
		stTest &= (0!=pingDevice())
		if(stTest==0):
			printE("Test Fail at Iteration "+ str(curCycl))
			break
		curCycl = curCycl + 1

	if(stTest):
		printS("All Test Pass")
		cmdStatus = 0
	else:
		cmdStatus = 1
	'''
	if(username and password):
		fbLogin(username,password)
		if(fbLoginSucess()):
			printS('Login Success !!')
		else:
			printE('login Fail !!')	
	else:
		printE('User name Password needed')
	if(fbLoginSucess()):
		printI('connecting device...')
		ser = setComPort(comport,baudRate,timeout)
		pingRes = pingDevice()
		if(isinstance(pingRes, bytes)):
			if(getSessionKey(pingRes,deviceModel)):
				printS("Session key generated!!")
				if(commision()):
					cmdStatus = 0
					pingRes = pingDevice()
					if(isinstance(pingRes, bytes)):
						if(getSessionKey(pingRes,deviceModel)):
							printS("Session key generated!!")
							sendAction()							
		elif(pingRes==1):
			printE("Device is in Bootloader..Failed")
		else:
			printE("Failed to Commission 2")
	else:
		printE('login to commision device')
	'''
	#commision()
	#action()
elif args.cmd == 'all':
    print('This tkes one munit...\ngenerating nessesory files...')
	#1) genrate factory image if not present, OTA and Factory image
	
	#2) Provision and flash the device
	
	#3) disconnect Programmer and connect power to USB COM port
	
	#4) ping device and validate we are in application (provision sucess!!!)
	
	#6) Commission the device (using fact key)...store both master and user keys
	
	#7) test Action command (with user key)
	
	#8) test config command (with master key)
	
	#9) send OTA command and send OTA image (OTA command -> Image -> app valid)
	
	#10) verify that we are in application after OTA
	
	#11) 7) test Action command (with user key) and 8) test config command (with master key)
	
	#12) Try sending Invalid Command or try sending Replay command...No Responce
	
	#12) Break OTA and try to recover device
	
	#13) Break OTA and FDR and try to recover device..
	
	#14) Test success!!!
	
else:
    printE(args.cmd + " is invalid command")

sys.exit(cmdStatus)
'''
if not os.path.isdir(input_path):
    print('The path specified does not exist')
    sys.exit()

print('\n'.join(os.listdir(input_path)))
'''