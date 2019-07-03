/*
 * Copyright 2016 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.tm.gazelletest.testStepData;

import net.ihe.gazelle.EVSclient.EVSClientResultsWrapper;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.evsclient.connector.api.EVSClientServletConnector;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.objects.model.ObjectInstanceValidation;
import net.ihe.gazelle.tls.ws.client.TlsTestResultProviderClient;
import net.ihe.gazelle.tm.gazelletest.bean.TestStepsDataValidation;
import net.ihe.gazelle.tm.gazelletest.model.instance.DataType;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsData;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Class Description : </b>TestStepDataWrapper<br>
 * <br>
 *
 * @author Jean-Francois Labbé / IHE-Europe development Project
 * @version 1.0 - 21/03/16
 * @class TestStepDataWrapper
 * @package net.ihe.gazelle.tm.gazelletest.testStepData
 * @see jean-francois.labbe@ihe-europe.net - http://gazelle.ihe.net
 */
public class TestStepDataWrapper implements Serializable, Comparable<TestStepDataWrapper> {

    private static final long serialVersionUID = -6857998051876994190L;
    private static final Logger LOG = LoggerFactory.getLogger(TestStepDataWrapper.class);
    private TestStepsData tsd;
    private TestStepsDataValidation testStepDataValidationResult;

    public TestStepDataWrapper(TestStepsData tsd) {
        this.tsd = tsd;
    }

    public TestStepsData getTsd() {
        return tsd;
    }

    public void setTsd(TestStepsData tsd) {
        this.tsd = tsd;
    }

    public boolean sampleValid() {
        return getStatus().toLowerCase().contains("sample valid");
    }

    public boolean notToDo() {
        return getStatus().toLowerCase().contains("not to do");
    }

    public boolean statusPassed() {
        return getStatus().toLowerCase().contains("passed");
    }

    public boolean statusFailed() {
        return getStatus().toLowerCase().contains("failed");
    }

    public boolean statusNotPerformed() {
        return getStatus().toLowerCase().contains("not performed");
    }

    public boolean sampleExamined() {
        return getStatus().toLowerCase().contains("valid");
    }

    public String getPermanentLink() {
        return getTestStepsDataValidation().getPermalink();
    }

    public String getDisplay() {
        return getDisplayLoaded();
    }

    public String getHTMLHeight() {
        String display = getDisplay();
        if ((display != null) && (display.length() > 100)) {
            return "height: 48px; ";
        } else {
            return "";
        }
    }

    public Date getLastCheck() {
        return EVSClientResultsWrapper.getLastCheck(tsd.getId());
    }

    public String getDisplayLoaded() {
        return getTestStepsDataValidation().getDisplay();
    }

    public String getStatus() {
        return getTestStepsDataValidation().getStatus();
    }

    public void refresh() {
        EVSClientResultsWrapper.cleanCache(tsd.getId());
        getTestStepsDataValidation();
    }

    private TestStepsDataValidation getTestStepsDataValidation() {
        if (testStepDataValidationResult == null) {
            testStepDataValidationResult = computeTestStepsDataValidation();
        }
        return testStepDataValidationResult;
    }

    private TestStepsDataValidation computeTestStepsDataValidation() {
        String permalink = null;
        String status = null;

        String display;

        String value = tsd.getValue();
        String comment = tsd.getComment();

        DataType dataType = tsd.getDataType();
        ObjectInstance objectInstance = null;
        if (dataType.getKeyword().equals(DataType.OBJECT_DT)) {
            if (StringUtils.isNumeric(value)) {
                objectInstance = ObjectInstance.getObjectInstanceById(Integer.parseInt(value));
            }
            if (objectInstance != null) {
                value = objectInstance.getName();
                if (objectInstance.getValidation() != null && objectInstance.getValidation().getValue() != null) {
                    status = objectInstance.getValidation().getValue();
                } else {
                    status = ObjectInstanceValidation.SAMPLE_NOT_YET_EXAMINATED;
                }
                permalink = tsd.createURL();
            } else {
                value = "?";
                if (tsd.getComment() == null || tsd.getComment().isEmpty()) {
                    tsd.setComment("This sample is no more reachable, it may have been deleted");
                }
                if (tsd.getValue() != null && !tsd.getValue().contains("deleted")) {
                    tsd.setValue(tsd.getValue() + " (deleted)");
                }
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                entityManager.merge(tsd);
                entityManager.flush();
                status = "sample not available";
            }
        } else if (dataType.getKeyword().equals(DataType.PROXY_DT)) {
            // Get proxy Id and test it
            String proxyId = null;
            String reg = "^(.*id=)([0-9]+)(.*)$";
            Pattern p = Pattern.compile(reg);
            Matcher m = p.matcher(value);
            if (m.find()) {
                proxyId = m.group(2);
            }
            if ((proxyId != null) && !proxyId.isEmpty()) {
                status = getLastResultStatus(proxyId);
                permalink = getValidationPermanentLinkByProxyId(proxyId);
            } else {
                LOG.error("Wrong proxy Id !");
            }
        } else if (dataType.getKeyword().equals(DataType.GSS_DT)) {
            // Get tls Id and test it
            String tlsId = null;
            String reg = "^(.*id=)([0-9]+)(.*)$";
            Pattern p = Pattern.compile(reg);
            Matcher m = p.matcher(value);
            if (m.find()) {
                tlsId = m.group(2);
            }
            if ((tlsId != null) && !tlsId.isEmpty()) {
                status = getTLSValidationStatus(tsd, tlsId);
                permalink = getLink();
            } else {
                LOG.error("Wrong gss Id !");
            }
        } else if (dataType.getKeyword().equals(DataType.EVS_DT)) {
            // Get oid and test it
            String oid = null;
            String privacyKey = null;
            String reg = "^(0|[1-9]\\d*)\\.((0|[1-9]\\d*)\\.)*(0|[1-9]\\d*)$";
            String regOid = "^(.*oid=)([0-9]\\.([0-9]+\\.)*([0-9]+))(.*)$";

            oid = value.replaceAll(regOid, "$2");
            if (oid.matches(reg) && (oid != null)) {

                if (value.contains("privacyKey")) {
                    String regPrivKey = "^(.*privacyKey=)(([0-9a-zA-Z]{16}))(.*)$";
                    privacyKey = value.replaceAll(regPrivKey, "$2");
                    permalink = getValidationPermanentLink(oid) + "&privacyKey=" + privacyKey;
                } else {
                    permalink = getValidationPermanentLink(oid);
                }
                status = getValidationStatus(oid);
            } else {
                LOG.error("Wrong OID !");
            }
        } else if (dataType.getKeyword().equals(DataType.URL)) {
            status = "not to do";
            permalink = null;
        } else if (dataType.getKeyword().equals(DataType.FILE_DT)) {
            status = getLastResultStatusByTmId(tsd.getUniqueKey());
            permalink = getValidationPermanentLinkByTmId(tsd.getUniqueKey());
        }

        if (StringUtils.isNotEmpty(comment)) {
            display = value + " (" + comment + ")";
        } else {
            display = value;
        }

        return new TestStepsDataValidation(permalink, display, status);
    }

