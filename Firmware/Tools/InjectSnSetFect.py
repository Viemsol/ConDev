### C:\Users\acer\Desktop\_GitConDev\Firmware\Tools>C:\Python27\python.exe C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\InjectSnSetFect.py C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\test.txt 5678
import sys
import os
import string
import pyrebase
from binascii import hexlify
sys.path.append(os.path.realpath('..'))
snpattern = ':02E1F8'
fectorypattern = ':02E1FC'
def split_by_n( seq, n ):
    """A generator to divide a sequence into chunks of n units."""
    while seq:
        yield seq[:n]
        seq = seq[n:]
        
def FectInjectSn(file_path,Sn,Pw):
    #prepare injectant
    
    ###checksum = 0xDF + ord(Sn[0]) + ord(Sn[1]) + ord(Sn[2]) + ord(Sn[3])
    b = bytearray(Pw[0:4],'utf-8')
    pwsum = (b[0] + b[1] + b[2] +b[3])&0xFF
    Pw = hexlify(Pw[:4])
    Sn =  hexlify(Sn)
    pwsum = hex(pwsum)[2:]
    print("PW Sum:"+pwsum)
    if(len(pwsum)==1):
        pwsum = '0' + pwsum
    New_Hex_File_Path=os.path.splitext(file_path)[0]
    New_Hex_File_Path=New_Hex_File_Path + "Factory.hex"
    thefile = open(New_Hex_File_Path, 'w')
    try:
        f = open(file_path,'r')
        for line in f:
            if snpattern in line:
                ###TODO : replace char in string
                
                pwLine = ":0AE1D000" + Pw[0:2] + "34" + Pw[2:4] + "34" + Pw[4:6] + "34" + Pw[6:8] + "34" +pwsum+"34CC"
                a = pwLine[1:(len(pwLine)-2)]
                b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
                c = [int(i, 16) for i in b] # [16, 248, 0, ...
                d = 256 - sum(c) % 256 # 0x30
                e = hex(d)[2:] # '30'
                if(len(e)==1):
                    e = '0' + e
                pwLine = ':' + a + e.upper()
                print "Injecting PW" + pwLine
                
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
                
                thefile.write(snLine + '\n' + pwLine + '\n' +line)
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
        config = {
          "apiKey": "YOUR_API_KEY",
          "authDomain": "cdmaster-f51fc.firebaseapp.com",
          "databaseURL": "https://cdmaster-f51fc.firebaseio.com",
          "storageBucket": "cdmaster-f51fc.appspot.com",
          ### "serviceAccount": "path/to/serviceAccountCredentials.json" ### this is needed to autenticate as admin
        }
        
        firebase = pyrebase.initialize_app(config)

        # Get a reference to the auth service
        auth = firebase.auth()

        # Log the user in
        user = auth.sign_in_with_email_and_password("EMAIL", "PW")
        
        # Get a reference to the database service
        db = firebase.database()
        #update in Storage
        storage = firebase.storage()
        # as admin
        
        db_key = "FirmwareMeta/DevKey/"+sys.argv[2]
        Pw = db.child(db_key).get().val()
        print "password:"+Pw
        FectInjectSn(fin_name,Sn[2:6],Pw)
    else:
        print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()