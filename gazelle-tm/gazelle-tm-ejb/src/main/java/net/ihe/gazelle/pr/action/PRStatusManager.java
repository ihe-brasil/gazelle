package net.ihe.gazelle.pr.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.dates.TimestampNano;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.messaging.MessagePropertyChanged;
import net.ihe.gazelle.messaging.MessagingProvider;
import net.ihe.gazelle.pr.bean.CrawlType;
import net.ihe.gazelle.pr.bean.CrawlerStatus;
import net.ihe.gazelle.pr.bean.ISTools;
import net.ihe.gazelle.pr.systems.model.CrawlerReporting;
import net.ihe.gazelle.pr.systems.model.PRMail;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.IntegrationStatementStatus;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemEventType;
import net.ihe.gazelle.tm.systems.model.SystemType;
import net.ihe.gazelle.users.action.UserManager.EmailType;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.Md5Encryption;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

@Name("prStatusManager")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@GenerateInterface("PRStatusManagerLocal")
@MetaInfServices(MessagingProvider.class)
public class PRStatusManager implements Serializable, MessagingProvider, PRStatusManagerLocal {

    private static final long serialVersionUID = 2464439846287174084L;

    private static final Logger LOG = LoggerFactory.getLogger(PRStatusManager.class);

    public static boolean equals(Object x, Object y) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("boolean equals");
        }
        if (x == y) {
            return true;
        }
        if (x == null) {
            return false;
        }
        if (y == null) {
            return false;
        }
        if (x.getClass() != y.getClass()) {
            return false;
        }
        if (x instanceof SystemType) {
            return equals(((SystemType) x).getId(), ((SystemType) y).getId());
        } else {
            return x.equals(y);
        }
    }

    @Override
    public void sendMail(EmailType emailType, System system, String logMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendMail");
        }
        sendMail(emailType, system, null, logMessage);
    }

    @Override
    public void sendMail(EmailType emailType, CrawlerReporting crawlerReporting, String logMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendMail");
        }
        sendMail(emailType, null, crawlerReporting, logMessage);
    }

    protected void sendMail(EmailType emailType, System system, CrawlerReporting crawlerReporting, String logMessage) {

        if (system != null) {
            system.addEvent(SystemEventType.PR_CRAWLER, logMessage, getLogin());
        }

        PRMail systemMail = new PRMail();
        systemMail.setSystem(system);
        systemMail.setCrawlerReporting(crawlerReporting);
        systemMail.setPageName(emailType.getPageName());
        systemMail.setSent(false);

        EntityManagerService.provideEntityManager().merge(systemMail);
        EntityManagerService.provideEntityManager().flush();
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

    @Override
    public void receiveMessage(Object message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("receiveMessage");
        }
        if (ApplicationManager.instance().isProductRegistry()) {
            if (message instanceof MessagePropertyChanged) {
                MessagePropertyChanged messagePropertyChanged = (MessagePropertyChanged) message;
                if (System.class.isAssignableFrom(messagePropertyChanged.getObject().getClass())) {
                    if (!equals(messagePropertyChanged.getOldValue(), messagePropertyChanged.getNewValue())) {
                        systemPropertyChanged(messagePropertyChanged);
                    }
                }
            }
        }
    }

    private void systemPropertyChanged(MessagePropertyChanged<System, ?> messagePropertyChanged) {
        System system = messagePropertyChanged.getObject();
        String propertyName = messagePropertyChanged.getPropertyName();

        if (!equals(messagePropertyChanged.getNewValue(), messagePropertyChanged.getOldValue())) {
            // Here we reset the IS status when the system is modified!
            if (("addAIPO".equals(propertyName)) || ("removeAIPO".equals(propertyName))) {
                system.setPrStatus(IntegrationStatementStatus.CREATED);
                system.setPrChecksumValidated(null);
                system.setPrChecksumGenerated(null);
            }
            if ("integrationStatementUrl".equals(propertyName)) {

            }

            if (propertyName.equals("prStatus")) {

                MessagePropertyChanged<System, IntegrationStatementStatus> messagePropertyChangedStatus = (MessagePropertyChanged<System,
                        IntegrationStatementStatus>) messagePropertyChanged;

                IntegrationStatementStatus oldValue = messagePropertyChangedStatus.getOldValue();
                IntegrationStatementStatus newValue = messagePropertyChangedStatus.getNewValue();
                if ((newValue == IntegrationStatementStatus.PUBLISHED)
                        || (newValue == IntegrationStatementStatus.VERIFIED)) {
                    system.setPrCounterCrawlerUnmatchingHashcode(0);
                    system.setPrCounterCrawlerUnreachableUrl(0);
                    system.setPrMailSentBecauseUnmatchingHashcode(false);
                    system.setPrMailSentBecauseUnreachableUrl(false);
                }
                if (newValue == IntegrationStatementStatus.UNMATCHING) {
                    system.setPrCounterCrawlerUnreachableUrl(0);
                    system.setPrMailSentBecauseUnreachableUrl(false);
                }

                system.addEvent(SystemEventType.PR_INTEGRATION_STATEMENT_STATUS, "Changed from " + oldValue + " to "
                        + newValue, getLogin());

            } else if (propertyName.equals("addAIPO")) {
                system.addEvent(SystemEventType.MODIFIED, "AIPO added : " + messagePropertyChanged.getNewValue(),
                        getLogin());
            } else if (propertyName.equals("removeAIPO")) {
                system.addEvent(SystemEventType.MODIFIED, "AIPO removed : " + messagePropertyChanged.getOldValue(),
                        getLogin());
            } else {
                system.addEvent(SystemEventType.MODIFIED, messagePropertyChanged.getPropertyName() + " changed from "
                                + messagePropertyChanged.getOldValue() + " to " + messagePropertyChanged.getNewValue(),
                        getLogin());

            }
        }
    }

    @Override
    public boolean newAdminStatus(System system, IntegrationStatementStatus status, String comment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("newAdminStatus");
        }
        if (status == IntegrationStatementStatus.VERIFIED) {
            File tempFile = null;
            String newMD5 = null;
            try {
                tempFile = File.createTempFile("newIS", ".pdf");
                ISTools.retrieveIS(system, tempFile);
                newMD5 = Md5Encryption.calculateMD5ChecksumForFile(tempFile);
            } catch (IOException e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,"Failed to retrieve integration statement");
                if (tempFile != null) {
                    if (tempFile.delete()) {
                        LOG.info("tempFile deleted");
                    } else {
                        LOG.error("Failed to delete tempFile");
                    }
                }
                return false;
            }
            if (tempFile.delete()) {
                LOG.info("tempFile deleted");
            } else {
                LOG.error("Failed to delete tempFile");
            }
            system.setPrChecksumValidated(newMD5);
            system.setPrDateValidatedByAdmin(new TimestampNano());
        } else {
            system.setPrChecksumValidated(null);
            system.setPrDateValidatedByAdmin(null);
        }
        system.setPrCommentValidatedByAdmin(comment);
        system.setPrStatus(status);

        EntityManagerService.provideEntityManager().merge(system);

        PRStatusManagerLocal prStatusManager = (PRStatusManagerLocal) Component.getInstance("prStatusManager");
        prStatusManager.sendMail(EmailType.TO_INSTITUTION_ADMIN_ON_PR_ADMIN_STATUS, system,
                "Send email saying that admin has changed system status");

        return true;
    }

    @Override
    public void newCrawlerStatus(System system, CrawlerStatus crawlerStatus, CrawlType crawlType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("newCrawlerStatus");
        }
        switch (crawlerStatus) {
            case MATCH:
                // set status to verified
                crawlerMatch(system, crawlType);
                break;
            case NOT_MATCHABLE:
                // set status to verified
                crawlerNotMatchable(system, crawlType);
                break;
            case MISMATCH:
                // may set status to UNMATCHED
                crawlerMismatch(system, crawlType);
                break;
            case UNREACHABLE:
                // may set status to UNREACHABLE
                crawlerUnreachable(system, crawlType);
                break;
            default:
                break;
        }
    }

    private void crawlerMatch(System system, CrawlType crawlType) {
        if (system.getPrStatus() != IntegrationStatementStatus.VERIFIED) {
            system.setPrDateValidatedByCrawler(new Date());
            system.addEvent(SystemEventType.PR_CRAWLER, "Auto verified using MD5", getLogin());
            system.setPrChecksumValidated(system.getPrChecksumGenerated());
            system.setPrStatus(IntegrationStatementStatus.VERIFIED);
        }
        system.setPrCounterCrawlerUnmatchingHashcode(0);
        system.setPrCounterCrawlerUnreachableUrl(0);
        mergeSystem(system);
    }

    private void crawlerNotMatchable(System system, CrawlType crawlType) {
        system.setPrCounterCrawlerUnmatchingHashcode(0);
        system.setPrCounterCrawlerUnreachableUrl(0);
        mergeSystem(system);
    }

    private void crawlerMismatch(System system, CrawlType crawlType) {
        system.setPrCounterCrawlerUnreachableUrl(0);

        Integer counterCrawlerJobsWithUnmatchingHashcode = system.getPrCounterCrawlerUnmatchingHashcode();
        if (counterCrawlerJobsWithUnmatchingHashcode == null) {
            counterCrawlerJobsWithUnmatchingHashcode = 0;
        }
        counterCrawlerJobsWithUnmatchingHashcode = counterCrawlerJobsWithUnmatchingHashcode + 1;
        system.setPrCounterCrawlerUnmatchingHashcode(counterCrawlerJobsWithUnmatchingHashcode);

        Boolean isEmailSentBecauseUnmatchingHashcode = system.getPrMailSentBecauseUnmatchingHashcode();
        if (isEmailSentBecauseUnmatchingHashcode == null) {
            isEmailSentBecauseUnmatchingHashcode = false;
        }

        if (counterCrawlerJobsWithUnmatchingHashcode <= getUnmatchesBeforeMail()) {
            system.addEvent(SystemEventType.PR_CRAWLER, "Unmatching because of MD5 : "
                    + counterCrawlerJobsWithUnmatchingHashcode + " time(s)", getLogin());
        }

        if (!isEmailSentBecauseUnmatchingHashcode
                && (counterCrawlerJobsWithUnmatchingHashcode >= getUnmatchesBeforeMail())) {
            sendUnmatchesMail(system);
            if (system.getPrStatus() != IntegrationStatementStatus.UNMATCHING) {
                system.addEvent(SystemEventType.PR_CRAWLER, "Marked as unmatching because of MD5", getLogin());
                system.setPrStatus(IntegrationStatementStatus.UNMATCHING);
            }
        }

        mergeSystem(system);
    }

    private void crawlerUnreachable(System system, CrawlType crawlType) {
        Integer counterCrawlerJobsWithUnreachableUrl = system.getPrCounterCrawlerUnreachableUrl();
        if (counterCrawlerJobsWithUnreachableUrl == null) {
            counterCrawlerJobsWithUnreachableUrl = 0;
        }
        counterCrawlerJobsWithUnreachableUrl = counterCrawlerJobsWithUnreachableUrl + 1;
        system.setPrCounterCrawlerUnreachableUrl(counterCrawlerJobsWithUnreachableUrl);

        Boolean isEmailSentBecauseUnreachableUrl = system.getPrMailSentBecauseUnreachableUrl();
        if (isEmailSentBecauseUnreachableUrl == null) {
            isEmailSentBecauseUnreachableUrl = false;
        }

        boolean sendUnreachable = false;
        boolean sendUnreference = false;

        if (!isEmailSentBecauseUnreachableUrl && (counterCrawlerJobsWithUnreachableUrl >= getUnreachableBeforeMail())) {
            sendUnreachable = true;
        }

        if ((counterCrawlerJobsWithUnreachableUrl <= getUnreachableBeforeUnreference())
                || (counterCrawlerJobsWithUnreachableUrl <= getUnreachableBeforeMail())) {
            system.addEvent(SystemEventType.PR_CRAWLER, "Unreachable PDF : " + counterCrawlerJobsWithUnreachableUrl
                    + " time(s)", getLogin());
        }

        if ((counterCrawlerJobsWithUnreachableUrl >= getUnreachableBeforeUnreference())
                && (system.getPrStatus() != IntegrationStatementStatus.UNREACHABLE)) {
            system.addEvent(SystemEventType.PR_CRAWLER, "Too many unreachable states ("
                    + counterCrawlerJobsWithUnreachableUrl + "), changing system status", getLogin());
            sendUnreference = true;
            system.setPrStatus(IntegrationStatementStatus.UNREACHABLE);
        }

        if (sendUnreference) {
            sendUnreachableUnreferenceMail(system);
        } else if (sendUnreachable) {
            sendUnreachableMail(system);
        }

        mergeSystem(system);
    }

    private void sendUnmatchesMail(System system) {
        sendMail(EmailType.TO_INSTITUTION_ADMIN_ON_PR_UNMATCHES, system, "Send email saying that md5 unmatches");
        system.setPrMailSentBecauseUnmatchingHashcode(true);
    }

    private void sendUnreachableMail(System system) {
        sendMail(EmailType.TO_INSTITUTION_ADMIN_ON_PR_UNREACHABLE, system, "Send email saying that file is unreachable");
        system.setPrMailSentBecauseUnreachableUrl(true);
    }

    private void sendUnreachableUnreferenceMail(System system) {
        sendMail(EmailType.TO_INSTITUTION_ADMIN_ON_PR_UNREFERENCE, system,
                "Send email saying that file is dereferenced");
    }

    private Integer getUnmatchesBeforeMail() {
        return ApplicationPreferenceManager.getIntegerValue("crawler_jobs_before_email_for_unmatching_hashcode");
    }

    private Integer getUnreachableBeforeMail() {
        return ApplicationPreferenceManager.getIntegerValue("crawler_jobs_before_email_for_unreachable_url");
    }

    private Integer getUnreachableBeforeUnreference() {
        return ApplicationPreferenceManager.getIntegerValue("crawler_jobs_before_deferencing");
    }

    protected void mergeSystem(System system) {
        EntityManagerService.provideEntityManager().merge(system);
        EntityManagerService.provideEntityManager().flush();
    }

}
