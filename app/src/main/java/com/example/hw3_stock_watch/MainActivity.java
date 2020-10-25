package com.example.hw3_stock_watch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "MainActivity";
    private List<Stock> stockList = new ArrayList<>();
    private Stock_Adapter stock_adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConnectivityManager connectivityManager;
    private EditText stockDialog;
    private Map<String,String> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Stock Watch");
        readFile();
        swipeRefreshLayout = findViewById(R.id.Swiper);
        recyclerView = findViewById(R.id.RecyclerView);
        stock_adapter = new Stock_Adapter(stockList, this);
        recyclerView.setAdapter(stock_adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stock_adapter.notifyDataSetChanged();



        if (!doNetCheck()){
            noConnectionAlert();
        }
        else {
            new API_AsyncTask(this).execute();
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: Start!");
                doSwipeRefresh();
            }
        });

        /*
        TODO
        check dups
        CHECK internet connection
         */

    }

    private boolean doNetCheck(){
        if (connectivityManager==null){
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager==null){
                Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            Toast.makeText(this, "Connected to Network", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "Cannot Connect to Network", Toast.LENGTH_SHORT).show();
            List<Stock> tempList = new ArrayList<>();
            Log.d(TAG, "doNetCheck: "+stockList.size());
            for (Stock s : stockList){
                s.setLatestPrice(0.0);
                s.setChange(0.0);
                s.setChangePercent(0.0);
                tempList.add(s);
                //new StockInfoAsyncTask(MainActivity.this).execute(s.getSymbol().toUpperCase());
            }
            stockList.clear();
            stockList.addAll(tempList);
            stock_adapter.notifyDataSetChanged();
            return false;
        }

    }

    //////////////////////////////////////////SWIPE REFRESH/////////////////////////////////////////
    private void doSwipeRefresh() {
        if (!doNetCheck()){
            noConnectionAlert();
            swipeRefreshLayout.setRefreshing(false);
            Log.d(TAG, "doSwipeRefresh: Finish!");
            return;
        }
        new API_AsyncTask(MainActivity.this).execute();
        for (Stock stock: stockList){
            new StockInfoAsyncTask(MainActivity.this).execute(stock.getSymbol().toUpperCase());
        }
        stock_adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        Log.d(TAG, "doSwipeRefresh: Finish");
    }



    ///////////////////////////Options MENU/////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.AddStockitem){
            if (!doNetCheck()){
                noConnectionAlert();
                return true;
            }
            LayoutInflater inflater = LayoutInflater.from(this);
            final View v = inflater.inflate(R.layout.stock_dialog,null);
            stockDialog = v.findViewById(R.id.stockDialogEditText);
            stockDialog.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(v);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Map<String,String> dadSMaps = new HashMap<>();
                    final ArrayList<String>  momSList = new ArrayList<>();
                    String stockText = stockDialog.getText().toString().toUpperCase();
                    //This for loop is checking if the user us looking up a duplicate stock
                    for (Stock s : stockList){
                        if (s.getSymbol().equals(stockText)){
                            duplicateStockAlert(stockText);
                            return;
                        }
                    }
                    for (Map.Entry<String,String> whatever :map.entrySet()){
                        if(whatever.getKey().toUpperCase().contains(stockText)|| whatever.getValue().toUpperCase().contains(stockText)){
                            dadSMaps.put(whatever.getKey(),whatever.getValue());
                            momSList.add(String.format(Locale.getDefault(),"%s - $s",whatever.getKey(),whatever.getValue()));
                        }
                    }

                    if (dadSMaps.size()==0){
                        Toast.makeText(MainActivity.this,"No Stocks",Toast.LENGTH_SHORT).show();
                        stockNotfoundAlert(stockText);
                        return;
                    }
                    else if(dadSMaps.size()==1){
                        String symbol = dadSMaps.keySet().toArray()[0].toString();
                        new StockInfoAsyncTask(MainActivity.this).execute(symbol);

                    }
                    else {
                        final CharSequence[] cs = momSList.toArray(new CharSequence[momSList.size()]);
                        AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
                        build.setTitle("Select Stock");
                        build.setItems(cs, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String stockName = cs[i].toString();
                                String[] keyValue = stockName.split(" ");
                                String symbol = keyValue[0];
                                //This for loop is checking if the user us looking up a duplicate stock
                                for (Stock s : stockList){
                                    if (s.getSymbol().equals(symbol)){
                                        duplicateStockAlert(symbol);
                                        return;
                                    }
                                }
                                new StockInfoAsyncTask(MainActivity.this).execute(symbol);
                            }
                        });
                        build.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this,"Cancel selection: ",Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog select =  build.create();
                        select.show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.setMessage("Please enter stock");
            builder.setTitle("Stock Selection");
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    ///////////////////////////////////////////////////////////OnClick Methods//////////////////////////////////////
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Will open to website");
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock selected = stockList.get(pos);
        Intent i = new Intent(Intent.ACTION_VIEW);
        String symbol = selected.getSymbol();
        i.setData(Uri.parse("http://www.marketwatch.com/investing/stock/"+symbol));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(final View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Note");
        builder.setMessage("Do you want to DELETE this stock?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int pos = recyclerView.getChildLayoutPosition(v);
                Stock stock = stockList.get(pos);
                Toast.makeText(MainActivity.this,"Deleted: "+stock.getSymbol(),Toast.LENGTH_LONG).show();
                stockList.remove(pos);
                stock_adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"Stock saved: ",Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    //////////////////////////////////////RETURNS FROM ASYNC TASKS////////////////////////////////////

    public void takeSymNames(String s){ //Takes in the Symbol and Company names from WEB API
        try {
            JSONArray array = new JSONArray(s);
            for (int i=0;i<array.length();i++){
                JSONObject obj = array.getJSONObject(i);
                String symbol = obj.getString("symbol").trim().toUpperCase();
                String company = obj.getString("name").trim().toUpperCase();
                map.put(symbol,company);
            }
        }catch (JSONException e){e.printStackTrace();}
        Log.d(TAG, "takeSymNames: "+map);
    }

    public void getStackData(String s) {
        try{
            JSONObject obj = new JSONObject(s);
            String symbol = obj.getString("symbol").toUpperCase();
            String company = obj.getString("companyName").toUpperCase();
            Double latestPrice = obj.getDouble("latestPrice");
            Double change = obj.getDouble("change");
            Double changePercent = obj.getDouble("changePercent") * 100;

            if(stockList.stream().anyMatch(stock -> stock.getSymbol().equals(symbol))) {
                for (Stock stock : stockList) {
                    if (stock.getSymbol().equals(symbol)) {
                        stock.setChange(change);
                        stock.setLatestPrice(latestPrice);
                        stock.setChangePercent(changePercent);
                        break;

                    }
                }
            } else {
                Stock newStock = new Stock(symbol, company, latestPrice, change, changePercent);
                stockList.add(newStock);
            }
            Collections.sort(stockList);
            stock_adapter.notifyDataSetChanged();


        } catch(JSONException e) {
            System.out.println("Error retrieving data");
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        try {
            writeFile();
            Toast.makeText(this, "Json file Saved", Toast.LENGTH_SHORT).show();
        }catch (IOException | JSONException e){Toast.makeText(this, "Json File not saved!", Toast.LENGTH_SHORT).show();}
        super.onPause();
    }


    /////////////////////////////////////   READ & WRITE JSON FILE OF SAVED STOCKS     ////////////////////////

    private void writeFile() throws IOException, JSONException{

        FileOutputStream fos =getApplicationContext().openFileOutput(getString(R.string.stocks_json), Context.MODE_PRIVATE);

        JSONArray jsonArray = new JSONArray();

        for (Stock stock : stockList){  //Parse thru Stocklist and add each stock info into a JsonObject
            JSONObject stockJson = new JSONObject();
            stockJson.put("symbol",stock.getSymbol());
            stockJson.put("company",stock.getCompanyName());
            stockJson.put("price",stock.getLatestPrice());
            stockJson.put("priceChange",stock.getChange());
            stockJson.put("percent",stock.getChangePercent());
            jsonArray.put(stockJson);
        }
        String jsonText = jsonArray.toString(); //Turns Json array into a string
        Log.d(TAG, "writeFile: "+jsonText);
        fos.write(jsonText.getBytes());
        fos.close();


    }

    private void readFile() {
        stockList.clear();
        try {
            InputStream is = getApplicationContext().openFileInput(getString(R.string.stocks_json));
            if (is != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = reader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                is.close();

                String jsonText = stringBuilder.toString();
                Log.d(TAG, "readFile: " + jsonText);

                try {
                    JSONArray jsonArray = new JSONArray(jsonText);
                    Log.d(TAG, "readFile: " + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String symbol = obj.getString("symbol").toUpperCase();
                        String company = obj.getString("company").toUpperCase();
                        Double latestPrice = obj.getDouble("price");
                        Double change = obj.getDouble("priceChange");
                        Double changePercent = obj.getDouble("percent") * 100;
                        Stock s = new Stock(symbol, company, latestPrice, change, changePercent);
                        stockList.add(s);

                    }
                    //this for loop will update stocks whenever the app is opened
                    for (Stock stock: stockList){
                        new StockInfoAsyncTask(MainActivity.this).execute(stock.getSymbol().toUpperCase());
                    }
                    Log.d(TAG, "readFile: StockList:"+stockList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////      ALERTS       ////////////////////////////////////

    private void noConnectionAlert() {
        Log.d(TAG, "noConnectionAlert: NO Internet Connection!");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("NO Network Connection!");
        builder.setMessage("Unable to load Stock data.");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void stockNotfoundAlert(String symbol) {
        Log.d(TAG, "stockNotfoundAlert: No Stocks Found!");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("symbol not found:"+symbol);
        builder.setMessage("Data for stock symbol");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void duplicateStockAlert(String symbol) {
        Log.d(TAG, "duplicateStockAlert: Duplicate stock found!");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Stock");
        builder.setMessage(String.format("Stock Symbol %s is already displayed",symbol));
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
