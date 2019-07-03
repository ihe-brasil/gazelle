package net.ihe.gazelle.tm.configurations.data;

import net.ihe.gazelle.tm.tee.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author tnabeel
 */
public class ProfileXMLLoader {

    private static final String PROFILE_DIR_PATH = "../gazelle-tm-ear/src/main/scripts/gazelle_hl7_profiles/";
    private static final Logger LOG = LoggerFactory.getLogger(ProfileXMLLoader.class);
    private String DB_PSSWD = "gazelle";
    private String DB_LOGIN = "gazelle";

    private ProfileXMLLoader() {
    }

    public static void main(String[] args) {
        try {
            System.out.println("This is the main method " + new File(".").getAbsolutePath());
            ProfileXMLLoader profileXMLLoader = new ProfileXMLLoader();
            profileXMLLoader.loadProfileXMLs();
        } catch (SQLException e) {
            LOG.error("" + e.getMessage());
        } catch (IOException e) {
            LOG.error("" + e.getMessage());
        }
    }

    private void loadProfileXMLs() throws IOException, SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:postgresql:gazelle-dev", DB_LOGIN, DB_PSSWD);
            stmt = conn
                    .prepareStatement("update tf_hl7_message_profile set profile_content=? where domain_id = ? and actor_id = ? and transaction_id " +
                            "= ? and message_type = ? and hl7_version = ?");

            updateProfileXML(stmt, "ADT_A01.xml", 2, 45, 50, "ADT_A01", "2.3.1");
            updateProfileXML(stmt, "ADT_A04.xml", 2, 45, 50, "ADT_A04", "2.3.1");
            updateProfileXML(stmt, "ADT_A05.xml", 2, 45, 50, "ADT_A05", "2.3.1");
            updateProfileXML(stmt, "ADT_A08.xml", 2, 45, 50, "ADT_A08", "2.3.1");
            updateProfileXML(stmt, "ADT_A40.xml", 2, 45, 50, "ADT_A40", "2.3.1");
            updateProfileXML(stmt, "ACK_A01.xml", 2, 27, 50, "ACK_A01", "2.3.1");
            updateProfileXML(stmt, "ACK_A04.xml", 2, 27, 50, "ACK_A04", "2.3.1");
            updateProfileXML(stmt, "ACK_A05.xml", 2, 27, 50, "ACK_A05", "2.3.1");
            updateProfileXML(stmt, "ACK_A08.xml", 2, 27, 50, "ACK_A08", "2.3.1");
            updateProfileXML(stmt, "ACK_A40.xml", 2, 27, 50, "ACK_A40", "2.3.1");
            updateProfileXML(stmt, "ADT_A31.xml", 2, 27, 52, "ADT_A31", "2.5");
            updateProfileXML(stmt, "ACK_ALL.xml", 2, 36, 91, "ACK", "2.5");
            updateProfileXML(stmt, "QBP_Q23.xml", 2, 67, 51, "QBP_Q23", "2.5");
            updateProfileXML(stmt, "RSP_K23.xml", 2, 27, 51, "RSP_K23", "2.5");
            updateProfileXML(stmt, "QBP_Q22.xml", 2, 36, 70, "QBP_Q22", "2.5");
            updateProfileXML(stmt, "RSP_K22.xml", 2, 37, 70, "RSP_K22", "2.5");
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void updateProfileXML(PreparedStatement stmt, String profileFileName, int domainId, int actorId,
                                  int transactionId, String messageType, String hl7Version) throws SQLException {
        stmt.setString(1, FileUtil.loadFileContents(PROFILE_DIR_PATH + profileFileName));
        stmt.setInt(2, domainId);
        stmt.setInt(3, actorId);
        stmt.setInt(4, transactionId);
        stmt.setString(5, messageType);
        stmt.setString(6, hl7Version);
        int rowsUpdated = stmt.executeUpdate();
        if (rowsUpdated == 0) {
            LOG.error("Failed to update profile record for " + profileFileName);
        }
        if (rowsUpdated > 0) {
            LOG.info("Updated profile record for " + profileFileName);
        }
    }

}
