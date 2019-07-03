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

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Name("aipoManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("AipoManagerLocal")
public class AipoManager implements Serializable, AipoManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357012043456235110L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AipoManager.class);

    @In
    private EntityManager entityManager;
    @In
    private FacesMessages facesMessages;

    // -------------------------------------- ActorIntegratonProfileOptions links
    @In(required = false)
    @Out(required = false)
    private ActorIntegrationProfileOption aipo;

    @DataModel
    private List<ActorIntegrationProfileOption> aipos;

    // -------------------------------------------------------------------- Actor
    @In(required = false)
    @Out(required = false)
    private Actor aipoActor;

    @DataModel
    private List<Actor> aipoActors;

    private List<Actor> aipoSelectedActors;

    @Out(required = false)
    private String actorSize;
    @Out(required = false)
    private Boolean actorReadOnly;

    // ------------------------------------------------------ Integration Profile
    @In(required = false)
    @Out(required = false)
    private IntegrationProfile aipoIntegrationProfile;

    @DataModel
    private List<IntegrationProfile> aipoIntegrationProfiles;

    private List<IntegrationProfile> aipoSelectedIntegrationProfiles;


    @Out(required = false)
    private String integrationProfileSize;
    @Out(required = false)
    private Boolean integrationProfileReadOnly;

    // ----------------------------------------------- integration profile option
    @In(required = false)
    @Out(required = false)
    private IntegrationProfileOption aipoOption;

    @DataModel
    private List<IntegrationProfileOption> aipoOptions;

    private List<IntegrationProfileOption> aipoSelectedOptions;


    @Out(required = false)
    private String optionSize;
    @Out(required = false)
    private Boolean optionReadOnly;

    // ************************************* Variable screen messages and labels
    @In
    private Map<String, String> messages;

    private String addTitle;
    private String addTitleName;
    private CallerClass callerClass;

    @Override
    public List<IntegrationProfileOption> getAipoSelectedOptions() {
        return aipoSelectedOptions;
    }

    @Override
    public void setAipoSelectedOptions(List<IntegrationProfileOption> aipoSelectedOptions) {
        this.aipoSelectedOptions = aipoSelectedOptions;
    }

    @Override
    public List<IntegrationProfile> getAipoSelectedIntegrationProfiles() {
        return aipoSelectedIntegrationProfiles;
    }

    @Override
    public void setAipoSelectedIntegrationProfiles(List<IntegrationProfile> aipoSelectedIntegrationProfiles) {
        this.aipoSelectedIntegrationProfiles = aipoSelectedIntegrationProfiles;
    }

    @Override
    public List<Actor> getAipoSelectedActors() {
        return aipoSelectedActors;
    }

    @Override
    public void setAipoSelectedActors(List<Actor> aipoSelectedActors) {
        this.aipoSelectedActors = aipoSelectedActors;
    }

    @Override
    public String getAddTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddTitle");
        }
        return messages.get(addTitle) + " " + addTitleName;
    }

    @Override
    public Boolean getActorReadOnly() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorReadOnly");
        }
        return actorReadOnly;
    }

    @Override
    public void setActorReadOnly(Boolean actorReadOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorReadOnly");
        }
        this.actorReadOnly = actorReadOnly;
    }

    @Override
    public Boolean getIntegrationProfileReadOnly() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileReadOnly");
        }
        return integrationProfileReadOnly;
    }

    @Override
    public void setIntegrationProfileReadOnly(Boolean integrationProfileReadOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIntegrationProfileReadOnly");
        }
        this.integrationProfileReadOnly = integrationProfileReadOnly;
    }

    @Override
    public String getCallerClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCallerClass");
        }
        return callerClass.name();
    }

    @Override
    public boolean isCallerClass(String n) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isCallerClass");
        }
        return callerClass.name().equalsIgnoreCase(n);
    }

    @Override
    public String addaipos(Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addaipos");
        }

        if (entityManager == null) {
            entityManager = EntityManagerService.provideEntityManager();
        }
        aipoActors = null;
        aipoSelectedActors = new ArrayList<Actor>();
        aipoOptions = null;
        aipoSelectedOptions = new ArrayList<IntegrationProfileOption>();
        aipoIntegrationProfiles = null;
        aipoSelectedIntegrationProfiles = new ArrayList<IntegrationProfile>();
        aipos = null;
        actorSize = "20";
        integrationProfileSize = "20";
        optionSize = "20";
        actorReadOnly = false;
        integrationProfileReadOnly = false;
        optionReadOnly = false;

        // -------------------------------------- determine and load passed class
        if (obj.getClass() == Actor.class) {

            Actor a = entityManager.find(Actor.class, ((Actor) obj).getId());
            aipoActors = new ArrayList<Actor>();
            aipoActors.add(a);
            aipoSelectedActors.add(a);
            actorSize = "2";
            callerClass = CallerClass.ACTOR;
            addTitle = "gazelle.tf.addaipo.Actor";
            addTitleName = a.getName();
            aipoActor = a;
            actorReadOnly = true;
        }
        if (obj.getClass() == IntegrationProfileOption.class) {

            IntegrationProfileOption t = entityManager.find(IntegrationProfileOption.class,
                    ((IntegrationProfileOption) obj).getId());
            aipoOptions = new ArrayList<IntegrationProfileOption>();
            aipoOptions.add(t);
            aipoSelectedOptions.add(t);
            optionSize = "2";
            callerClass = CallerClass.OPTION;
            addTitle = "gazelle.tf.ipo.aipo.Add";
            addTitleName = t.getName();
            aipoOption = t;
            optionReadOnly = true;
        }

        if (obj.getClass() == IntegrationProfile.class) {

            IntegrationProfile p = entityManager.find(IntegrationProfile.class, ((IntegrationProfile) obj).getId());
            aipoIntegrationProfiles = new ArrayList<IntegrationProfile>();
            aipoIntegrationProfiles.add(p);
            aipoSelectedIntegrationProfiles.add(p);
            integrationProfileSize = "2";
            callerClass = CallerClass.INTEGRATION_PROFILE;
            addTitle = "gazelle.tf.addaipo.IntegrationProfile";
            addTitleName = p.getName();
            aipoIntegrationProfile = p;
            integrationProfileReadOnly = true;
        }
        // ----------------------- Now load the other two lists, not loaded above
        if (aipoActors == null) {
            if (callerClass == CallerClass.INTEGRATION_PROFILE) {
                ActorQuery q = new ActorQuery();
                q.actorIntegrationProfiles().integrationProfile().id().eq(aipoIntegrationProfiles.get(0).getId());
                q.keyword().order(true);
                aipoActors = q.getList();
            } else {
                ActorQuery q = new ActorQuery();
                q.keyword().order(true);
                aipoActors = q.getList();
            }
        }
        if (aipoIntegrationProfiles == null) {
            if (callerClass == CallerClass.ACTOR) {
                IntegrationProfileQuery q = new IntegrationProfileQuery();
                q.actorIntegrationProfiles().actor().id().eq(aipoActors.get(0).getId());
                q.keyword().order(true);
                aipoIntegrationProfiles = q.getList();
            } else {
                IntegrationProfileQuery q = new IntegrationProfileQuery();
                q.keyword().order(true);
                aipoIntegrationProfiles = q.getList();
            }
        }
        if (aipoOptions == null) {
            IntegrationProfileOptionQuery q = new IntegrationProfileOptionQuery();
            q.keyword().order(true);
            aipoOptions = q.getList();
        }

        return "/tf/actorIntegrationProfileOption/addActorIntegrationProfileOptions.seam";
    }

    /**
     * createLinks() - action method which creates Profile Links for the selected items in the lists
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String createLinks(List<Actor> asa, List<IntegrationProfile> asip, List<IntegrationProfileOption> aso) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLinks");
        }
        aipoSelectedActors = asa;
        aipoSelectedIntegrationProfiles = asip;
        aipoSelectedOptions = aso;
        return createLinks();
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String createLinks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLinks");
        }

        if (entityManager == null) {
            entityManager = EntityManagerService.provideEntityManager();
        }
        if (facesMessages == null) {
            facesMessages = FacesMessages.instance();
        }
        ActorIntegrationProfile aip;
        ActorIntegrationProfile newAip = new ActorIntegrationProfile();
        int countAIP = 0;
        int countDup = 0;
        int countCreated = 0;
        // ***************************************** Make sure selections are made
        boolean err = false;
        if (aipoSelectedActors.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Actors selected");
        }
        if (aipoSelectedIntegrationProfiles.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Integration Profiles selected");
        }
        if (aipoSelectedOptions.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Options selected");
        }
        if (err) {
            return null;
        }
        String query;
        // ***************************** iterate through the various permutations
        for (int ac = 0; ac < aipoSelectedActors.size(); ac++) {
            Actor a = aipoSelectedActors.get(ac);
            for (int ic = 0; ic < aipoSelectedIntegrationProfiles.size(); ic++) {
                IntegrationProfile i = aipoSelectedIntegrationProfiles.get(ic);
                // ******* verify that Actor/IntegrationProfile entity exists
                query = "select aip from ActorIntegrationProfile aip where aip.actor.id = " + a.getId()
                        + " and aip.integrationProfile.id = " + i.getId();
                try {
                    aip = (ActorIntegrationProfile) entityManager.createQuery(query).getSingleResult();
                } catch (Exception e) {
                    LOG.info(" AIP not found");
                    countAIP++;
                    continue;
                }
                for (int oc = 0; oc < aipoSelectedOptions.size(); oc++) {
                    IntegrationProfileOption o = entityManager.find(IntegrationProfileOption.class, aipoSelectedOptions
                            .get(oc).getId());
                    // ************************** Verify that Link does not exist
                    query = "select count(aipo.id) from ActorIntegrationProfileOption aipo where aipo.actorIntegrationProfile.id = "
                            + aip.getId() + " and aipo.integrationProfileOption.id = " + o.getId();
                    LOG.info("query=" + query);
                    Long aipoc = (Long) entityManager.createQuery(query).getSingleResult();
                    LOG.info("count=" + aipoc);
                    if (aipoc != 0) {
                        countDup++;
                        continue;
                    }
                    // ************************************* Make new ProfileLink
                    ActorIntegrationProfileOption aipo = new ActorIntegrationProfileOption();
                    aipo.setActorIntegrationProfile(aip);
                    aipo.setIntegrationProfileOption(o);
                    entityManager.persist(aipo);
                    entityManager.flush();
                    o.addActorIntegrationProfileOption(aipo);
                    aip.addActorIntegrationProfileOption(aipo);
                    newAip.addActorIntegrationProfileOption(aipo);
                    countCreated++;
                }
            }
        }
        if (countAIP > 0) {
            facesMessages.add(StatusMessage.Severity.ERROR, "Invalid Actor/Integration Profile pairs: " + countAIP);
        }
        if (countDup > 0) {
            facesMessages.add(StatusMessage.Severity.ERROR, "Links already present: " + countDup);
        }
        facesMessages.add(StatusMessage.Severity.INFO, "Profile Links created: " + countCreated);
        if (newAip != null && newAip.getActorIntegrationProfileOption() != null) {
            for (ActorIntegrationProfileOption aipo : newAip.getActorIntegrationProfileOption()) {
                facesMessages.add(StatusMessage.Severity.INFO, "Actor : " + aipo.getActorIntegrationProfile() + ", Option : "
                        + aipo.getIntegrationProfileOption() + " is added");
            }
        }
        // ************************************************ Reset list selections
        if (callerClass != CallerClass.ACTOR) {
            aipoSelectedActors = new ArrayList<Actor>();
        }
        if (callerClass != CallerClass.INTEGRATION_PROFILE) {
            aipoSelectedIntegrationProfiles = new ArrayList<IntegrationProfile>();
        }
        if (callerClass != CallerClass.OPTION) {
            aipoSelectedOptions = new ArrayList<IntegrationProfileOption>();
        }
        return null;
    }

    /**
     * finished() - action method which returns to calling screen
     *
     * @return screen to display
     */
    @Override
    public String finished() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("finished");
        }

        switch (callerClass) {
            case ACTOR:
                ActorManagerLocal am = (ActorManagerLocal) Component.getInstance("actorManager");
                return am.editActor(aipoActors.get(0), true);
            case INTEGRATION_PROFILE:
                IntegrationProfileManagerLocal im = (IntegrationProfileManagerLocal) Component
                        .getInstance("integrationProfileManager");
                return im.editIntegrationProfile(aipoIntegrationProfiles.get(0));
            case OPTION:
                IntegrationProfileOptionManagerLocal tm = (IntegrationProfileOptionManagerLocal) Component
                        .getInstance("integrationProfileOptionManager");
                return tm.editIntegrationProfileOption(aipoOptions.get(0));
        }

        return null;
    }

    /**
     * delaipo - deletes aipo link @ aipo aipo to delete @ obj - object being edited There is no delete effects display for this action.
     *
     * @return - returns to calling screen.
     */
    @Override
    public String delaipo(Object obj, ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delaipo");
        }

        try {
            ActorIntegrationProfileOption p = entityManager.find(ActorIntegrationProfileOption.class, aipo.getId());
            IntegrationProfileOption o = p.getIntegrationProfileOption();
            ActorIntegrationProfile aip = p.getActorIntegrationProfile();
            o.delActorIntegrationProfileOption(p);
            aip.delActorIntegrationProfileOption(p);

            entityManager.remove(p);
            entityManager.flush();

            if ((obj != null) && (obj.getClass() == Actor.class)) {
                Actor a = entityManager.find(Actor.class, ((Actor) obj).getId());
                ActorManagerLocal am = (ActorManagerLocal) Component.getInstance("actorManager");
                return am.editActor(a, true);
            }

            if ((obj != null) && (obj.getClass() == IntegrationProfileOption.class)) {

                IntegrationProfileOption q = entityManager.find(IntegrationProfileOption.class,
                        ((IntegrationProfileOption) obj).getId());

                IntegrationProfileOptionManagerLocal tm = (IntegrationProfileOptionManagerLocal) Component
                        .getInstance("integrationProfileOptionManager");
                return tm.editIntegrationProfileOption(q);
            }

            if ((obj != null) && (obj.getClass() == IntegrationProfile.class)) {

                IntegrationProfile ip = entityManager
                        .find(IntegrationProfile.class, ((IntegrationProfile) obj).getId());
                IntegrationProfileManagerLocal im = (IntegrationProfileManagerLocal) Component
                        .getInstance("integrationProfileManager");
                return im.editIntegrationProfile(ip);
            }
            LOG.warn("invalid calling obj to delProfileLink");
            return null;

        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This object is already mapped with a ConfigurationMappedWithAIPO object. It" +
                    " cannot be deleted because it is used...");
            return null;
        }
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public void saveMaybeSupportive(ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveMaybeSupportive");
        }
        EntityManagerService.provideEntityManager().merge(aipo);
    }

    public String checkForDomainOf(ActorIntegrationProfileOption aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkForDomainOf");
        }
        if (aipo != null) {
            HQLQueryBuilder<Domain> hql = new HQLQueryBuilder<Domain>(Domain.class);
            hql.addEq("integrationProfilesForDP.id", aipo.getActorIntegrationProfile().getIntegrationProfile().getId());
            List<Domain> ff = hql.getList();
            if ((ff != null) && (ff.size() > 0)) {
                return ff.get(0).getKeyword();
            }
        }
        return null;
    }

    /**
     * Addaipos: method called from .xhtml tabs which list AIPOs related to [actor|transaction|integration profile option] to add inks to the
     * calling entity. Displays an appropriately set up set of
     * lists.
     *
     * @param obj The entity to which the links are added.
     * @return String the add links .xhtml
     */
    enum CallerClass {
        ACTOR,
        INTEGRATION_PROFILE,
        OPTION
    }

}
