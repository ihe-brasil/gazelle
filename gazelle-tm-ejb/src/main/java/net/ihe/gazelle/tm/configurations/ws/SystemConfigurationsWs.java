package net.ihe.gazelle.tm.configurations.ws;

import net.ihe.gazelle.csv.CSVExporter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tf.ws.data.ActorWrapper;
import net.ihe.gazelle.tf.ws.data.TFConfigurationTypeWrapper;
import net.ihe.gazelle.tf.ws.data.TFConfigurationTypesWrapper;
import net.ihe.gazelle.tm.configurations.dao.ConfigurationTypeMappedWithAIPODAO;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemActorProfilesQuery;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Stateless
@Name("systemConfigurationWS")
public class SystemConfigurationsWs implements SystemConfigurationsWsApi {
    private static final String ALL = "ALL";
    private static final Logger LOG = LoggerFactory.getLogger(SystemConfigurationsWs.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Response getOIDBySystemByRequirement(String systemKeyword, String oidRequirementLabel,
                                                String testingSessionId, String institutionKeyword, String actorKeyword) {
        TestingSession testingSession = null;
        if (testingSessionId == null || testingSessionId.isEmpty()) {
            return Response.status(404).header("Warning", "Testing session id (tsid) is mandatory").build();
        } else {
            try {
                Integer sessionId = Integer.valueOf(testingSessionId);
                testingSession = TestingSession.GetSessionById(sessionId);
                if (testingSession == null) {
                    return Response.status(404)
                            .header("Warning", testingSessionId + " does not reference an existing testing session")
                            .build();
                }
            } catch (NumberFormatException e) {
                return Response.status(404)
                        .header("Warning", "Testing session id (tsid) is invalid, a positive integer is expected")
                        .build();
            }
        }
        System system = null;
        OIDRequirement requirement = null;
        Institution institution = null;
        Actor actor = null;
        if (!systemKeyword.isEmpty() && !systemKeyword.equalsIgnoreCase(ALL)) {
            system = System.getSystemByAllKeyword(systemKeyword);
            if (system == null) {
                return Response.status(404).header("Warning", systemKeyword + " does not reference an existing system")
                        .build();
            }
        }
        if (!oidRequirementLabel.isEmpty() && !oidRequirementLabel.equalsIgnoreCase(ALL)) {
            OIDRequirementQuery query = new OIDRequirementQuery();
            query.label().like(oidRequirementLabel);
            requirement = query.getUniqueResult();
            if (requirement == null) {
                query = new OIDRequirementQuery();
                List<String> validValues = query.label().getListDistinct();
                return Response.status(404).header("Warning", "Valid values for OID requirement are: " + validValues)
                        .build();
            }
        }
        if (!institutionKeyword.isEmpty() && !institutionKeyword.equalsIgnoreCase(ALL)) {
            institution = Institution.findInstitutionWithKeyword(institutionKeyword);
            if (institution == null) {
                return Response.status(404)
                        .header("Warning", institutionKeyword + " does not reference an existing institution").build();
            }
        }
        if (!actorKeyword.isEmpty() && !actorKeyword.equalsIgnoreCase(ALL)) {
            actor = Actor.findActorWithKeyword(actorKeyword);
            if (actor == null) {
                return Response.status(404).header("Warning", actorKeyword + " does not reference an existing actor")
                        .build();
            }
        }
        OIDSystemAssignmentQuery query = new OIDSystemAssignmentQuery();
        query.systemInSession().testingSession().eq(testingSession);
        if (system != null) {
            query.systemInSession().system().eq(system);
        }
        if (requirement != null) {
            query.oidRequirement().eq(requirement);
        }
        if (institution != null) {
            query.systemInSession().system().institutionSystems().institution().eq(institution);
        }
        if (actor != null) {
            query.oidRequirement().actorIntegrationProfileOptionList().actorIntegrationProfile().actor().eq(actor);
        }
        List oids = query.getList();
        if (oids.isEmpty()) {
            return Response.status(404).header("Warning", "No OID found with the given parameters").build();
        } else {
            String export = CSVExporter.exportCSV(oids);
            return Response.ok(export).build();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Response listSystemConfigurationsFiltered(String testingSessionId, String systemKeyword, String configType,
                                                     String actorKeyword, String institutionKeyword) {
        TestingSession testingSession = null;
        if (testingSessionId == null || testingSessionId.isEmpty()) {
            return Response.status(404).header("Warning", "Testing session id (tsid) is mandatory").build();
        } else {
            try {
                Integer sessionId = Integer.valueOf(testingSessionId);
                testingSession = TestingSession.GetSessionById(sessionId);
                if (testingSession == null) {
                    return Response.status(404)
                            .header("Warning", testingSessionId + " does not reference an existing testing session")
                            .build();
                }
            } catch (NumberFormatException e) {
                return Response.status(404)
                        .header("Warning", "Testing session id (tsid) is invalid, a positive integer is expected")
                        .build();
            }
        }
        ConfigurationType selectedType = null;
        Actor actor = null;
        System system = null;
        Institution institution = null;

        if (!configType.isEmpty() && !configType.equalsIgnoreCase(ALL)) {
            ConfigurationTypeQuery configTypeQuery = new ConfigurationTypeQuery();
            configTypeQuery.className().like(configType);
            selectedType = configTypeQuery.getUniqueResult();
            if (selectedType == null) {
                return Response
                        .status(404)
                        .header("Warning",
                                "valid values for type are DicomSCU, DicomSCP, HL7V2Initiator, HL7V2Responder,HL7V3Initiator, HL7V3Responder, " +
                                        "Webservice,  Syslog, Raw")
                        .build();
            }
        }
        if (!actorKeyword.isEmpty() && !actorKeyword.equalsIgnoreCase(ALL)) {
            actor = Actor.findActorWithKeyword(actorKeyword);
            if (actor == null) {
                return Response.status(404).header("Warning", actorKeyword + " does not reference an existing actor")
                        .build();
            }
        }
        if (!systemKeyword.isEmpty() && !systemKeyword.equalsIgnoreCase(ALL)) {
            system = System.getSystemByAllKeyword(systemKeyword);
            if (system == null) {
                return Response.status(404).header("Warning", systemKeyword + " does not reference an existing system")
                        .build();
            }
        }
        if (!institutionKeyword.isEmpty() && !institutionKeyword.equalsIgnoreCase(ALL)) {
            institution = Institution.findInstitutionWithKeyword(institutionKeyword);
            if (institution == null) {
                return Response.status(404)
                        .header("Warning", institutionKeyword + " does not reference an existing institution").build();
            }
        }
        AbstractConfigurationQuery query = new AbstractConfigurationQuery();
        query.configuration().systemInSession().testingSession().eq(testingSession);
        if (selectedType != null) {
            query.configuration().configurationType().eq(selectedType);
        }
        if (actor != null) {
            query.configuration().actor().eq(actor);
        }
        if (system != null) {
            query.configuration().systemInSession().system().eq(system);
        }
        if (institution != null) {
            query.configuration().systemInSession().system().institutionSystems().institution().eq(institution);
        }
        List configurations = query.getList();
        if (configurations.isEmpty()) {
            return Response.status(404).header("Warning", "No entry found for the given parameters").build();
        } else {
            String export = CSVExporter.exportCSV(configurations);
            return Response.ok(export).build();
        }
    }

    @Override
    @Deprecated
    public Response listTFConfigurationsForSystemInSession(String systemKeyword) {
        return listTFConfigurationsForSystemInSession(systemKeyword, null);
    }

    @Override
    public Response listTFConfigurationsForSystemInSession(String systemKeyword, String testingSessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTFConfigurationsForSystemInSession");
        }
        System system = null;
        if (systemKeyword.isEmpty()) {
            return Response.status(404).header("Warning", "System keyword is mandatory").build();
        } else {
            SystemQuery query = new SystemQuery();
            query.keyword().eq(systemKeyword);
            if (testingSessionId != null && !testingSessionId.isEmpty()) {
                query.systemsInSession().testingSession().id().eq(Integer.parseInt(testingSessionId));
            }
            system = query.getUniqueResult();
            if (system == null) {
                return Response.status(404).header("Warning", "No system found with keyword " + systemKeyword).build();
            }
            List<ActorIntegrationProfileOption> aipos = query.systemActorProfiles().actorIntegrationProfileOption()
                    .getListDistinct();
            TFConfigurationTypesWrapper wrapper = ConfigurationTypeMappedWithAIPODAO
                    .getTFConfigurationTypesForAIPOs(aipos);
            return Response.ok(wrapper).build();
        }
    }

    @Override
    @Deprecated
    public Response listInboundsForSystemInSession(String systemKeyword, String networkCommunicationTypeKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listInboundsForSystemInSession");
        }
        return listInboundsForSystemInSession(systemKeyword, networkCommunicationTypeKeyword, null);
    }

    @Override
    public Response listInboundsForSystemInSession(String systemKeyword, String networkCommunicationTypeKeyword, String testingSessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listInboundsForSystemInSession");
        }
        return listNetworkCommunicationsForSystemInSession(systemKeyword, networkCommunicationTypeKeyword, true, testingSessionId);
    }

