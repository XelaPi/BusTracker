package com.blaine.bustracker;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to submit an HTTP request
 *
 * @author Alex Vanyo
 */
class HTTPAsyncTask extends AsyncTask<String, Void, Boolean> {

    private final Resources mResources;
    private final String mUrl;
    private final String mMethod;

    /**
     * @param resources Resource object to call getString
     * @param url       Url extension for specific request
     * @param method    HTTP method, ex. "POST", "GET"
     */
    public HTTPAsyncTask(Resources resources, String url, String method) {
        mResources = resources;
        mUrl = url;
        mMethod = method;
    }

    /**
     * Optional override by subclass, will be called on a successful HTTP request
     *
     * @param jsonObject Returned JSON from HTTP request
     * @throws JSONException
     */
    void onSuccessInBackground(JSONObject jsonObject) throws JSONException {
    }

    @Override
    protected Boolean doInBackground(String... arguments) {
        Map<String, String> params = new HashMap<>();

        for (int i = 0; i < arguments.length; i += 2) {
            params.put(arguments[i], arguments[i + 1]);
        }

        JSONObject json = makeHttpRequest(params);

        try {
            if (json.getInt(mResources.getString(R.string.key_success)) == 1) {
                onSuccessInBackground(json);
                return true;
            }
        } catch (JSONException ex) {
            Log.e(this.getClass().getName(), ex.toString());
        }
        return false;
    }

    /**
     * Utility method to make an HTTP request
     *
     * @param params Map of Key/Value pairs
     * @return JSONObject
     */
    private JSONObject makeHttpRequest(Map<String, String> params) {

        InputStream is = null;
        JSONObject jObj = null;
        String json = "";

        String paramString = "";

        if (!params.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder();
            try {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(param.getKey(), "utf-8")).append('=').append(URLEncoder.encode(param.getValue(), "utf-8")).append('&');
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(this.getClass().getName(), e.toString());
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
            paramString = urlBuilder.toString();
        }

        try {
            if (mMethod.equals("POST")) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(mResources.getString(R.string.url_base) + mUrl).openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod(mMethod);
                BufferedWriter outputStreamWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
                outputStreamWriter.write(paramString);
                outputStreamWriter.close();
                is = httpURLConnection.getInputStream();

            } else if (mMethod.equals("GET")) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(mResources.getString(R.string.url_base) + mUrl + "?" + paramString).openConnection();
                is = httpURLConnection.getInputStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            json = sb.toString();

        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        // return JSON String
        return jObj;
    }
}
