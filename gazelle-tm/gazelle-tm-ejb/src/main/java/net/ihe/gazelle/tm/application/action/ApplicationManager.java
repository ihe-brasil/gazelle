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
package net.ihe.gazelle.tm.application.action;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceLocal;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.pages.Page;
import net.ihe.gazelle.common.pages.menu.Menu;
import net.ihe.gazelle.common.preference.PreferenceType;
import net.ihe.gazelle.common.session.GazelleSessionListener;
import net.ihe.gazelle.common.session.GazelleSessionListener.HttpSessionUser;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.GazelleMenu;
import net.ihe.gazelle.menu.Pages;
import net.ihe.gazelle.preferences.SimplePreferences;
import net.ihe.gazelle.tm.gazelletest.bean.ConnectathonStatisticsBuilder;
import net.ihe.gazelle.tm.gazelletest.bean.PartnersStatisticsBuilder;
import net.ihe.gazelle.tm.messages.MessageManager;
import net.ihe.gazelle.tm.skin.SkinBean;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionQuery;
import net.ihe.gazelle.tm.users.action.UserManagerExtra;
import net.ihe.gazelle.tm.users.action.UserManagerExtraLocal;
import net.ihe.gazelle.users.action.EmailManagerLocal;
import net.ihe.gazelle.users.action.UserManager.EmailType;
import net.ihe.gazelle.users.model.User;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.*;
import org.jboss.seam.async.QuartzDispatcher;
import org.jboss.seam.async.Schedule;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <b>Class Description : </b>ApplicationManager<br>
 * <br>
 * This class manage the application variables. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Get
 * an application version</li> <li>Get an application
 * release date</li> <li>Get application contributors</li> <li>etc...</li>
 *
 * @author Jean-Renan Chatel / INRIA Rennes IHE development Project
 * @version 1.0 - 2008, April 25
 * @class ApplicationManager.java
 * @package net.ihe.gazelle.pr.application.action
 * @see > Jchatel@irisa.fr - http://www.ihe-europe.org
 */

@Name("applicationManager")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@GenerateInterface("ApplicationManagerLocal")
public class ApplicationManager implements Serializable, ApplicationManagerLocal {

