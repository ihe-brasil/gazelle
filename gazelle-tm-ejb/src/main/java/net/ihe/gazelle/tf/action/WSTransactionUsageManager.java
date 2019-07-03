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

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.util.DatabaseUtil;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.jboss.seam.ScopeType.SESSION;

/**
 * <b>Class Description : </b>TransactionManager<br>
 * <br>
 * This class manage the Transaction object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a
 * transaction</li> <li>Delete a transaction</li> <li>
 * Show a transaction</li> <li>Edit a transaction</li> <li>etc...</li>
 *
 * @author abderrazek boufahja
 * @package net.ihe.gazelle.tf.action
 */
@SuppressWarnings("unused")

@Name("wsTransactionUsageManager")
@Scope(SESSION)
@GenerateInterface("WSTransactionUsageManagerLocal")
public class WSTransactionUsageManager implements Serializable, WSTransactionUsageManagerLocal {

    private static final long serialVersionUID = -135701171456235656L;
    private static final Logger LOG = LoggerFactory.getLogger(WSTransactionUsageManager.class);
    private static Integer TEXT_MAX_SIZE_FOR_DROPDOWNLIST = 30;
    private static Integer TEXT_MAX_SIZE_FOR_DROPDOWNLIST_SYSTEM = 30;
    /**
     * em is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    /**
     * Transaction objects managed by this bean
     */

    private WSTransactionUsage selectedWSTransactionUsage;
    private Filter<WSTransactionUsage> filter;
    private IntegrationProfile selectedIntegrationProfile;
    private Actor selectedActor;

