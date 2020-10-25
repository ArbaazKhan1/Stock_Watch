package com.example.hw3_stock_watch;

public class Stock implements Comparable<Stock>{

    private String symbol;
    private String companyName;
    private double latestPrice;
    private double change;
    private double changePercent;


    public Stock(String symbol, String companyName, double latestPrice, double change, double changePercent) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.latestPrice = latestPrice;
        this.change = change;
        this.changePercent = changePercent;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }


    @Override
    public int compareTo(Stock stock) {
        return this.getSymbol().compareTo(stock.getSymbol());
    }
}