    // 30s
    public static final long SCHEDULE_CHECK_INTERVAL_30_seconds = 1000 * 30L;
    private static final String UPDATE_CONNECTATHON_RESULTS = "updateConnectathonResults";
    private static final Integer PING_TIME = 60;
    private static final double PING_TIME_CLEANUP_RATIO = 3;

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -7034446058820515371L;
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationManager.class);
    private static Map<Integer, Date> lastPartnersUpdate = Collections.synchronizedMap(new HashMap<Integer, Date>());
    @In
    private ApplicationPreferenceManager applicationPreferenceManager;
    /**
     * Variable corresponding to the name of the property used to get the Invoices path used for Gazelle, retrieved the database through the
     * ApplicationPreference entity
     */
    private String gazelleInvoicesPathName = "invoices_path";
    /**
     * Variable corresponding to the name of the property used to get the Contracts path used for Gazelle, retrieved the database through the
     * ApplicationPreference entity
     */
    private String gazelleContractsPathName = "contracts_path";
    /**
     * Variable corresponding to the name of the property used to get the Objects path used for Gazelle, retrieved the database through the
     * ApplicationPreference entity
     */
    private String gazelleObjectsPathName = "objects_path";
    /**
     * Variable corresponding to the name of the property used to get the Log Returns path used for Gazelle, retrieved the database through the
     * ApplicationPreference entity
     */
    private String gazelleLogReturnPathName = "log_return_path";
    private Map<Date, Usage> usages;
    private String email = "";
    private String firstName = "";
    private String lastName = "";

    public ApplicationManager() {

    }

    public static ApplicationManager instance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ApplicationManager instance");
        }
        ApplicationManager applicationManager = (ApplicationManager) Component
                .getInstance("applicationManager");
        return applicationManager;
    }

    public String getEmail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEmail");
        }
        return email;
    }

    public void setEmail(String email) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEmail");
        }
        this.email = email.trim();
    }

    public String getFirstName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFirstName");
        }
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFirstName");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLastName");
        }
        return lastName;
    }

    public void setLastName(String lastName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLastName");
        }
        this.lastName = lastName;
    }

    @Override
    @Observer("org.jboss.seam.postInitialization")
    @Transactional
    public void initializeApplication() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeApplication");
        }
        QuartzDispatcher.instance().scheduleTimedEvent(UPDATE_CONNECTATHON_RESULTS,
                new TimerSchedule(SCHEDULE_CHECK_INTERVAL_30_seconds));
        QuartzDispatcher.instance().scheduleTimedEvent("updatePartners",
                new TimerSchedule(SCHEDULE_CHECK_INTERVAL_30_seconds));

        // wait for 2 minutes
        Long duration = 1000 * 120L;

        // execute every 30 secs
        Long intervalDuration = SCHEDULE_CHECK_INTERVAL_30_seconds;
        Schedule schedule = new TimerSchedule(duration, intervalDuration);
        QuartzDispatcher.instance().scheduleTimedEvent("deployEARTimer", schedule);

        QuartzDispatcher.instance().scheduleAsynchronousEvent("updateSchema");

        QuartzDispatcher.instance().scheduleTimedEvent("addUsage", new TimerSchedule(1000L, 10000L));
    }

    @Override
    @Observer("addUsage")
    public void addUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addUsage");
        }
        if (usages == null) {
            // 1 day
            usages = Collections.synchronizedMap(new org.apache.commons.collections.map.LRUMap(3600 * 24));
        }
        try {
            usages.put(new Date(), new Usage());
        } catch (Exception e) {
            LOG.error("Failed to add usage", e);
        }
    }

    @Override
    public Map<Date, Usage> getUsages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsages");
        }
        if (usages == null) {
            return null;
        }
        synchronized (usages) {
            return new TreeMap<Date, Usage>(usages);
        }
    }

    @Override
    @Observer("updateSchema")
    public void updateSchema() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSchema");
        }
        if ("async-update".equals(SimplePreferences.getProperty("hibernate.hbm2ddl.auto"))) {

            List<Class> entities = new ArrayList<Class>();

            EntityManager entityManager = EntityManagerService.provideEntityManager();
            Object delegate = entityManager.getDelegate();

            SessionFactoryImplementor factory = null;

            if (delegate instanceof Session) {
                Session session = (Session) delegate;
                SessionFactory sessionFactory = session.getSessionFactory();
                if (sessionFactory instanceof SessionFactoryImplementor) {
                    factory = (SessionFactoryImplementor) sessionFactory;
                }
            }
            if (factory == null) {
                LOG.error("updateSchema : Delegate is not a session instance");
                return;
            }

            Map<String, ClassMetadata> allClassMetadata = factory.getAllClassMetadata();

            Collection<ClassMetadata> values = allClassMetadata.values();
            for (ClassMetadata classMetadata : values) {
                Class<?> mappedClass = classMetadata.getMappedClass();
                if (mappedClass.getAnnotation(Entity.class) != null) {
                    entities.add(mappedClass);
                }
            }

            String seamName = SimplePreferences.getProperty("seamName") + "-PersistenceUnit";

            Ejb3Configuration cfg = new Ejb3Configuration();
            for (Class<?> persistentClass : entities) {
                cfg.addAnnotatedClass(persistentClass);
            }
            Ejb3Configuration configured = cfg.configure(seamName, null);
            if (configured == null) {
                throw new IllegalStateException("Could not find Ejb3Configuration for '" + seamName + "'");
            }
            Configuration hibernateConfiguration = configured.getHibernateConfiguration();
            SchemaUpdate schemaUpdate = new SchemaUpdate(hibernateConfiguration);
            schemaUpdate.execute(true, true);
        }
    }

    @Override
    @Observer("deployEARTimer")
    public void deployEARTimer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deployEARTimer");
        }
        if (ApplicationPreferenceManager.instance().isDeployScheduled()) {
            Date deployTime = ApplicationPreferenceManager.instance().getDeployTime();
            if (deployTime != null) {
                long now = new Date().getTime();
                long scheduledDeploy = deployTime.getTime();
                long diff = scheduledDeploy - now;
                // deploy interval ?
                if ((diff > 0) && (diff < SCHEDULE_CHECK_INTERVAL_30_seconds)) {
                    QuartzDispatcher.instance().scheduleTimedEvent("deployEARPerform", new TimerSchedule(diff));
                }
            }
        }
    }

    @Override
    @Observer("deployEARPerform")
    public void deployEARPerform() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deployEARPerform");
        }
        startDeploy(ApplicationPreferenceManager.instance().getDeploySource(), ApplicationPreferenceManager.instance()
                .getDeployTarget());
    }

    private void startDeploy(String deploySource, String deployTarget) {
        if ((deploySource != null) && (deployTarget != null)) {
            File srcFile = new File(deploySource);
            File destFile = new File(deployTarget);
            try {
                if (srcFile.exists()) {
                    FileUtils.copyFile(srcFile, destFile);
                } else {
                    throw new IOException(deploySource + " doesn't exist!");
                }
            } catch (IOException e) {
                LOG.error("Failed to perform deploy", e);
            }
        }
    }

    protected boolean performUpdate(Integer testingSessionId) {
        boolean performUpdate = false;

        List<HttpSessionUser> httpSessions = new ArrayList<HttpSessionUser>(GazelleSessionListener.getSessions()
                .values());
        for (HttpSessionUser httpSession : httpSessions) {
            if (!performUpdate) {
                Object testingSessionObject = httpSession.getSession().getAttribute("selectedTestingSession");
                if ((testingSessionObject != null) && (testingSessionObject instanceof TestingSession)) {
                    Integer userTestingSessionId = ((TestingSession) testingSessionObject).getId();
                    if ((userTestingSessionId != null) && userTestingSessionId.equals(testingSessionId)) {
                        performUpdate = true;
                    }
                }
            }
        }
        return performUpdate;
    }

    @Override
    @Observer(UPDATE_CONNECTATHON_RESULTS)
    public void updateConnectathonResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateConnectathonResults");
        }
        if (isTestManagement() && ApplicationPreferenceManager.getBooleanValue("auto_update_results")) {
            TestingSessionQuery testingSessionQuery = new TestingSessionQuery();
            testingSessionQuery.disableAutoResults().neq(true);
            List<Integer> testingSessionIds = testingSessionQuery.id().getListDistinct();

            QuartzDispatcher.instance().scheduleAsynchronousEvent("updateConnectathonResultsList", testingSessionIds);
        } else {
            QuartzDispatcher.instance().scheduleTimedEvent(UPDATE_CONNECTATHON_RESULTS,
                    new TimerSchedule(Long.valueOf(ApplicationPreferenceManager.instance().getCATResultCheckInterval())));
        }
    }

    @Override
    @Observer("updateConnectathonResultsList")
    @Transactional
    public void updateConnectathonResultsList(List<Integer> testingSessionIds) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateConnectathonResultsList");
        }
        if (testingSessionIds.size() > 0) {
            Integer testingSessionId = testingSessionIds.remove(0);
            if (performUpdate(testingSessionId)) {
                TestingSessionQuery testingSessionQuery = new TestingSessionQuery();
                testingSessionQuery.id().eq(testingSessionId);
                TestingSession testingSession = testingSessionQuery.getUniqueResult();
                try {

                    ConnectathonStatisticsBuilder connectathonStatisticsBuilder = new ConnectathonStatisticsBuilder(
                            testingSession);
                    connectathonStatisticsBuilder.updateStatistics();
                } catch (Exception e) {
                    LOG.warn("Failed to update session " + testingSession.getDescription(), e);
                }
            }
            QuartzDispatcher.instance().scheduleAsynchronousEvent("updateConnectathonResultsList", testingSessionIds);
        } else {
            QuartzDispatcher.instance().scheduleTimedEvent(UPDATE_CONNECTATHON_RESULTS,
                    new TimerSchedule(Long.valueOf(ApplicationPreferenceManager.instance().getCATResultCheckInterval())));
        }
    }

    @Override
    @Observer("updatePartners")
    public void updatePartners() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updatePartners");
        }
        if (isTestManagement()) {
            TestingSessionQuery testingSessionQuery = new TestingSessionQuery();
            List<TestingSession> list = testingSessionQuery.getList();
            for (TestingSession testingSession : list) {
                if (performUpdate(testingSession.getId())) {
                    try {

                        PartnersStatisticsBuilder.update(testingSession);
                        lastPartnersUpdate.put(testingSession.getId(), new Date());
                    } catch (Exception e) {
                        LOG.warn("Failed to update partners", e);
                    }
                }
            }
        }
        QuartzDispatcher.instance().scheduleTimedEvent("updatePartners",
                new TimerSchedule(Long.valueOf(ApplicationPreferenceManager.instance().getPartnersCheckInterval())));
    }

    @Override
    public Date getLastPartnersUpdate(Integer testingSessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLastPartnersUpdate");
        }
        return lastPartnersUpdate.get(testingSessionId);
    }

    /**
     * Get the Invoices path. Returns absolute path for Invoices directory depending on the Gazelle Data Path. Example if Gazelle data is
     * /opt/gazelle/data, it returns : /opt/gazelle/data/invoices
     *
     * @return String : absolute path for Invoices directory : eg /opt/gazelle/data/invoices
     */
    @Override
    public String getGazelleInvoicesPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGazelleInvoicesPath");
        }
        return applicationPreferenceManager.getGazelleDataPath() + File.separatorChar
                + ApplicationPreferenceManager.getStringValue(gazelleInvoicesPathName);
    }

    /**
     * Get the Contracts path. Returns absolute path for Contracts directory depending on the Gazelle Data Path. Example if Gazelle data is
     * /opt/gazelle/data, it returns : /opt/gazelle/data/contracts
     *
     * @return String : absolute path for Contracts directory : eg /opt/gazelle/data/contracts
     */
    @Override
    public String getGazelleContractsPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGazelleContractsPath");
        }
        return applicationPreferenceManager.getGazelleDataPath() + File.separatorChar
                + ApplicationPreferenceManager.getStringValue(gazelleContractsPathName);
    }

    /**
     * Get the LOG return path. Returns absolute path for Log return directory depending on the Gazelle Data Path. Example if Gazelle data is
     * /opt/gazelle/data, it returns :
     * /opt/gazelle/data/logReturns
     *
     * @return String : absolute path for Log Returns directory : eg /opt/gazelle/data/logReturns
     */
    @Override
    public String getGazelleLogReturnPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGazelleLogReturnPath");
        }
        return applicationPreferenceManager.getGazelleDataPath() + File.separatorChar
                + ApplicationPreferenceManager.getStringValue(gazelleLogReturnPathName);
    }

    /**
     * Get the Objects path. Returns absolute path for Objects directory depending on the Gazelle Data Path. Example if Gazelle data is
     * /opt/gazelle/data, it returns : /opt/gazelle/data/objects
     *
     * @return String : absolute path for Objects directory : eg /opt/gazelle/data/objects
     */
    @Override
    public String getObjectsPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectsPath");
        }
        return applicationPreferenceManager.getGazelleDataPath() + File.separatorChar
                + ApplicationPreferenceManager.getStringValue(gazelleObjectsPathName);
    }

    @Override
    public boolean isUseMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isUseMessages");
        }
        if (isTestManagement()) {
            return ApplicationPreferenceManager.getBooleanValue("use_messages");
        } else {
            return false;
        }
    }

    @Override
    public Boolean isMasterModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMasterModel");
        }
        return ApplicationPreferenceManager.getBooleanValue("is_master_model");
    }

    @Override
    public Boolean isTestManagement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestManagement");
        }
        return ApplicationPreferenceManager.getBooleanValue("is_test_management");
    }

    @Override
    public Boolean isProductRegistry() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProductRegistry");
        }
        return ApplicationPreferenceManager.getBooleanValue("is_product_registry");
    }

    @Override
    public String getCertificatesUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCertificatesUrl");
        }
        return ApplicationPreferenceManager.getStringValue("certificates_url");
    }

    @Override
    public String getTlsUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTlsUrl");
        }
        return ApplicationPreferenceManager.getStringValue("tls_url");
    }

    @Override
    public String getTlsName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTlsName");
        }

        String name = ApplicationPreferenceManager.getStringValue("tls_url");
        int index = name.lastIndexOf("/") + 1;
        name = name.substring(index, name.length());
        return name;
    }

    @Override
    public String isAssertionEnabled() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAssertionEnabled");
        }
        return ApplicationPreferenceManager.getStringValue("use_assertions");
    }

    @Override
    public String getAssertionRestApi() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAssertionRestApi");
        }
        return ApplicationPreferenceManager.getStringValue("assertions_manager_rest_api");
    }

    @Override
    public String getJiraUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getJiraUrl");
        }
        return ApplicationPreferenceManager.getStringValue("jira_base_url");
    }

    @Override
    public String getJiraTestIssueProject() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getJiraTestIssueProject");
        }
        return ApplicationPreferenceManager.getStringValue("jira_test_issue_project");
    }

    @Override
    public String getJiraReportTestIssueProject() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getJiraReportTestIssueProject");
        }
        return ApplicationPreferenceManager.getStringValue("jira_report_test_issue_project");
    }

    @Override
    public Boolean IsJiraActivated() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("IsJiraActivated");
        }
        return ApplicationPreferenceManager.getBooleanValue("use_jira_test_issues");
    }

    public Boolean isJiraSetup() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isJiraSetup");
        }
        return !("".equals(ApplicationManager.instance().getJiraUrl()) || ""
                .equals(ApplicationManager.instance().getJiraReportTestIssueProject()));
    }

    @Override
    public Boolean isTestsDisplayAsNotLoggedIn() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestsDisplayAsNotLoggedIn");
        }
        return ApplicationPreferenceManager.getBooleanValue("is_tests_display_as_not_logged_in");
    }

    @Override
    public void getOpenSearchDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOpenSearchDescription");
        }
        try {
            FileCache.getFile("OpenSearchDescription", applicationPreferenceManager.getApplicationUrl(),
                    new FileCacheRenderer() {

                        @Override
                        public void render(OutputStream out, String value) throws Exception {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("render");
                            }
                            StringBuilder xml = getXmlOpenSearchDescription();
                            out.write(xml.toString().getBytes(StandardCharsets.UTF_8.name()));
                        }

                        @Override
                        public String getContentType() {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("getContentType");
                            }
                            return "text/xml";
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StringBuilder getXmlOpenSearchDescription() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\" "
                + "xmlns:moz=\"http://www.mozilla.org/2006/browser/search/\" >");
        xml.append("	<ShortName>Gazelle</ShortName>");
        xml.append("	<Description>Search in Gazelle</Description>");
        xml.append("	<Tags>Gazelle</Tags>");
        xml.append("	<Image height=\"16\" width=\"16\">data:image/x-icon;base64,");
        xml.append("AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAD5pfT"
                + "/+KH0//ic8//4mPP/+JPy//iP8f/4ivH/+Ibw//eB7//3fu+w/4T/BL5CgRO/Q4Nqv0ODhb9Dgzm/Q4MB+KH0//id8/"
                + "/4mPP/+JTy//iP8f/4i/H/+Ibw//iC8P/3fe//93nv0+5u3Bm+Q4KPv0OD/L9Dg/+/Q4PYv0ODN/ic8//4mPP/+JTy/"
                + "/iP8f/4i/H/+Ibw//iC8P/3fe//93nu//d179/dXbw6vkOC2b9Dg/+/Q4P/v0OD/79Dg4D4mPP/+JTy//iP8f/4i/H/"
                + "+Ibw//iC8P/3fe//93nu//d07v/3ce7Y4V3DKr9DgsW/Q4P/v0OD/79Dg/i/Q4Nl+JPy//iP8f/4i/H/+Ibw//iC8P/"
                + "3fe//93nu//d17v/3cO3/923tuP9x/ge/Q4JHv0ODxr9Dg9u/Q4OLv0ODEfiP8f/4i/H/+Ibw//iC8P/3fe//93nu//"
                + "d17v/3cO3/92zs/vdp7HAAAAAAAAAAAL9DgxC/Q4MZv0ODBQAAAAD4ivH/+Ibw//iC8P/3fe//93nu//d17v/3cO3/9"
                + "2zs//do7Mr3Z+0bAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA+Ibw//iC8P/3fe//93nu//d17v/3cO3/92zs//do7N73"
                + "Zu1AnTVlFaY6ciimOnIMAAAAAAAAAAAAAAAAAAAAAPiC8P33fe//93nu//d07v/3cO3/92zt+Pdo7Lr5Z/A4pTlwSKY"
                + "6ctGmOnLqpjpyrKY6chwAAAAAAAAAAAAAAAD3fu9h93rvk/h28Kz4cu+q923tjPdq7E37afIOojhsC6Y6cr6mOnL/pj"
                + "py/6Y6cv+mOnJzAAAAAAAAAAAAAAAAAAAAAJEzZgiWN20mlzduHZY1bQIAAAAAAAAAAKY6chCmOnLLpjpy/6Y6cv+mO"
                + "nL/pjpygQAAAAAAAAAAAAAAAIwxYBGMMWCTjDFg5YwxYNaMMWBbAAAAAAAAAAAAAAAApjpycqY6cvSmOnL/pjpy2aY6"
                + "cjQAAAAAAAAAAAAAAACMMWBgjDFg+IwxYP+MMWD/jDFg04wxYBgAAAAAAAAAAKY6cgimOnJDpjpyX6Y6ciqmOnIBAAA"
                + "AAAAAAAAAAAAAjDFgcYwxYP2MMWD/jDFg/4wxYOGMMWAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAIwxYCiMMWDIjDFg/owxYPmMMWCOjDFgBQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAjDFgKIwxYG+MMWBbjDFgEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADsA"
                + "AAAhAAAAIAAAACEAAAAxAAAAfwAAAH8AAAD/AAABjwAAhw8AAP8HAACPjwAAh/8AAIf/AACH/wAA//8AAA==");
        xml.append("</Image>");
        xml.append("	<Url type=\"application/x-suggestions+json\"");
        xml.append("		rel=\"suggestions\" template=\"").append(applicationPreferenceManager.getApplicationUrl())
                .append("search/searchJSON.seam?q={searchTerms}\" />");
        xml.append("	<Url type=\"text/html\"");
        xml.append("		template=\"").append(applicationPreferenceManager.getApplicationUrl())
                .append("search/search.seam?q={searchTerms}\" />");
        xml.append("	<moz:SearchForm>").append(applicationPreferenceManager.getApplicationUrl())
                .append("search/search.seam</moz:SearchForm>");
        xml.append("</OpenSearchDescription>");
        return xml;
    }

    @Override
    @Factory(value = "applicationDefaultColor")
    public String getDefaultColor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDefaultColor");
        }
        String result = ApplicationPreferenceManager.getStringValue("default_color");
        if (result == null) {
            applicationPreferenceManager.setValue(PreferenceType.STRING, "default_color", SkinBean.DEFAULT_COLOR_HTML);
        }
        return result;
    }

    @Override
    // @Factory(value = "gazelleMenu")
    public Menu getMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMenu");
        }
        return GazelleMenu.getMenu();
    }

    @Override
    public boolean isShowHelp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowHelp");
        }
        return getHelpUrl() != null;
    }

    @Override
    public String getHelpUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHelpUrl");
        }
        if (ApplicationPreferenceManager.getBooleanValue("help_show")) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                    .getRequest();
            String servletPath = request.getServletPath();
            String servletPathXHTML = servletPath.replace(".seam", ".xhtml");
            Pages[] pages = Pages.values();
            for (Page page : pages) {
                if (page.getLink().startsWith(servletPathXHTML)) {
                    return ApplicationPreferenceManager.getStringValue("help_url") + page.getId();
                }
            }
        }
        return null;
    }

    @Override
    public long getCloseTabsTime() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCloseTabsTime");
        }
        return Math.round(getPingTime() * PING_TIME_CLEANUP_RATIO);
    }

    @Override
    public long getPingTime() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPingTime");
        }
        Integer result = ApplicationPreferenceManager.getIntegerValue("ping_time");
        if (result == null) {
            result = PING_TIME;
            applicationPreferenceManager.setValue(PreferenceType.INTEGER, "ping_time", result);
        }
        return result;
    }

    @Override
    public boolean isPingEnabled() {
        Boolean value = ApplicationPreferenceManager.getBooleanValue("ping_enabled");
        if (value == null) {
            value = false;
            applicationPreferenceManager.setValue(PreferenceType.BOOLEAN, "ping_enabled", value);
        }
        return value;
    }

    @Override
    public boolean isSendEmailsToAllAdmins() {
        Boolean value = ApplicationPreferenceManager.getBooleanValue("send_emails_to_all_admins");
        if (value == null) {
            value = false;
            applicationPreferenceManager.setValue(PreferenceType.BOOLEAN, "send_emails_to_all_admins", value);
        }
        return value;
    }

    @Override
    public String getViewId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getViewId");
        }
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }

    @Override
    public void setViewId(String viewId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setViewId");
        }
        // noop
    }

    @Override
    public void ping() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ping");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        String viewId = requestParameterMap.get("viewId");
        String viewState = requestParameterMap.get("viewState");

        String offset = requestParameterMap.get("offset");
        HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Object jsTimezoneOffset = session.getAttribute("jsTimezoneOffset");
        if (offset != null) {
            if (!offset.equals(jsTimezoneOffset)) {
                session.setAttribute("jsTimezoneOffset", offset);
            }
        }

        StringBuilder response = new StringBuilder();

        UserManagerExtraLocal userManagerExtra = (UserManagerExtraLocal) Component
                .getInstance("userManagerExtra");
        ApplicationPreferenceLocal applicationPreferenceManager = (ApplicationPreferenceLocal) Component
                .getInstance("applicationPreferenceManager");
        long logoutDateTime = userManagerExtra.getLogoutDateTime();
        long serverDateTime = applicationPreferenceManager.getServerDateTime();

        if ((request.getMethod() != null) && "head".equalsIgnoreCase(request.getMethod())) {

        } else {
            if (!knownPage(fc, viewId, viewState)) {
                response.append("document.getElementById('invalidPageStatus').className='schedule-visible gzl-alert gzl-alert-red';");
                response.append("clearTimeout(nextPingTimeout);");
            } else {
                response.append("startCountDownLogOut(").append(logoutDateTime).append(", ").append(serverDateTime)
                        .append(");\n");
                if (applicationPreferenceManager.isDeployScheduled()) {
                    long deployDateTime = applicationPreferenceManager.getDeployDateTime();
                    response.append("startCountDown(").append(deployDateTime).append(", ").append(serverDateTime)
                            .append(");\n");
                } else {
                    response.append("stopCountDown();");
                }
                if ((User.loggedInUser() != null) && isUseMessages()) {
                    String newMessagesNotificationsBase64 = MessageManager.newMessagesNotificationsBase64();
                    if (!"KFtdKQ==".equals(newMessagesNotificationsBase64)) {
                        response.append("if (window.webkitNotifications) {");
                        response.append("if (window.webkitNotifications.checkPermission() == 0) {");
                        response.append("checkNewMessages('").append(newMessagesNotificationsBase64).append("');");
                        response.append("}");
                        response.append("}");
                    }
                    int unreadCount = MessageManager.getUnreadCount();
                    response.append(
                            "if (document.getElementById('mbf:messageCounterButton')) {document.getElementById('mbf:messageCounterButton').value = ")
                            .append(unreadCount).append(";}\n");
                }
            }
        }

        String result = response.toString();
        try {
            HttpServletResponse catalinaResponse = (HttpServletResponse) fc.getExternalContext().getResponse();
            catalinaResponse.setContentType("text/javascript");
            catalinaResponse.getOutputStream().write(result.getBytes(StandardCharsets.UTF_8.name()));
            fc.responseComplete();
        } catch (Exception e) {

        }
    }

    private boolean knownPage(FacesContext fc, String viewId, String viewState) {
        return true;
    }

    @Override
    public boolean isIncompatibleBrowser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isIncompatibleBrowser");
        }
        ExternalContext ex = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ex.getRequest();
        HttpSession session = request.getSession();
        Boolean isCompatible = (Boolean) session.getAttribute("isCompatible");
        if (isCompatible != null) {
            return isCompatible;
        }
        isCompatible = false;
        try {
            String browserInfo = request.getHeader("user-agent");
            if (browserInfo != null) {
                UserAgent userAgent = UserAgent.parseUserAgentString(browserInfo);
                Browser browser = userAgent.getBrowser().getGroup();
                if ((browser != Browser.SAFARI) && (browser != Browser.CHROME) && (browser != Browser.FIREFOX)) {
                    isCompatible = true;
                }
            }
        } catch (Exception e) {
            isCompatible = false;
        }
        session.setAttribute("isCompatible", isCompatible);
        return isCompatible;
    }

    @Override
    public boolean displayToolTips() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayToolTips");
        }
        if (org.jboss.seam.security.Identity.instance() != null
                && org.jboss.seam.security.Identity.instance().isLoggedIn()) {
            UserManagerExtra um = new UserManagerExtra();
            if (um.getSelectedUserPreferences() != null && !um.getSelectedUserPreferences().getDisplayTooltips()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void testEmail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testEmail");
        }
        EmailManagerLocal emailManager = (EmailManagerLocal) Component.getInstance("emailManager");
        User recipient = new User();
        recipient.setEmail(getEmail());
        recipient.setFirstname(getFirstName());
        recipient.setLastname(getLastName());
        try {
            emailManager.setRecipient(recipient);
            emailManager.sendEmail(EmailType.TO_ADMIN_TO_TEST_EMAIL);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Email sent to " + recipient.getFirstname() + " " + recipient.getLastname());
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to send email !\n" + e);
            LOG.error("" + e);
        }
    }
}
