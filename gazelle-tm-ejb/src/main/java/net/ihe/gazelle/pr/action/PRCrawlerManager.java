package net.ihe.gazelle.pr.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.pr.bean.CrawlType;
import net.ihe.gazelle.pr.bean.CrawlerStatus;
import net.ihe.gazelle.pr.bean.ISTools;
import net.ihe.gazelle.pr.systems.model.CrawlerReporting;
import net.ihe.gazelle.pr.systems.model.PRMail;
import net.ihe.gazelle.pr.systems.model.PRMailQuery;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.users.action.EmailManagerLocal;
import net.ihe.gazelle.users.action.EmailTemplate;
import net.ihe.gazelle.users.action.UserManager.EmailType;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.model.UserQuery;
import net.ihe.gazelle.util.Md5Encryption;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.async.QuartzDispatcher;
import org.jboss.seam.async.Schedule;
import org.jboss.seam.async.TimerSchedule;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.*;

@Name("prCrawlerManager")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@GenerateInterface("PRCrawlerManagerLocal")
public class PRCrawlerManager implements Serializable, PRCrawlerManagerLocal {

    private static final long serialVersionUID = -4666484235155102638L;

    private static final int N_TASKS = 8;

    private static final Logger LOG = LoggerFactory.getLogger(PRCrawlerManager.class);

    private CrawlerReporting currentCrawlerReporting = null;

