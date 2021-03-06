package codegreed_devs.com.igasvendor.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;
import codegreed_devs.com.igasvendor.utils.Utils;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ProgressBar registeringBusiness;
    private EditText etBusinessName, etBusinessEmail, etPhone, etPassword;
    private CheckBox togglePassword;
    private CheckBox termsAndCondiditions;
    private Button btnRegister, btnLocation;
    private TextView tvSignIn;
    private String businessName, businessEmail, businessPhone, password;
    private Location businesslocation;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mDatabaseReference;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    GeoFire geoFire;
    private String businessAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(Constants.A_MINUTE);
        mLocationRequest.setFastestInterval(Constants.A_MINUTE);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        registeringBusiness = findViewById(R.id.registering);
        etBusinessName = findViewById(R.id.business_name);
        etBusinessEmail = findViewById(R.id.email);
        etPhone = findViewById(R.id.phone_number);
        etPassword = findViewById(R.id.reg_password);
        togglePassword = (CheckBox)findViewById(R.id.toggle_password);
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
                    businessAddress = Utils.getAddressFromLocation(getApplicationContext(), location.getLatitude(), location.getLongitude());
                    break;
                }
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        };

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBusinessLocation();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (businesslocation == null)
                {
                    alertToEnterBusinessLocation();
                    return;
                }

                signInWithEmail();
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        togglePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked)
                {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                else
                {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
            }
        });


    }

    private void signInWithEmail(){

        getBusinessDetails();

        if (validateUserData()) {

            registeringBusiness.setVisibility(View.VISIBLE);

            btnRegister.setClickable(false);
            btnLocation.setClickable(false);

            mAuth.createUserWithEmailAndPassword(businessEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //write user to the database
                        final FirebaseUser user = mAuth.getCurrentUser();

                        assert user != null;

                        Map<String, String> generalDetails = new HashMap<String, String>();

                        generalDetails.put("business_id", user.getUid());
                        generalDetails.put("business_name", businessName);
                        generalDetails.put("business_email", businessEmail);
                        generalDetails.put("business_phone", businessPhone);

                        if (businessAddress != null)
                            generalDetails.put("business_address", businessAddress);

                        mDatabaseReference.child("vendors").child(user.getUid()).setValue(generalDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    if (businesslocation != null)
                                    {
                                        geoFire = new GeoFire(mDatabaseReference.child("vendors").child(user.getUid()));
                                        geoFire.setLocation("business_location", new GeoLocation(businesslocation.getLatitude(), businesslocation.getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                if (error != null)
                                                    Log.e("GEOFIRE ERROR", error.getMessage());
                                            }
                                        });
                                    }
                                    registeringBusiness.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Registration Successful.Now Login", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                } else if (task.getException() != null){
                                    Log.e("DATABASE ERROR", task.getException().getMessage());
                                }
                            }
                        });

                    } else {
                        btnRegister.setClickable(true);
                        btnLocation.setClickable(true);
                        registeringBusiness.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void alertToEnterBusinessLocation() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.no_location_set));
        alert.setPositiveButton("Set Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getBusinessLocation();
            }
        });
        alert.setNegativeButton("Set Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signInWithEmail();
            }
        });
        alert.show();
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
        }  else if (businessPhone.isEmpty()) {
            etPhone.setError("Enter a business phone number");
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
        businessPhone = etPhone.getText().toString().trim();
        password = etPassword.getText().toString().trim();
    }

    private void getBusinessLocation() {
        Log.e(TAG, "Location button pressed");
        //take the users location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSIONS_REQUEST_CODE);
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                registeringBusiness.setVisibility(View.GONE);
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    businesslocation = location;
                    businessAddress = Utils.getAddressFromLocation(getApplicationContext(), location.getLatitude(), location.getLongitude());
                    btnLocation.setText("Current Address: " + businessAddress);
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
        if(requestCode == Constants.LOCATION_PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getBusinessLocation();
        } else {
            Toast.makeText(this, "Allow app to have location permissions", Toast.LENGTH_LONG).show();
        }
    }
}

