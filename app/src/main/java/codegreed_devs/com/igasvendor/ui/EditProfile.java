package codegreed_devs.com.igasvendor.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;
import codegreed_devs.com.igasvendor.utils.Utils;

public class EditProfile extends AppCompatActivity {

    private EditText etBusinessName, etPhone, etBusinessPrices;
    private ProgressDialog loadUpdate;
    private String businessName, businessPrices;
    private Location businessLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private DatabaseReference mDatabaseReference;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Button btnUpdate, btnLocation;
    private GeoFire geoFire;
    private String businessId, businessPhone, currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //get business details from SP
        businessId = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ID);
        businessName = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_NAME);
        businessPhone = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_PHONE);
        currentAddress = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ADDRESS);
        businessPrices = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_PRICES);

        //set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initialize views
        etBusinessName = findViewById(R.id.business_name);
        etPhone = findViewById(R.id.business_phone);
        etBusinessPrices = findViewById(R.id.business_prices);
        btnUpdate = findViewById(R.id.update);
        btnLocation = findViewById(R.id.fetch_location);
        loadUpdate = new ProgressDialog(this);


        //initialise location api variables
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(Constants.A_MINUTE);
        mLocationRequest.setFastestInterval(Constants.A_MINUTE);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    businessLocation = location;
                    break;
                }
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                super.onLocationResult(locationResult);
            }
        };

        //initialize firebase and geofire variables
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("vendors").child(businessId);
        geoFire = new GeoFire(mDatabaseReference);

        //update ui
        etBusinessName.setText(businessName);
        etPhone.setText(businessPhone);
        etBusinessPrices.setText(businessPrices);
        btnLocation.setText("Current Address: " + currentAddress);

        //handle item clicks
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastKnownLocation();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate())
                    btnUpdate.setEnabled(false);
                    updateBusinessDetails();
            }
        });

    }

    private void updateBusinessDetails() {

        loadUpdate.setMessage("Updating...");
        loadUpdate.setCancelable(false);
        loadUpdate.show();

        if (businessLocation != null)
            geoFire.setLocation("business_location", new GeoLocation(businessLocation.getLatitude(), businessLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Utils.setPrefFloat(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LAT, (float) businessLocation.getLatitude());
                    Utils.setPrefFloat(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LONG, (float) businessLocation.getLongitude());
                    if(error != null){
                        Log.e("GEOFIRE ERROR", error.getMessage());
                    }
                }
            });

        mDatabaseReference.child("business_name").setValue(businessName);
        mDatabaseReference.child("business_phone").setValue(businessPhone);
        mDatabaseReference.child("business_address").setValue(currentAddress);
        mDatabaseReference.child("business_prices").setValue(businessPrices).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    updateSharedPreferences();
                }
                else
                {
                    Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                    if (task.getException() != null)
                        Log.e("DATABASE ERROR", task.getException().getMessage());
                }
                btnUpdate.setEnabled(true);
                loadUpdate.dismiss();
            }
        });

    }

    private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSIONS_REQUEST_CODE);
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    businessLocation = location;
                    currentAddress = Utils.getAddressFromLocation(getApplicationContext(), businessLocation.getLatitude(), businessLocation.getLongitude());
                    btnLocation.setText("Current Address: " + currentAddress);
                } else {
                    if (ActivityCompat.checkSelfPermission(EditProfile.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditProfile.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfile.this, new String[]{}, Constants.LOCATION_PERMISSIONS_REQUEST_CODE);
                        return;
                    }
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateSharedPreferences(){
        Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_NAME, businessName);
        Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_PHONE, businessPhone);
        Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_PRICES, businessPrices);
        Utils.setPrefBoolean(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_PRICES_SET, true);
        Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ADDRESS, currentAddress);

        Toast.makeText(EditProfile.this, "Updated", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(),Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private boolean validate() {

        businessName = etBusinessName.getText().toString().trim();
        businessPhone = etPhone.getText().toString().trim();
        businessPrices = etBusinessPrices.getText().toString().trim();

        if (TextUtils.isEmpty(businessName))
        {
            etBusinessName.setError("Enter valid business name");
            return false;
        }
        else if (TextUtils.isEmpty(businessPhone))
        {
            etPhone.setError("Please enter valid phone number");
            return false;
        }
        else if (TextUtils.isEmpty(businessPrices))
        {
            etBusinessPrices.setError("Please enter your business prices");
            return false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constants.LOCATION_PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getLastKnownLocation();
        } else {
            Toast.makeText(this, "Allow app to have location permissions", Toast.LENGTH_LONG).show();
        }
    }
}
