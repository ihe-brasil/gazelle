package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import net.ihe.gazelle.tm.utils.systems.IHEImplementationForSystem;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

import static org.jboss.seam.ScopeType.PAGE;

/**
 * Created by gthomazon on 19/07/17.
 */
@Name("integrationStatementManager")
@Scope(PAGE)
@GenerateInterface("IntegrationStatementManagerLocal")
public class IntegrationStatementManager implements Serializable, IntegrationStatementManagerLocal {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationStatementManager.class);
    private static final String SEPARATOR = " - ";
    protected SystemInSession selectedSystemInSession = new SystemInSession();
    @In
    private EntityManager entityManager;

    public static String getInstitutionsNameForSystemStatic(System selectedSystem) {
        LOG.debug("String getInstitutionsNameForSystemStatic");
        SystemQuery query = new SystemQuery();
        query.id().eq(selectedSystem.getId());
        List<String> institutions = query.institutionSystems().institution().name().getListDistinct();
        return StringUtils.join(institutions, SEPARATOR);
    }

    public SystemInSession getSelectedSystemInSession() {
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        this.selectedSystemInSession = selectedSystemInSession;
    }

    @Override
    @Create
    public void init() {
        String systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        //Get the systemInSessionId in the context previously set in PRManager.java
        if (systemInSessionId == null && Contexts.getEventContext().isSet("systemInSessionId")) {
            systemInSessionId = Contexts.getEventContext().get("systemInSessionId").toString();
        }
        try {
            Integer id = Integer.parseInt(systemInSessionId);
            selectedSystemInSession = entityManager.find(SystemInSession.class, id);
        } catch (NumberFormatException e) {
            LOG.error("failed to find system in session with id = " + systemInSessionId);
        }
    }

    @Override
    public String getInstitutionsNameForSystem() {
        LOG.debug("getInstitutionsNameForSystem");
        return getInstitutionsNameForSystemStatic(selectedSystemInSession.getSystem());
    }

    /**
     * Search the URL of the repository containing Integration Statements of this company corresponding to the system. Used by JSF to render.
     *
     * @return String = URL of the repository containing Integration Statements to display
     */
    @Override
    public String getIntegrationStatementsRepositoryURLForSystem(final System selectedSystem) {
        LOG.debug("getIntegrationStatementsRepositoryURLForSystem");
        SystemQuery query = new SystemQuery();
        query.id().eq(selectedSystem.getId());
        return query.institutionSystems().institution().integrationStatementsRepositoryUrl().getListDistinct().get(0);
    }

    public List<IHEImplementationForSystem> getIheImplementationsToDisplay(){
        return IHEImplementationForSystemDAO.getIHEImplementationsToDisplay(selectedSystemInSession.getSystem());
    }
}
