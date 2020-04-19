package com.example.cduser;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServLib
{

    // all service related / convertion / encription functions
    public static final String TAG = "TAG_ServLib";

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String display(byte[] b1) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte val : b1) {
            strBuilder.append(String.format("%02x", val & 0xff));
        }
        return strBuilder.toString();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static int get_crc(byte[] data_in)
    {
        CRC32 crc = new CRC32();
        crc.update(data_in);
        return((int)crc.getValue());
    }
    public static byte[] GetKey (String raw_key)
    {
        byte[] keyStart = raw_key.getBytes();
        return (Arrays.copyOfRange(keyStart, 0, 16));
    }

    public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {

        IvParameterSpec ivSpec = new IvParameterSpec(raw); // use key as init vector
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec,ivSpec);
       return cipher.doFinal(clear);

    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {

        IvParameterSpec ivSpec = new IvParameterSpec(raw);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec,ivSpec);
        return  cipher.doFinal(encrypted);
    }

    static public int cmpTime(String time, String endtime) {

        String pattern = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(time);
            Date date2 = sdf.parse(endtime);
            Log.d(TAG,"Time 1:" + date1);
            Log.d(TAG,"Time 2:" + date2);
            if(date1.equals(date2))
            {
                return 0;
            }
            else if(date1.before(date2))
            {
                return 1;
            }
            else {

                return -1;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return -1;
    }

    static public int cmpDate(String time, String endtime) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date1 = sdf.parse(time);
            Date date2 = sdf.parse(endtime);

            if(date1.equals(date2))
            {
                Log.d(TAG,"Date Equal");
                return 0;

            }
            else if(date1.before(date2))
            {
                Log.d(TAG,"Date 1 is less then date 2");
                return 1;
            }
            else {
                Log.d(TAG,"Date 1 is greater then date 2");
                return -1;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return -1;
    }

    static public String GetDate()
    {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day

        return (mDay + "/"
                + (mMonth + 1) + "/" + mYear);
    }

    static public String GetTime()
    {
        final Calendar c = Calendar.getInstance();

        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int  mMinute = c.get(Calendar.MINUTE);
        return (mHour + ":" + mMinute);
    }
    public static void getRandomOfSize(byte[] data)
    {
        int len = data.length;
        if(len>0)
        {
            len--;
            data[len] = (byte)getNZRandom();
        }
        Log.d(TAG,"Random Array:"+ Arrays.toString(data));
    }
   public static int getNZRandom()
   {
       Random r = new Random();
       int tmpRand ;
       do
       {
           tmpRand =(r.nextInt(255));
       } while(tmpRand==0);
       Log.d(TAG,"Rand Int :"+tmpRand);
       return tmpRand;

   }
    public static String generateMaapUUID()
    {
        byte[] data = {0,0,0,0,0,0};
        getRandomOfSize(data);
       return(Arrays.toString(data).replace(", ", ""));
    }
}
