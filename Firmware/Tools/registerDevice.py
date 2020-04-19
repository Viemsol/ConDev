import pyrebase
import sys
import os
import string
import random
sys.path.append(os.path.realpath('..'))
def randomString(stringLength):
    letters = string.ascii_lowercase + string.digits + string.ascii_uppercase
    return ''.join(random.choice(letters) for i in range(stringLength))
def main():
    print "\nLog:\nTotal argument passed: " + str(len(sys.argv))
    for arg in sys.argv[1:]:
            print "Argument :" + arg
    if(len(sys.argv)>=1): # imagepath , type , version, image commant
        print "\n"
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
        
        db_key = "FirmwareMeta/DevKey/"+sys.argv[1]
        db_val = randomString(16) #image name + image comment     
        results = db.child(db_key).set(db_val)
    else:
        print "Failed invalid argument length !!!"
if __name__ == "__main__":
    main()