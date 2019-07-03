package net.ihe.gazelle.tm.tee.validation;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.tee.dao.TestStepMsgProfileDAO;
import net.ihe.gazelle.tm.tee.model.TmTestStepMessageProfile;
import net.ihe.gazelle.tm.tee.util.XMLUtil;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Manages Test Step Message Profiles and Message Profiles
 *
 * @author tnabeel
 */
@Name(value = "stepMsgProfileManager")
@Scope(ScopeType.PAGE)
public class StepMsgProfileManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(StepMsgProfileManager.class);

    private TmTestStepMessageProfile testStepMsgProfile;
    private Integer mpId;
    private Integer smpId;

    public TmTestStepMessageProfile getTestStepMsgProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepMsgProfile");
        }
        return testStepMsgProfile;
    }

    public void setTestStepMsgProfile(TmTestStepMessageProfile testStepMsgProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepMsgProfile");
        }
        this.testStepMsgProfile = testStepMsgProfile;
    }

    public Integer getMpId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMpId");
        }
        return mpId;
    }

    public void setMpId(Integer mpId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMpId");
        }
        this.mpId = mpId;
    }

    public Integer getSmpId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSmpId");
        }
        return smpId;
    }

    public void setSmpId(Integer smpId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSmpId");
        }
        this.smpId = smpId;
    }

    @Create
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init");
        }
        String strSmpId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("smpId");
        if (StringUtils.isEmpty(strSmpId)) {
            return;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        if (!StringUtils.isEmpty(strSmpId)) {
            try {
                setSmpId(Integer.valueOf(strSmpId));
            } catch (NumberFormatException e) {
                LOG.error("Invalid parameter value supplied for smpId: " + strSmpId);
                return;
            }
            setTestStepMsgProfile(new TestStepMsgProfileDAO(em).findTestStepMsgProfileById(getSmpId()));
        }
    }

    public String fetchValidationContext() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchValidationContext");
        }
        String content = null;
        if (getSmpId() == null) {
            content = "Invalid id";  // Should never happen if user is navigating via links
        } else if (getTestStepMsgProfile() == null) {
            content = "Record not found"; // Should never happen if user is navigating via links
        } else if (getTestStepMsgProfile().getValidationContextContent() == null || getTestStepMsgProfile().getValidationContextContent().trim()
                .isEmpty()) {
            content = "Validation Context not available";
        } else {
            content = XMLUtil.getPrettyContents(getTestStepMsgProfile().getValidationContextContent());
        }

        return content;
    }

    public String fetchExampleMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchExampleMessage");
        }
        String exampleMessage = "";
        if (getSmpId() == null) {
            exampleMessage = "Invalid id";  // Should never happen if user is navigating via links
        } else if (getTestStepMsgProfile() == null) {
            exampleMessage = "Record not found"; // Should never happen if user is navigating via links
        } else if (getTestStepMsgProfile().getExampleMsgContent() == null || getTestStepMsgProfile().getExampleMsgContent().trim().isEmpty()) {
            exampleMessage = "Example Message not available";
        } else {
            exampleMessage = getTestStepMsgProfile().getExampleMsgContent();
        }

        return exampleMessage;
    }

}
