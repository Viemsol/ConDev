import pyrebase
import sys
import os
sys.path.append(os.path.realpath('..'))
def main():
    print "\nLog:\nTotal argument passed: " + str(len(sys.argv))
    for arg in sys.argv[1:]:
            print "Argument :" + arg
    if(len(sys.argv)>=5): # imagepath , type , version, image commant
        print "\n"
        dirname = os.path.dirname(__file__)
        config_fin_name = os.path.join(dirname, sys.argv[1])
        ### update OTA file
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
        Strpath=""
        db_key = ""
        db_val =""
        valid = 0
        if(int(sys.argv[2])==1): #type 1
            Strpath = "cdmasterStorage/FwImages/"+"CD_Img01_V"+sys.argv[3]+".OTA"
            db_key = "FirmwareMeta/DevTyp1/Image/Img"+sys.argv[3]
            db_val = "CD_Img01_V"+sys.argv[3]+".OTA%"+sys.argv[4] #image name + image comment   
            valid=1         
        if(valid>0):
            #----------------Update Image to Location 
            try:
                storage.child(Strpath).put(config_fin_name) #test1 is a path of file to beloaded and example.txt file name in database
            except:
                print "Error: File read error"
            # wite to storage 
            #----------------Update the Db with firmware image and Info
            results = db.child(db_key).set(db_val)
        else:
            print "Error: Type not supported"
    else:
        print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()