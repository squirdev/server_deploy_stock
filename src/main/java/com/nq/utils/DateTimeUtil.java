package com.nq.utils;


import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class DateTimeUtil {
    private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);


    public static final Map<Integer, String> formatMap = Maps.newHashMap();


    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String STANDARD_FORMAT2 = "yyyy-MM-dd HH:mm";


    public static final String YMD_FORMAT = "yyyy-MM-dd";


    public static final String HM_FORMAT = "HH:mm";


    static  {
        formatMap.put(STANDARD_FORMAT.length(), STANDARD_FORMAT);
        formatMap.put(STANDARD_FORMAT2.length(), STANDARD_FORMAT2);
    }

    public static Date parse(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        String formatStr = formatMap.get(dateStr.length());

        if (StringUtils.isEmpty(formatStr)) {
            return null;
        }

        return strToDate(dateStr, formatStr);
    }


    public static Date getCurrentDate() {
        return new Date();
    }





    public static Date strToDate(String dateTimeStr, String formatStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }


    public static String dateToStr(Date date, String formatStr) {
        if (date == null) {
            return "";
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }

    public static Date strToDate(String dateTimeStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    /**
     * 当前日期
     * @param time
     * @return
     */
    public static String dateToStr1(Date time) {
        if (time == null) {
            return "";
        }
        DateTime dateTime = new DateTime(time);
        return dateTime.toString("yyyy-MM-dd");
    }

    /*
     * 將時間轉換為時間戳
     */
    public static String dateToStamp(String time){
        String stamp = "";
        if (!"".equals(time)) {//時間不為空
            try {
                String res;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = simpleDateFormat.parse(time);
                long ts = date.getTime();
                res = String.valueOf(ts);
                return res;
            } catch (Exception e) {
                System.out.println("參數為空！");
            }
        }else {    //時間為空
            long current_time = System.currentTimeMillis();  //獲取當前時間
            stamp = String.valueOf(current_time/1000);
        }
        return stamp;
    }


    /*
     * 將時間戳轉換為時間
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /*獲取當前時間戳*/
    public static String getStampNow() {
        Long startTs = System.currentTimeMillis(); // 當前時間戳
        return  startTs.toString();
    }





    public static Timestamp searchStrToTimestamp(String dateTimeStr) {
        return Timestamp.valueOf(dateTimeStr);
    }


    public static String dateToStr(Date date) {
        if (date == null) {
            return "";
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString("yyyy-MM-dd HH:mm:ss");
    }


    public static Date longToDate(Long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String d = format.format(time);

        Date date = null;
        try {
            date = format.parse(d);
        } catch (Exception e) {
            log.error("datetime utils longToDate error");
        }
        return date;
    }


    public static Date doEndTime(Date begintime, int month) {
        Long begintimelong = Long.valueOf(begintime.getTime() / 1000L);
        log.info("計算時間 傳入時間 = {} , 時間戳 = {}", dateToStr(begintime), begintimelong);

        Long endtimelong = Long.valueOf(begintimelong.longValue() + (2592000 * month));
        Date endtimedate = longToDate(Long.valueOf(endtimelong.longValue() * 1000L));

        log.info("endtime 時間戳 = {},時間 = {} , 格式化時間={}", new Object[]{endtimelong, endtimedate,
                dateToStr(endtimedate)});

        return endtimedate;
    }


    public static String getCurrentTimeMiao() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }
    //获取当前时间戳转年月日时分 秒都是0
    public static String getCurrentTimeMiaoZero() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        String[] split = date.split(" ");
        String[] split1 = split[0].split("-");
        String[] split2 = split[1].split(":");
        String s = split1[0] + "-" + split1[1] + "-" + split1[2] + " " + split2[0] + ":" + split2[1] + ":00";
        //s转编码
        String s1 = null;
        try {
            s1 = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s1;
    }



    public static Date parseToDateByMinute(int minuteTimes) {
        Date nowDate = new Date();
        Long nowtimes = Long.valueOf(nowDate.getTime() / 1000L);

        Long beginTimesLong = Long.valueOf(nowtimes.longValue() - (minuteTimes * 60));
        return longToDate(Long.valueOf(beginTimesLong.longValue() * 1000L));
    }
    //分钟时间戳


    public static boolean isCanSell(Date buyDate, int maxMinutes) {
        Long buyDateTimes = Long.valueOf(buyDate.getTime() / 1000L);

        buyDateTimes = Long.valueOf(buyDateTimes.longValue() + (maxMinutes * 60));

        Long nowDateTimes = Long.valueOf((new Date()).getTime() / 1000L);

        if (nowDateTimes.longValue() > buyDateTimes.longValue()) {
            return true;
        }
        return false;
    }

    public static boolean isCanSell(Date buyDate) {
        log.error("下单时间和最新时间",buyDate,new Date());
        return !sameDate(buyDate, new Date());
    }

    /*日期年月日是否相同*/
    public static boolean sameDate(Date d1, Date d2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        //fmt.setTimeZone(new TimeZone()); // 如果需要設置時間區域，可以在這裏設置
        return fmt.format(d1).equals(fmt.format(d2));
    }

        /**
         * 【參考】https://www.cnblogs.com/zhaoKeju-bokeyuan/p/12125711.html
         * 基於指定日期增加天數
         * @param date
         * @param num 整數往後推，負數往前移
         * @return
         */
    public static Date addDay(Date date,int num) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, num);
        return cal.getTime();
    }

    //获取当前时间 判断是否是周六日 如果是周六日 则获取周五的时间
    public static String getWeekDay() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        if (day == 1) {
            cal.add(Calendar.DATE, -2);
        } else if (day == 7) {
            cal.add(Calendar.DATE, -1);
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    public static LocalDate getWeekDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.minusDays(2);
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.minusDays(1);
        }
        return date;
    }







    public static void main(String[] args) {

        System.out.println(getWeekDay(LocalDate.of(2024, 5, 26)));
    }
}
