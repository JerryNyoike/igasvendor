package codegreed_devs.com.igasvendor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import codegreed_devs.com.igasvendor.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    ProgressBar registeringBusiness;
    EditText etBusinessName, etBusinessEmail, etPassword;
    CheckBox termsAndCondiditions;
    Button btnRegister, btnLocation;
    TextView tvSignIn;
    String businessName, businessEmail, password;
    Location businesslocation;
    FirebaseAuth mAuth;
    FusedLocationProviderClient mFusedLocationClient;
    DatabaseReference mDatabaseReference;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        registeringBusiness = findViewById(R.id.registering);
        etBusinessName = findViewById(R.id.business_name);
        etBusinessEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.reg_password);
        termsAndCondiditions = findViewById(R.id.checkboxTerms);
        btnRegister = findViewById(R.id.btn_register);
        btnLocation = findViewById(R.id.fetch_location);
        tvSignIn = findViewById(R.id.sign_in);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    businesslocation = location;
                    break;
                }
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        };

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeringBusiness.setVisibility(View.VISIBLE);
                getBusinessLocation();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeringBusiness.setVisibility(View.VISIBLE);

                getBusinessDetails();

                if (validateUserData()) {
                    btnRegister.setClickable(false);
                    btnLocation.setClickable(false);
                    mAuth.createUserWithEmailAndPassword(businessEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                registeringBusiness.setVisibility(View.GONE);
                                //write user to the database

                            } else {
                                registeringBusiness.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                    });
                }


            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });


    }

    private boolean validateUserData() {

        if (businessName.isEmpty()) {
            etBusinessName.setError("Enter a business name");
            registeringBusiness.setVisibility(View.GONE);
            return false;
        } else if (businessEmail.isEmpty()) {
            etBusinessEmail.setError("Enter a business email");
            registeringBusiness.setVisibility(View.GONE);
            return false;
        } else if (password.length() < 6) {
            etPassword.setError("Password must be more than 6 characters!");
            registeringBusiness.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

    private void getBusinessDetails() {
        businessName = etBusinessName.getText().toString().trim();
        businessEmail = etBusinessEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
    }

    private void getBusinessLocation() {
        Log.e(TAG, "Location button pressed");
        //take the users location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                registeringBusiness.setVisibility(View.GONE);
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    Log.e(TAG, location.toString());
                } else {
                    //request location
                    if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                        return;
                    }
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getBusinessLocation();
        } else {
            Toast.makeText(this, "Allow app to have location permissions", Toast.LENGTH_LONG).show();
        }
    }
}

