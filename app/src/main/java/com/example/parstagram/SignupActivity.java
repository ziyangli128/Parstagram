package com.example.parstagram;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";

    EditText etSignupEmail;
    EditText etSignupUsername;
    EditText etSignupPassword;
    Button btnSignupSubmit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupUsername = findViewById(R.id.etSignupUsername);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        btnSignupSubmit = findViewById(R.id.btnSignupSubmit);

        btnSignupSubmit.setOnClickListener(new View.OnClickListener() {
            ParseUser user = new ParseUser();
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: Signup submit button");
                user.setEmail(etSignupEmail.getText().toString());
                user.setUsername(etSignupUsername.getText().toString());
                user.setPassword(etSignupPassword.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue with signup", e);
                            Toast.makeText(SignupActivity.this,
                                    "Signup failed! Please enter valid information!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                });
            }
        });
    }
}
