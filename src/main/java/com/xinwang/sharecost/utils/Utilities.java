package com.xinwang.sharecost.utils;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xinwang on 12/15/17.
 */

public class Utilities {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    public static String getFormattedDateForDisplay(Date date) {
        return simpleDateFormat.format(date);
    }

    public static String getFormattedCentForDisplay(long cent) {
        double dollar = cent / 100d;
        return String.format("%.2f", dollar);
    }

    public static long getCentValueFromString(String dollar) {
        try {
            BigDecimal num = new BigDecimal(dollar);
            num.setScale(2, BigDecimal.ROUND_FLOOR);
            return num.multiply(new BigDecimal("100")).longValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
