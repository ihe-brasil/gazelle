package net.ihe.gazelle.tm.gazelletest.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Scanner;

public class IpComparator implements Comparator<String> {
    private static final Logger LOG = LoggerFactory.getLogger(IpComparator.class);

    static Long toNumeric(String ip) {
        Scanner sc = null;
        try {
            sc = new Scanner(ip).useDelimiter("\\.");
            return (sc.nextLong() << 24) + (sc.nextLong() << 16) + (sc.nextLong() << 8) + (sc.nextLong());
        } catch (RuntimeException e) {
            return 0L;
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }

    @Override
    public int compare(String ip1, String ip2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compare");
        }
        return toNumeric(ip1).compareTo(toNumeric(ip2));
    }

}
