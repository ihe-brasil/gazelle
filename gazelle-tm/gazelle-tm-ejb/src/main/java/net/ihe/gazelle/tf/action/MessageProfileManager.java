package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Hl7MessageProfile;
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
 * Unlike HL7MessageProfileManager which is session scoped, this bean is page-scoped and can be used by multiple browser windows to view different
 * message profile contents.
 *
 * @author tnabeel
 */
@Name(value = "messageProfileManager")
@Scope(ScopeType.PAGE)
public class MessageProfileManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MessageProfileManager.class);

    private Hl7MessageProfile messageProfile;
    private Integer mpId;

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

    public Hl7MessageProfile getMessageProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageProfile");
        }
        return messageProfile;
    }

    public void setMessageProfile(Hl7MessageProfile messageProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageProfile");
        }
        this.messageProfile = messageProfile;
    }

    @Create
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init");
        }
        String strMpId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("mpId");
        if (StringUtils.isEmpty(strMpId)) {
            return;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        if (!StringUtils.isEmpty(strMpId)) {
            try {
                setMpId(Integer.valueOf(strMpId));
            } catch (NumberFormatException e) {
                LOG.error("Invalid parameter value supplied for mpId: " + strMpId);
                return;
            }
            setMessageProfile(em.find(Hl7MessageProfile.class, getMpId()));

        }
    }

    public String fetchProfileContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchProfileContent");
        }
        String profileContent = "";
        if (getMessageProfile() != null && !StringUtils.isEmpty(getMessageProfile().getProfileContent())) {
            profileContent = getMessageProfile().getProfileContent();
        }

        return profileContent;
    }

}
