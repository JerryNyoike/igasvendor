package codegreed_devs.com.igasvendor.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.utils.Constants;
import codegreed_devs.com.igasvendor.utils.Utils;

public class ViewOrder extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private Button submit;
    private ImageView viewInMap, callClient;
    private String orderId, gasBrand, clientId, clientName, clientEmail, clientLocation, clientPhone, paidAmount, cylinderSize,
            gasType, noOfCylinders, vendorId, clientFCMToken;
    private DatabaseReference rootRef;
    private ArrayList<String> orderDetails;
    private ArrayAdapter<String> detailsAdapter;
    private ProgressDialog loadAction;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        //set up toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get extras from previous activity
        clientId = getIntent().getExtras().getString("client_id");
        orderId = getIntent().getExtras().getString("order_id");

        //get vendor id from SP
        vendorId = Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_ID);

        //initialize views
        ListView orderDetailsList = (ListView) findViewById(R.id.order_details);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        submit = (Button) findViewById(R.id.submit);
        viewInMap = (ImageView) findViewById(R.id.view_in_map);
        callClient = (ImageView) findViewById(R.id.call_client);
        loadAction = new ProgressDialog(this);

        //initialize variables
        orderDetails = new ArrayList<String>();
        detailsAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.detail_item, orderDetails);
        rootRef = FirebaseDatabase.getInstance().getReference();

        //handle item clicks
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit.setEnabled(false);
                switch (submit.getText().toString().trim())
                {
                    case "Accept Order":
                        acceptOrder();
                        break;
                    case "Confirm Payment":
                        confirmPayment();
                        break;
                }
            }
        });

        viewInMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewInMap();
            }
        });

        callClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callClient();
            }
        });

        //method calls
        getOrderDetails();
        getOrderLocation();

        //update ui
        orderDetailsList.setAdapter(detailsAdapter);

    }

    //get order details for order
    //with id passed from list activity
    private void getOrderDetails() {

        loadAction.setMessage("Fetching details...");
        loadAction.setCancelable(false);
        loadAction.show();

        rootRef.child("Order Details").child(vendorId).child(orderId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                orderDetails.clear();

                String order_status = dataSnapshot.child("orderStatus").getValue(String.class);
                paidAmount = dataSnapshot.child("price").getValue(String.class);
                gasBrand = dataSnapshot.child("gasBrand").getValue(String.class);
                gasType =  dataSnapshot.child("gasType").getValue(String.class);
                noOfCylinders = dataSnapshot.child("mnumberOfCylinders").getValue(String.class);
                cylinderSize = dataSnapshot.child("gasSize").getValue(String.class);

                orderDetails.add("Gas Brand : " + gasBrand);
                orderDetails.add("Cylinder size : " + cylinderSize);
                orderDetails.add("Gas Type : " + gasType);
                orderDetails.add("Number of cylinders : " + noOfCylinders);
                orderDetails.add("Order Status : " + order_status);

                getClientDetails();

                if (!TextUtils.isEmpty(order_status))
                {
                    if (order_status.equals("waiting"))
                    {
                        submit.setVisibility(View.VISIBLE);
                        submit.setText(getResources().getString(R.string.accept_order));
                    }
                    else if (order_status.equals("accepted"))
                    {
                        submit.setVisibility(View.VISIBLE);
                        submit.setText(getResources().getString(R.string.confirm_payment));
                    }
                    else if(order_status.equals("finished"))
                    {
                        viewInMap.setVisibility(View.GONE);
                        submit.setVisibility(View.GONE);
                    }
                }

                detailsAdapter.notifyDataSetChanged();
                loadAction.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadAction.dismiss();
                Toast.makeText(ViewOrder.this, "Couldn't get order details", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //fetch details of the client that ordered the gas
    private void getClientDetails() {

        rootRef.child("clients").child(clientId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (clientPhone != null)
                {
                    orderDetails.remove("Client Name : " + clientName);
                    orderDetails.remove("Client Email : " + clientEmail);
                    orderDetails.remove("Client Location : " + clientLocation);
                    orderDetails.remove("Client Phone Number : " + clientPhone);
                }

                clientName = dataSnapshot.child("clientName").getValue(String.class);
                clientEmail = dataSnapshot.child("clientEmail").getValue(String.class);
                clientLocation = dataSnapshot.child("regLocation").getValue(String.class);
                clientPhone = dataSnapshot.child("regPhoneNumber").getValue(String.class);
                clientFCMToken = dataSnapshot.child("fcm_token").getValue(String.class);

                orderDetails.add("Client Name : " + clientName);
                orderDetails.add("Client Email : " + clientEmail);
                orderDetails.add("Client Location : " + clientLocation);
                orderDetails.add("Client Phone Number : " + clientPhone);

                if (clientPhone != null)
                    callClient.setVisibility(View.VISIBLE);

                detailsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //launch dial intent
    private void callClient(){

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, Constants.CALL_PHONE_PERMISSION_REQUEST_CODE);
            return;
        }

        Intent call = new Intent(Intent.ACTION_DIAL);
        call.setData(Uri.parse("tel:" + clientPhone));
        startActivity(Intent.createChooser(call, "Call with..."));

    }

    //get client location using geofire
    private void getOrderLocation(){

        GeoFire geoFire = new GeoFire(rootRef.child("clientRequest"));
        geoFire.getLocation(clientId, new LocationCallback() {
           @Override
           public void onLocationResult(String key, GeoLocation location) {
               if (location != null)
               {
                   latitude = location.latitude;
                   longitude = location.longitude;
                   viewInMap.setVisibility(View.VISIBLE);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
        });

    }

    //launch map intent
    private void viewInMap() {

        Intent viewInMap = new Intent(Intent.ACTION_VIEW);
        if (latitude != 0.0)
            viewInMap.setData(Uri.parse("http://maps.google.com/maps?q="+ latitude  +"," + longitude +"("+
                    Utils.getAddressFromLocation(getApplicationContext(), latitude, longitude) + ")&iwloc=A&hl=es"));
        startActivity(Intent.createChooser(viewInMap, "Open with :"));

    }

    //this function updates the order status of a selected order and updates
    private void acceptOrder() {

        loadAction.setMessage("Accepting order...");
        loadAction.show();

        //update the order details
        rootRef.child("Order Details").child(clientId).child(orderId).child("orderStatus").setValue("accepted");
        rootRef.child("Order Details").child(vendorId).child(orderId).child("orderStatus").setValue("accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (clientFCMToken != null)
                {
//                    String jsonData = "{";
//                    jsonData += "\"message\":";
//                    jsonData += "{";
//                    jsonData += "\"token\":";
//                    jsonData += "\"" + clientFCMToken + "\",";
//                    jsonData += "\"notification\":";
//                    jsonData += "{";
//                    jsonData += "\"title\":";
//                    jsonData += "\"New order\",";
//                    jsonData += "\"body\":";
//                    jsonData += "\"You have a new order\"";
//                    jsonData += "},";
//                    jsonData += "\"data\":{";
//                    jsonData += "\"order_id\":";
//                    jsonData += "\"" + orderId + "\",";
//                    jsonData += "\"vendor_id\":";
//                    jsonData += "\"" + vendorId + "\"";
//                    jsonData += "}";
//                    jsonData += "}";
//                    jsonData += "}";

                    String jsonData = "{";
                    jsonData += "\"data\":";
                    jsonData += "{";
                    jsonData += "\"title\":";
                    jsonData += "\"New order\",";
                    jsonData += "\"body\":";
                    jsonData += "\"You have a new order\"";
                    jsonData += "},";
                    jsonData += "\"to\":";
                    jsonData += "\"" + clientFCMToken + "\"";
                    jsonData += "}";

                    try {

                        JSONObject jsonObject = new JSONObject(jsonData);

                        Log.e("FCM JSON", jsonObject.toString());

                        Utils.sendFCMNotification(jsonObject.toString(), new Callback() {
                            @Override
                            public void onFailure(Request request, final IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadAction.dismiss();
                                        submit.setEnabled(true);
                                        Log.e("FCM SEND ERROR", e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onResponse(final Response response) throws IOException {

                                int response_code = response.code();

                                Log.e("HTTP RESPONSE CODE", String.valueOf(response_code));

                                if (response_code == Constants.HTTP_RESPONSE_OK)
                                {

                                    final String responseMessage = response.body().string();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            Log.e("FCM RESPONSE", responseMessage);

                                            try {

                                                JSONObject jsonObject = new JSONObject(responseMessage);

                                                int success = jsonObject.getInt("success");

                                                if (success == 1)
                                                {
                                                    Toast.makeText(ViewOrder.this, "Client notified!", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(ViewOrder.this, "Client not notified!", Toast.LENGTH_SHORT).show();
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            submit.setEnabled(true);
                                            loadAction.dismiss();
                                        }
                                    });

                                }
                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ViewOrder.this, "Client not notified!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    loadAction.dismiss();
                    submit.setEnabled(true);
                    Toast.makeText(ViewOrder.this, "Client not notified!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //confirms transaction is complete
    //and sends receipt to user via email
    private void  confirmPayment() {

        loadAction.setMessage("Sending Receipt...");
        loadAction.show();

        String message = "Hello ";
        message += clientName;
        message += ". Your payment of Ksh. ";
        message += paidAmount;
        message += " has been received.\n\n";
        message += "Here is a breakdown : \n";
        message += "Gas Brand : " + gasBrand + "\n";
        message += "Cylinder size : " + cylinderSize + "\n";
        message += "Gas Type : " + gasType + "\n";
        message += "Number of cylinders : " + noOfCylinders + "\n";
        message += "Amount paid : Ksh. " + paidAmount + "\n\n";
        message += "Thank you on behalf of ";
        message += Utils.getPrefString(getApplicationContext(), Constants.SHARED_PREF_NAME_BUSINESS_NAME);
        message += " and iGas.\n";
        message += "We do look forward to carrying out more transaction with you our esteemed client.";

        RequestBody requestBody = new FormEncodingBuilder()
                .add("email", clientEmail)
                .add("subject", getResources().getString(R.string.payment_confirmation))
                .add("message", message)
                .build();

        post(requestBody, new Callback() {
            @Override
            public void onFailure(Request request, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadAction.dismiss();
                        submit.setEnabled(true);
                        Toast.makeText(ViewOrder.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        Log.e("OKHTTP ERROR", e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {

                int responseCode = response.code();
                Log.e("HTTP RESPONSE CODE", String.valueOf(responseCode));

                if (responseCode == Constants.HTTP_RESPONSE_OK)
                {
                    String serverResponse = response.body().string();
                    Log.e("Response", serverResponse);
                    try
                    {
                        JSONObject jsonObject = new JSONObject(serverResponse);

                        String success_state = jsonObject.getString("success_state");

                        if (success_state != null && success_state.equals("1"))
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    finishTransaction();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(ViewOrder.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    loadAction.dismiss();
                    Toast.makeText(ViewOrder.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //carry out a http post request
    private void post(RequestBody requestBody, Callback callback){

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setProtocols(Collections.singletonList(Protocol.HTTP_1_1));

        Request request = new Request.Builder()
                .url(Constants.CONFIRM_EMAIL_URL)
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);

    }

    //set order status to finished in db
    private void finishTransaction() {

        rootRef.child("Order Details").child(clientId).child(orderId).child("orderStatus").setValue("finished");
        rootRef.child("Order Details").child(vendorId).child(orderId).child("orderStatus").setValue("finished")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            //inform vendor to check the history where they can view finished orders
                            Snackbar mySnackbar = Snackbar.make(coordinatorLayout, "Receipt sent to client's email.\nCheck your history page to view finished orders.", Snackbar.LENGTH_LONG);
                            mySnackbar.setAction("Go", new HistoryListener());
                            mySnackbar.show();
                        }
                        else
                        {
                            Toast.makeText(ViewOrder.this, "Payment not confirmed.Send Again", Toast.LENGTH_SHORT).show();
                        }

                        loadAction.dismiss();
                        submit.setEnabled(true);
                    }
                });

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

        if (requestCode == Constants.CALL_PHONE_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            callClient();
        }

    }

    private class HistoryListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            startActivity(new Intent(ViewOrder.this, History.class));
        }

    }


}
