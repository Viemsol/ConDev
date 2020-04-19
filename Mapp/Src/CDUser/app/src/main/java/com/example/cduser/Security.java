package com.example.cduser;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.example.cduser.GLOBAL_CONSTANTS.ENCRIPTION_EN;

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
    private static final String TAG = "TAG_Security";
    public static void nen_enc_dec(byte [] data,byte fink)
    {
        byte data_len = (byte)data.length;  //3
        data_len--; // exclude command from encription  2
        while(data_len!=0)
        {
            data[((int)data_len)] ^= fink;                   // data[2]
            data_len--; //  1
            fink ^= data_len;
        }
    }
    public static byte CRC_Chk(byte[] data, int len , int cmd_crc_calc,byte mac_key)
    {

        byte st=0;
        int sum = 0,i=0;
        while(len>0)
        {
            sum+=data[i];
            i++;
            len--;
        }
        if(ENCRIPTION_EN == 1)
        {
            sum ^= mac_key;
        }
        if(cmd_crc_calc == 1) // CRC calculate
        {
            Log.d(TAG,"Checksum :" + (byte)sum);
            return((byte)sum);
        }

        if(((byte)sum) == data[i])
        {
            st =1;
            Log.d(TAG,"Checksum matching ");
        }
        Log.d(TAG,"Checksum" + sum);
        return(st);
    }

    public static String  EncodeStrBase64(String Data_in)
  {
      byte[] data = new byte[0];
      try {
          data = Data_in.getBytes(StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }
      return(Base64.encodeToString(data, Base64.DEFAULT));

  }
    public static String  DecodeStrBase64(String Data_in)
    {
        byte[] decPw = Base64.decode(Data_in, Base64.DEFAULT);
        return(decPw.toString());
    }

    public static String EncryptString(String Data_in,String Key)
    {
        String out;
        Log.d(TAG, "EncryptionKey:"+Key);
        byte[] tmp_key = ServLib.GetKey(Key);
        Log.d(TAG, "EncryptionKey(128 bit):"+ Arrays.toString(tmp_key) );
        byte[] tmp_data = Data_in.getBytes();
        Log.d(TAG, Arrays.toString(tmp_data) );
        try{
            byte [] tmp_out =ServLib.encrypt(tmp_key,tmp_data);
            Log.d(TAG, "EncryptedData:"+Arrays.toString(tmp_out) );
            out = (Base64.encodeToString(tmp_out, Base64.DEFAULT));
        }
        catch (Exception e)
        {
            out ="";
        }
        return out;
    }
    public static String DecryptString(String Data_in,String Key)
    {
        String out;
        Log.d(TAG, "Decryptionkey:"+Key);
        byte[] tmp_key = ServLib.GetKey(Key);
        Log.d(TAG, "DecryptionKey(128 bit)"+ Arrays.toString(tmp_key) );
        byte[] tmp_data = Base64.decode(Data_in, Base64.DEFAULT);

        try{
            byte[] tmp_out = ServLib.decrypt(tmp_key,tmp_data);
            Log.d(TAG, "DecryptedData:"+  Arrays.toString(tmp_out) );
            String Temp = new String(tmp_out);
            Log.d(TAG, Temp);
            out = Temp;
        }
        catch (Exception e)
        {
            out ="";
        }
        return out;
    }
}
