package com.nq.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HolidayUtil {
    /**
     * @param urlAll
     *            :請求接口
     * @param httpArg
     *            :參數
     * @return 返回結果
     */
    public static String request(String httpArg) {
        String httpUrl = "http://tool.bitefu.net/jiari/";
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?d=" +httpArg;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*public static void main(String[] args) {
        // 處理節假日
        String httpArg = "20190720";
		*//*SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		httpArg = f.format(new Date());*//*
        String jsonResult = HolidayUtil.request(httpArg);
        // 0 上班  1周末 2節假日
        if ("0".equals(jsonResult)) {
            //return resultObject.getFailResult("上班");
            System.out.println("0上班");
        }
        if ("1".equals(jsonResult)) {
            //return resultObject.getFailResult("1周末");
            System.out.println("1周末");
        }
        if ("2".equals(jsonResult)) {
            //return resultObject.getFailResult("");
            System.out.println("2節假日");
        }
    }*/

}
