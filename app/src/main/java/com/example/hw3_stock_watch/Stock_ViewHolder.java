package com.example.hw3_stock_watch;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class Stock_ViewHolder extends RecyclerView.ViewHolder {

    TextView symbol;
    TextView companyName;
    TextView latestPrice;
    TextView change;
    TextView changePercent;

    public Stock_ViewHolder(View itemView) {
        super(itemView);
        symbol = itemView.findViewById(R.id.symbolTextView);
        companyName = itemView.findViewById(R.id.companyNameTextView);
        latestPrice = itemView.findViewById(R.id.latestPriceTextView);
        change = itemView.findViewById(R.id.changeTextView);
        changePercent = itemView.findViewById(R.id.changePerecntTextView);
    }
}
