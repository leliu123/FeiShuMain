package com.feishu.AIChat.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.text.TextUtils;
import android.util.Log;

public class TimeUtil {

    private static final String TAG = "TimeUtils";

    private static String[] weeks = new String[] {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public static String formatTime(long time, TimeUnit timeUnit) {
        try {
            if (time == 0) {
                return "00:00";
            }
            int unit = 1;
            if (timeUnit == TimeUnit.MILLISECONDS) {
                unit = 1000;
            } else if (timeUnit == TimeUnit.SECONDS) {
                unit = 1;
            }
            String min = (int) (time / unit / 60) + "";
            if (Integer.valueOf(min) < 10) {
                min = "0" + min;
            }

            String seconds = (time - time / unit / 60 * 60 * unit) / unit + "";
            if (Integer.valueOf(seconds) < 10) {
                seconds = "0" + seconds;
            }
            return min + ":" + seconds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatTime(long time) {
        return formatTime(time, TimeUnit.MILLISECONDS);
    }

    /**
     * 返回格式化时间（hh:mm:ss or mm:ss)
     *
     * @param seconds 输入秒数
     * @return String
     */
    public static String getTime(int seconds, boolean isShowHour) {
        if (seconds <= 0) {
            if (isShowHour) {
                return "00:00:00";
            }
            return "00:00";
        }

        int hour = seconds / 3600;
        int minute = (seconds % 3600) / 60;
        int second = seconds % 60;

        String hourStr = (hour < 10 ? "0" : "") + hour;
        String minuteStr = (minute < 10 ? "0" : "") + minute;
        String secondStr = (second < 10 ? "0" : "") + second;

        if (isShowHour) {
            return hourStr + ":" + minuteStr + ":" + secondStr;
        }

        return minuteStr + ":" + secondStr;
    }

    /**
     * 返回格式化时间（HH:mm)
     *
     * @param hour 小时
     * @param min  分钟
     * @return String
     */
    public static String getTime(int hour, int min) {
        if (hour < 0 || min < 0) {
            return "－:－";
        }
        StringBuilder ret = new StringBuilder();
        if (hour < 10) {
            ret.append("0");
        }
        ret.append(hour).append(":");
        if (min < 10) {
            ret.append("0");
        }
        ret.append(min);
        return ret.toString();
    }

    /**
     * 返回格式化日期（年-月-日）
     *
     * @param dateStr 输入时间值的字符串,单位为秒
     * @return String yyyy-mm-dd
     */
    public static String getShortDateStr(String dateStr) {
        Date date = new Date();

        try {
            date.setTime(Long.parseLong(dateStr + "000"));
        } catch (NumberFormatException nfe) {
            Log.w(TAG, "getShortDateStr: " + nfe.getMessage());
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formatDateStr = sdf.format(date);
        return formatDateStr;
    }

    /**
     * 返回时间戳
     *
     * @param time 输入时间08:00:00
     * @return long
     */
    public static long parseTime(String time) {
        if (!TextUtils.isEmpty(time)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            String format1 = format.format(new Date());
            String resultTime = format1 + " " + time;
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                        .parse(resultTime).getTime();
            } catch (Exception e) {
                Log.w(TAG, "", e);
            }
        }
        return 0;
    }

    /**
     * 判断是否为今天
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     */
    public static boolean isToday(String day) {

        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);
        Calendar cal = Calendar.getInstance();
        try {
            Date date = getDateFormat().parse(day);
            cal.setTime(date);
            if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
                int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                        - pre.get(Calendar.DAY_OF_YEAR);

                if (diffDay == 0) {
                    return true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否为昨天
     *
     * @param day 传入的时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     */
    public static boolean isYesterday(String day) {

        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        try {
            Date date = getDateFormat().parse(day);
            cal.setTime(date);

            if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
                int diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);

                if (diffDay == -1) {
                    return true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String updateCurrentTime() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        boolean is24hFormart = true;
        if (!is24hFormart && hour >= 12) {
            hour = hour - 12;
        }

        String time = "";
        if (hour >= 10) {
            time += Integer.toString(hour);
        } else {
            time += "0" + Integer.toString(hour);
        }
        time += ":";

        if (minute >= 10) {
            time += Integer.toString(minute);
        } else {
            time += "0" + Integer.toString(minute);
        }

        return time;
    }

    public static boolean isAm() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (hour > 11) {
            return false;
        } else {
            return true;
        }
    }

    private static SimpleDateFormat getDateFormat() {
        if (null == sDateLocal.get()) {
            sDateLocal.set(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA));
        }
        return sDateLocal.get();
    }

    private static ThreadLocal<SimpleDateFormat> sDateLocal = new ThreadLocal<>();

    public static String formatISO8601Time(String s) {
        String str = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = sd.parse(s);
            str = sdf.format(date);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return str;
        }
        return str;
    }

    /**
     * 将时分秒12：00：00转成时分12：00
     * 减去3是为了截掉最后一个冒号和秒数
     *
     * @return
     */
    public static String getHourAndMinute(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        String hourAndMinute = time.substring(0, time.length() - 3);
        return hourAndMinute;
    }

    /**
     * 获取当天日期，形如:20191112
     *
     * @return
     */
    public static String getCurrentDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        return date;
    }

    /**
     * 获取前一天日期，形如:20191111
     *
     * @return
     */
    public static String getLastDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        return date;
    }

    public static String getTimeByTimeStamp(Long timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeStamp);
        String time = simpleDateFormat.format(date);
        return time;
    }

    /**
     * 获取星期
     *
     * @return
     */
    public static String getWeekDayStr(int weekOfDay) {
        return weeks[weekOfDay - 1];
    }

    private static long tagTime;

    /**
     * 根据当前时间是否大于2019年7月1日判断是否与时间同步
     * @return 是否同步
     */
    public static boolean isTimeSycToServer() {
        try {
            if (tagTime <= 0) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                Date parse = dateFormat.parse("2019-07-01 00:00:00");
                tagTime = parse.getTime();
                Log.d(TAG, "parse: " + tagTime + " / " + System.currentTimeMillis());
            }
            return tagTime < System.currentTimeMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断是上午还是下午
     * @return 上午还是下午文本
     */
    public static String isAMOrPM() {
        GregorianCalendar ca = new GregorianCalendar();
        if (ca.get(GregorianCalendar.AM_PM) == 0) {
            return "上午";
        } else {
            return "下午";
        }
    }

    /**
     * 获取当前小时分钟,格式为16：34
     *
     * @return
     */
    public static String getCurrentHourMinute() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        String h = hour >= 10 ? String.valueOf(hour) : ("0" + hour);
        String m = minute >= 10 ? String.valueOf(minute) : ("0" + minute);
        return h + ":" + m;
    }
}

