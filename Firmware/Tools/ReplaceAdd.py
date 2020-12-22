#C:\Python27\python.exe C:\Users\acer\Desktop\MASTER_DOCUMENT_V1\START_UP\Batchfile_Python\Run.py a.txt b.txt
#!/usr/bin/python
#this file update shared memory locations address  in boot loader build to the application file
import sys
import os
import string
sys.path.append(os.path.realpath('..'))
def main():
	print( "-------------Replacing App Function Addressfrom Boot loader -----------\nLog:\nTotal argument passed: " + str(len(sys.argv)))
	count = 0
	if(len(sys.argv)>=3):
		for arg in sys.argv[1:]:
			print( "Argument :" + arg)
		# extecting the Share function addresses from 
		print( "\n")
		dirname = os.path.dirname(__file__)
		fin_name = os.path.join(dirname, sys.argv[1])
		fout_name = os.path.join(dirname, sys.argv[2])
		fin = open(fin_name, "rt")
		fout = open(fout_name, "rt")
		data_temp = fout.read()
		fout.close()
		fout_name = os.path.join(dirname, sys.argv[2])
		error = 0
		check_str = ['_Bt_WriteEep','_Bt_FlashWriteBlock','_Bt_ReadData']
		for line1 in fin:
			if(line1.startswith(tuple(check_str))):
				list1_temp = line1.split()
				print("Replacing address for :" + list1_temp[0] + " to " + list1_temp[2])
				found = 0
				for line2 in data_temp.split('\n'):
					if(line2.startswith('extern void') and  (list1_temp[0][1:] in line2)): # test to remoe _ from function name
						found = 1
						list2_temp = line2.split()
						line2tmp = line2.replace(list2_temp[4], "0x" + list1_temp[2] + ";")
						data_temp = data_temp.replace(line2,line2tmp)
						count = count + 1
						break
				if(found == 0):
					print( "Error ,Not found in output file : " + list1_temp[0])
					error = 1
		if(error==0):
			fout = open(fout_name, "wt")
			fout.write(data_temp)
			fout.close()
			print( "Total Address Replaced " + str(count) + "\nSuccess !!!")
		fin.close()
	else:
		print( "Argument length not correct !!! Required 3 Arguments")
		
if __name__ == "__main__":
    main()