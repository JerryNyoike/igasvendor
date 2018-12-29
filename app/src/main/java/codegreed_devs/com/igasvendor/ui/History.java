package codegreed_devs.com.igasvendor.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import codegreed_devs.com.igasvendor.adapters.HistoryAdapter;
import codegreed_devs.com.igasvendor.R;
import codegreed_devs.com.igasvendor.models.OrderModel;

public class History extends AppCompatActivity {

    private HistoryAdapter historyAdapter;
    private ArrayList<OrderModel> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //set up toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setup history listview with adapter
        ListView orderList = (ListView)findViewById(R.id.order_list);
        orders = new ArrayList<OrderModel>();
        historyAdapter = new HistoryAdapter(getApplicationContext(), orders);
        orderList.setAdapter(historyAdapter);

        //method call
        getRecords();

        //handle item clicks
        orderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String id = orders.get(i).getOrderId();

                Intent viewOrder = new Intent(History.this, ViewOrder.class);
                viewOrder.putExtra("order_id", id);
                startActivity(viewOrder);

            }
        });

    }

    private void getRecords() {

        for (int i = 0; i < 5; i++)
        {
            OrderModel order = new OrderModel("hbfuyewbuewink", "Hashi", "13kg", "Cylinder Only", "1", "Pending");
            orders.add(order);
        }

        historyAdapter.notifyDataSetChanged();

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
}
