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
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tf.model.TransactionLink;
import net.ihe.gazelle.tf.model.TransactionLinkQuery;
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


@Name("tlManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("TransactionLinkManagerLocal")
public class TransactionLinkManager implements Serializable, TransactionLinkManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357084543456235110L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TransactionLinkManager.class);
    @In
    private EntityManager entityManager;
    @In
    private FacesMessages facesMessages;
    // -------------------------------------- ActorIntegratonProfileOptions links
    @In(required = false)
    @Out(required = false)
    private TransactionLink transactionLink;

    // --------------------------------------------------------------- from Actor
    @In(required = false)
    @Out(required = false)
    private Actor fromActor;

    @In(required = false)
    @Out(required = false)
    private Actor dispActor;

    @DataModel
    private List<Actor> fromActors;

    private Actor fromSelectedActor;

    @Out(required = false)
    private String fromActorSize;
    @Out(required = false)
    private Boolean fromActorReadOnly;

    // ----------------------------------------------------------------- to Actor
    @In(required = false)
    @Out(required = false)
    private Actor toActor;

    @DataModel
    private List<Actor> toActors;

    private Actor toSelectedActor;

    @Out(required = false)
    private String toActorSize;
    @Out(required = false)
    private Boolean toActorReadOnly;

    // ----------------------------------------------- integration profile option
    @In(required = false)
    @Out(required = false)
    private Transaction tlTransaction;

    @DataModel
    private List<Transaction> tlTransactions;

    private List<Transaction> tlSelectedTransactions;

    @Out(required = false)
    private String tlTransactionSize;
    @Out(required = false)
    private Boolean tlTransactionReadOnly;

    // ************************************* Variable screen messages and labels
    @In
    private Map<String, String> messages;

    private String addTitle;
    private CallerClass callerClass;

    @Override
    public String getAddTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddTitle");
        }
        return messages.get(addTitle);
    }

    ;

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
        return callerClass.name().contains(n);
    }

    public Actor getFromSelectedActor() {
        return fromSelectedActor;
    }

    public void setFromSelectedActor(Actor fromSelectedActor) {
        this.fromSelectedActor = fromSelectedActor;
    }

    public List<Transaction> getTlSelectedTransactions() {
        return tlSelectedTransactions;
    }

    public void setTlSelectedTransactions(List<Transaction> tlSelectedTransactions) {
        this.tlSelectedTransactions = tlSelectedTransactions;
    }

    public Actor getToSelectedActor() {
        return toSelectedActor;
    }

    public void setToSelectedActor(Actor toSelectedActor) {
        this.toSelectedActor = toSelectedActor;
    }

    public Boolean getFromActorReadOnly() {
        return fromActorReadOnly;
    }

    public void setFromActorReadOnly(Boolean fromActorReadOnly) {
        this.fromActorReadOnly = fromActorReadOnly;
    }

    public Boolean getToActorReadOnly() {
        return toActorReadOnly;
    }

    public void setToActorReadOnly(Boolean toActorReadOnly) {
        this.toActorReadOnly = toActorReadOnly;
    }

    public Boolean getTlTransactionReadOnly() {
        return tlTransactionReadOnly;
    }

    public void setTlTransactionReadOnly(Boolean tlTransactionReadOnly) {
        this.tlTransactionReadOnly = tlTransactionReadOnly;
    }

    @Override
    public String addTransactionLinks(Object obj, String className) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTransactionLinks");
        }
        fromActors = null;
        fromSelectedActor = null;
        toActors = null;
        toSelectedActor = null;
        tlTransaction = null;
        tlSelectedTransactions = new ArrayList<Transaction>();
        fromActorSize = "20";
        toActorSize = "20";
        tlTransactionSize = "20";
        fromActorReadOnly = false;
        toActorReadOnly = false;
        tlTransactionReadOnly = false;

        // -------------------------------------- determine and load passed class
        if (className.equalsIgnoreCase("FROM_ACTOR")) {

            Actor a = entityManager.find(Actor.class, ((Actor) obj).getId());
            fromActors = new ArrayList<Actor>();
            fromActors.add(a);
            fromSelectedActor = a;
            fromActorSize = "2";
            callerClass = CallerClass.FROM_ACTOR;
            addTitle = "Add transaction Links from this Actor";
            fromActor = a;
            dispActor = a;
            fromActorReadOnly = true;
        }
        if (className.equalsIgnoreCase("TO_ACTOR")) {

            Actor a = entityManager.find(Actor.class, ((Actor) obj).getId());
            toActors = new ArrayList<Actor>();
            toActors.add(a);
            toSelectedActor = a;
            toActorSize = "2";
            callerClass = CallerClass.TO_ACTOR;
            addTitle = "gazelle.tf.tlToActor.add";
            toActor = a;
            dispActor = a;
            toActorReadOnly = true;
        }

        if (className.equalsIgnoreCase("TRANSACTION")) {

            Transaction t = entityManager.find(Transaction.class, ((Transaction) obj).getId());
            tlTransactions = new ArrayList<Transaction>();
            tlTransactions.add(t);
            tlSelectedTransactions.add(t);
            tlTransactionSize = "2";
            callerClass = CallerClass.TRANSACTION;
            addTitle = "gazelle.tf.addtl.transaction";
            tlTransaction = t;
            tlTransactionReadOnly = true;
        }
        // ----------------------- Now load the other two lists, not loaded above
        if (fromActors == null) {
            fromActors = entityManager.createQuery("select a from Actor a order by a.keyword").getResultList();
        }
        if (toActors == null) {
            toActors = entityManager.createQuery("select a from Actor a order by a.keyword").getResultList();
        }
        if (tlTransactions == null) {
            tlTransactions = entityManager.createQuery("select o from Transaction o order by o.keyword")
                    .getResultList();
        }

        return "/tf/transactionLink/addTransactionLinks.seam";
    }

    /**
     * createLinks() - action method which creates Transaction Link for the selected item in the lists
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String createLinks(Actor fsa, Actor tsa, List<Transaction> tst) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLinks");
        }
        fromSelectedActor = fsa;
        toSelectedActor = tsa;
        tlSelectedTransactions = tst;
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
        int countDup = 0;
        int countCreated = 0;
        // ***************************************** Make sure selections are made
        boolean err = false;
        if (fromSelectedActor == null) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No from Actor selected");
        }
        if (toSelectedActor == null) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No to Actor selected");
        }
        if (tlSelectedTransactions.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Transactions selected");
        }
        if (err) {
            return null;
        }

        fromSelectedActor = getSelectedActor(fromSelectedActor.getId());
        toSelectedActor = getSelectedActor(toSelectedActor.getId());

        for (int tc = 0; tc < tlSelectedTransactions.size(); tc++) {
            Transaction t = entityManager.find(Transaction.class, tlSelectedTransactions.get(tc).getId());
            // ********************************** Verify that Link does not exist
            TransactionLinkQuery query = new TransactionLinkQuery();
            query.fromActor().id().eq(fromSelectedActor.getId());
            query.transaction().id().eq(t.getId());
            query.toActor().id().eq(toSelectedActor.getId());
            query.id().getCountOnPath();

            Long tlc = (long) query.id().getCountOnPath();

            if (tlc != 0) {
                countDup++;
                continue;
            }
            // **************************************************** Make new Link
            TransactionLink tl = new TransactionLink();
            tl.setFromActor(fromSelectedActor);
            tl.setToActor(toSelectedActor);
            tl.setTransaction(t);
            entityManager.persist(tl);
            entityManager.flush();
            t.addTransactionLink(tl);
            fromSelectedActor.addTransactionLinksWhereActingAsSource(tl);
            toSelectedActor.addTransactionLinksWhereActingAsReceiver(tl);
            countCreated++;
        }

        if (countDup > 0) {
            facesMessages.add(StatusMessage.Severity.ERROR, "Links already present: " + countDup);
        }
        facesMessages.add(StatusMessage.Severity.INFO, "Links created: " + countCreated);
        // ************************************************ Reset list selections
        if (callerClass != CallerClass.FROM_ACTOR) {
            fromSelectedActor = null;
        }
        if (callerClass != CallerClass.TO_ACTOR) {
            toSelectedActor = null;
        }
        if (callerClass != CallerClass.TRANSACTION) {
            tlSelectedTransactions = new ArrayList<Transaction>();
        }
        return null;
    }

    private Actor getSelectedActor(int actorId) {
        return entityManager.find(Actor.class, actorId);
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

        ActorManagerLocal am = null;
        switch (callerClass) {
            case FROM_ACTOR:
                am = (ActorManagerLocal) Component.getInstance("actorManager");
                return am.editActor(fromSelectedActor, true);
            case TO_ACTOR:
                am = (ActorManagerLocal) Component.getInstance("actorManager");
                return am.editActor(toSelectedActor, true);
            case TRANSACTION:
                TransactionManagerLocal tm = (TransactionManagerLocal) Component.getInstance("transactionManager");
                return tm.editTransaction(tlSelectedTransactions.get(0), true);
        }

        return null;
    }

    /**
     * deltl - deletes transaction link @ tl transaction link to delete @ obj - object being edited There is no delete effects display for this
     * action.
     *
     * @return - returns to calling screen.
     */
    @Override
    public String delTransactionLink(Object obj, TransactionLink tl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delTransactionLink");
        }

        TransactionLink p = entityManager.find(TransactionLink.class, tl.getId());
        Actor a = p.getFromActor();
        a.delTransactionLinksWhereActingAsSource(p);
        a = p.getToActor();
        a.delTransactionLinksWhereActingAsReceiver(p);
        Transaction t = p.getTransaction();
        t.delTransactionLink(p);

        entityManager.remove(p);
        entityManager.flush();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Transaction links removed");
        if (obj != null) {
            if (obj.getClass() == Actor.class) {
                Actor tmpActor = ((Actor) obj);

                a = entityManager.find(Actor.class, tmpActor.getId());
                ActorManagerLocal am = (ActorManagerLocal) Component.getInstance("actorManager");
                return am.editActor(a, true);
            }

            if (obj.getClass() == Transaction.class) {
                t = entityManager.find(Transaction.class, ((Transaction) obj).getId());
                TransactionManagerLocal tm = (TransactionManagerLocal) Component.getInstance("transactionManager");
                return tm.editTransaction(t, true);
            }
        }
        LOG.warn("invalid calling obj to delProfileLink");
        return null;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    /**
     * addTransactionLinks: method called from .xhtml tabs which list tls related to from and to actors to add links to the calling entity.
     * Displays an appropriately set up set of lists.
     *
     * @param obj The entity to which the links are added.
     * @return String the add links .xhtml
     */
    enum CallerClass {
        FROM_ACTOR,
        TO_ACTOR,
        TRANSACTION
    }

}
