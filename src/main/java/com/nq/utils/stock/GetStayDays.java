package com.nq.utils.stock;

import com.nq.utils.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;


public class GetStayDays {
    private static final Logger log = LoggerFactory.getLogger(GetStayDays.class);


    public static Date getBeginDate(Date beginDate) {
        String date = DateTimeUtil.dateToStr(beginDate, "yyyy-MM-dd");
        return DateTimeUtil.strToDate(date, "yyyy-MM-dd");
    }


    public static int getWorkDays(Date StartDate) {
        Calendar cl1 = Calendar.getInstance();
        Calendar cl2 = Calendar.getInstance();

        Date begin_date = getBeginDate(StartDate);
        Date end_date = getBeginDate(new Date());

        try {
            cl1.setTime(begin_date);
            cl2.setTime(end_date);
        } catch (Exception e) {
            System.out.println("日期格式非法");
            e.printStackTrace();
        }
        int count = 0;
        while (cl1.compareTo(cl2) <= 0) {
            if (cl1.get(7) != 7 && cl1.get(7) != 1)
                count++;
            cl1.add(5, 1);
        }
        return count - 1;
    }


    public static int getDays(Date StartDate) {
        Calendar cl1 = Calendar.getInstance();
        Calendar cl2 = Calendar.getInstance();

        try {
            cl1.setTime(StartDate);
            cl2.setTime(new Date());
        } catch (Exception e) {
            System.out.println("日期格式非法");
            e.printStackTrace();
        }
        int count = 0;
        while (cl1.compareTo(cl2) <= 0) {
            count++;
            cl1.add(5, 1);
        }
        return count - 1;
    }



    public static Integer durationDays(Date startDate) {

        LocalDateTime start = LocalDate.parse(DateTimeUtil.dateToStr(startDate, DateTimeUtil.YMD_FORMAT),
                DateTimeFormatter.ofPattern(DateTimeUtil.YMD_FORMAT)).atStartOfDay();

        LocalDateTime now = LocalDate.now().atStartOfDay();
        return Integer.valueOf(Long.toString(Duration.between(start, now).toDays())) + 1;
    }


    public static void main(String[] args) {
        System.out.println(durationDays(DateTimeUtil.strToDate("2024-05-30", DateTimeUtil.YMD_FORMAT)));
    }


    public static int testWorkDays(Date beginDate, Date endDate) {
        Calendar cl1 = Calendar.getInstance();
        Calendar cl2 = Calendar.getInstance();

        try {
            cl1.setTime(beginDate);
            cl2.setTime(endDate);
        } catch (Exception e) {
            System.out.println("日期格式非法");
            e.printStackTrace();
        }
        int count = 0;
        while (cl1.compareTo(cl2) <= 0) {
            if (cl1.get(7) != 7 && cl1.get(7) != 1)
                count++;
            cl1.add(5, 1);
        }
        return count - 1;
    }

//
//    public static void main(String[] args) throws Exception {
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        Date start = df.parse("2019-03-10");
//
//        System.out.println("排除周末：" + getWorkDays(start));
//        System.out.println("不排除周末： " + getDays(start));
//
//        System.out.println(" -------- ");
//
//        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date begindate = df2.parse("2019-03-10 11:39:09");
//        Date enddate = df2.parse("2019-03-11 07:15:00");
//        log.info("測試的天數 = {}", Integer.valueOf(testWorkDays(begindate, enddate)));
//        log.info("測試的天數 = {}", Integer.valueOf(getDays(begindate)));
//
//
//        System.out.println(DateTimeUtil.dateToStr(getBeginDate(begindate)));
//        System.out.println(DateTimeUtil.dateToStr(getBeginDate(enddate)));
//    }
}
