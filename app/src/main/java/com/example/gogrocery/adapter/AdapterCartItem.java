package com.example.gogrocery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gogrocery.R;
import com.example.gogrocery.activities.ShopDetailsActivity;
import com.example.gogrocery.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout of row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, final int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);

        final String id = modelCartItem.getId();
        String pId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();
        final String cost = modelCartItem.getCost();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemPriceEachTv.setText(""+price);
        holder.itemQuantityTv.setText("["+quantity+"]");

        //handle remove click listner, delete from cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //will create table if not exists, but in that case will must exist
                EasyDB easyDB = EasyDB.init(context,"ITEM_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTv.getText().toString().trim().replace("₹","")));
                double totalPrice = tx - Double.parseDouble(cost.replace("₹",""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("₹","")));
                double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice)) - Double.parseDouble(String.format("%.2f", deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).sTotalTv.setText("₹"+String.format("%.2f",sTotalPrice));
                ((ShopDetailsActivity)context).allTotalPriceTv.setText("₹"+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();//return number of records
    }


    class HolderCartItem extends RecyclerView.ViewHolder{

        //ui view of row_cartitem.xml
        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv,itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);

        }
    }
}
