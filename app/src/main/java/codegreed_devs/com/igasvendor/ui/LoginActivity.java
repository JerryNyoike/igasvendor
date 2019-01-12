package codegreed_devs.com.igasvendor.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;
import codegreed_devs.com.igasvendor.utils.Utils;

public class LoginActivity extends AppCompatActivity {

    private EditText logEmail, logPassword;
    private Button signIn;
    private TextView signUp;
    private ProgressBar loadLogin;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private GeoFire geoFire;
    private String email, password;

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

        //initialize variables
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        //check if user is logged in
        if(auth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }


        //handle item clicks
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate())
                    signInWithEmail();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

    }

    //use firebase auth to sign user
    // in with provided email and password
    private void signInWithEmail(){

        signIn.setEnabled(false);
        loadLogin.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && auth.getCurrentUser() != null)
                        {
                            saveUserDetails(auth.getCurrentUser().getUid());
                        }
                        else
                        {
                            signIn.setEnabled(true);
                            loadLogin.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Couldn't log you in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //fetch user details based on user id
    // signed in and store in a shared preference
    private void saveUserDetails(final String u_id) {

        rootRef.child("vendors").child(u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String business_email = dataSnapshot.child("business_email").getValue(String.class);
                String business_name = dataSnapshot.child("business_name").getValue(String.class);
                String business_address = dataSnapshot.child("business_address").getValue(String.class);

                getBusinessLocation(u_id);
                getBusinessPrices(u_id);

                if (business_address != null)
                {
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ID, u_id);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_NAME, business_name);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_EMAIL, business_email);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ADDRESS, business_address);
                    Utils.setPrefBoolean(getApplicationContext(), Constants.SHARED_PREF_NAME_IS_FIRST_LOGIN, false);

                    String token = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_FCM_TOKEN);

                    if (!token.equals(""))
                        saveFCMTokenToDatabase(token, u_id);

                    loadLogin.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadLogin.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Couldn't fetch your details", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //fetch vendor prices
    private void getBusinessPrices(String u_id) {

        rootRef.child("vendors").child(u_id).child("business_prices").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String complete_six_kg = dataSnapshot.child("complete_six_kg").getValue(String.class);
                String complete_thirteen_kg = dataSnapshot.child("complete_thirteen_kg").getValue(String.class);
                String six_kg = dataSnapshot.child("six_kg").getValue(String.class);
                String thirteen_kg = dataSnapshot.child("thirteen_kg").getValue(String.class);

                if (thirteen_kg != null)
                {
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_COMPLETE_SIX_KG_PRICE, complete_six_kg);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_COMPLETE_THIRTEEN_KG_PRICE, complete_thirteen_kg);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_SIX_KG_PRICE, six_kg);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_THIRTEEN_KG_PRICE, thirteen_kg);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //fetch vendor location
    private void getBusinessLocation(String u_id) {

        geoFire = new GeoFire(rootRef.child("vendors").child(u_id));
        geoFire.getLocation("business_location", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null)
                {
                    float latitude = (float) location.latitude;

                    float longitude = (float) location.longitude;

                    if (latitude != 0)
                    {
                        Utils.setPrefFloat(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LAT, latitude);
                        Utils.setPrefFloat(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LONG, longitude);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //save token to database and shared preference
    private void saveFCMTokenToDatabase(String token, String vendorId) {

        rootRef.child("vendors").child(vendorId).child("fcm_token")
                .setValue(token)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful() && task.getException() != null)
                {
                        Log.e("FCM ERROR", task.getException().getMessage());
                }
            }
        });

    }

    private boolean validate(){

        email = logEmail.getText().toString().trim();
        password = logPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            logEmail.setError("Enter email");
            return false;
        }
        else if (TextUtils.isEmpty(password))
        {
            logEmail.setError("Enter password");
            return false;
        }

        return true;
    }
}
