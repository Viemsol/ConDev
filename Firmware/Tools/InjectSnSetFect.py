### C:\Users\acer\Desktop\_GitConDev\Firmware\Tools>C:\Python27\python.exe C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\InjectSnSetFect.py C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\test.txt 5678
import sys
import os
import string
from binascii import hexlify
sys.path.append(os.path.realpath('..'))
snpattern = ':02E1F8'
fectorypattern = ':02E1FC'
def split_by_n( seq, n ):
    """A generator to divide a sequence into chunks of n units."""
    while seq:
        yield seq[:n]
        seq = seq[n:]
        
def FectInjectSn(file_path,Sn):
    #prepare injectant
    
    ###checksum = 0xDF + ord(Sn[0]) + ord(Sn[1]) + ord(Sn[2]) + ord(Sn[3])
    Sn =  hexlify(Sn)
    New_Hex_File_Path=os.path.splitext(file_path)[0]
    New_Hex_File_Path=New_Hex_File_Path + "Factory.hex"
    thefile = open(New_Hex_File_Path, 'w')
    try:
        f = open(file_path,'r')
        for line in f:
            if snpattern in line:
                ###TODO : replace char in string
                snLine = ":08E1F000" + Sn[0:2] + "34" + Sn[2:4] + "34" + Sn[4:6] + "34" + Sn[6:8] + "34CC"
                a = snLine[1:(len(snLine)-2)]
                b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
                c = [int(i, 16) for i in b] # [16, 248, 0, ...
                d = 256 - sum(c) % 256 # 0x30
                e = hex(d)[2:] # '30'
                if(len(e)==1):
                    e = '0' + e
                snLine = ':' + a + e.upper()
                print "Injecting SN" + snLine
                thefile.write(snLine + '\n' + line)
            elif fectorypattern in line:#adding Factory test complete
                line2 = ":02E1FE000134CC"
                a = line2[1:(len(line2)-2)]
                b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
                c = [int(i, 16) for i in b] # [16, 248, 0, ...
                d = 256 - sum(c) % 256 # 0x30
                e = hex(d)[2:] # '30'
                if(len(e)==1):
                    e = '0' + e
                line2 = ':' + a + e.upper()
                print "Injecting Factory Complete" + line2
                thefile.write(line + line2+'\n')
            else:
                thefile.write(line)
    finally:
        f.close()

def main():
    print "\nLog:\nTotal argument passed: " + str(len(sys.argv))
    for arg in sys.argv[1:]:
            print "Argument :" + arg
    if(len(sys.argv)>=3):
        Sn = (sys.argv[2])
        print "\n"
        dirname = os.path.dirname(__file__)
        fin_name = os.path.join(dirname, sys.argv[1])
        print("Reading Hex File Entered : " + fin_name)
        FectInjectSn(fin_name,Sn)
    else:
        print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()