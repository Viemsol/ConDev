### C:\Users\acer\Desktop\_GitConDev\Firmware\Tools>C:\Python27\python.exe C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\InjectSnSetFect.py C:\Users\acer\Desktop\_GitConDev\Firmware\Tools\test.txt 5678
import sys
import os
import string
from binascii import hexlify
sys.path.append(os.path.realpath('..'))
snpattern = ':0AE1F0'
fectorypattern = ':02E1FC'
def split_by_n( seq, n ):
	"""A generator to divide a sequence into chunks of n units."""
	while seq:
		yield seq[:n]
		seq = seq[n:]
		
def FectInjectSn(file_path,Sn,DevTyp):
	#prepare injectant
	
	###checksum = 0xDF + ord(Sn[0]) + ord(Sn[1]) + ord(Sn[2]) + ord(Sn[3])
	Sn =  hexlify(Sn)
	tempDevTyp = hex(DevTyp).split('x')[-1]
	if(len(tempDevTyp)==1):
	   tempDevTyp = '0' + tempDevTyp
	New_Hex_File_Path=os.path.splitext(file_path)[0]
	New_Hex_File_Path=New_Hex_File_Path + "Factory.hex"
	thefile = open(New_Hex_File_Path, 'w')
	try:
		f = open(file_path,'r')
		for line in f:
			if snpattern in line:
				snsubst = ':0AE1F000'+ Sn[0:2] +'34'+ Sn[2:4] +'34'+ Sn[4:6] +'34'+ Sn[6:8] + '34'+ tempDevTyp +'34'+ 'CC'
				a = snsubst[1:(len(snsubst)-2)]
				b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
				c = [int(i, 16) for i in b] # [16, 248, 0, ...
				d = 256 - sum(c) % 256 # 0x30
				e = hex(d)[2:] # '30'
				snsubst = ':' + a + e
				print snsubst
				thefile.write(snsubst+'\n')
			elif fectorypattern in line:
				line2 = line[0:-3] + '0134CC'
				a = line2[1:(len(line2)-2)]
				b = [a[i:i+2] for i in range(0, len(a), 2)] # ['10', 'F8', '00', ...
				c = [int(i, 16) for i in b] # [16, 248, 0, ...
				d = 256 - sum(c) % 256 # 0x30
				e = hex(d)[2:] # '30'
				line2 = ':' + a + e
				
				new = list(line2)
				new[2] = '4'
				new = ''.join(new)
				print new
				thefile.write(new+'\n')
			else:
				thefile.write(line)
	finally:
		f.close()

def main():
	print "\nLog:\nTotal argument passed: " + str(len(sys.argv))
	for arg in sys.argv[1:]:
			print "Argument :" + arg
	if(len(sys.argv)>=4):
		Sn = (sys.argv[2])
		DevTyp = int(sys.argv[3])
		print "\n"
		dirname = os.path.dirname(__file__)
		fin_name = os.path.join(dirname, sys.argv[1])
		print("Hex File Entered : " + fin_name)
		FectInjectSn(fin_name,Sn,DevTyp)
	else:
		print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()