package com.example.cdmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

public class Login extends AppCompatActivity {

    final private String TAG = "TAG_Login_Thread";

    public EditText EtEmail_temp,EtPassword_temp;
    public Button ButLogin_temp;
    public TextView TvSignup_temp,TvForgotPw_temp;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;



    @Override
    protected void onStart()
    {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    public  void onBackPressed()
    {
        //

        // Alert user and take input
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Login.this);
        alertDialogBuilder.setMessage("Want to Exit ?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                Login.this.finish();
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
    public  void onPause()
    {
        super.onPause();
        Login.this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // this is to avoid restart of activiry/app on orientation change

        mFirebaseAuth = FirebaseAuth.getInstance();
        EtEmail_temp = findViewById(R.id.EtEmail);
        EtPassword_temp = findViewById(R.id.EtPassword);
        ButLogin_temp = findViewById(R.id.ButLogin);
        TvSignup_temp = findViewById(R.id.TvSignup);
        TvForgotPw_temp = findViewById(R.id.TvForgotPw);

        String title = getString(R.string.app_name) + "   (Ver: " +BuildConfig.VERSION_NAME +")";
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle("Login");
        //set alert
        // create alert window
        AlertDialog.Builder alertDialogBuilder_email = new AlertDialog.Builder(this);
        alertDialogBuilder_email.setMessage("Email Not Verified !\nVerify your email by clicking link sent");
        alertDialogBuilder_email.setPositiveButton("Resend Email", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mFirebaseAuth.getCurrentUser().sendEmailVerification();

            }
        });
        alertDialogBuilder_email.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // finish();
            }
        });

        final AlertDialog alertDialog_emailver = alertDialogBuilder_email.create();

        //Auto login
        mAuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if((mFirebaseUser != null)&& (mFirebaseUser.isEmailVerified()))
                {
                    Log.d(TAG,"You are Logged in");
                    Intent i = new Intent(Login.this, MainActivity.class);
                    startActivity(i);
                }
                else
                {
                    Log.d(TAG,"Please sign in");
                }
            }
        };
        // log in
        ButLogin_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String _emailid = EtEmail_temp.getText().toString();
                String _password = EtPassword_temp.getText().toString();

                if(_emailid.isEmpty() || _password.isEmpty())
                {
                    Toast.makeText(Login.this,"Email/Password empty!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mFirebaseAuth.signInWithEmailAndPassword(_emailid,_password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                            {
                                Log.d(TAG,"Login Fail!!");

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
                                    Log.d(TAG,""+e);
                                }
                                Toast.makeText(Login.this,"Email/Password Format !", Toast.LENGTH_SHORT).show();

                            }
                            else
                            {
                                if(mFirebaseAuth.getCurrentUser().isEmailVerified()) {
                                    Log.d(TAG, "Login Successful!!");
                                    Intent i = new Intent(Login.this, MainActivity.class);

                                    startActivity(i);
                                }
                                else
                                {
                                    alertDialog_emailver.show();
                                }
                            }
                        }
                    });
                }

            }
        });

        TvSignup_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this,Signup.class);
                startActivity(i);
            }
        });

        TvForgotPw_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String _emailid = EtEmail_temp.getText().toString();

                if(_emailid.isEmpty() )
                {
                    Toast.makeText(Login.this,"Email not valid!", Toast.LENGTH_SHORT).show();
                }
                else {

                    // Alert user and take input
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Login.this);
                    alertDialogBuilder.setMessage("Want to reset your Password ?");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mFirebaseAuth.sendPasswordResetEmail(_emailid)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Login.this,"Email sent to reset Password..", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

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

            }
        });




    }


}
