package codegreed_devs.com.igasvendor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    ProgressBar registeringBusiness;
    EditText etBusinessName, etBusinessEmail, etPassword;
    CheckBox termsAndCondiditions;
    Button btnRegister;
    TextView tvSignIn;
    FirebaseAuth mAuth;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        registeringBusiness = findViewById(R.id.registering);
        etBusinessName = findViewById(R.id.business_name);
        etBusinessEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.reg_password);
        termsAndCondiditions = findViewById(R.id.checkboxTerms);
        btnRegister = findViewById(R.id.btn_register);
        tvSignIn = findViewById(R.id.sign_in);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                }

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeringBusiness.setVisibility(View.VISIBLE);
                String businessName = etBusinessName.getText().toString().trim();
                String businessEmail = etBusinessEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if(businessEmail.isEmpty() || businessEmail.equals("")){
                    etBusinessEmail.setError("Enter a business email");
                } else if (businessName.isEmpty()){
                    etBusinessName.setError("Enter a business name");
                } else if ( password.isEmpty() ){
                    etPassword.setError("Enter etPassword");
                }

                mAuth.createUserWithEmailAndPassword(businessEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            registeringBusiness.setVisibility(View.GONE);
                            //redirect to home activity
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                    }
                });
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        registeringBusiness.setVisibility(View.GONE);
//                        startActivity(new Intent(getApplicationContext(), Home.class));
//                    }
//                }, Constants.SPLASH_TIME_OUT);

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
