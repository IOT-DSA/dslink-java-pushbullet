package org.dsa.iot;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Logan Gorence
 */
public class Utils {

    private static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String epochToISO8601(long epoch) {
        Date date = new Date(epoch * 1000L);
        return isoFormat.format(date);
    }

}
