package com.example.hw3_stock_watch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Stock_Adapter extends RecyclerView.Adapter<Stock_ViewHolder> {
    private static final String TAG = "Stock_Adapter";
    private List<Stock> stockList;
    private MainActivity mainActivity;

    public Stock_Adapter(List<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }


    @NonNull
    @Override
    public Stock_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Making New ViewHolder");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new Stock_ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Stock_ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: FILLING VIEW HOLDER Employee " + position);

        Stock stock = stockList.get(position);

        if (stock.getChange()>0.0){
            holder.symbol.setTextColor(Color.GREEN);  //parseColor("#32cd32")
            holder.companyName.setTextColor(Color.GREEN);
            holder.latestPrice.setTextColor(Color.GREEN);
            holder.change.setTextColor(Color.GREEN);
            holder.changePercent.setTextColor(Color.GREEN);
        }
        else if (stock.getChange()==0.0){
            holder.symbol.setTextColor(Color.WHITE);
            holder.companyName.setTextColor(Color.WHITE);
            holder.latestPrice.setTextColor(Color.WHITE);
            holder.change.setTextColor(Color.WHITE);
            holder.changePercent.setTextColor(Color.WHITE);
        }
        else {
            holder.symbol.setTextColor(Color.RED);
            holder.companyName.setTextColor(Color.RED);
            holder.latestPrice.setTextColor(Color.RED);
            holder.change.setTextColor(Color.RED);
            holder.changePercent.setTextColor(Color.RED);

        }

        holder.symbol.setText(stock.getSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.latestPrice.setText(String.format(Locale.getDefault(),"%.2f",stock.getLatestPrice()));
        if ((stock.getChange()>0.00)){
            holder.change.setText(String.format(Locale.getDefault(),"▲ %.2f",stock.getChange()));

        }
        else if(stock.getChange()==0.0){
            holder.change.setText(String.format(Locale.getDefault(),"%.2f",stock.getChange()));
        }
        else {
            holder.change.setText(String.format(Locale.getDefault(),"▼ %.2f",stock.getChange()));
        }

        holder.changePercent.setText(String.format(Locale.getDefault(),"%.2f",stock.getChangePercent()));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
