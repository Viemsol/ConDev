### C:\Users\acer\Desktop\_GitConDev\Firmware\Tools>C:\Python27\python.exe C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\InjectSnSetFect.py C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\test.txt 5678
import sys
import os
import string
from binascii import hexlify
sys.path.append(os.path.realpath('..'))
DEV_TYP_PETTERN='#define DEVICE_TYP'
otacrcPattern = '2 241 240'
##eeprom version tag in combined hex file, we will serch for this tag to insert checksum and app valid above it
snpattern = ':02E1E4'
def split_by_n( seq, n ):
    """A generator to divide a sequence into chunks of n units."""
    while seq:
        yield seq[:n]
        seq = seq[n:]
        
def FectInjectCRCAppValid(file_path,otafile,Device_Typ):
    #prepare injectant
    tempChecksum_in = ""
    tempDevTyp = hex(Device_Typ).split('x')[-1]
    if(len(tempDevTyp)==1):
        tempDevTyp = '0' + tempDevTyp
    print "Adding Device Type value 0x" + tempDevTyp
    try:
        f = open(otafile,'r')
        for line in f:
            if otacrcPattern in line:
                tempChecksum_in = int(line.split()[3])
                tempChecksum_in = hex(tempChecksum_in).split('x')[-1]
                if(len(tempChecksum_in)==1):
                    tempChecksum_in = '0' + tempChecksum_in
                print "Adding Checksum value 0x" + tempChecksum_in
    finally:
        f.close()
    temp = ""
    ###checksum = 0xDF + ord(Sn[0]) + ord(Sn[1]) + ord(Sn[2]) + ord(Sn[3])
    try:
        f = open(file_path,'r')
        for line in f:
            if snpattern in line:
                crc = ':04E1E000'+tempDevTyp+'34'+tempChecksum_in+'34CC'
                a = crc[1:(len(crc)-2)]
                b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
                c = [int(i, 16) for i in b] # [16, 248, 0, ...
                d = 256 - sum(c) % 256 # 0x30
                e = hex(d)[2:] # '30'
                if(len(e)==1):
                    e = '0' + e
                crc = ':' + a + e.upper()
                crc2 = ':02E1F800'+tempDevTyp+'34CC'
                a = crc2[1:(len(crc2)-2)]
                b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
                c = [int(i, 16) for i in b] # [16, 248, 0, ...
                d = 256 - sum(c) % 256 # 0x30
                e = hex(d)[2:] # '30'
                if(len(e)==1):
                    e = '0' + e
                crc2 = ':' + a + e.upper()
                temp = temp + crc + '\n' + line+crc2+'\n'              
            else:
                temp = temp + line
        f.close()
        f = open(file_path,'w')     
        f.write(temp)
        f.close()
    finally:
        f.close()

def main():
    print "\nLog:\nTotal argument passed: " + str(len(sys.argv))
    for arg in sys.argv[1:]:
            print "Argument :" + arg
    if(len(sys.argv)>=4):
        print "\n"
        dirname = os.path.dirname(__file__)
        config_fin_name = os.path.join(dirname, sys.argv[3] + "\Src\system_config.h")
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
        fin_name = os.path.join(dirname, sys.argv[1])
        print("Reading File  : " + fin_name)
        dirname = os.path.dirname(__file__)
        otafile = os.path.join(dirname, sys.argv[2])
        print("Reading File  : " + otafile)
        FectInjectCRCAppValid(fin_name,otafile,Device_Typ)
    else:
        print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()