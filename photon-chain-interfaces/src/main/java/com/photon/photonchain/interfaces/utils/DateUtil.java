package com.photon.photonchain.interfaces.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author: lqh
 * @description: date utils
 * @program: photon-chain
 * @create: 2018-01-17 19:40
 **/
public class DateUtil {


    public static final String DATE_DEFAULT_FORMAT = "yyyy-MM-dd";


    public static final String DATETIME_DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String TIME_DEFAULT_FORMAT = "HH:mm:ss";


    private static DateFormat dateFormat = null;


    private static DateFormat dateTimeFormat = null;

    private static DateFormat timeFormat = null;

    private static Calendar gregorianCalendar = null;

    static {
        dateFormat = new SimpleDateFormat(DATE_DEFAULT_FORMAT);
        dateTimeFormat = new SimpleDateFormat(DATETIME_DEFAULT_FORMAT);
        timeFormat = new SimpleDateFormat(TIME_DEFAULT_FORMAT);
        gregorianCalendar = new GregorianCalendar();
    }



    public static String stampToDate(Long s){
        String res;
        long lt = new Long(s);
        Date date = new Date(lt);
        res = dateTimeFormat.format(date);
        return res;
    }


    public static String dateToStamp(String s) throws ParseException{
        String res;
        Date date = dateTimeFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }



    public static Date formatDate(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getDateFormat(Date date) {
        return dateFormat.format(date);
    }


    public static String getDateTimeFormat(Date date) {
        return dateTimeFormat.format(date);
    }


    public static String getTimeFormat(Date date) {
        return timeFormat.format(date);
    }




    public static Date getDateFormat(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Date getDateTimeFormat(String date) {
        try {
            return dateTimeFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Date getNowDate() {
        return DateUtil.getDateFormat(dateFormat.format(new Date()));
    }


    public static Date getFirstDayOfWeek() {
        gregorianCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        gregorianCalendar.setTime(new Date());
        gregorianCalendar.set(Calendar.DAY_OF_WEEK, gregorianCalendar.getFirstDayOfWeek()); // Monday
        return gregorianCalendar.getTime();
    }


    public static Date getLastDayOfWeek() {
        gregorianCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        gregorianCalendar.setTime(new Date());
        gregorianCalendar.set(Calendar.DAY_OF_WEEK, gregorianCalendar.getFirstDayOfWeek() + 6); // Monday
        return gregorianCalendar.getTime();
    }


    public static Date getFirstDayOfWeek(Date date) {
        if (date == null) {
            return null;
        }
        gregorianCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.DAY_OF_WEEK, gregorianCalendar.getFirstDayOfWeek()); // Monday
        return gregorianCalendar.getTime();
    }


    public static Date getLastDayOfWeek(Date date) {
        if (date == null) {
            return null;
        }
        gregorianCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.DAY_OF_WEEK, gregorianCalendar.getFirstDayOfWeek() + 6); // Monday
        return gregorianCalendar.getTime();
    }


    public static Date getFirstDayOfMonth() {
        gregorianCalendar.setTime(new Date());
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return gregorianCalendar.getTime();
    }


    public static Date getLastDayOfMonth() {
        gregorianCalendar.setTime(new Date());
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, 1);
        gregorianCalendar.add(Calendar.MONTH, 1);
        gregorianCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return gregorianCalendar.getTime();
    }


    public static Date getFirstDayOfMonth(Date date) {
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return gregorianCalendar.getTime();
    }


    public static Date getLastDayOfMonth(Date date) {
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, 1);
        gregorianCalendar.add(Calendar.MONTH, 1);
        gregorianCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return gregorianCalendar.getTime();
    }


    public static Date getDayBefore(Date date) {
        gregorianCalendar.setTime(date);
        int day = gregorianCalendar.get(Calendar.DATE);
        gregorianCalendar.set(Calendar.DATE, day - 1);
        return gregorianCalendar.getTime();
    }


    public static Date getDayAfter(Date date) {
        gregorianCalendar.setTime(date);
        int day = gregorianCalendar.get(Calendar.DATE);
        gregorianCalendar.set(Calendar.DATE, day + 1);
        return gregorianCalendar.getTime();
    }


    public static int getNowYear() {
        Calendar d = Calendar.getInstance();
        return d.get(Calendar.YEAR);
    }


    public static int getNowMonth() {
        Calendar d = Calendar.getInstance();
        return d.get(Calendar.MONTH) + 1;
    }


    public static int getNowMonthDay() {
        Calendar d = Calendar.getInstance();
        return d.getActualMaximum(Calendar.DATE);
    }


    public static List<Date> getEveryDay(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }

        startDate = DateUtil.getDateFormat(DateUtil.getDateFormat(startDate));
        endDate = DateUtil.getDateFormat(DateUtil.getDateFormat(endDate));
        List<Date> dates = new ArrayList<Date>();
        gregorianCalendar.setTime(startDate);
        dates.add(gregorianCalendar.getTime());
        while (gregorianCalendar.getTime().compareTo(endDate) < 0) {
            gregorianCalendar.add(Calendar.DAY_OF_MONTH, 1);
            dates.add(gregorianCalendar.getTime());
        }
        return dates;
    }


    public static Date getFirstMonth(int monty) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -monty);
        return c.getTime();
    }


}
