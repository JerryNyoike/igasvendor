package codegreed_devs.com.igasvendor.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;
import codegreed_devs.com.igasvendor.utils.Utils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        }, Constants.SPLASH_TIME_OUT);

    }

    private void updateUI() {

        Intent login = new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent home = new Intent(getApplicationContext(), Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (Utils.isFirstLogin(getApplicationContext()))
        {
            startActivity(login);
            finish();
        }
        else
        {
            startActivity(home);
            finish();
        }

    }
}
