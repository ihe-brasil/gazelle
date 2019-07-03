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
package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.constraints.AipoCriterion;
import net.ihe.gazelle.tf.model.constraints.AipoRule;
import net.ihe.gazelle.tf.model.constraints.AipoRuleQuery;
import net.ihe.gazelle.tm.gazelletest.action.ConnectathonResultDataModel;
import net.ihe.gazelle.tm.gazelletest.model.instance.StatusResults;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSessionQuery;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Name("tfDependenciesManager")
@Scope(ScopeType.PAGE)
public class TFDependenciesManager implements Serializable {

    private static final long serialVersionUID = 6595112112256224536L;
    private static final Logger LOG = LoggerFactory.getLogger(TFDependenciesManager.class);
    private List<AipoRule> invalidRules = null;

    public static List<AipoRule> getKnowledgeBase(EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getKnowledgeBase");
        }
        AipoRuleQuery aipoRuleQuery = new AipoRuleQuery();
        return aipoRuleQuery.getList();
    }

    public static List<AipoRule> getInvalidRules(net.ihe.gazelle.tm.systems.model.System system) {
        EntityManager em = EntityManagerService.provideEntityManager();

        List<AipoRule> dbRules = getKnowledgeBase(em);
        List<AipoRule> invalidRules = new ArrayList<AipoRule>();

        List<ActorIntegrationProfileOption> vAIPO = SystemActorProfiles.getListOfActorIntegrationProfileOptions(system,
                em);

        for (AipoRule rule : dbRules) {
            if (!rule.validate(vAIPO)) {
                invalidRules.add(rule);
            }
        }
        return invalidRules;
    }

    public static List<AipoRule> getInvalidRules(List<SystemAIPOResultForATestingSession> aipoResultList) {
        EntityManager em = EntityManagerService.provideEntityManager();

        List<AipoCriterion> aipoCriteria = new ArrayList<>();
        List<ActorIntegrationProfileOption> vAIPO = new ArrayList<>();

        for (SystemAIPOResultForATestingSession systemAIPOResultForATestingSession : aipoResultList) {
            systemAIPOResultForATestingSession.setDependencyMissing(false);
            if (systemAIPOResultForATestingSession.getStatus() != null && systemAIPOResultForATestingSession.getStatus().equals(StatusResults
                    .getSTATUS_RESULT_PASSED())) {
                vAIPO.add(systemAIPOResultForATestingSession.getSystemActorProfile().getActorIntegrationProfileOption());
            }
        }

        List<AipoRule> dbRules = getKnowledgeBase(em);
        List<AipoRule> invalidRules = new ArrayList<AipoRule>();


        for (AipoRule rule : dbRules) {
            if (!rule.validate(vAIPO)) {
                invalidRules.add(rule);
            }
        }
        for (AipoRule invalidRule : invalidRules) {
            aipoCriteria.add(invalidRule.getCause());
        }
        aipoCriteria = removeDuplicateCriterion(aipoCriteria);
        for (AipoCriterion aipoCriterion : aipoCriteria) {
            for (SystemAIPOResultForATestingSession systemAIPOResultForATestingSession : aipoResultList) {
                if ((systemAIPOResultForATestingSession.getStatus() != null && systemAIPOResultForATestingSession.getStatus().equals(StatusResults
                        .getSTATUS_RESULT_PASSED())) && aipoCriterion.toString().contains(systemAIPOResultForATestingSession.getSystemActorProfile
                        ().getActorIntegrationProfileOption().getActorIntegrationProfile().getIntegrationProfile().getKeyword()) &&
                        (aipoCriterion.toString().contains(systemAIPOResultForATestingSession.getSystemActorProfile()
                                .getActorIntegrationProfileOption().getActorIntegrationProfile().getActor().getKeyword()) ||
                                aipoCriterion.toString().contains("*"))) {
                    systemAIPOResultForATestingSession.setDependencyMissing(true);
                } else {
                    systemAIPOResultForATestingSession.setDependencyMissing(false);
                }
            }
        }
        return invalidRules;
    }

    public static List<AipoCriterion> removeDuplicateCriterion(List<AipoCriterion> l) {
        // add elements to al, including duplicates
        Set<AipoCriterion> hs = new HashSet<AipoCriterion>();
        hs.addAll(l);
        l.clear();
        l.addAll(hs);
        return l;
    }

    public boolean validatesCriterion(AipoCriterion criterion, net.ihe.gazelle.tm.systems.model.System system) {
        if (criterion == null) {
            return false;
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        List<ActorIntegrationProfileOption> vAIPO = SystemActorProfiles.getListOfActorIntegrationProfileOptions(system,
                em);
        return (criterion.matches(vAIPO) != null);
    }

    public boolean validatesCriterion(AipoCriterion criterion, ConnectathonResultDataModel dataModel) {
        if (criterion == null) {
            return false;
        }
        List<ActorIntegrationProfileOption> vAIPO = new ArrayList<>();
        List<SystemAIPOResultForATestingSession> aipoResultList = dataModel.getAllItems(FacesContext.getCurrentInstance());
        for (SystemAIPOResultForATestingSession systemAIPOResultForATestingSession : aipoResultList) {
            if (systemAIPOResultForATestingSession.getStatus() != null && systemAIPOResultForATestingSession.getStatus().equals(StatusResults
                    .getSTATUS_RESULT_PASSED())) {
                vAIPO.add(systemAIPOResultForATestingSession.getSystemActorProfile().getActorIntegrationProfileOption());
            }
        }
        return (criterion.matches(vAIPO) != null);
    }

    public String validateTFDependencies(net.ihe.gazelle.tm.systems.model.System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTFDependencies : system");
        }
        if (system.getId() != null) {
            invalidRules = getInvalidRules(system);
        }
        return "";
    }

    public String validateTFDependencies(ConnectathonResultDataModel dataModel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTFDependencies : dataModel");
        }
        List<SystemAIPOResultForATestingSession> systemAIPOResultForATestingSessionsListFromDM =
                getSystemAIPOResultForATestingSessionsFromDataModel(dataModel);
        List<SystemAIPOResultForATestingSession> systemAIPOResultForATestingSessionsListFromDB = getSystemAIPOResultForATestingSessionsFromDB
                (dataModel);
        if (!systemAIPOResultForATestingSessionsListFromDM.isEmpty() && !systemAIPOResultForATestingSessionsListFromDB.isEmpty()
                && systemAIPOResultForATestingSessionsListFromDB.size() == systemAIPOResultForATestingSessionsListFromDM.size()) {
            invalidRules = getInvalidRules(systemAIPOResultForATestingSessionsListFromDM);
        } else if (!systemAIPOResultForATestingSessionsListFromDB.isEmpty()) {
            invalidRules = getInvalidRules(systemAIPOResultForATestingSessionsListFromDB);
        }
        return "";
    }

    private List<SystemAIPOResultForATestingSession> getSystemAIPOResultForATestingSessionsFromDataModel(ConnectathonResultDataModel dataModel) {
        System sys = (System) dataModel.getFilter().getFilterValues().get("system");
        TestingSession selectedTestingSession = (TestingSession) dataModel.getFilter().getFilterValues().get("testSession");
        List<SystemAIPOResultForATestingSession> res = new ArrayList<>();
        if (sys != null && selectedTestingSession != null) {
            res = dataModel.getAllItems(FacesContext.getCurrentInstance());
        }
        return res;
    }

    private List<SystemAIPOResultForATestingSession> getSystemAIPOResultForATestingSessionsFromDB(ConnectathonResultDataModel dataModel) {
        System sys = (System) dataModel.getFilter().getFilterValues().get("system");
        TestingSession selectedTestingSession = (TestingSession) dataModel.getFilter().getFilterValues().get("testSession");
        SystemAIPOResultForATestingSessionQuery query = new SystemAIPOResultForATestingSessionQuery();
        query.setCachable(false);
        query.testSession().eq(selectedTestingSession);
        query.systemActorProfile().system().eq(sys);
        return query.getList();
    }


    public List<AipoRule> getInvalidRules() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInvalidRules");
        }
        return invalidRules;
    }

    public void setInvalidRules(List<AipoRule> invalidRules) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInvalidRules");
        }
        this.invalidRules = invalidRules;
    }

    public boolean isMissingTfDependencies() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMissingTfDependencies");
        }
        if ((invalidRules != null) && (invalidRules.size() > 0)) {
            return true;
        }
        return false;
    }
}