    public void getValidationLink() {
        if (tsd.isFile()) {
            EVSClientServletConnector.sendToValidation(tsd, FacesContext.getCurrentInstance().getExternalContext(),
                    getEvsClientUrl(), ApplicationPreferenceManager.getStringValue("app_instance_oid"));
        }
    }

    public String getLink() {
        if (tsd.isFile()) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "testInstanceData/ti/" + tsd.getId()
                    + "/" + getURLValue();
        }
        return tsd.createURL();
    }

    public String getURLValue() {
        try {
            return URIUtil.encodePath(tsd.getValue());
        } catch (URIException e) {
            return "file";
        }
    }

    public String getValidationPermanentLink(String oid) {
        String url = getEvsClientUrl();
        String result = "";
        if (url != null && !url.isEmpty()) {
            result = EVSClientResultsWrapper.getValidationPermanentLink(oid, url, tsd.getId());
        }
        return result;
    }

    public String getValidationStatus(String oid) {
        String url = getEvsClientUrl();
        String result = "";
        if (url != null && !url.isEmpty()) {
            result = EVSClientResultsWrapper.getValidationStatus(oid, url, tsd.getId());
        }
        return result;
    }

    public String getLastResultStatus(String proxyId) {
        String url = getEvsClientUrl();
        String result = "";
        if (url != null && !url.isEmpty()) {
            result = EVSClientResultsWrapper.getLastResultStatusByExternalId(proxyId,
                    ApplicationPreferenceManager.getStringValue("gazelle_proxy_oid"), url, tsd.getId());
        }
        return result;
    }

    public String getValidationPermanentLinkByProxyId(String proxyId) {
        String url = getEvsClientUrl();
        String result = "";
        if (url != null && !url.isEmpty()) {
            result = EVSClientResultsWrapper.getLastResultPermanentLinkByExternalId(proxyId,
                    ApplicationPreferenceManager.getStringValue("gazelle_proxy_oid"), url, tsd.getId());
        }
        return result;
    }

    public String getValidationPermanentLinkByTmId(String tmId) {
        String url = getEvsClientUrl();
        String result = "";
        if (url != null && !url.isEmpty()) {
            result = EVSClientResultsWrapper.getLastResultPermanentLinkByExternalId(tmId,
                    ApplicationPreferenceManager.getStringValue("app_instance_oid"), url, tsd.getId());
        }
        return result;
    }

    public String getLastResultStatusByTmId(String tmId) {
        String url = getEvsClientUrl();
        String result = "";
        if (url != null && !url.isEmpty()) {
            result = EVSClientResultsWrapper.getLastResultStatusByExternalId(tmId,
                    ApplicationPreferenceManager.getStringValue("app_instance_oid"), url, tsd.getId());
        }
        return result;
    }

    public String getTLSValidationStatus(TestStepsData tsd, String id) {
        TlsTestResultProviderClient client = new TlsTestResultProviderClient() {
            @Override
            protected String getTLSApplicationUrl() {
                return ApplicationPreferenceManager.instance().getTlsURL();
            }
        };
        String result = null;
        if (tsd.getValue().contains("connection")) {
            result = client.getTlsConnectionStatus(id);
        } else if (tsd.getValue().contains("testinstance")) {
            result = client.getTlsTestInstanceStatus(id);
        } else {
            result = "not performed";
        }
        return result;
    }

    public String getEvsClientUrl() {
        return ApplicationPreferenceManager.instance().getEVSClientURL();
    }

    @Override
    public int compareTo(TestStepDataWrapper o) {
        if (tsd == null) {
            return 1;
        }
        if (o == null) {
            return -1;
        }
        if (o.tsd.getId() == null) {
            return -1;
        }
        return tsd.getId().compareTo(o.tsd.getId());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
