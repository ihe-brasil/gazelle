/*
* Copyright 2008 IHE International (http://www.ihe.net)
*
* Licensed under the Apache License, Version 2.0 (the "License")
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.ihe.gazelle.tm.financial.action;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.util.DocumentFileUpload;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.financial.InvoiceCopy;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <b>Class Description : </b>FinancialManager<br>
 * <br>
 * This session bean manage financial activities, such as generate contract and invoice. It corresponds to the Business Layer.
 * <p/>
 * <p/>
 * All operations to implement are done in this class : <li>generateContract</li> <li>generateInvoice</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel (INRIA, FR) - Jean-Baptiste Meyer (INRIA, FR)
 * @version 1.0 - July 15, 2008
 * @class FinancialManager.java
 * @package net.ihe.gazelle.tm.financial.action
 */

@Scope(ScopeType.SESSION)
@Name("invoiceManager")
@GenerateInterface("InvoiceManagerLocal")
public class InvoiceManager implements Serializable, InvoiceManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1L;

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceManager.class);

    // ~ Attributes //

    private List<InvoiceCopy> invoiceCopies;

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public List<InvoiceCopy> getInvoiceCopies() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInvoiceCopies");
        }
        return invoiceCopies;
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public void initListInvoiceCopies() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initListInvoiceCopies");
        }
        invoiceCopies = new ArrayList<InvoiceCopy>();
        List<File> lf = this.getListReportFile();
        if (lf != null) {
            List<String> lins = this.getListInstitutionAsString();
            for (File file : lf) {
                String content = this.invoiceContent(file);
                for (String string : lins) {
                    if (content.contains(string)) {
                        InvoiceCopy ic = new InvoiceCopy();
                        ic.setInstitution(Institution.findInstitutionWithName(string));
                        ic.setYear(this.getYearFromFileCountry(file));
                        ic.setDateGeneration(this.getDateCreation(file));
                        ic.setName(file.getName().split("[\\._]")[0] + ".pdf");
                        ic.setPathName(file.getName());
                        ic.setPath(file.getAbsolutePath());
                        ic.setNumber(getNumberFromName(file.getName()));
                        invoiceCopies.add(ic);
                    }
                }
            }
        }
    }

    private String getNumberFromName(String name) {
        if (name != null) {
            String[] ss = name.split("[\\._-]");
            if (ss.length > 0) {
                String pp = ss[1];
                try {
                    Integer.parseInt(pp);
                } catch (Exception e) {
                    return null;
                }
                return pp;
            }
        }
        return null;
    }

    @Override
    @Restrict("#{s:hasPermission('InvoiceAdminManager', 'saveInvoice', null)}")
    public void displaySelectedInvoiceCopie(InvoiceCopy ic) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displaySelectedInvoiceCopie");
        }
        if (ic != null) {
            DocumentFileUpload.showFile(ic.getPath(), ic.getPathName(), true);
        }
    }

    private Date getDateCreation(File file) {
        if (file != null) {
            String name = file.getName();
            String[] ss = name.split("[_\\.]");
            if (ss != null) {
                if (ss.length >= 3) {
                    long timestamp = Long.parseLong(ss[1]);
                    return new Date(timestamp);
                }
            }
            return new Date(file.lastModified());
        }
        return null;
    }

    private String getYearFromFileCountry(File file) {
        if (file != null) {
            String name = file.getName();
            String[] ss = name.split("[-\\._]");
            if (ss != null) {
                return ss[0].substring(3);
            }
        }
        return null;
    }

    private List<String> getListInstitutionAsString() {
        List<Institution> li = Institution.getPossibleInstitutions();
        List<String> lins = new ArrayList<String>();
        for (Institution ins : li) {
            lins.add(ins.getName());
        }
        return lins;
    }

    private List<File> getListReportFile() {
        File gazelleDataInvoices = new File(ApplicationManager.instance().getGazelleInvoicesPath());
        if (gazelleDataInvoices.exists()) {
            File[] lf = gazelleDataInvoices.listFiles();
            if (lf != null) {
                return Arrays.asList(lf);
            }
        }
        return null;
    }

    private String invoiceContent(File file) {
        StringBuilder res = new StringBuilder();
        try {
            PdfReader reader = new PdfReader(file.getAbsolutePath());
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int i = 0; i < reader.getNumberOfPages(); i++) {
                res.append(extractor.getTextFromPage(i + 1));
            }
        } catch (IOException e) {
            LOG.error("" + e.getMessage());
        }
        return res.toString();
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
