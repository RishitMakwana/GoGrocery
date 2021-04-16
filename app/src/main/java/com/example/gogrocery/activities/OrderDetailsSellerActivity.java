package com.example.gogrocery.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gogrocery.R;
import com.example.gogrocery.adapter.AdapterOrderedItem;
import com.example.gogrocery.models.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private TextView orderIdTv,dateTv,orderStatusTv,emailTv,phoneTv,totalItemsTv,amountTv,addressTv;
    private ImageButton backBtn,editBtn,mapBtn;
    private RecyclerView itemsRv;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    String sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;

    private FirebaseAuth firebaseAuth;
    private String orderTo,orderId,orderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        mapBtn = findViewById(R.id.mapBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);

        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        orderTo = intent.getStringExtra("orderTo");
        orderBy = intent.getStringExtra("orderBy");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        final String[] options = {"In Progress", "Completed", "Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status:")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String selectedOption = options[i];
                        editOrderStatus(selectedOption);

                    }
                })
                .show();
    }

    private void editOrderStatus(final String selectedOption) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //status updated
                        Toast.makeText(OrderDetailsSellerActivity.this, "Order is now"+selectedOption, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "http://maps.google.com/maps?saddr=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," + destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        sourceLatitude = ""+dataSnapshot.child("latitude").getValue();
                        sourceLongitude = ""+dataSnapshot.child("longitude").getValue();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get buyer info
                        destinationLatitude = ""+dataSnapshot.child("latitude").getValue();
                        destinationLongitude = ""+dataSnapshot.child("longitude").getValue();
                        String email = ""+dataSnapshot.child("email").getValue();
                        String phone = ""+dataSnapshot.child("phone").getValue();

                        //set info
                        emailTv.setText(email);
                        phoneTv.setText(phone);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadOrderDetails() {
        //System.out.println(orderId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String orderBy = ""+dataSnapshot.child("orderBy").getValue();
                        String orderCost = ""+dataSnapshot.child("orderCost").getValue();
                        String orderId = ""+dataSnapshot.child("orderId").getValue();
                        String orderStatus = ""+dataSnapshot.child("orderStatus").getValue();
                        String orderTime = ""+dataSnapshot.child("orderTime").getValue();
                        String orderTo = ""+dataSnapshot.child("orderTo").getValue();
                        String deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                        String address = ""+dataSnapshot.child("address").getValue();

                        //conver timestamp to proper formate
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();//e.g. 09/04/2021 12:00 PM

                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("₹" +orderCost+"[Including delivery fee ₹"+deliveryFee+"]");
                        dateTv.setText(formatedDate);
                        addressTv.setText(address);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadOrderedItems() {
        //init list
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderedItemArrayList.clear();//before loading items clear list
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);
                        }
                        //all items added to list
                        //setup adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, orderedItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set items count
                        totalItemsTv.setText(""+dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}