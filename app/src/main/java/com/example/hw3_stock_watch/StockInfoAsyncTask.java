package com.example.hw3_stock_watch;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StockInfoAsyncTask extends AsyncTask<String,Void,String>{

    private static final String TAG = "API_AsyncTask";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private static final String baseUrl = "https://cloud.iexapis.com/stable/stock/";
    private String APIKey = "API_KEY";


    StockInfoAsyncTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    protected String doInBackground(String... dataIn) {

        Uri dataUrl = Uri.parse(baseUrl + dataIn[0] + "/quote?token=" + APIKey);
        String urlToUse = dataUrl.toString();


        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }


        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: "+s);
        mainActivity.getStackData(s);
    }
}


