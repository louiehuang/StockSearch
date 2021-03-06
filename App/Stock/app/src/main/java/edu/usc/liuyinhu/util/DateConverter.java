package edu.usc.liuyinhu.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by hlyin on 27/11/2017.
 */

public class DateConverter {


    /**
     * Convert GMT time to PDT/PST time (Los Angeles time zone)<br/>
     * Sample input: "Mon, 27 Nov 2017 09:00:54 -0500"<br/>
     * https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html<br/>
     * template: "EEE, DD MMM yyyy HH:mm:ss Z"<br/>
     * @param dateString
     * @return
     */
    public static String convertDateToLA(String dateString){
        String formatPattern = "EEE, dd MMM yyyy HH:mm:ss Z";
        DateFormat utcFormat = new SimpleDateFormat(formatPattern, Locale.US);
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //"GMT"

        Date date = null;
        try {
            date = utcFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat pstFormat = new SimpleDateFormat(formatPattern, Locale.US);
        pstFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        dateString = pstFormat.format(date); //Mon, 27 Nov 2017 09:00:54 -0500 => Mon, 27 Nov 2017 01:00:54 -0800
        dateString = dateString.substring(0, dateString.length() - 6); //Mon, 27 Nov 2017 01:00:54

        TimeZone zone = TimeZone.getTimeZone("America/Los_Angeles");
        if(zone.useDaylightTime()){
            dateString = dateString + " PST";//PST
        }else{
            dateString = dateString + " PDT";//PST
        }

        return dateString;
    }


    /**
     * fake method, timeZone will always be US/Eastern
     * @param dateString
     * @param timeZone
     * @return
     */
    public static String convertDateWithTimeZone(String dateString, String timeZone){
        TimeZone zone = TimeZone.getTimeZone(timeZone);
        if(zone.useDaylightTime()){
            dateString = dateString + " EST";//EST
        }else{
            dateString = dateString + " EDT";//EST
        }
        return dateString;
    }



}
