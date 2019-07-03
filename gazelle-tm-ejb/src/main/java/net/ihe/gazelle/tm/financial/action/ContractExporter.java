package net.ihe.gazelle.tm.financial.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.SimpleFileResolver;
import org.jboss.seam.annotations.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Synchronized(timeout = 10000)
public class ContractExporter {

    private static final Logger LOG = LoggerFactory.getLogger(ContractExporter.class);

    public static Long exportContractToPDF(Integer institutionId, String institutionKeyword, TestingSession ts,
                                           String gazelleDataContractsPath, EntityManager em) throws FileNotFoundException, JRException {
        if ((institutionId == null) || (ts == null)) {
            return null;
        }
        long timestamp = (new Date()).getTime();
        String reportName = ts.getType() + "-" + ts.getZone() + "-" + ts.getYear() + "-" + institutionKeyword + ".pdf";
        String reportNameDest = "CAT" + ts.getYear() + "-" + institutionKeyword + "-" + timestamp + ".pdf";
        String reportDest = gazelleDataContractsPath + File.separatorChar + reportNameDest;

        Map<String, Object> params = new HashMap<String, Object>();

        String subreports = ApplicationPreferenceManager.instance().getGazelleReportsPath();

        params.put("institutionId", institutionId);
        params.put("testingSessionId", ts.getId());
        params.put(JRParameter.REPORT_FILE_RESOLVER, new SimpleFileResolver(new File(subreports)));
        params.put("SUBREPORT_DIR", subreports + File.separatorChar);
        ReportExporterManager.exportToPDFAndSaveOnServer(ts.getPathToContractTemplate(), reportName, reportDest,
                params, em);
        return timestamp;

    }

    /**
     * Generate the invoice from parameters
     *
     * @param em                 entityManager
     * @param invoiceNumber      the number of invoice, checked from the database
     * @param params             list params for jasperreport (companyKeyword, testingSessionId)
     * @param reportSource       "invoice"
     * @param subdirReports      the /opt/gazelle/reports/ folder
     * @param reportName         the name of the invoice generated "invoice.pdf"
     * @param reportDest         the destination of the report "/repdest/invoice.pdf"
     * @param institutionKeyword
     * @param ts                 a testingSession
     * @throws JRException
     * @throws FileNotFoundException
     */
    public static void exportInvoiceToPDF(EntityManager em, int invoiceNumber, String reportSource,
                                          String subdirReports, String reportName, String reportDest, String institutionKeyword, TestingSession ts)
            throws FileNotFoundException, JRException {
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("companyKeyword", institutionKeyword);
        params.put("testingSessionId", ts.getId());
        params.put(JRParameter.REPORT_FILE_RESOLVER, new SimpleFileResolver(new File(subdirReports)));

        File f = new File(reportDest);
        JRPdfExporter exporter = new JRPdfExporter();
        ReportExporterManager.exportToFormatForOutputStream(subdirReports, reportSource, reportName, params, em,
                exporter, "application/pdf", new FileOutputStream(f));
    }

}
