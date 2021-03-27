package com.example.gogrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gogrocery.Constants;
import com.example.gogrocery.R;
import com.example.gogrocery.adapter.AdapterCartItem;
import com.example.gogrocery.adapter.AdapterProductUser;
import com.example.gogrocery.models.ModelCartItem;
import com.example.gogrocery.models.ModelProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui view
    private ImageView shopIv;
    private TextView shopNameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv;
    private ImageButton callBtn,mapBtn,cartBtn,backBtn,filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;

    private String shopUid;
    private String myLatitude,myLongitude;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;
    public String deliveryFee;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelProduct> productList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init ui views
        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productsRv = findViewById(R.id.productsRv);

        //get uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        //each shop have its own products and orders if user add items to cart and go back and open cart in different shop then cart should be different
        //so cart data whenever user open this activity
        deleteCartData();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go previous activity
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Filter Products:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadShopProducts();
                                }
                                else {
                                    //loads filtered
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(this,"ITEM_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();//delete all records from cart
    }

    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making public
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();

        //inflate layout view
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init view
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        TextView sTotalLabelTv = view.findViewById(R.id.sTotalLabelTv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view on dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this,"ITEM_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext())
        {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+cost,
                    ""+price,
                    ""+quantity
            );

            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this,cartItemList);
        //set recycle view
        cartItemsRv.setAdapter(adapterCartItem);

        dFeeTv.setText("₹"+deliveryFee);
        sTotalTv.setText("₹"+String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("₹"+(allTotalPrice + Double.parseDouble(deliveryFee.replace("₹", ""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        //reset total price on dialog
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });


    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "http://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener(){
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                       for(DataSnapshot ds: dataSnapshot.getChildren()){
                           //get user data
                           String name = ""+ds.child("name").getValue();
                           String email = ""+ds.child("email").getValue();
                           String phone = ""+ds.child("phone").getValue();
                           String profileImage = ""+ds.child("profileImage").getValue();
                           String accountType = ""+ds.child("accountType").getValue();
                           String city = ""+ds.child("city").getValue();
                           myLatitude = ""+ds.child("Latitude").getValue();
                           myLongitude = ""+ds.child("Longitude").getValue();
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError){

                   }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener(){
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               //get shop data
               String name = ""+dataSnapshot.child("name").getValue();
                shopName = ""+dataSnapshot.child("shopName").getValue();
                shopEmail = ""+dataSnapshot.child("email").getValue();
                shopPhone = ""+dataSnapshot.child("phone").getValue();
                shopAddress = ""+dataSnapshot.child("address").getValue();
                shopLatitude = ""+dataSnapshot.child("Latitude").getValue();
                shopLongitude = ""+dataSnapshot.child("Longitude").getValue();
                deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage = ""+dataSnapshot.child("profileImage").getValue();
                String shopOpen = ""+dataSnapshot.child("shopOpen").getValue();

                //set data
               shopNameTv.setText(shopName);
               emailTv.setText(shopEmail);
               deliveryFeeTv.setText("Delivery Fee: ₹"+deliveryFee);
               addressTv.setText(shopAddress);
               phoneTv.setText(shopPhone);

               if (shopOpen.equals("true")){
                   openCloseTv.setText("Open");
               }
               else{
                   openCloseTv.setText("Closed");
               }
               try {
                   Picasso.get().load(profileImage).into(shopIv);
               }
               catch (Exception e){

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError){

           }
        });
    }

    private void loadShopProducts() {
        //init list
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("products")
                .addValueEventListener(new ValueEventListener(){
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                       //clear list before adding items
                       productList.clear();
                       for(DataSnapshot ds: dataSnapshot.getChildren()){
                           ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                           productList.add(modelProduct);
                       }
                       //setup adapter
                       adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productList);
                       //set adapter
                       productsRv.setAdapter(adapterProductUser);
                   }
                   public void onCancelled(@NonNull DatabaseError databaseError){

                   }
                });
    }
}