package codegreed_devs.com.igasvendor.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    ProgressBar registeringBusiness;
    EditText businessName, businessEmail, password;
    CheckBox termsAndCondiditions;
    Button btnRegister;
    TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registeringBusiness = findViewById(R.id.registering);
        businessName = findViewById(R.id.business_name);
        businessEmail = findViewById(R.id.email);
        password = findViewById(R.id.reg_password);
        termsAndCondiditions = findViewById(R.id.checkboxTerms);
        btnRegister = findViewById(R.id.btn_register);
        tvSignIn = findViewById(R.id.sign_in);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeringBusiness.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        registeringBusiness.setVisibility(View.GONE);
                        startActivity(new Intent(getApplicationContext(), Home.class));
                    }
                }, Constants.SPLASH_TIME_OUT);

            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });


    }
}
