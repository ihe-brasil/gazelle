package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingType;
import net.ihe.gazelle.tm.utils.systems.IHEImplementationForSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <p>IHEImplementationForSystemManagerDAO class.</p>
 *
 * @author abe
 * @version 1.0: 28/01/19
 */

public class IHEImplementationForSystemDAO {

    private static final Logger LOG = LoggerFactory.getLogger(IHEImplementationForSystemDAO.class);

    private static void listActorProfiles(List<SystemActorProfiles> listSystemActorProfiles, List<IHEImplementationForSystem> iheImplementations) {
        IntegrationProfileOption ipo;
        for (int i = 0; i < listSystemActorProfiles.size(); i++) {

            ActorIntegrationProfileOption aipo = listSystemActorProfiles.get(i)
                    .getActorIntegrationProfileOption();
            if ((aipo != null) && (aipo.getIntegrationProfileOption() != null)) {
                ipo = aipo.getIntegrationProfileOption();

                IHEImplementationForSystem oneIheImplementation = new IHEImplementationForSystem(aipo
                        .getActorIntegrationProfile().getIntegrationProfile(), aipo
                        .getActorIntegrationProfile().getActor(), ipo, listSystemActorProfiles.get(i)
                        .getTestingType(), listSystemActorProfiles.get(i).getWantedTestingType(),
                        listSystemActorProfiles.get(i).getTestingTypeReviewed());
                iheImplementations.add(oneIheImplementation);
            }
        }
    }

    /**
     * Get the list of all ActorIntegrationProfileOption for a selected system
     */

    public static List<IHEImplementationForSystem> getAllIHEImplementationsForSystem(System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllIHEImplementationsForSystem");
        }

        if (system != null) {
            List<SystemActorProfiles> listSystemActorProfiles = null;
            try {
                HQLQueryBuilder<SystemActorProfiles> queryBuilder = new HQLQueryBuilder<SystemActorProfiles>(
                        SystemActorProfiles.class);
                queryBuilder.addEq("system.id", system.getId());
                queryBuilder.addOrder(
                        "actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.keyword", true);
                queryBuilder.addOrder("actorIntegrationProfileOption.actorIntegrationProfile.actor.keyword", true);
                listSystemActorProfiles = queryBuilder.getList();
            } catch (NullPointerException e) {
                ExceptionLogging.logException(e, LOG);

            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);

            }

            if (listSystemActorProfiles != null) {
                List<IHEImplementationForSystem> iheImplementations = new Vector<IHEImplementationForSystem>();
                listActorProfiles(listSystemActorProfiles, iheImplementations);

                return iheImplementations;
            } else {
                LOG.info("No AIPO found for system " + system.getKeyword());
                return null;
            }

        } else {
            LOG.debug("system is null, cannot list AIPO");
            return null;
        }
    }

    static List<IHEImplementationForSystem> getIHEImplementationsToDisplay(System system){
        List<IHEImplementationForSystem> iheImplementationForSystems = getAllIHEImplementationsForSystem(system);
        return getIHEImplementationsToDisplay(iheImplementationForSystems);
    }


    static List<IHEImplementationForSystem> getIHEImplementationsToDisplay(List<IHEImplementationForSystem> iheImplementations) {
        if (iheImplementations != null) {
            List<IHEImplementationForSystem> list = new ArrayList<IHEImplementationForSystem>();
            for (IHEImplementationForSystem iheImp : iheImplementations) {
                IntegrationProfile ip = new IntegrationProfile(iheImp.getIntegrationProfile());
                ip.setId(iheImp.getIntegrationProfile().getId());
                Actor a = new Actor(iheImp.getActor());
                a.setId(iheImp.getActor().getId());
                IntegrationProfileOption option = new IntegrationProfileOption(iheImp.getIntegrationProfileOption());
                option.setId(iheImp.getIntegrationProfileOption().getId());

                TestingType type = null;
                if (iheImp.getTestingType() != null) {
                    type = new TestingType(iheImp.getTestingType());
                    type.setId(iheImp.getTestingType().getId());
                }
                TestingType wantedType = null;
                if (iheImp.getWantedTestingType() != null) {
                    wantedType = new TestingType(iheImp.getWantedTestingType());
                    wantedType.setId(iheImp.getWantedTestingType().getId());
                }

                IHEImplementationForSystem iheImpForSys = new IHEImplementationForSystem(ip, a, option, type,
                        wantedType, iheImp.isTestingTypeReviewed());
                list.add(iheImpForSys);

            }

            reinitListIHEImp(list);
            return list;

        } else {
            return null;
        }
    }

    private static void reinitListIHEImp(List<IHEImplementationForSystem> list) {

        Iterator<IHEImplementationForSystem> it = list.iterator();
        IHEImplementationForSystem iheImp;

        while (it.hasNext()) {
            iheImp = it.next();
            iheImp.getActor().setToDisplay(true);
            iheImp.getIntegrationProfile().setToDisplay(true);
            iheImp.getIntegrationProfileOption().setToDisplay(true);
        }
    }

    static SystemActorProfiles getSystemActorProfiles(EntityManager entityManager, IHEImplementationForSystem iheImplementationForSystem,
                                                      SystemInSession systemInSession) {
        SystemActorProfiles currentSystemActorProfile;
        currentSystemActorProfile = (SystemActorProfiles) entityManager
                .createQuery(
                        "select distinct sap from SystemActorProfiles sap where sap.actorIntegrationProfileOption.actorIntegrationProfile.actor.id = "
                                + iheImplementationForSystem.getActor().getId()
                                + " and sap.system.id = "
                                + systemInSession.getSystem().getId()
                                + " and sap.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.id = "
                                + iheImplementationForSystem.getIntegrationProfile().getId())
                .getSingleResult();
        return currentSystemActorProfile;
    }

    static SystemActorProfiles getSystemActorProfilesQuery(EntityManager entityManager, IHEImplementationForSystem iheImplementationForSystem,
                                                           SystemInSession systemInSession) {
        SystemActorProfiles currentSystemActorProfile;
        currentSystemActorProfile = (SystemActorProfiles) entityManager
                .createQuery(
                        "select distinct sap from SystemActorProfiles sap where sap.actorIntegrationProfileOption.actorIntegrationProfile.actor.id = "
                                + iheImplementationForSystem.getActor().getId()
                                + " and sap.system.id = "
                                + systemInSession.getSystem().getId()
                                + " and sap.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.id = "
                                + iheImplementationForSystem.getIntegrationProfile().getId()
                                + " and sap.actorIntegrationProfileOption.integrationProfileOption.id = "
                                + iheImplementationForSystem.getIntegrationProfileOption().getId())
                .getSingleResult();
        return currentSystemActorProfile;
    }

}
