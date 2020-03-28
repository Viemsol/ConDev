package com.example.cduser;

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
    public TextView TvSignup_temp;
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
        getSupportActionBar().setSubtitle("Login");
        //Auto login
        mAuthStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null)
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
                                Log.d(TAG,"Login Successful!!");
                                Intent i = new Intent(Login.this,MainActivity.class);

                                startActivity(i);
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

    }
}
