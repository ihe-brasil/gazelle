package net.ihe.gazelle.objects.action;

import net.ihe.gazelle.EVSclient.EVSClientResultsWrapper;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.objects.model.ObjectInstanceFile;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Name("sampleDisplayManager")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class SampleDisplayManager extends AbstractSampleManager implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SampleDisplayManager.class);
    private Boolean userAllowedToEditSystemFileReaderOfSample = null;

    @Create
    public void initialize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialize");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String resultOid = EVSClientResultsWrapper.getResultOidFromUrl(params);
        String id = params.get("fileId");
        if (id != null) {
            /* fileId is built as sample_{id from db} */
            String[] parts = id.split("_");
            Integer fileId = Integer.valueOf(parts[1]);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            ObjectInstanceFile file = entityManager.find(ObjectInstanceFile.class, fileId);
            if (file != null) {
                if (resultOid != null && !resultOid.isEmpty()) {
                    file.addResultOid(resultOid);
                    // a new validation has been performed, clean cache for the last result
                    EVSClientResultsWrapper.cleanCache(file.getId());
                }
                entityManager.merge(file);
                entityManager.flush();
                selectedObjectInstance = file.getInstance();
            }
        } else if (params.containsKey("id")) {
            Integer sampleId = Integer.valueOf(params.get("id"));
            selectedObjectInstance = ObjectInstance.getObjectInstanceById(sampleId);
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Either fileId or id shall be specified in URL");
            selectedObjectInstance = null;
        }
        if (selectedObjectInstance == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "The application has not been able to retrieve the specified sample");
        }
    }

    public void removeResult(ObjectInstanceFile file, String resultOid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeResult");
        }
        file.removeResultOid(resultOid);
    }

    public void addResult(ObjectInstanceFile file, String resultOid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addResult");
        }
        file.addResultOid(resultOid);
    }

    public boolean isUserAllowedToEditSystemFileReaderOfSample(SystemInSession selectedSis) {

        if (userAllowedToEditSystemFileReaderOfSample == null) {

            userAllowedToEditSystemFileReaderOfSample = false;
            Institution inst = Institution.getLoggedInInstitution();
            EntityManager em = EntityManagerService.provideEntityManager();
            TestingSession ts = TestingSession.getSelectedTestingSession();
            List<SystemInSession> sisList = SystemInSession.getSystemsInSessionForCompanyForSession(em, inst, ts);

            if (sisList != null && !ts.testingSessionClosedForUser() && selectedSis != null) {
                for (SystemInSession sis : sisList) {
                    if (sis.getId().equals(selectedSis.getId())) {
                        userAllowedToEditSystemFileReaderOfSample = true;
                    }
                }
            }
        }

        return (boolean) userAllowedToEditSystemFileReaderOfSample;

    }

    public String displayList(ObjectInstanceFile currentObjectInstanceFile) {
        if (currentObjectInstanceFile.getResultOid().isEmpty()) {
            return "empty";
        }
        return "first";
    }
}
