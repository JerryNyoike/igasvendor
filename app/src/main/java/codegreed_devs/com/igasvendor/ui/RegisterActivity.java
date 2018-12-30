package codegreed_devs.com.igasvendor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import codegreed_devs.com.igasvendor.Models.Vendor;
import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.services.FetchAddressIntentService;
import codegreed_devs.com.igasvendor.utils.Constants;

public class RegisterActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "RegisterActivity";
    ProgressBar registeringBusiness;
    EditText etBusinessName, etBusinessEmail, etPassword;
    CheckBox termsAndCondiditions;
    Button btnRegister, btnLocation;
    TextView tvSignIn;
    String businessName, businessEmail, password;
    FirebaseAuth mAuth;
    protected Location businessLocation;
    FusedLocationProviderClient mFusedLocationClient;
    DatabaseReference mDatabaseReference;

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

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBusinessLocation();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeringBusiness.setVisibility(View.VISIBLE);

                getBusinessDetails();

                if (validateUserData()) {
                    registeringBusiness.setVisibility(View.VISIBLE);
                    btnRegister.setClickable(false);
                    btnLocation.setClickable(false);
                    mAuth.createUserWithEmailAndPassword(businessEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                registeringBusiness.setVisibility(View.GONE);

                                //write user to the database
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Map<String, String> data = new HashMap<String, String>();
                                    data.put("business_name", businessName);
                                    data.put("business_email", businessEmail);
                                    data.put("business_location", businessName);
                                }
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
        if (password.length() < 6) {
            etPassword.setError("Password must be more than 6 characters!");
            return false;
        } else if (businessEmail == null || businessEmail.isEmpty()) {
            etBusinessEmail.setError("Enter a business email");
            return false;
        } else if (businessName.isEmpty() || businessName == null) {
            etBusinessName.setError("Enter a business name");
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
                    businessLocation = location;

                    if (businessLocation == null) {
                        Log.e(TAG, "Location is null");
                        return;
                    }

                    if (!Geocoder.isPresent()) {
                        Log.e(TAG, "No Geocoder present");
                        return;
                    }
                    Log.e(TAG, businessLocation.toString());
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

//    protected void startIntentService(Location location, AddressResultReceiver receiver){
//        Intent intent = new Intent(this, FetchAddressIntentService.class);
//        intent.putExtra(Constants.RECEIVER, receiver);
//        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
//        startService(intent);
//    }

//    private class AddressResultReceiver extends ResultReceiver {
//        /**
//         * Create a new ResultReceive to receive results.  Your
//         * {@link #onReceiveResult} method will be called from the thread running
//         * <var>handler</var> if given, or from an arbitrary thread if null.
//         *
//         * @param handler
//         */
//        public AddressResultReceiver(Handler handler) {
//            super(handler);
//        }
//

//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            super.onReceiveResult(resultCode, resultData);
//
//            if(resultData == null){
//                Log.e(TAG, "Result data is null");
//                return;
//            }
//            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
//            //write the address to the database
//            if (addressOutput == null){
//                addressOutput = "";
//            }
//            Log.e(TAG, addressOutput);
//            Toast.makeText(RegisterActivity.this, "Location address: " + addressOutput, Toast.LENGTH_LONG).show();
//        }
//    }

//    private void writeNewUser(Vendor vendor) {
//        mDatabaseReference.child("vendors").child(vendor.getUserId()).setValue(vendor).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful())
//                {
//                    progressDialog.dismiss();
//                    startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                    finish();
//                }
//                else
//                {
//                    progressDialog.dismiss();
//                    Toast.makeText(SignUpActivity.this, "Couldn't save your details to database", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
}
