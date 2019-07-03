package net.ihe.gazelle.tm.tee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for dates.
 *
 * @author tnabeel
 */
public class DateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    /**
     * Returns a string representing the amount of time elapsed since the provided start time
     *
     * @param start
     * @return
     */
    public static String getElapsedTime(long start) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getElapsedTime");
        }
        return getElapsedTime(start, true);
    }

    /**
     * Returns a string representing the amount of time elapsed since the provided start time
     *
     * @param start
     * @param includeMillis
     * @return
     */
    public static String getElapsedTime(long start, boolean includeMillis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getElapsedTime");
        }
        return getElapsedTimeStr(System.currentTimeMillis() - start, includeMillis);
    }

    /**
     * Returns a string representing the amount of time elapsed
     *
     * @param elapsed
     * @param includeMillis
     * @return
     */
    private static String getElapsedTimeStr(long elapsed, boolean includeMillis) {
        StringBuilder msg = new StringBuilder();
        long hrs = (elapsed / 3600000);
        long mins = (elapsed / 60000);
        long secs = ((elapsed / 1000) - (elapsed / 60000) * 60);
        if (hrs > 0) {
            mins = mins - (hrs * 60);
            secs = secs - (mins * 60);
            msg.append(hrs);
            msg.append(" hr(s) ");
        }
        if (mins > 0) {
            msg.append(mins);
            msg.append(" min(s) ");
        }
        if (secs > 0) {
            msg.append(secs);
            msg.append(" sec(s) ");
        }
        if (includeMillis) {
            long millis = 0;
            if (secs > 0) {
                millis = elapsed % (secs * 1000);
            } else {
                millis = elapsed;
            }
            msg.append(millis);
            msg.append(" ms(s)");
        }
        return msg.toString();
    }

}
