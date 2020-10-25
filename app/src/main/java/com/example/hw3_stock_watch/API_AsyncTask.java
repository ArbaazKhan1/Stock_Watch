package com.example.hw3_stock_watch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class API_AsyncTask extends AsyncTask<String,Void,String> {
    private static final String TAG = "API_AsyncTask";

    private MainActivity mainActivity;
    private static final String symbolNamesAPI = "https://api.iextrading.com/1.0/ref-data/symbols";

    public API_AsyncTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... strings) {

       // String apiEndPoint = dataIn[0];
        Uri buildURL = Uri.parse(symbolNamesAPI);
        String urlToUse = buildURL.toString();


            Log.d(TAG, "doInBackground: " + urlToUse);

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
        mainActivity.takeSymNames(s);
    }
}
