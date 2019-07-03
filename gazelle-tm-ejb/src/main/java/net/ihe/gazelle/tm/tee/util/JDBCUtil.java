package net.ihe.gazelle.tm.tee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Contains utility methods for JDBC
 *
 * @author tnabeel
 */
public class JDBCUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCUtil.class);

    /**
     * Closes various resources.
     *
     * @param stmt
     * @param rs
     * @param conn
     */
    public static void closeResources(Statement stmt, ResultSet rs, Connection conn) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("void closeResources");
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable e) {
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Throwable e) {
            }
        }
    }

}
