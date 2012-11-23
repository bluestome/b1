
package android.skymobi.messenger.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * @ClassName: DateUtil
 * @author Anson.Yang
 * @date 2012-3-7 下午8:51:50
 */
public class DateUtil extends DateUtils {

    // 完整时间格式
    public static final String FULL_STANDARD_PATTERN = "yyyy.MM.dd.HH.mm.ss";

    /**
     * 返回当前年月
     * 
     * @return [yyyy-mm]
     */
    public static String getCurrentYMDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * 返回当前日期
     * 
     * @return [yyyy-mm-dd]
     */
    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /***
     * 返回年月日 时分秒
     */
    public static String getCurrentYMDHMS() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * 返回当前日期
     * 
     * @return [yyyy-mm-dd]
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowc = new Date();
        String pid = formatter.format(nowc);
        return pid;
    }

    /**
     * 返回long型
     * 
     * @param strDate
     * @return
     */
    public static long getLongTime(String strDate) {
        if (strDate != null && strDate.length() > 0) {
            Date date = getDate(strDate);
            return date.getTime();
        } else {
            return 0;
        }
    }

    /**
     * 返回long型
     * 
     * @param strDate
     * @return
     */
    public static long getLongTimeByStamp(String strDate) {
        if (strDate != null && strDate.length() > 0) {
            Date date = getDateByStamp(strDate);
            return date.getTime();
        } else {
            return 0;
        }
    }

    /**
     * 返回日期型
     * 
     * @param strDate
     * @return
     */
    public static Date getDateByStamp(String strDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(FULL_STANDARD_PATTERN);
            Date date = formatter.parse(strDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    /**
     * 返回日期型
     * 
     * @param strDate
     * @return
     */
    public static Date getDate(String strDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(strDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    /**
     * 返回年份
     * 
     * @param strDate
     * @return
     */
    public static String getYear(String strDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDate(strDate));
        int year = calendar.get(Calendar.YEAR);
        return String.valueOf(year);
    }

    /**
     * 返回月份
     * 
     * @param strDate
     * @return
     */
    public static String getFormatMonth(String strDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDate(strDate));
        int month = calendar.get(Calendar.MONTH) + 1;
        return month < 10 ? "0" + month : String.valueOf(month);
    }

    /**
     * 返回天 例:"09"
     * 
     * @param strDate
     * @return
     */
    public static String getFormatDate(String strDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDate(strDate));
        int date = calendar.get(Calendar.DATE);
        return date < 10 ? "0" + date : String.valueOf(date);
    }

    /**
     * 返回月和日 如:"12月09日"
     * 
     * @param strDate
     * @return
     */
    public static String getFormatMonthAndDate(String strDate) {
        String month = getFormatMonth(strDate);
        String date = getFormatDate(strDate);
        return month.concat("月").concat(date).concat("日");
    }

    /**
     * 返回月和日 例如:"12月09日"
     * 
     * @param birthday
     * @return
     */
    public static String getFormatDate(long dateValue) {
        if (dateValue == 0) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return getFormatMonthAndDate(formatter.format(new Date(dateValue)));
    }

    /**
     * 返回年月日， 如:"2012年12月09日"
     * 
     * @param strDate
     * @return
     */
    public static String getFormatYearMonthAndDate(String strDate) {
        String year = getYear(strDate);
        String month = getFormatMonth(strDate);
        String date = getFormatDate(strDate);
        return year.concat("年").concat(month).concat("月").concat(date).concat("日");
    }

    // 返回100年前的long time时间
    public static long get100YearBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR) - 100;
        int month = calendar.get(Calendar.MONDAY);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day);
        return calendar.getTimeInMillis();
    }

    // 返回100年前的年份
    public static int get100YearBefore2() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR) - 100;
        return year;
    }

    // 返回当前的年份
    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    /**
     * 获取随机的六位数密码
     * 
     * @return
     */
    public static String getRandomPwd() {
        String PASSWD_COTAIN = new String("1234567890");
        Random rd = new Random();
        rd.setSeed((new Date()).getTime());
        char[] temp = new char[6];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = PASSWD_COTAIN.charAt(rd.nextInt(PASSWD_COTAIN.length()));
        }
        return new String(temp);
    }

    // 格式化消息列表时间
    public static String formatTimeForMessageList(Context ctx, long time) {
        Time then = new Time();
        then.set(time);
        Time now = new Time();
        now.setToNow();

        int format_flags = DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_CAP_AMPM |
                DateUtils.FORMAT_24HOUR;
        if (then.yearDay != now.yearDay || then.year != then.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR
                    | DateUtils.FORMAT_SHOW_DATE;
        } else {
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }
        return DateUtils.formatDateTime(ctx, time, format_flags);
    }

    // 格式化聊天记录时间
    public static String formatTimeForChatList(Context ctx, long time) {
        Time then = new Time();
        then.set(time);
        Time now = new Time();
        now.setToNow();
        int format_flags = DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_CAP_AMPM |
                DateUtils.FORMAT_24HOUR;
        if (then.yearDay != now.yearDay || then.year != then.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR
                    | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
        } else {
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }
        return DateUtils.formatDateTime(ctx, time, format_flags);
    }

    /**
     * change the string to date
     * 
     * @param String
     * @param defaultValue
     * @return Date
     */
    public static Date parseDate(String date, String df) {
        return parseDate(date, df, null);
    }

    /**
     * change the string to date
     * 
     * @param String
     * @param df DateFormat
     * @param defaultValue if parse failed return the default value
     * @return Date
     */
    public static Date parseDate(String date, String df, Date defaultValue) {
        if (date == null) {
            return defaultValue;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(df);

        try {
            return formatter.parse(date);
        } catch (ParseException e) {
        }

        return defaultValue;
    }

}