    @Create
    public void init() {
        // First set the default cookie manager.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    @Override
    public CrawlerReporting getCurrentCrawlerReporting() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentCrawlerReporting");
        }
        return currentCrawlerReporting;
    }

    @Override
    @Observer("org.jboss.seam.postInitialization")
    public void scheduleJobs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("scheduleJobs");
        }
        // wait for 2 minutes
        Long duration = 1000 * 120L;
        // execute every 30 secs
        Long intervalDuration = ApplicationManager.SCHEDULE_CHECK_INTERVAL_30_seconds;
        Schedule schedule = new TimerSchedule(duration, intervalDuration);
        QuartzDispatcher.instance().scheduleTimedEvent("startPRCrawlerTimer", schedule);
        QuartzDispatcher.instance().scheduleTimedEvent("startPRMailerTimer", new TimerSchedule(intervalDuration));
    }

    @Override
    @Observer("startPRMailerTimer")
    @Transactional
    public void startPRMailerTimer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startPRMailerTimer");
        }
        if (ApplicationManager.instance().isProductRegistry()) {
            PRMailQuery prMailQuery = new PRMailQuery();
            prMailQuery.sent().eq(false);
            List<PRMail> mails = prMailQuery.getList();
            for (PRMail systemMail : mails) {
                if (sendMail(systemMail)) {
                    systemMail.setSent(true);
                    EntityManagerService.provideEntityManager().persist(systemMail);
                    EntityManagerService.provideEntityManager().flush();
                }
            }
        }

        QuartzDispatcher.instance().scheduleTimedEvent("startPRMailerTimer",
                new TimerSchedule(ApplicationManager.SCHEDULE_CHECK_INTERVAL_30_seconds));
    }

    private boolean getSendRealMail() {
        return ApplicationPreferenceManager.getBooleanValue("crawler_send_real_mail");
    }

    private boolean sendMail(final PRMail systemMail) {
        EmailManagerLocal emailManager = (EmailManagerLocal) Component.getInstance("emailManager");
        List<User> recipients = null;
        if (getSendRealMail()) {
            if (systemMail.getSystem() != null) {
                Set<Integer> institutionIds = new HashSet<Integer>();
                Set<InstitutionSystem> institutionSystems = systemMail.getSystem().getInstitutionSystems();
                if (institutionSystems != null) {
                    for (InstitutionSystem institutionSystem : institutionSystems) {
                        Institution institution = institutionSystem.getInstitution();
                        if (institution != null) {
                            institutionIds.add(institution.getId());
                        }
                    }
                    UserQuery query = new UserQuery();
                    query.roles().id().eq(Role.getVENDOR_ADMIN_ROLE().getId());
                    query.institution().id().in(institutionIds);
                    query.activated().eq(true);
                    recipients = query.getListNullIfEmpty();
                }
            }
        } else {
            UserQuery query = new UserQuery();
            List<String> usernames = new ArrayList<String>();
            usernames.add("glandais");
            usernames.add("epoiseau");
            query.username().in(usernames);
            recipients = query.getList();
        }
        if (recipients == null) {
            UserQuery query = new UserQuery();
            query.roles().id().eq(Role.getADMINISTRATOR_ROLE().getId());
            query.activated().eq(true);
            recipients = query.getList();
        }
        emailManager.setInstitutionAdmins(recipients);

        Contexts.getConversationContext().set("system", systemMail.getSystem());
        Contexts.getConversationContext().set("crawlerReporting", systemMail.getCrawlerReporting());

        try {
            emailManager.sendEmail(new EmailTemplate() {
                @Override
                public String getPageName() {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getPageName");
                    }
                    return systemMail.getPageName();
                }
            });
        } catch (Exception e) {
            LOG.error("Failed to send mail", e);
            return false;
        }
        return true;
    }

    @Override
    @Observer("startPRCrawlerTimer")
    @Transactional
    public void startPRCrawlerTimer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startPRCrawlerTimer");
        }
        if (ApplicationManager.instance().isProductRegistry()) {
            if (ApplicationPreferenceManager.getBooleanValue("pr_crawler_enabled")) {
                Date crawlerStart = ApplicationPreferenceManager.getDateValue("pr_crawler_start");
                if (crawlerStart != null) {
                    Calendar crawler = Calendar.getInstance();
                    crawler.setTime(crawlerStart);

                    Calendar now = Calendar.getInstance();
                    now.setTime(new Date());
                    now.set(Calendar.YEAR, crawler.get(Calendar.YEAR));
                    now.set(Calendar.DAY_OF_YEAR, crawler.get(Calendar.DAY_OF_YEAR));

                    long diff = crawler.getTimeInMillis() - now.getTimeInMillis();
                    if ((diff > 0) && (diff < ApplicationManager.SCHEDULE_CHECK_INTERVAL_30_seconds)) {
                        startCrawling();
                    }
                }
            }
        }
    }

    @Override
    public void startCrawling() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startCrawling");
        }
        if (currentCrawlerReporting == null) {

            SystemQuery query = new SystemQuery();
            query.addRestriction(IntegrationStatementStatus.systemIntegrationStatementNeedCrawling(query));
            List<Integer> ids = query.id().getListDistinct();
            CrawlerReporting crawlerReporting = new CrawlerReporting(new Date());

            currentCrawlerReporting = crawlerReporting;

            crawlerReporting.setSystemIdsToCrawl(ids);
            crawlerReporting.setCrawlingCounter(N_TASKS);
            for (int i = 0; i < N_TASKS; i++) {
                QuartzDispatcher.instance().scheduleAsynchronousEvent("crawlNextSystem", crawlerReporting);
            }

            QuartzDispatcher.instance().scheduleTimedEvent("logCrawlerProgress", new TimerSchedule(1000 * 5L),
                    crawlerReporting);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

            }
        }
    }

    @Override
    @Observer("logCrawlerProgress")
    public void logCrawlerProgress(CrawlerReporting crawlerReporting) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("logCrawlerProgress");
        }
        if (crawlerReporting.getCrawlingCounter() != 0) {

            QuartzDispatcher.instance().scheduleTimedEvent("logCrawlerProgress", new TimerSchedule(1000 * 5L),
                    crawlerReporting);
        }
    }

    @Override
    @Observer("crawlNextSystem")
    @Transactional
    public void crawlNextSystem(CrawlerReporting crawlerReporting) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("crawlNextSystem");
        }
        Integer systemId = null;
        synchronized (crawlerReporting) {
            if (crawlerReporting.getSystemIdsToCrawl().size() == 0) {
                // this "thread" is other
                crawlerReporting.setCrawlingCounter(crawlerReporting.getCrawlingCounter() - 1);
                if (crawlerReporting.getCrawlingCounter() == 0) {
                    // all are finished
                    long elapsed = new Date().getTime() - crawlerReporting.getExecutionStartingDate().getTime();
                    crawlerReporting.setExecutionElapsedTime((int) elapsed);
                    EntityManagerService.provideEntityManager().persist(crawlerReporting);
                    EntityManagerService.provideEntityManager().flush();

                    PRStatusManagerLocal prStatusManager = (PRStatusManagerLocal) Component
                            .getInstance("prStatusManager");
                    prStatusManager.sendMail(EmailType.TO_ADMINS_ON_CRAWLER_END, crawlerReporting,
                            "Crawler is finished");

                    currentCrawlerReporting = null;
                } else {

                }
            } else {

                systemId = crawlerReporting.getSystemIdsToCrawl().remove(0);
            }
        }
        if (systemId != null) {
            crawlSystem(systemId, crawlerReporting);
        }
    }

    @Override
    public void crawlSystem(Integer systemId, CrawlerReporting crawlerReporting) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("crawlSystem");
        }
        CrawlerStatus crawlerStatus = null;
        System system = null;

        IntegrationStatementStatus oldStatus = null;
        IntegrationStatementStatus newStatus = null;

        try {
            SystemQuery query = new SystemQuery();
            query.id().eq(systemId);
            system = query.getUniqueResult();

            if (system != null) {

                oldStatus = system.getPrStatus();
                crawlerStatus = crawlSystem(system, CrawlType.CRAWLER);
                newStatus = system.getPrStatus();

            }
        } catch (Throwable e) {
            if (system != null && system.getName() != null) {
                LOG.error("Failed to crawl system " + system.getName(), e);
            } else {
                LOG.error("Failed to crawl system ", e);
            }
        }

        if (crawlerReporting != null) {

            synchronized (crawlerReporting) {
                if (crawlerStatus != null) {
                    switch (crawlerStatus) {
                        case MATCH:
                        case NOT_MATCHABLE:
                            crawlerReporting.setNumberOfSuccessfullyChecked(crawlerReporting
                                    .getNumberOfSuccessfullyChecked() + 1);
                            break;
                        case MISMATCH:
                            crawlerReporting.getUnmatchingHashcodeForIntegrationStatements().add(system);
                            break;
                        case UNREACHABLE:
                            crawlerReporting.getUnreachableIntegrationStatements().add(system);
                            break;
                        default:
                            break;
                    }
                }

                if (oldStatus != newStatus) {
                    crawlerReporting.getChangedIntegrationStatements().add(system);
                }

                crawlerReporting.setNumberOfCheckedIntegrationStatements(crawlerReporting
                        .getNumberOfCheckedIntegrationStatements() + 1);

                QuartzDispatcher.instance().scheduleAsynchronousEvent("crawlNextSystem", crawlerReporting);
            }

        }
    }

    @Override
    public CrawlerStatus crawlSystem(System system, CrawlType crawlType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("crawlSystem");
        }

        CrawlerStatus crawlerStatus = getCrawlerStatus(system);

        String login = getLogin();
        system.addEvent(SystemEventType.PR_CRAWLER, "Crawled integration statement : " + crawlerStatus, login);

        if (system.getPrStatus() == IntegrationStatementStatus.CREATED) {
            system.setPrStatus(IntegrationStatementStatus.PUBLISHED);
        }

        PRStatusManagerLocal prStatusManager = (PRStatusManagerLocal) Component.getInstance("prStatusManager");
        prStatusManager.newCrawlerStatus(system, crawlerStatus, crawlType);

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(system);
        entityManager.flush();

        return crawlerStatus;
    }

    protected String getLogin() {
        String login = null;
        if (User.loggedInUser() != null) {
            login = User.loggedInUser().getUsername();
        } else {
            login = "PRCrawler";
        }
        return login;
    }

    private CrawlerStatus getCrawlerStatus(System system) {
        CrawlerStatus result = null;

        File tempFile = null;
        try {
            tempFile = File.createTempFile("integrationStatement", ".pdf");
        } catch (IOException e1) {
            throw new RuntimeException("Failed to create temp file", e1);
        }

        try {
            retrieveIS(system, tempFile, 3);
            result = verifyIS(system, tempFile);
        } catch (Throwable e) {

            system.addEvent(SystemEventType.PR_CRAWLER, "Failed to crawl integration statement : " + e.getMessage(),
                    "PRCrawler");
            // unable to retrieve file
            result = CrawlerStatus.UNREACHABLE;
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to crawl integration statement");
            LOG.error("getCrawlerStatus() : " + e.getMessage());
        }

        if (tempFile.delete()) {
            LOG.info("tempFile deleted");
        } else {
            LOG.error("Failed to delete tempFile");
        }

        return result;
    }

    private void retrieveIS(System system, File tempFile, int testsLeft) throws Throwable {
        try {
            ISTools.retrieveIS(system, tempFile);
        } catch (Throwable e) {
            if (testsLeft == 0) {
                throw e;
            } else {
                retrieveIS(system, tempFile, testsLeft - 1);
            }
        }
    }

    private CrawlerStatus verifyIS(System system, File tempFile) {
        // first check that the file is a PDF
        // tempFile
        try {
            final BufferedImage thumbnailOfPDF = ISTools.getThumbnailOfPDF(tempFile);
            FileCache.forceCache("SystemIS_PDF_" + system.getId(), system.getIntegrationStatementUrl(),
                    new FileCacheRenderer() {
                        @Override
                        public void render(OutputStream out, String value) throws Exception {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("render");
                            }
                            ImageIO.write(thumbnailOfPDF, "png", out);
                        }

                        @Override
                        public String getContentType() {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("getContentType");
                            }
                            return "image/png";
                        }
                    });
        } catch (Throwable e) {

            system.addEvent(SystemEventType.PR_CRAWLER, "Failed to parse PDF", "PRCrawler");
            // Not a PDF!
            return CrawlerStatus.UNREACHABLE;
        }

        String integrationStatementGeneratedChecksum = system.getPrChecksumGenerated();
        String integrationStatementValidatedByAdminChecksum = system.getPrChecksumValidated();

        if ((StringUtils.trimToNull(integrationStatementGeneratedChecksum) == null)
                && (StringUtils.trimToNull(integrationStatementValidatedByAdminChecksum) == null)) {
            return CrawlerStatus.NOT_MATCHABLE;
        }

        String pdfMD5 = Md5Encryption.calculateMD5ChecksumForFile(tempFile);
        if (!pdfMD5.equals(integrationStatementGeneratedChecksum)
                && !pdfMD5.equals(integrationStatementValidatedByAdminChecksum)) {
            system.addEvent(SystemEventType.PR_CRAWLER, "MD5 of PDF (" + pdfMD5
                    + ") doesn't match MD5 of generated IS (" + integrationStatementGeneratedChecksum
                    + ") nor validated IS (" + integrationStatementValidatedByAdminChecksum + ")", "PRCrawler");
            return CrawlerStatus.MISMATCH;
        }
        return CrawlerStatus.MATCH;
    }

}
