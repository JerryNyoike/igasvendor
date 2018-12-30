package codegreed_devs.com.igasvendor.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

        //handle item clicks
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithEmail();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

    }

    //use firebase auth to sign user
    // in with provided email and password
    private void signInWithEmail(){

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
                            loadLogin.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Couldn't log you in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //fetch user details based on user id
    // signed in and store in a shared preference
    private void saveUserDetails(String u_id) {
        rootRef.child("users").child(u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String business_name = dataSnapshot.child("business_name").getValue(String.class);
                String lat = dataSnapshot.child("lat").getValue(String.class);
                String Long = dataSnapshot.child("long").getValue(String.class);

                if (business_name != null)
                {
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LAT, lat);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LONG, Long);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_NAME, business_name);
                    Utils.setPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_EMAIL, email);

                    loadLogin.setVisibility(View.GONE);
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
}