    @Override
    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    @Override
    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
    }

    @Override
    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    @Override
    public void setSelectedActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = selectedActor;
    }

    @Override
    public WSTransactionUsage getSelectedWSTransactionUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedWSTransactionUsage");
        }
        return selectedWSTransactionUsage;
    }

    @Override
    public void setSelectedWSTransactionUsage(WSTransactionUsage selectedWSTransactionUsage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedWSTransactionUsage");
        }
        this.selectedWSTransactionUsage = selectedWSTransactionUsage;
    }

    public Filter<WSTransactionUsage> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            WSTransactionUsageQuery query = new WSTransactionUsageQuery();
            filter = new Filter<WSTransactionUsage>(query.getHQLCriterionsForFilter());
        }
        return filter;
    }

    public FilterDataModel<WSTransactionUsage> getWsTransactionUsages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWsTransactionUsages");
        }
        return new FilterDataModel<WSTransactionUsage>(getFilter()) {
            @Override
            protected Object getId(WSTransactionUsage wsTransactionUsage) {
                return wsTransactionUsage.getId();
            }
        };
    }

    @Override
    public void initializeSelectedWSTransactionUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedWSTransactionUsage");
        }
        this.selectedWSTransactionUsage = new WSTransactionUsage();
    }

    @Override
    public boolean canDeleteSelectedWSTrans() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canDeleteSelectedWSTrans");
        }
        return true;
    }

    @Override
    public void deleteSelectedWSTransactionUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedWSTransactionUsage");
        }
        if (this.selectedWSTransactionUsage != null) {
            if (this.selectedWSTransactionUsage.getId() != null) {
                this.selectedWSTransactionUsage = entityManager.find(WSTransactionUsage.class,
                        this.selectedWSTransactionUsage.getId());
                entityManager.remove(this.selectedWSTransactionUsage);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "webservice transaction usage was deleted.");
            }
        }
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void saveSelectedWSTransactionUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveSelectedWSTransactionUsage");
        }
        if (selectedWSTransactionUsage != null) {
            Integer id = this.selectedWSTransactionUsage.getId();
            this.selectedWSTransactionUsage = entityManager.merge(this.selectedWSTransactionUsage);
            if (id == null) {
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The new webservice transaction usage was saved.");
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The selected webservice transaction usage was updated.");
            }
            entityManager.flush();
        }
    }

    @Override
    public List<IntegrationProfile> getAllPossibleIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPossibleIntegrationProfile");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query query;
        StringBuffer queryString = new StringBuffer();
        HashSet<String> prefixes = new HashSet<String>();
        HashSet<String> prefixesUsed = new HashSet<String>();
        HashMap<String, String> mapOfJoin = new HashMap<String, String>();
        HashMap<String, Object> mapOfParameters = new HashMap<String, Object>();

        if (selectedActor != null) {
            DatabaseUtil.addANDorWHERE(queryString);
            prefixes.add("IntegrationProfile ip");
            prefixes.add("ActorIntegrationProfile aip");
            queryString.append("aip.integrationProfile=ip AND aip.actor=:act");
            mapOfParameters.put("act", selectedActor);
        }

        prefixes.add("IntegrationProfile ip");

        List<String> listOfPrefixes = new ArrayList<String>(prefixes);
        StringBuffer firstPartOfQuery = new StringBuffer();

        for (int i = 0; i < listOfPrefixes.size(); i++) {
            if (i == 0) {
                firstPartOfQuery.append("SELECT distinct ip FROM ");
            }

            if (!prefixesUsed.contains(listOfPrefixes.get(i))) {
                if (prefixesUsed.size() > 0) {
                    firstPartOfQuery.append(" , ");
                }

                firstPartOfQuery.append(listOfPrefixes.get(i));
                if (mapOfJoin.containsKey(listOfPrefixes.get(i))) {
                    firstPartOfQuery.append(" " + mapOfJoin.get(listOfPrefixes.get(i)) + " ");
                }

                prefixesUsed.add(listOfPrefixes.get(i));
            }
        }

        queryString.insert(0, firstPartOfQuery);
        queryString.append(" ORDER BY ip.keyword ASC");

        if (queryString.toString().trim().length() == 0) {
            return new ArrayList<IntegrationProfile>();

        }
        query = em.createQuery(queryString.toString());

        List<String> listOfParameters = new ArrayList<String>(mapOfParameters.keySet());
        for (String param : listOfParameters) {
            query.setParameter(param, mapOfParameters.get(param));
        }

        return query.getResultList();
    }

    @Override
    public List<Actor> getAllPossibleActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPossibleActors");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query query;
        StringBuffer queryString = new StringBuffer();
        HashSet<String> prefixes = new HashSet<String>();
        HashSet<String> prefixesUsed = new HashSet<String>();
        HashMap<String, String> mapOfJoin = new HashMap<String, String>();
        HashMap<String, Object> mapOfParameters = new HashMap<String, Object>();

        if (selectedIntegrationProfile != null) {
            DatabaseUtil.addANDorWHERE(queryString);
            prefixes.add("Actor act");
            prefixes.add("ActorIntegrationProfile aip");
            queryString.append("aip.actor=act AND aip.integrationProfile=:ip");
            mapOfParameters.put("ip", selectedIntegrationProfile);
        }

        prefixes.add("Actor act");

        List<String> listOfPrefixes = new ArrayList<String>(prefixes);
        StringBuffer firstPartOfQuery = new StringBuffer();

        for (int i = 0; i < listOfPrefixes.size(); i++) {
            if (i == 0) {
                firstPartOfQuery.append("SELECT distinct act FROM ");
            }

            if (!prefixesUsed.contains(listOfPrefixes.get(i))) {
                if (prefixesUsed.size() > 0) {
                    firstPartOfQuery.append(" , ");
                }

                firstPartOfQuery.append(listOfPrefixes.get(i));
                if (mapOfJoin.containsKey(listOfPrefixes.get(i))) {
                    firstPartOfQuery.append(" " + mapOfJoin.get(listOfPrefixes.get(i)) + " ");
                }

                prefixesUsed.add(listOfPrefixes.get(i));
            }
        }

        queryString.insert(0, firstPartOfQuery);
        queryString.append(" ORDER BY act.keyword ASC");

        if (queryString.toString().trim().length() == 0) {
            return new ArrayList<Actor>();

        }
        query = em.createQuery(queryString.toString());

        List<String> listOfParameters = new ArrayList<String>(mapOfParameters.keySet());
        for (String param : listOfParameters) {
            query.setParameter(param, mapOfParameters.get(param));
        }

        return query.getResultList();
    }

    @Override
    public List<Transaction> getAllPossibleTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPossibleTransactions");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query query;
        StringBuffer queryString = new StringBuffer();
        HashSet<String> prefixes = new HashSet<String>();
        HashSet<String> prefixesUsed = new HashSet<String>();
        HashMap<String, String> mapOfJoin = new HashMap<String, String>();
        HashMap<String, Object> mapOfParameters = new HashMap<String, Object>();

        if (selectedIntegrationProfile != null) {
            DatabaseUtil.addANDorWHERE(queryString);
            prefixes.add("Transaction trans");
            prefixes.add("ProfileLink pl");
            queryString.append("pl.transaction=trans AND pl.actorIntegrationProfile.integrationProfile=:ip");
            mapOfParameters.put("ip", selectedIntegrationProfile);
        }

        if (selectedActor != null) {
            DatabaseUtil.addANDorWHERE(queryString);
            prefixes.add("Transaction trans");
            prefixes.add("ProfileLink pl");
            queryString.append("pl.transaction=trans AND pl.actorIntegrationProfile.actor=:act");
            mapOfParameters.put("act", selectedActor);
        }

        prefixes.add("Transaction trans");

        List<String> listOfPrefixes = new ArrayList<String>(prefixes);
        StringBuffer firstPartOfQuery = new StringBuffer();

        for (int i = 0; i < listOfPrefixes.size(); i++) {
            if (i == 0) {
                firstPartOfQuery.append("SELECT distinct trans FROM ");
            }

            if (!prefixesUsed.contains(listOfPrefixes.get(i))) {
                if (prefixesUsed.size() > 0) {
                    firstPartOfQuery.append(" , ");
                }

                firstPartOfQuery.append(listOfPrefixes.get(i));
                if (mapOfJoin.containsKey(listOfPrefixes.get(i))) {
                    firstPartOfQuery.append(" " + mapOfJoin.get(listOfPrefixes.get(i)) + " ");
                }

                prefixesUsed.add(listOfPrefixes.get(i));
            }
        }

        queryString.insert(0, firstPartOfQuery);
        queryString.append(" ORDER BY trans.keyword ASC");

        if (queryString.toString().trim().length() == 0) {
            return new ArrayList<Transaction>();

        }
        query = em.createQuery(queryString.toString());

        List<String> listOfParameters = new ArrayList<String>(mapOfParameters.keySet());
        for (String param : listOfParameters) {
            query.setParameter(param, mapOfParameters.get(param));
        }

        return query.getResultList();
    }

    @Override
    public void initializeActorAndIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeActorAndIP");
        }
        this.selectedActor = null;
        this.selectedIntegrationProfile = null;
    }

    @Override
    public String truncateTextForDropDownListBoxesInSearchPage(Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("truncateTextForDropDownListBoxesInSearchPage");
        }
        if (obj.getClass().getSimpleName().equals("String")) {
            return truncateTextForDropDownListBoxesInSearchPage(obj.getClass().getSimpleName());
        }

        String inString = "";

        if (obj instanceof IntegrationProfile) {
            inString = ((IntegrationProfile) obj).getKeyword() + " - " + ((IntegrationProfile) obj).getName();
        }

        if (obj instanceof Actor) {
            inString = ((Actor) obj).getKeyword() + " - " + ((Actor) obj).getName();
        }

        if (obj instanceof Transaction) {
            inString = ((Transaction) obj).getKeyword() + " - " + ((Transaction) obj).getName();
        }

        if (inString.length() > TEXT_MAX_SIZE_FOR_DROPDOWNLIST) {
            inString = inString.substring(0, TEXT_MAX_SIZE_FOR_DROPDOWNLIST);
            inString = inString.concat("...");
        }
        return inString;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    private static enum LinkType {
        INTEGRATIONPROFILES,
        ACTORS
    }

}
