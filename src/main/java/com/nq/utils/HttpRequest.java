package com.nq.utils;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);


    public static String doGet(String url, String params) throws Exception {
        URL localURL = new URL(url + params);
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "application/text");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection

                    .getResponseCode());
        }
        try {
            inputStream = httpURLConnection.getInputStream();
             inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return resultBuffer.toString();
    }

    /*
    *抓數據專用，
    *signature：授權碼
    * timestamp：時間戳
    * url
    */
    public static String doGrabGet(String url) throws Exception {
        // Request URL: http://gateway.jinyi999.cn/rjhy-news/api/1/tcy/news/hotlist?columnCodes=cjyw&appCode=tcy&showPermission=0&limit=20&hasContent=0&pageNo=1
        URL localURL = new URL(url);
        URLConnection connection = localURL.openConnection();

        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection

                    .getResponseCode());
        }
        try {
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            //reader = new BufferedReader(inputStreamReader);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return resultBuffer.toString();
    }


    public static String doPost(String url, Map<String, String> params) throws Exception {
        HttpPost httpPost = new HttpPost(url);

        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (params != null) {
            BasicNameValuePair bnvp = null;
            for (Map.Entry<String, String> p : params.entrySet()) {
                bnvp = new BasicNameValuePair((String) p.getKey(), (String) p.getValue());
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        HttpResponse response = defaultHttpClient.execute(httpPost);
        HttpEntity respEntity = response.getEntity();
        String text = EntityUtils.toString(respEntity, "UTF-8");

        defaultHttpClient.getConnectionManager().shutdown();

        return text;
    }


    public static void main(String[] args) {
        String url = "";
        String ips = "183.32.208.138";

        String getret = "";
        try {
            getret = doGet(url, ips);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("get ret : " + getret);
    }


}
