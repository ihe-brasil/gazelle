package net.ihe.gazelle.objects.action;

import net.ihe.gazelle.EVSclient.EVSClientResultsWrapper;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.evsclient.connector.api.EVSClientServletConnector;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.object.tools.ObjectManipulation;
import net.ihe.gazelle.objects.model.Annotation;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.objects.model.ObjectInstanceAnnotation;
import net.ihe.gazelle.objects.model.ObjectInstanceFile;
import net.ihe.gazelle.preferences.PreferenceService;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractSampleManager extends ObjectManipulation {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSampleManager.class);
    protected ObjectInstance selectedObjectInstance;
    protected Annotation selectedAnnotation;
    protected String commentOfCurrentSIS;
    protected ObjectInstanceFile selectedObjectInstanceFile;
    protected String selectedSpecification;
    private String evsClientUrl;
    private String toolOid;
    private boolean sortDateAscending = false;

    public void validate(ObjectInstanceFile file) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validate");
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        EVSClientServletConnector.sendToValidation(file, context, getEVSClientUrl(), getToolOid());
    }

    public String getValidationStatus(String resultOid, int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationStatus");
        }
        return EVSClientResultsWrapper.getValidationStatus(resultOid, getEVSClientUrl(), id);
    }

    public String getValidationDate(String resultOid, int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationDate");
        }
        return EVSClientResultsWrapper.getValidationDate(resultOid, getEVSClientUrl(), id);
    }

    public String getValidationColor(String resultOid, int id) {
        String status = getValidationStatus(resultOid, id);
        return getColor(status);
    }

    private String getColor(String status) {
        if (status == null) {
            return "";
        } else if ("PASSED".equals(status)) {
            return "gzl-label-green";
        } else if ("FAILED".equals(status)) {
            return "gzl-label-red";
        } else if ("ABORTED".equals(status)) {
            return "gzl-label-orange";
        } else if ("NOT PERFORMED".equals(status)) {
            return "gzl-label-grey";
        }  else if ("not performed".equals(status)) {
            return "gzl-label-grey";
        } else if ("VALIDATION NOT PERFORMED".equals(status)) {
            return "gzl-label-grey";
        } else if ("UNKNOWN".equals(status))  {
            return "gzl-label-blue";
        } else {
            return "gzl-label-grey";
        }
    }

    public String getLastValidationColor(ObjectInstanceFile oif) {
        String status = getLastValidationStatusForFile(oif);
        return getColor(status);
    }


    public String getLinkToResult(String resultOid, int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkToResult");
        }
        return EVSClientResultsWrapper.getValidationPermanentLink(resultOid, getEVSClientUrl(), id);
    }

    private String getEVSClientUrl() {
        if (evsClientUrl == null) {
            evsClientUrl = ApplicationPreferenceManager.instance().getEVSClientURL();
        }
        return evsClientUrl;
    }

    private String getToolOid() {
        if (toolOid == null) {
            toolOid = PreferenceService.getString("app_instance_oid");
        }
        return toolOid;
    }

    public List<Annotation> getReadersDescriptionsOnCreate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReadersDescriptionsOnCreate");
        }
        if (this.selectedObjectInstance != null) {

            List<Annotation> readersDescriptions = getReadersDescriptions(selectedObjectInstance);

            if (sortDateAscending) {
                Collections.sort(readersDescriptions, new Comparator<Annotation>() {
                    @Override
                    public int compare(Annotation annotation, Annotation t1) {
                        return annotation.getLastChanged().compareTo(t1.getLastChanged());
                    }
                });
            } else {
                Collections.sort(readersDescriptions, new Comparator<Annotation>() {
                    @Override
                    public int compare(Annotation annotation, Annotation t1) {
                        return t1.getLastChanged().compareTo(annotation.getLastChanged());
                    }
                });
            }
            return readersDescriptions;
        }

        return null;
    }

    public void sortReadersDescriptionsByDate() {
        sortDateAscending = !sortDateAscending;
    }

    public String getPermanentLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermanentLink");
        }
        return getPermanentLink(selectedObjectInstance);
    }

    public boolean currentUserCanEditSample() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("currentUserCanEditSample");
        }
        User currentUser = User.loggedInUser();
        if (selectedObjectInstance == null) {
            return false;
        }
        if (currentUser.getRoles().contains(Role.getADMINISTRATOR_ROLE())) {
            return true;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Identity.instance().getCredentials().getUsername();
        Institution userinst = Institution.getLoggedInInstitution();
        List<SystemInSession> systemInSessionList = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                userinst, TestingSession.getSelectedTestingSession());
        if (systemInSessionList != null) {
            if (this.selectedObjectInstance.getSystem() != null) {
                if (systemInSessionList.contains(this.selectedObjectInstance.getSystem())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean currentUserCanAddCommentSample() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("currentUserCanAddCommentSample");
        }
        User currentUser = User.loggedInUser();
        if (selectedObjectInstance == null) {
            return false;
        }
        if (currentUser.getRoles().contains(Role.getADMINISTRATOR_ROLE()) || currentUser.getRoles().contains(Role.getMONITOR_ROLE())) {
            return true;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Identity.instance().getCredentials().getUsername();
        Institution userinst = Institution.getLoggedInInstitution();
        List<SystemInSession> systemInSessionList = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                userinst, TestingSession.getSelectedTestingSession());
        if (systemInSessionList.contains(this.selectedObjectInstance.getSystem())) {
            return true;
        }
        List<SystemInSession> sislist = SystemInSession.getSystemInSessionFiltered(em, null,
                TestingSession.getSelectedTestingSession(), userinst, null, null, null, null, null, null, null, null,
                null, null, null, this.selectedObjectInstance.getObject(), null, null);

        if (sislist != null) {
            if (sislist.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public String backToSamplePage(ObjectInstance inObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("backToSamplePage");
        }
        if (inObjectInstance != null) {
            if (inObjectInstance.getId() != null) {
                return "/objects/sample.seam?id=" + inObjectInstance.getId();
            }
        }
        return null;
    }

    public boolean viewDeleteImg(Annotation ann) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewDeleteImg");
        }
        User usr = User.loggedInUser();
        if (usr.getRoles().contains(Role.getADMINISTRATOR_ROLE())) {
            return true;
        }
        if (usr.getLastModifierId() == null) {
            return false;
        }
        if (ann == null) {
            return false;
        }
        if (ann.getLastModifierId() == null) {
            return false;
        }
        if (usr.getLastModifierId().equals(ann.getLastModifierId())) {
            return true;
        }
        return false;
    }

    public void saveModificationsForSelectedAnnotation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveModificationsForSelectedAnnotation");
        }
        if (this.selectedAnnotation != null) {
            if ((this.selectedAnnotation.getValue() != null) && (this.selectedAnnotation.getValue().length() > 499)) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.CommentSampleLong']}");
                return;
            }
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            this.selectedAnnotation = entityManager.merge(this.selectedAnnotation);
            entityManager.flush();
        }
    }

    public void deleteSelectedAnnotation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedAnnotation");
        }
        if (this.selectedAnnotation != null) {
            ObjectInstanceAnnotation.deleteObjectInstanceAnnotation(this.selectedAnnotation);
            Annotation.deleteAnnotation(this.selectedAnnotation);
        }
    }

    public void saveCommentOfObjectInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveCommentOfObjectInstance");
        }
        this.saveCommentOfObjectInstance(this.commentOfCurrentSIS, this.selectedObjectInstance);
        this.commentOfCurrentSIS = null;
    }

    public boolean canEditSampleByCreator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditSampleByCreator");
        }
        return this.canEditSampleByCreator(selectedObjectInstance);
    }

    public String getSelectedObjectInstanceFileContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstanceFileContent");
        }
        if (this.selectedObjectInstanceFile != null && this.selectedObjectInstanceFile.isWritable()) {
            String filepath = selectedObjectInstanceFile.getFileAbsolutePath();
            try (BufferedReader br = new BufferedReader(new FileReader(new File(filepath)))) {
                StringBuilder buf = new StringBuilder();
                String ll = br.readLine();
                while (ll != null) {
                    buf.append(ll).append("\n");
                    ll = br.readLine();
                }
                br.close();
                return buf.toString();
            } catch (FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public String getInstitution(ObjectInstance oi) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitution");
        }
        if (oi != null && oi.getSystem() != null) {
            System inSystem = oi.getSystem().getSystem();
            InstitutionSystemQuery query = new InstitutionSystemQuery();
            query.system().eq(inSystem);
            InstitutionSystem institutionSystem = query.getUniqueResult();
            if (institutionSystem != null) {
                return institutionSystem.getInstitution().getName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean canViewContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canViewContent");
        }
        return (this.selectedObjectInstanceFile != null && selectedObjectInstanceFile.isWritable());
    }

    public Boolean getSelectedObjectInstanceExistsOnDisc() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstanceExistsOnDisc");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        if (selectedObjectInstance != null) {
            selectedObjectInstance = em.find(ObjectInstance.class, selectedObjectInstance.getId());
            return this.getSelectedObjectInstanceExistsOnDisc(selectedObjectInstance);
        }
        return false;
    }

    public String getLastValidationStatusForFile(ObjectInstanceFile oif) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLastValidationStatusForFile");
        }
        String status = EVSClientResultsWrapper.getLastResultStatusByExternalId(oif.getUniqueKey(), getToolOid(), getEVSClientUrl(), oif.getId());
        if (status == null) {
            status = "N/A";
        }
        return status;
    }

    public ObjectInstance getSelectedObjectInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstance");
        }
        return selectedObjectInstance;
    }

    public void setSelectedObjectInstance(ObjectInstance selectedObjectInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectInstance");
        }
        this.selectedObjectInstance = selectedObjectInstance;
    }

    public Annotation getSelectedAnnotation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedAnnotation");
        }
        return selectedAnnotation;
    }

    public void setSelectedAnnotation(Annotation selectedAnnotation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedAnnotation");
        }
        this.selectedAnnotation = selectedAnnotation;
    }

    public String getCommentOfCurrentSIS() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCommentOfCurrentSIS");
        }
        return commentOfCurrentSIS;
    }

    public void setCommentOfCurrentSIS(String commentOfCurrentSIS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCommentOfCurrentSIS");
        }
        this.commentOfCurrentSIS = commentOfCurrentSIS;
    }

    public ObjectInstanceFile getSelectedObjectInstanceFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedObjectInstanceFile");
        }
        return selectedObjectInstanceFile;
    }

    public void setSelectedObjectInstanceFile(ObjectInstanceFile selectedObjectInstanceFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedObjectInstanceFile");
        }
        this.selectedObjectInstanceFile = selectedObjectInstanceFile;
    }

    public String getSelectedSpecification() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSpecification");
        }
        if (selectedSpecification == null) {
            selectedSpecification = "cdar2";
        }
        return selectedSpecification;
    }

    public void setSelectedSpecification(String selectedSpecification) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSpecification");
        }
        this.selectedSpecification = selectedSpecification;
    }
}
