import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by hlyin on 27/11/2017.
 */

public class DateConverter {

    public static void main(String[] args){

        Date d = new Date();
        long milliseconds = d.getTime();
        System.out.println(milliseconds);

        DecimalFormat df = new DecimalFormat("0.00"); //2 decimal, make sure have the trailing 0's.
        df.setRoundingMode(RoundingMode.FLOOR);

        Float num = Float.parseFloat("0.1000");
        System.out.println(df.format(num));


        convertDateToLA("Mon, 27 Nov 2017 09:00:54 -0500");

        convertDateWithTimeZone("2017-10-19 16:00:00", "US/Eastern");
    }

    public static String convertDateWithTimeZone(String dateString, String timeZone){
        TimeZone zone = TimeZone.getTimeZone(timeZone);
        if(zone.useDaylightTime()){
            dateString = dateString + " EST";//EST
        }else{
            dateString = dateString + " EDT";//EST
        }
        System.out.println(dateString);
        return dateString;
    }


    /**
     * Convert GMT time to PDT/PST time (Los Angeles time zone)
     * Sample input: "Mon, 27 Nov 2017 09:00:54 -0500"
     * https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
     * template: "EEE, DD MMM yyyy HH:mm:ss Z"
     * @param dateString
     * @return
     */
    public static String convertDateToLA(String dateString){
        String formatPattern = "EEE, dd MMM yyyy HH:mm:ss Z";

        System.out.println(dateString);
        //From
        DateFormat utcFormat = new SimpleDateFormat(formatPattern, Locale.US);
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //"GMT"

        //To
        DateFormat pstFormat = new SimpleDateFormat(formatPattern, Locale.US);
        pstFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        Date date = null;
        try {
            date = utcFormat.parse(dateString);
            dateString = pstFormat.format(date); //Mon, 27 Nov 2017 09:00:54 -0500 => Mon, 27 Nov 2017 01:00:54 -0800
            dateString = dateString.substring(0, dateString.length() - 6); //Mon, 27 Nov 2017 01:00:54
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TimeZone zone = TimeZone.getTimeZone("America/Los_Angeles");
        if(zone.useDaylightTime()){
            dateString = dateString + " PST";//PST
        }else{
            dateString = dateString + " PDT";//PST
        }

        System.out.println(dateString);
        return dateString;
    }







}
