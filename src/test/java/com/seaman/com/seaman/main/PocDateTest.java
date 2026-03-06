package com.seaman.com.seaman.main;

import com.seaman.utils.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;

public class PocDateTest {


    public static void main(String[] args) {
        String[] dates = new String[] {
                "9999-99-99",
                "2013-02-28",
                "2000-02-45",
                "2000-02-30"
        };

        for (String str : dates) {
            System.out.println(verifyDateFormatYYYYMMDD(str));
        }
    }

    public static java.util.Date verifyDateFormatYYYYMMDD(String input) {

        if (input != null) {
            try {
                java.util.Date ret = sdf.parse(input.trim());
                if (sdf.format(ret).equals(input.trim())) {
                    return ret;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");

    private static void calDate() {

        Date certEndDate = convertStringToDate("2023-02-5 00:00:00", DateUtil.YEAR_MONTH_DATE);

        Calendar certEndDateCal = dateToCalendar(certEndDate);

        LocalDate endDate = LocalDate.of(certEndDateCal.get(Calendar.YEAR),certEndDateCal.get(Calendar.MONTH) + 1, certEndDateCal.get(Calendar.DATE));
        LocalDate today = LocalDate.now();

        System.out.println("End Date :  " + endDate);
        System.out.println("Today : " + today);

        Period age = Period.between(today, endDate);
        int years = age.getYears();
        int months = age.getMonths();
        int days = age.getDays();

        System.out.println("number of days: " + days);
        System.out.println("number of months: " + months);
        System.out.println("number of years: " + years);
    }

    private static Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    public static Date convertStringToDate(String dateStr, String dateFormatStr) {
        if (dateStr == null) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat(dateFormatStr)).parse(dateStr);
            } catch (ParseException var3) {
                return null;
            }
        }
    }

}
