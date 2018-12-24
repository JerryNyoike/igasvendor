package codegreed_devs.com.igasvendor.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private EditText logEmail, logPassword;
    private Button signIn;
    private TextView signUp;
    ProgressBar loadLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize views
        logEmail = (EditText)findViewById(R.id.log_email);
        logPassword = (EditText)findViewById(R.id.log_password);
        signIn = (Button)findViewById(R.id.login);
        signUp = (TextView)findViewById(R.id.sign_up);
        loadLogin = (ProgressBar)findViewById(R.id.load_login);

        //handle item clicks
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLogin.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadLogin.setVisibility(View.GONE);
                        startActivity(new Intent(getApplicationContext(), Home.class));
                    }
                }, Constants.SPLASH_TIME_OUT);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

    }
}
