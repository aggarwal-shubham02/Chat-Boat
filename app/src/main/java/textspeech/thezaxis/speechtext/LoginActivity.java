package textspeech.thezaxis.speechtext;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import textspeech.thezaxis.speechtext.Helper.VolleyCallBack;
import textspeech.thezaxis.speechtext.Helper.VolleyRequest;

public class LoginActivity extends AppCompatActivity {

    private EditText editPhone, editCode;
    private Button buttonSendCode, buttonSignIn;
    private FirebaseAuth mAuth;
    int customerCount;
    String codeSent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editCode = findViewById(R.id.editTextCode);
        editPhone = findViewById(R.id.editTextPhone);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
        buttonSendCode = findViewById(R.id.buttonGetVerificationCode);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        mAuth = FirebaseAuth.getInstance();
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });
        buttonSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUserExist(editPhone.getText().toString());
            }
        });
    }

    private void isUserExist(String phone) {
        VolleyRequest request1 = new VolleyRequest();

        String query = "select count(*) as count from Customer where CustomerContact ="+phone;
        request1.fetchData(new VolleyCallBack() {
            @Override
            public void onSuccess(JSONArray resultArray) {
                try {
                    JSONObject obj = resultArray.getJSONObject(0);
                     customerCount= obj.getInt("count");
                    if (customerCount==1){
                        sendVerificationCode();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },"http://www.skyline69.co.nf/request.php?query=",  this, query);
    }

    private void verifySignInCode() {
        String code = editCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //here you can open new activity
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        String phone = editPhone.getText().toString();

        if(phone.isEmpty()){
            editPhone.setError("Phone number is required");
            editPhone.requestFocus();
            return;
        }

        if(phone.length() < 10 ){
            editPhone.setError("Please enter a valid phone");
            editPhone.requestFocus();
            return;
        }


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(LoginActivity.this, "Code Sent: "+s, Toast.LENGTH_SHORT).show();
            codeSent = s;
        }
    };
}
