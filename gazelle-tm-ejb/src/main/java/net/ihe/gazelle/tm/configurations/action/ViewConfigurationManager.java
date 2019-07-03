/*
 * Copyright 2009 IHE International (http://www.ihe.net)
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

package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.util.DocumentFileUpload;
import net.ihe.gazelle.csv.CSVExporter;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfigurationQuery;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Abderrazek Boufahja > INRIA Rennes IHE development Project
 */

@Name("viewConfigurationManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ViewConfigurationManagerLocal")
public class ViewConfigurationManager implements ViewConfigurationManagerLocal, Serializable {

    private static final long serialVersionUID = -8734567233751607120L;

    // ~ Statics variables and Class initializer ////////////////////////////////////////

    private static final Logger LOG = LoggerFactory.getLogger(ViewConfigurationManager.class);

    // methods /////////////////////////////////////////////////////////////////////////

    @Override
    public void getSpecificConfigurationsBySystemKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecificConfigurationsBySystemKeyword");
        }

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String systemKeyword = params.get("systemKeyword");

        String testingSessionIdString = params.get("testingSessionId");

        String configurationType = params.get("configurationType");

        String oid = params.get("oid");

        System sys = System.getSystemByAllKeyword(systemKeyword);
        int testingSessionId = -1;
        if (testingSessionIdString != null) {
            testingSessionId = Integer.parseInt(testingSessionIdString);
        }
        TestingSession selectedTestingSession = TestingSession.GetSessionById(testingSessionId);

        Class<?> selectedClass = null;
        Class<?>[] configurationTypes = AbstractConfiguration.GetAllClassNameArrayOfConfiguration();

        if (configurationType != null) {
            for (Class<?> clazz : configurationTypes) {
                if (clazz.getCanonicalName().toLowerCase().contains(configurationType.toLowerCase())) {
                    selectedClass = clazz;
                    break;
                }
            }
        }

        String baseUrl = ApplicationPreferenceManager.instance().getApplicationUrl()
                + "systemConfigurations.seam?testingSessionId=";
        if (selectedTestingSession == null) {
            StringBuilder res = new StringBuilder("You need to provide the testing session id as a parameter testingSessionId : ");
            res.append("<br />" + "You can set one of the following id for these testing sessions : <br />");
            List<TestingSession> lts = TestingSession.getPossibleTestingSessions();
            Collections.sort(lts);
            for (TestingSession ts : lts) {
                res.append("<a href=\"").append(baseUrl).append(ts.getId()).append("\">").append(ts.getDescription()).append("</a><br />");
            }

            try {
                DocumentFileUpload.showFile(res.toString().getBytes(StandardCharsets.UTF_8), "error.html", false);
            } catch (IOException e) {
                LOG.warn(e.getMessage());
            }
            return;
        }

        if ((sys == null) && (selectedClass == null) && (oid == null)) {
            StringBuilder res = new StringBuilder("In addition to the parameter testingSessionId, you have to provide a system keyword " +
                    "(systemKeyword) and/or"
                    + " a configuration type (configurationType) parameter<br />"
                    + "To get oid add oid=true in addition to the parameter testingSessionId and/or a system keyword (systemKeyword)<br />");
            res.append("You can ask for the following configuration type : <br />");
            if (configurationTypes != null) {
                for (Class<?> cc : configurationTypes) {
                    res.append("<a href=\"").append(baseUrl).append(testingSessionId).append("&configurationType=").append(cc.getSimpleName())
                            .append("\">").append(cc.getSimpleName()).append("</a><br />");
                }
            }
            res.append("<a href=\"").append(baseUrl).append(testingSessionId).append("&oid=true").append("\">").append("List OID").append("</a><br " +
                    "/>");
            try {
                DocumentFileUpload.showFile(res.toString().getBytes(StandardCharsets.UTF_8), "error.html", false);
            } catch (IOException e) {
                LOG.warn(e.getMessage());
            }
            return;
        }

        if (oid != null) {
            OIDSystemAssignmentQuery query = new OIDSystemAssignmentQuery();
            List<OIDSystemAssignment> loid;
            String result = "In addition to the parameter testingSessionId, you have to provide &oid=true <br />";
            if (sys != null) {
                query.systemInSession().system().keyword().eq(systemKeyword);
            }
            query.systemInSession().testingSession().id().eq(testingSessionId);

            loid = query.getList();
            int line = 0;
            if (loid != null && loid.size() > 0) {
                List exportables = loid;
                result = line == 0 ? "" : result;
                result = result + CSVExporter.exportCSV(exportables);
            } else {
                result = "No result found for this testing session";
            }
            line++;
            String fileName = "ListOID";
            if (systemKeyword != null) {
                fileName = fileName + "_" + systemKeyword;
            }
            fileName = fileName + ".csv";
            try {
                DocumentFileUpload.showFile(result.getBytes(StandardCharsets.UTF_8), fileName, true);
            } catch (IOException e) {
                LOG.warn(e.getMessage());
            }
            return;
        }

        String res = "No configurations for system";
        if (systemKeyword != null) {
        	res = res + " " + systemKeyword;
        }

        if (selectedClass != null) {
            List<? extends AbstractConfiguration> lac = null;
            if (selectedClass.getSimpleName().equals("DicomSCUConfiguration")) {
                DicomSCUConfigurationQuery qq = new DicomSCUConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("DicomSCPConfiguration")) {
                DicomSCPConfigurationQuery qq = new DicomSCPConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("HL7V2InitiatorConfiguration")) {
                HL7V2InitiatorConfigurationQuery qq = new HL7V2InitiatorConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("HL7V3InitiatorConfiguration")) {
                HL7V3InitiatorConfigurationQuery qq = new HL7V3InitiatorConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("HL7V2ResponderConfiguration")) {
                HL7V2ResponderConfigurationQuery qq = new HL7V2ResponderConfigurationQuery();
                if (sys != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("HL7V3ResponderConfiguration")) {
                HL7V3ResponderConfigurationQuery qq = new HL7V3ResponderConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("WebServiceConfiguration")) {
                WebServiceConfigurationQuery qq = new WebServiceConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("SyslogConfiguration")) {
                SyslogConfigurationQuery qq = new SyslogConfigurationQuery();
                if (systemKeyword != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            } else if (selectedClass.getSimpleName().equals("RawConfiguration")) {
                RawConfigurationQuery qq = new RawConfigurationQuery();
                if (sys != null) {
                    qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
                }
                qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
                lac = qq.getList();
            }
            int line = 0;
            if ((lac != null) && (lac.size() > 0)) {
                List exportables = lac;
                res = line == 0 ? "" : res;
                res = res + CSVExporter.exportCSV(exportables);
            }
            line++;

        } else {
            AbstractConfigurationQuery qq = new AbstractConfigurationQuery();
            qq.configuration().systemInSession().system().keyword().eq(systemKeyword);
            qq.configuration().systemInSession().testingSession().id().eq(testingSessionId);
            List<AbstractConfiguration> lac = qq.getList();

            int line = 0;
            if ((lac != null) && (lac.size() > 0)) {
                List exportables = lac;
                res = line == 0 ? "" : res;
                res = res + CSVExporter.exportCSV(exportables);
            }
            line++;
        }

        String fileName = "";
        if (systemKeyword != null) {
            fileName = fileName + systemKeyword;
        }
        if (selectedClass != null) {
            if (systemKeyword != null) {
                fileName = fileName + "_";
            }
            fileName = fileName + selectedClass.getSimpleName();
        }
        fileName = fileName + ".csv";
        try {
            DocumentFileUpload.showFile(res.getBytes(StandardCharsets.UTF_8), fileName, true);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }

    }

    // destroy method ///////////////////////////////////////////////////////////////////

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
