package com.example.cduser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;

public class Signup extends AppCompatActivity {

    final private String TAG = "TAG_Signup_Thread";

    public EditText EtEmail_temp,EtPassword_temp,EtPasswordRe_temp,EtName_temp,EtPhoneNo_Temp;
    public Button ButSignup_temp;
    public TextView TvSignin_temp;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference TempRef;



    @Override
    public  void onBackPressed()  // this is done to kill the activity on back button press
    {
        // Alert user and take input
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Signup.this);
        alertDialogBuilder.setMessage("Want to Exit ?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                Signup.this.finish();
            }
        });
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // send command
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @Override
    public  void onPause()   // this is done to kill the activity on finish
    {
        super.onPause();
        Signup.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is to avoid restart of activiry/app on orientation change

        getSupportActionBar().setSubtitle("Sign Up");
        mFirebaseAuth = FirebaseAuth.getInstance();
        EtEmail_temp = findViewById(R.id.EtEmail);
        EtPassword_temp = findViewById(R.id.EtPassword);
        EtPasswordRe_temp = findViewById(R.id.EtPasswordRe);
        EtName_temp = findViewById(R.id.EtName);
        EtPhoneNo_Temp = findViewById(R.id.EtPhoneNo);

        ButSignup_temp = findViewById(R.id.ButSignup);
        TvSignin_temp = findViewById(R.id.TvSignin);

        //signing up user
        ButSignup_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String _emailid = EtEmail_temp.getText().toString();
                final String _password = EtPassword_temp.getText().toString();
                final String _passwordRe = EtPasswordRe_temp.getText().toString();
                final String _name = EtName_temp.getText().toString();
                final String _phoneNo = EtPhoneNo_Temp.getText().toString();
                if(_emailid.isEmpty() || _password.isEmpty() || _passwordRe.isEmpty() || _name.isEmpty() ||_phoneNo.isEmpty() || !(_password.equals(_passwordRe)))
                {
                    Toast.makeText(Signup.this,"Email/Password/Name/Phone format Incorrect OR empty!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.d("TAG","creating user");
                    mFirebaseAuth.createUserWithEmailAndPassword(_emailid,_password).addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                            {
                                Log.d(TAG,"Login Fail!!"); // nan1banyahoo.com , nandyahoo ,,,https://www.youtube.com/watch?v=4DTgE7qbD_8&list=PLgCYzUzKIBE_cyEsXgIcwC3P8ipvlSFd_&index=3
                                int error_st=0;
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    // log error here
                                    error_st =1; //user exist
                                    Log.d(TAG,""+e);


                                } catch (FirebaseNetworkException e) {
                                    // log error here
                                    Log.d(TAG,""+e);
                                } catch (Exception e) {
                                    // log error here
                                    Log.d(TAG,""+e);
                                }
                                if(error_st!=0)
                                {
                                    Toast.makeText(Signup.this,"User Already Exist!", Toast.LENGTH_SHORT).show();

                                }
                                else
                                {
                                    Toast.makeText(Signup.this,"Email/Password/Name Format !", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else
                            {
                                Log.d(TAG,"Login Successful!!");
                                // create suer profile
                                FirebaseUser user_tmp = mFirebaseAuth.getCurrentUser();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(_name).build();

                                user_tmp.updateProfile(profileUpdates);

                                String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                TempRef = FirebaseDatabase.getInstance().getReference("User");
                                // Encode data on your side using BASE64
                                byte[] data = new byte[0];
                                try
                                {
                                    data = _password.getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                String base64Pw = Base64.encodeToString(data, Base64.DEFAULT);

                                // Decoding password
                                byte[] decPw = Base64.decode(base64Pw, Base64.DEFAULT);
                                String pasword = decPw.toString();

                                UserProfileDb User_data = new UserProfileDb(UID,_name,_emailid,base64Pw,_phoneNo);
                                TempRef.child(UID).child("UserInfo").setValue(User_data).addOnCompleteListener(new OnCompleteListener<Void>() {

                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful())
                                        {

                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthUserCollisionException e) {
                                                // log error here
                                                Log.d(TAG,""+e);

                                            } catch (FirebaseNetworkException e) {
                                                // log error here
                                                Log.d(TAG,""+e);
                                            } catch (Exception e) {
                                                // log error here
                                                Log.d("TAG_err2",""+e);
                                            }
                                            Log.d(TAG,"User write fail");
                                            Toast.makeText(Signup.this, "Failed to Create User", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Log.d(TAG,"Log_created Success! ");
                                            startActivity(new Intent(Signup.this,MainActivity.class));
                                        }
                                    }
                                });


                            }
                        }
                    });
                }
            }
        });

        TvSignin_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(Signup.this,Login.class);
                startActivity(i);
            }
        });

    }
}
