package net.ihe.gazelle.pr.action;

import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.common.util.DocumentFileUpload;
import net.ihe.gazelle.dates.TimestampNano;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictionNot;
import net.ihe.gazelle.pr.bean.CrawlType;
import net.ihe.gazelle.pr.bean.ISTools;
import net.ihe.gazelle.pr.search.model.ISDownload;
import net.ihe.gazelle.preferences.PreferenceService;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tm.systems.action.IHEImplementationForSystemManagerLocal;
import net.ihe.gazelle.tm.systems.action.SystemManagerLocal;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.UserService;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.Md5Encryption;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.document.ByteArrayDocumentData;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.faces.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Name("prManager")
@Scope(ScopeType.PAGE)
public class PRManager implements Serializable {

    private static final long serialVersionUID = -8375992814320336995L;

    private static final Logger LOG = LoggerFactory.getLogger(PRManager.class);

    private List<System> allSystems = null;

    private List<System> systemsNotReferenced = null;

    private List<System> systemsReferenced = null;

    private List<System> systemsWaitingForValidation = null;

    private System systemShown = null;

    private System selectedSystem = null;

    private boolean selectedSystemAvailableIS;

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public System getSelectedSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystem");
        }
        return selectedSystem;
    }

    public void setSelectedSystem(System selectedSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystem");
        }
        this.selectedSystem = selectedSystem;

        // retrieve IS URL
        if (selectedSystem == null) {
            selectedSystemAvailableIS = false;
        } else {
            String integrationStatementUrl = selectedSystem.getIntegrationStatementUrl();
            if (integrationStatementUrl == null) {
                selectedSystemAvailableIS = false;
            } else {
                selectedSystemAvailableIS = isURLAvailable(integrationStatementUrl);
            }
        }
    }

    public boolean isSelectedSystemAvailableIS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSelectedSystemAvailableIS");
        }
        return selectedSystemAvailableIS;
    }

    public List<System> getAllSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllSystems");
        }
        if (allSystems == null) {
            SystemQuery query = getSystemQuery();
            allSystems = query.getList();
        }
        return allSystems;
    }

    public int getAllSystemsSize() {
        SystemQuery query = getSystemQuery();
        query.isCachable();
        return query.getCount();
    }

    public List<System> getSystemsReferenced() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsReferenced");
        }
        if (systemsReferenced == null) {
            SystemQuery query = getSystemQuery();
            query.addRestriction(IntegrationStatementStatus.systemIntegrationStatementVisible(query));
            systemsReferenced = query.getList();
        }
        return systemsReferenced;
    }

    public int getSystemsReferencedSize() {
        SystemQuery query = getSystemQuery();
        query.isCachable();
        query.addRestriction(IntegrationStatementStatus.systemIntegrationStatementVisible(query));
        return query.getCount();
    }

    public List<System> getSystemsNotReferenced() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsNotReferenced");
        }
        if (systemsNotReferenced == null) {
            SystemQuery query = getSystemQuery();
            query.addRestriction(new HQLRestrictionNot(IntegrationStatementStatus
                    .systemIntegrationStatementVisible(query)));
            systemsNotReferenced = query.getList();
        }
        return systemsNotReferenced;
    }

    public int getSystemsNotReferencedSize() {
        SystemQuery query = getSystemQuery();
        query.addRestriction(new HQLRestrictionNot(IntegrationStatementStatus
                .systemIntegrationStatementVisible(query)));
        query.isCachable();
        return query.getCount();
    }

    public List<System> getSystemsWaitingForValidation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsWaitingForValidation");
        }
        if (systemsWaitingForValidation == null) {
            SystemQuery query = getSystemQuery();
            query.addRestriction(IntegrationStatementStatus.systemIntegrationStatementWaitingForValidation(query));
            systemsWaitingForValidation = query.getList();
        }
        return systemsWaitingForValidation;
    }

    public int getSystemsWaitingForValidationSize() {
        SystemQuery query = getSystemQuery();
        query.addRestriction(IntegrationStatementStatus.systemIntegrationStatementWaitingForValidation(query));
        query.isCachable();
        return query.getCount();
    }

    private SystemQuery getSystemQuery() {
        SystemQuery query = new SystemQuery();
        if (!UserService.hasRole("admin_role")) {
            Institution loggedInInstitution = Institution.getLoggedInInstitution();
            if (loggedInInstitution != null) {
                query.institutionSystems().institution().id().eq(loggedInInstitution.getId());
            } else {
                query.institutionSystems().institution().id().eq(-1);
            }
        }
        return query;
    }

    public String downloadISLabel(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadISLabel");
        }
        String result = ResourceBundle.instance().getString("gazelle.testmanagement.object.Download");

        if (system != null) {
            result = system.getIntegrationStatementDownloadLabel(result);
        }

        return result;
    }

    @Transactional(TransactionPropagationType.REQUIRED)
    public void downloadISOrRedirect(int systemId) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadISOrRedirect");
        }
        SystemQuery query = new SystemQuery();
        query.id().eq(systemId);
        System system = query.getUniqueResult();

        // retrieve IS URL
        if (system == null) {
            return;
        }
        String integrationStatementUrl = system.getIntegrationStatementUrl();
        if (integrationStatementUrl == null) {
            return;
        }

        // count download
        countDownloadIS(system);

        boolean redirect = isURLAvailable(integrationStatementUrl);

        if (redirect) {
            FacesContext.getCurrentInstance().getExternalContext().redirect(integrationStatementUrl);
        } else {
            Redirect.instance().setViewId("/pr/systemImplementations.xhtml");
            Redirect.instance().setParameter("systemId", system.getId());
            Redirect.instance().execute();
        }
    }

    public boolean isURLAvailable(String integrationStatementUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isURLAvailable");
        }
        // check that file is retrievable
        boolean redirect = true;

        try {
            URL url = new URL(integrationStatementUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int statusCode = conn.getResponseCode();
            if (statusCode != 200) {
                redirect = false;
            }

            // request.releaseConnection();
            conn.disconnect();
        } catch (Exception e) {
            redirect = false;
        }
        return redirect;
    }

    public void downloadIS(System system) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadIS");
        }
        if (system == null) {
            return;
        }
        String integrationStatementUrl = system.getIntegrationStatementUrl();
        if (integrationStatementUrl == null) {
            return;
        }
        countDownloadIS(system);
        FacesContext.getCurrentInstance().getExternalContext().redirect(integrationStatementUrl);
    }

    public String getIntegrationStatementSnapshotURL(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationStatementSnapshotURL");
        }
        int id;
        if ((system == null) || (system.getId() == null)) {
            id = 0;
        } else {
            id = system.getId();
        }
        return PreferenceService.getString("application_url") + "isSnapshot.seam?id=" + id;
    }

    protected void countDownloadIS(System system) {

        if (system != null) {

            EntityManager entityManager = EntityManagerService.provideEntityManager();

            system = entityManager.find(System.class, system.getId());

            String remoteIP = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                    .getRemoteAddr();
            String remoteHostname = "";

            try {
                InetAddress address = InetAddress.getByName(remoteIP);

                remoteHostname = address.getHostName();
            } catch (UnknownHostException e) {

            }

            ISDownload isd = new ISDownload();
            isd.setSystem(system);
            isd.setDate(new TimestampNano());
            if (User.loggedInUser() != null) {
                isd.setPerformerUsername(User.loggedInUser().getUsername());
            }

            String browserInfo = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                    .getRequest()).getHeader("user-agent");

            // We truncate the String message if it is too long for column size
            // constraint (database)
            if (browserInfo.length() > 256) {
                browserInfo = browserInfo.substring(0, 256);
            }
            isd.setBrowserInformation(browserInfo);
            isd.setPerformerHostname(remoteHostname);
            isd.setPerformerIpAddress(remoteIP);

            PRSearch prSearch = (PRSearch) Component.getInstance("prSearch");
            if (prSearch != null) {
                if (prSearch.getCurrentSearchLogReport() != null) {
                    isd.setSearchLogReport(prSearch.getCurrentSearchLogReport());
                }
            }

            entityManager.persist(isd);
            entityManager.flush();

        }

    }

    public void getIsStream() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIsStream");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse catalinaResponse = (HttpServletResponse) externalContext.getResponse();
        HttpServletRequest catalinaRequest = (HttpServletRequest) externalContext.getRequest();
        boolean noError = true;
        while (noError) {
            try {
                catalinaResponse = (HttpServletResponse) PropertyUtils.getProperty(catalinaResponse, "response");
            } catch (NoSuchMethodException e1) {
                noError = false;
                // nothing to do
                // we are at org.apache.catalina.connector.ResponseFacade
            }
        }
        noError = true;
        while (noError) {
            try {
                catalinaRequest = (HttpServletRequest) PropertyUtils.getProperty(catalinaResponse, "request");
            } catch (NoSuchMethodException e1) {
                noError = false;
                // nothing to do
                // we are at org.apache.catalina.connector.ResponseFacade
            }
        }

        catalinaResponse.reset();

        // send file
        ServletOutputStream out = catalinaResponse.getOutputStream();
        // Send value to OutputStream

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        final int systemId = Integer.parseInt(params.get("id"));

        SystemQuery query = new SystemQuery();
        query.id().eq(systemId);
        System system = query.getUniqueResult();
        String integrationStatementUrl = "";
        if (system != null) {
            integrationStatementUrl = system.getIntegrationStatementUrl();
        }


        URL url = new URL(integrationStatementUrl);
        URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0");
        urlConnection.connect();
        final InputStream is = urlConnection.getInputStream();
        try {
            IOUtils.copyLarge(is, out);
        } finally {
            is.close();
        }
        out.flush();
        out.close();

        facesContext.responseComplete();
    }

    public void getIsSnapshot() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIsSnapshot");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        final int systemId = Integer.parseInt(params.get("id"));

        SystemQuery query = new SystemQuery();
        query.id().eq(systemId);
        final System system = query.getUniqueResult();
        String integrationStatementUrl = "";
        if (system != null) {
            integrationStatementUrl = system.getIntegrationStatementUrl();
        }

        FileCache.getFile("SystemIS_PDF_" + systemId, integrationStatementUrl, new FileCacheRenderer() {

            @Override
            public void render(OutputStream out, String value) throws Exception {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("render");
                }
                File tempFile = File.createTempFile("integrationStatement", ".pdf");
                try {
                    ISTools.retrieveIS(system, tempFile);
                    BufferedImage thumbnailOfPDF = ISTools.getThumbnailOfPDF(tempFile);
                    ImageIO.write(thumbnailOfPDF, "png", out);
                } catch (Throwable e) {
                    byte[] bytes = null;
                    InputStream resourceAsStream = this.getClass().getResourceAsStream("/nopdf.png");
                    if (resourceAsStream != null) {
                        bytes = IOUtils.toByteArray(resourceAsStream);
                        resourceAsStream.close();
                    }
                    if (bytes != null) {
                        out.write(bytes);
                    }
                }
                if (tempFile.delete()) {
                    LOG.info("tempFile deleted");
                } else {
                    LOG.error("Failed to delete tempFile");
                }
            }

            @Override
            public String getContentType() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getContentType");
                }
                return "image/png";
            }
        });

    }

    public System getSystemShown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemShown");
        }
        if (systemShown == null) {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap();
            final int systemId = Integer.parseInt(params.get("systemId"));

            SystemQuery query = new SystemQuery();
            query.id().eq(systemId);
            systemShown = query.getUniqueResult();
        }
        return systemShown;
    }

    public GazelleListDataModel<ActorIntegrationProfileOption> getAIPOs(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAIPOs");
        }
        if (system != null) {
            SystemQuery query = new SystemQuery();
            query.id().eq(system.getId());
            List<ActorIntegrationProfileOption> result = query.systemActorProfiles().actorIntegrationProfileOption()
                    .getListDistinct();
            Collections.sort(result);
            GazelleListDataModel<ActorIntegrationProfileOption> dm = new GazelleListDataModel<ActorIntegrationProfileOption>(result);
            return dm;
        } else {
            return null;
        }
    }

    public void generatePDF(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generatePDF");
        }
        EntityManagerService.provideEntityManager().merge(systemInSession);
        EntityManagerService.provideEntityManager().merge(systemInSession.getSystem());
        generatePDF(systemInSession.getSystem().getId());
    }

    public void generatePDF(int systemId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generatePDF");
        }
        Redirect redirect = new Redirect();
        redirect.setViewId("/pr/generateIS.xhtml");
        redirect.setParameter("id", systemId);
        redirect.execute();
    }

    public void generatePDF() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generatePDF");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        final int systemId = Integer.parseInt(params.get("id"));

        SystemInSessionQuery query = new SystemInSessionQuery();
        query.system().id().eq(systemId);
        SystemInSession systemInSession = query.getUniqueResult();
        System system = systemInSession.getSystem();

        SystemManagerLocal systemManager = (SystemManagerLocal) Component.getInstance("systemManager");
        systemManager.setSelectedSystemInSession(systemInSession);

        IHEImplementationForSystemManagerLocal iheImplementationForSystemManager = (IHEImplementationForSystemManagerLocal) Component
                .getInstance("iheImplementationForSystemManager");
        iheImplementationForSystemManager.getAllIHEImplementationsForSystemInSession(systemInSession);

        //Set the systemInSessionId in the context to be retrieve in init() from IntegrationStatementManager.java
        Contexts.getEventContext().set("systemInSessionId", systemInSession.getId());
        Renderer.instance().render("/systems/system/integrationStatement.xhtml");
        DocumentStore doc = DocumentStore.instance();
        ByteArrayDocumentData byteData = (ByteArrayDocumentData) doc.getDocumentData("1");
        byte[] pdf = byteData.getData();

        ByteArrayInputStream bis = new ByteArrayInputStream(pdf);
        String md5 = null;
        try {
            md5 = Md5Encryption.calculateMD5ChecksumForInputStream(bis);
        } catch (Exception e) {
            LOG.error("failed to compute md5", e);
        }
        system.setPrChecksumGenerated(md5);

        system.addEvent(SystemEventType.IS_GENERATED, "New integration statement generated with MD5 " + md5,
                UserService.getUsername());

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(system);

        sendPDF(pdf);
    }

    private void sendPDF(byte[] pdf) throws Exception {
        DocumentFileUpload.showFile(pdf, "IntegrationStatement.pdf", true);
    }

    public void publish(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("publish");
        }
        PRCrawlerManagerLocal prCrawlerManager = (PRCrawlerManagerLocal) Component.getInstance("prCrawlerManager");
        prCrawlerManager.crawlSystem(system, CrawlType.VENDOR);
    }

}
