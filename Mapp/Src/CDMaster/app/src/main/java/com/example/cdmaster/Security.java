package com.example.cdmaster;

public class Security
{
    // ping the device and send the K1
    // get K2 in responce
    // calculate fink = sekretkey + K1^K2
    // this key will be valid for current session
    // Device  will expier key if no communication for 5 seconds
    // Radom key for every session  will protect from reply attack
    // broute forse attack is dificult as , for any wrong crc session wil expie and new session request to be made

    // function for encription and decription purpose , fink to be calulated as above
    public static void nen_enc_dec(byte [] data,byte fink)
    {
        byte data_len = (byte)data.length;  //3
        data_len--; // exclude command from encription  2
        while(data_len!=0)
        {
            data_len--;                            //  1
            data[((int)data_len+1)] ^= fink;                   // data[1]
            fink ^= data_len;

        }
    }

}
