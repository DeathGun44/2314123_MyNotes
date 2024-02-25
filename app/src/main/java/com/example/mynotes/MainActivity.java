package com.example.mynotes;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText mloginemail,mloginpassword;
    private RelativeLayout mlogin,mgotosignup;
    private TextView mgotoforgotpassword;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mlogin=findViewById(R.id.login);
        mgotoforgotpassword=findViewById(R.id.gotoforgotpassword);
        mgotosignup=findViewById(R.id.gotosignup);
        mloginpassword=findViewById(R.id.loginpassword);
        mloginemail=findViewById(R.id.loginemail);
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser!=null){
            finish();
            startActivity(new Intent(MainActivity.this,mynotesActivity.class));
        }

        mgotosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, signup.class));
            }
        });

        mgotoforgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, forgotpassword.class));
            }
        });
        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail=mloginemail.getText().toString().trim();
                String password=mloginpassword.getText().toString().trim();

                if (mail.isEmpty()||password.isEmpty()) {
                    Toast.makeText(getApplicationContext(),"All fields Are Required",Toast.LENGTH_SHORT).show();
                }
                else {
                    //login the user
                    firebaseAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                checkmailverification();
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"Account doesn't Exist",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
    private void checkmailverification(){
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser.isEmailVerified()==true){
            Toast.makeText(getApplicationContext(),"Logged in",Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(MainActivity.this,mynotesActivity.class));
        }
        else {
            Toast.makeText(getApplicationContext(),"Verify your mail first",Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }
}
