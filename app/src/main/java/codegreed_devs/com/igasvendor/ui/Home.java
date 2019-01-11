package codegreed_devs.com.igasvendor.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.adapters.OrdersAdapter;
import codegreed_devs.com.igasvendor.models.OrderModel;
import codegreed_devs.com.igasvendor.utils.Constants;
import codegreed_devs.com.igasvendor.utils.Utils;

public class Home extends AppCompatActivity {

    private TextView onlineStatus;
    private ArrayList<OrderModel> pendingOrders;
    private OrdersAdapter ordersAdapter;
    private DatabaseReference rootDBRef;
    private GeoFire geoFire;
    private String vendorId;
    private ProgressDialog loadAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //set up toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get data from shared preference
        vendorId = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ID);

        //initialize views
        final SwitchCompat switchCompat = findViewById(R.id.toggle_online_status);
        onlineStatus = findViewById(R.id.online_status);
        ListView ordersList = findViewById(android.R.id.list);
        TextView emptyListView = findViewById(android.R.id.empty);
        loadAction = new ProgressDialog(this);

        //initialize firebase variables
        rootDBRef = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(rootDBRef.child("Available Vendors"));

        //initialize other variables
        pendingOrders = new ArrayList<OrderModel>();
        ordersAdapter = new OrdersAdapter(getApplicationContext(), pendingOrders);

        //method calls
        getPendingOrderDetails();

        //update ui
        ordersList.setAdapter(ordersAdapter);
        ordersList.setEmptyView(emptyListView);

        if (Utils.getPrefBoolean(getApplicationContext(), Constants.SHARED_PREF_NAME_IS_ONLINE))
        {
            switchCompat.setChecked(true);
            onlineStatus.setText(getResources().getString(R.string.online));
            toggleOnlineStatus(false);
        }

        //handle item clicks
        ordersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent viewOrder = new Intent(Home.this, ViewOrder.class);
                viewOrder.putExtra("client_id", pendingOrders.get(position).getClientId());
                viewOrder.putExtra("order_id", pendingOrders.get(position).getOrderId());
                startActivity(viewOrder);
            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                {
                    toggleOnlineStatus(false);
                }
                else
                {
                    toggleOnlineStatus(true);
                }
            }
        });

    }

    //fetch all details in the db for the vendor
    private void getPendingOrderDetails(){

        loadAction.setMessage("Getting orders...");
        loadAction.setCancelable(false);
        loadAction.show();

        rootDBRef.child("Order Details").child(vendorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                pendingOrders.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String orderStatus = ds.child("orderStatus").getValue(String.class);

                    if(orderStatus != null && (orderStatus.equals("waiting") || orderStatus.equals("accepted")))
                    {
                        pendingOrders.add(new OrderModel(ds.child("orderId").getValue(String.class),
                                ds.child("clientId").getValue(String.class),
                                ds.child("vendorId").getValue(String.class),
                                ds.child("gasBrand").getValue(String.class),
                                ds.child("gasSize").getValue(String.class),
                                ds.child("gasType").getValue(String.class),
                                ds.child("price").getValue(String.class),
                                ds.child("mnumberOfCylinders").getValue(String.class),
                                ds.child("orderStatus").getValue(String.class)));
                    }

                }

                ordersAdapter.notifyDataSetChanged();
                loadAction.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadAction.dismiss();
                Log.e("DATABASE ERROR", databaseError.getMessage());
                Toast.makeText(Home.this, "Couldn't get orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.history)
        {
            startActivity(new Intent(getApplicationContext(), History.class));
        }
        else if (id == R.id.edit_profile)
        {
            startActivity(new Intent(getApplicationContext(), EditProfile.class));
        }
        else if (id == R.id.log_out)
        {
            logOut();
        }
        else if (id == R.id.exit)
        {
            finish();
        }
        
        return true;
    }

    //change vendor's online status
    private void toggleOnlineStatus(boolean isOnline){

        double latitude = (double) Utils.getPrefFloat(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LAT);
        double longitude = (double) Utils.getPrefFloat(getApplicationContext(), Constants.SHARED_PREF_NAME_LOC_LONG);

        if (!isOnline)
        {
            geoFire.setLocation(vendorId, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null)
                    {
                        Toast.makeText(Home.this, "Something went wrong.Check connection!", Toast.LENGTH_SHORT).show();
                        Log.e("GEOFIRE ERROR", error.getMessage());
                    }
                    else
                    {
                        Utils.setPrefBoolean(getApplicationContext(), Constants.SHARED_PREF_NAME_IS_ONLINE, true);
                        onlineStatus.setText(getResources().getString(R.string.online));
                        Toast.makeText(Home.this, "You are online!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            geoFire.removeLocation(vendorId, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null)
                    {
                        Toast.makeText(Home.this, "Something went wrong.Check connection!", Toast.LENGTH_SHORT).show();
                        Log.e("GEOFIRE ERROR", error.getMessage());
                    }
                    else
                    {
                        Utils.setPrefBoolean(getApplicationContext(), Constants.SHARED_PREF_NAME_IS_ONLINE, false);
                        onlineStatus.setText(getResources().getString(R.string.offline));
                        Toast.makeText(Home.this, "You are offline!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    //log out user
    private void logOut() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Logout");
        alert.setMessage("Are you sure you want to log out...?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.clearSP(getApplicationContext());
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //ignore and dismiss dialog
            }
        });
        alert.show();

    }
}
