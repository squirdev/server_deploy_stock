package com.nq.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class BigDecimalUtil {


    public static final DecimalFormat df = new DecimalFormat("#,###.00");

    public static final DecimalFormat df2 = new DecimalFormat("#0.00");



    public static BigDecimal add(double v1, double v2) {

        BigDecimal b1 = new BigDecimal(Double.toString(v1));

        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.add(b2);

    }


    public static BigDecimal sub(double v1, double v2) {

        BigDecimal b1 = new BigDecimal(Double.toString(v1));

        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.subtract(b2);

    }


    public static BigDecimal mul(double v1, double v2) {

        BigDecimal b1 = new BigDecimal(Double.toString(v1));

        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.multiply(b2);

    }


    public static BigDecimal div(double v1, double v2) {

        BigDecimal b1 = new BigDecimal(Double.toString(v1));

        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.divide(b2, 2, 4);

    }

    public static String format(BigDecimal num) {
        if (num == null) {
            return null;
        }
        return df.format(num);
    }

    public static String formatWithNoTQ(BigDecimal num) {
        if (num == null) {
            return null;
        }
        num.setScale(2, BigDecimal.ROUND_HALF_UP);
        return df2.format(num);
    }

    public static String format(BigDecimal num, DecimalFormat format) {
        if (num == null) {
            return null;
        }
        return format.format(num);
    }


    public static double getTwoNum() {

        double num = Math.random();


        return (new BigDecimal(num)).setScale(2, 4).doubleValue();

    }


    public static void main(String[] args) {
        System.out.println(getTwoNum());
    }

}
