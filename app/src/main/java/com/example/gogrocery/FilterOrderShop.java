package com.example.gogrocery;


import android.widget.Filter;

import com.example.gogrocery.adapter.AdapterOrderShop;
import com.example.gogrocery.models.ModelOrderShop;
import com.example.gogrocery.adapter.AdapterProductSeller;
import com.example.gogrocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {
    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if(constraint != null && constraint.length()>0){
            //search field not empty

            //change to upper case to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i=0; i < filterList.size(); i++){
                //check search by title and category
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //filtered data in list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            //search field empty
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList= (ArrayList<ModelOrderShop>) results.values;
        //refresh adepter
        adapter.notifyDataSetChanged();
    }
}