    @Override
    @Deprecated
    public Response listOutboundsForSystemInSession(String systemKeyword, String networkCommunicationTypeKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listOutboundsForSystemInSession");
        }
        return listOutboundsForSystemInSession(systemKeyword, networkCommunicationTypeKeyword, null);
    }

    @Override
    public Response listOutboundsForSystemInSession(String systemKeyword, String networkCommunicationTypeKeyword, String testingSessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listOutboundsForSystemInSession");
        }
        return listNetworkCommunicationsForSystemInSession(systemKeyword, networkCommunicationTypeKeyword, false, testingSessionId);
    }

    private Response listNetworkCommunicationsForSystemInSession(String systemKeyword,
                                                                 String networkCommunicationTypeKeyword, boolean in, String testingSessionId) {
        System sis = null;
        NetworkCommunicationType networkCommunicationType = null;
        if (!systemKeyword.isEmpty()) {
            SystemQuery query = new SystemQuery();
            query.keyword().eq(systemKeyword);
            if (testingSessionId != null && !testingSessionId.isEmpty()) {
                query.systemsInSession().testingSession().id().eq(Integer.parseInt(testingSessionId));
            }
            sis = query.getUniqueResult();
            if (sis == null) {
                return Response.status(404).header("Warning", "No system found with keyword " + systemKeyword).build();
            }
        } else {
            return Response.status(404).header("Warning", "System's keyword is a required parameter").build();
        }
        if (!networkCommunicationTypeKeyword.isEmpty() && !networkCommunicationTypeKeyword.equalsIgnoreCase(ALL)) {
            networkCommunicationType = NetworkCommunicationType.getEnumWithLabel(networkCommunicationTypeKeyword);
            if (networkCommunicationType == null) {
                return Response
                        .status(404)
                        .header("Warning",
                                "Available values for communicationType are All, " + Arrays.toString(NetworkCommunicationType.values()))
                        .build();
            }
        }
        SystemActorProfilesQuery query = new SystemActorProfilesQuery();
        query.system().eq(sis);
        List<ActorIntegrationProfileOption> aipos = query.actorIntegrationProfileOption().getListDistinct();
        List<HQLRestriction> restrictionsAnd = new ArrayList<HQLRestriction>();
        TransactionLinkQuery tquery = new TransactionLinkQuery();
        for (ActorIntegrationProfileOption aipo : aipos) {
            HQLRestriction onActor = null;
            HQLRestriction onTransaction = tquery.transaction().profileLinks().actorIntegrationProfile()
                    .eqRestriction(aipo.getActorIntegrationProfile());
            if (in) {
                onActor = tquery.toActor().eqRestriction(aipo.getActorIntegrationProfile().getActor());
            } else {
                onActor = tquery.fromActor().eqRestriction(aipo.getActorIntegrationProfile().getActor());
            }
            restrictionsAnd.add(HQLRestrictions.and(onActor, onTransaction));
        }
        HQLRestriction[] array = new HQLRestriction[restrictionsAnd.size()];
        HQLQueryBuilder<TransactionLink> builder = new HQLQueryBuilder<TransactionLink>(TransactionLink.class);
        builder.addRestriction(HQLRestrictions.or(restrictionsAnd.toArray(array)));
        builder.addRestriction(tquery.transaction().transactionStatusType().keyword().neqRestriction("DEPRECATED"));
        if (networkCommunicationType != null) {
            HQLRestriction restriction = tquery.transaction().standards().networkCommunicationType()
                    .eqRestriction(networkCommunicationType);
            builder.addRestriction(restriction);
        }
        List<TransactionLink> links = builder.getList();
        if (links != null) {
            return Response.ok(wrapTransactionLink(links, in)).build();
        } else {
            return Response.status(404).header("Warning", "No match found").build();
        }
    }

    public TFConfigurationTypesWrapper wrapTransactionLink(List<TransactionLink> links, boolean in) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("wrapTransactionLink");
        }
        TFConfigurationTypesWrapper wrapper = new TFConfigurationTypesWrapper();
        for (TransactionLink link : links) {
            if (link.getTransaction().getStandards() != null && !link.getTransaction().getStandards().isEmpty()) {
                for (Standard std : link.getTransaction().getStandards()) {
                    if (!std.getNetworkCommunicationType().equals(NetworkCommunicationType.NONE)) {
                        TFConfigurationTypeWrapper single = new TFConfigurationTypeWrapper();
                        if (in) {
                            single.setActor(new ActorWrapper(link.getToActor().getKeyword(), link.getToActor()
                                    .getName()));
                        } else {
                            single.setActor(new ActorWrapper(link.getFromActor().getKeyword(), link.getFromActor()
                                    .getName()));
                        }
                        single.setUsage(link.getTransaction().getKeyword() + " - " + link.getTransaction().getName());
                        single.setConfigurationType(std.getNetworkCommunicationType().getLabel());
                        if (!wrapper.getConfigurations().contains(single)) {
                            wrapper.addConfiguration(single);
                        }
                    }
                }
            }
        }
        return wrapper;
    }
}
