package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.filter.util.MapNotifierListener;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.criterion.*;
import net.ihe.gazelle.hql.paths.HQLSafePathEntityCriterion;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.WSTransactionUsage;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.interfaces.HTTPConfiguration;
import net.ihe.gazelle.tm.configurations.model.interfaces.ServerConfiguration;
import net.ihe.gazelle.tm.filter.BooleanLabelProvider;
import net.ihe.gazelle.tm.filter.PropertyCriterionConfigurationClass;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;

@Name("configurationsOverview")
@Scope(ScopeType.PAGE)
public class ConfigurationsOverview implements Serializable, MapNotifierListener, QueryModifier<SystemInSession> {

    private static final int DROP_DOWN_LIST_MAX_LENGTH = 20;

    private static final String TEST_SESSION = "testSession";

    private static final String SYSTEM = "system";

    private static final String INSTITUTION = "institution";

    private static final String CLASS = "class";

    private static final long serialVersionUID = 1786625699912135119L;

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsOverview.class);
    @In(create = true)
    GenerateConfigurationForANotDroppedSystemInSessionJobInterface generateConfigurationForANotDroppedSystemInSessionJob;
    private FilterDataModel<AbstractConfiguration> datamodel = null;
    private Filter<AbstractConfiguration> filter = null;
    private FilterDataModel<SystemInSession> datamodelSystems = null;
    private Filter<SystemInSession> filterSystems = null;
    private AbstractConfiguration selectedConfiguration;
    private Map<String, String> friendlyNames = new HashMap<String, String>();
    private Map<Integer, Class<?>> configurationTypes = new HashMap<Integer, Class<?>>();
    private Integer hl7V3ResponderConfigurationDiscriminator;
    private Integer webserviceConfigurationDiscriminator;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }

    }

    public FilterDataModel<SystemInSession> getDatamodelSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDatamodelSystems");
        }
        if (datamodelSystems == null) {
            datamodelSystems = new FilterDataModel<SystemInSession>(getFilterSystems()) {
                @Override
                protected Object getId(SystemInSession t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return datamodelSystems;
    }

    public Filter<AbstractConfiguration> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            filter = createFilter(requestParameterMap);
        }
        return filter;
    }

    public Filter<SystemInSession> getFilterSystems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterSystems");
        }
        if (filterSystems == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            SystemInSessionQuery sisQuery = new SystemInSessionQuery();
            HQLCriterionsForFilter<SystemInSession> hqlCriterions = sisQuery.getHQLCriterionsForFilter();
            hqlCriterions.addPath(TEST_SESSION, sisQuery.testingSession(), TestingSession.getSelectedTestingSession(),
                    TestingSession.getSelectedTestingSession());
            hqlCriterions.addPath(INSTITUTION, sisQuery.system().institutionSystems().institution(),
                    InstitutionFixer.INSTANCE);
            hqlCriterions.addPath(SYSTEM, sisQuery);

            hqlCriterions.addQueryModifier(this);

            filterSystems = new Filter<SystemInSession>(hqlCriterions, requestParameterMap);
            filterSystems.getFilterValues().addListener(this);
            Collection<HQLCriterion<SystemInSession, ?>> criterions = filterSystems.getCriterions().values();
            for (HQLCriterion<SystemInSession, ?> hqlCriterion : criterions) {
                hqlCriterion.setCountEnabled(false);
            }
        }
        return filterSystems;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemInSession> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemInSessionQuery query = new SystemInSessionQuery(queryBuilder);
        // Exclude dropped systems
        queryBuilder.addRestriction(HQLRestrictions.or(query.system().systemsInSession().registrationStatus()
                .neqRestriction(SystemInSessionRegistrationStatus.DROPPED), query.system().systemsInSession()
                .registrationStatus().isNullRestriction()));
        queryBuilder.addRestriction(query.system().systemsInSession().testingSession()
                .eqRestriction(TestingSession.getSelectedTestingSession()));
    }

    @Override
    public void modified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modified");
        }
        getFilter().getFilterValues().put(INSTITUTION, filterSystems.getFilterValues().get(INSTITUTION));
        getFilter().getFilterValues().put(SYSTEM, filterSystems.getFilterValues().get(SYSTEM));

    }

    public void clearFilters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearFilters");
        }
        getFilter().clear();
        getFilterSystems().clear();
    }

    public void setSelectedActor(Actor actor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        getFilter().getFilterValues().put("actor", actor);
    }

    private Filter<AbstractConfiguration> createFilter(Map<String, String> requestParameterMap) {
        AbstractConfigurationQuery abstractConfigurationQuery = new AbstractConfigurationQuery();

        HQLCriterionsForFilter<AbstractConfiguration> hqlCriterions = abstractConfigurationQuery
                .getHQLCriterionsForFilter();

        hqlCriterions.addPath(TEST_SESSION, abstractConfigurationQuery.configuration().systemInSession()
                        .testingSession(), TestingSession.getSelectedTestingSession(),
                TestingSession.getSelectedTestingSession());
        hqlCriterions.addPath(INSTITUTION, abstractConfigurationQuery.configuration().systemInSession().system()
                .institutionSystems().institution(), InstitutionFixer.INSTANCE);
        hqlCriterions.addPath(SYSTEM, abstractConfigurationQuery.configuration().systemInSession());
        hqlCriterions.addPath("actor", abstractConfigurationQuery.configuration().actor());

        AbstractCriterion<AbstractConfiguration, Boolean> approvedCriterion = (AbstractCriterion<AbstractConfiguration, Boolean>) hqlCriterions
                .addPath("approved", abstractConfigurationQuery.configuration().isApproved());

        hqlCriterions.addPath("secured", abstractConfigurationQuery.configuration().isSecured());
        approvedCriterion.setLabelProvider(BooleanLabelProvider.INSTANCE);

        List<HQLCriterion<AbstractConfiguration, ?>> criterions = hqlCriterions.getCriterionsList();
        Map<String, List<QueryModifier<AbstractConfiguration>>> queryModifiersForSuggest = hqlCriterions
                .getQueryModifiersForSuggest();

        retrieveConfigurationTypes();

        PropertyCriterion<AbstractConfiguration, Integer> propertyCriterion = new PropertyCriterionConfigurationClass(
                Integer.class, CLASS, CLASS, friendlyNames);
        criterions.add(propertyCriterion);

        HQLSafePathEntityCriterion wsTransactionUsageCriterion = new HQLSafePathEntityCriterion(
                WSTransactionUsage.class, "wsTransactionUsage", "wsTransactionUsage");
        criterions.add(wsTransactionUsageCriterion);

        hqlCriterions.addQueryModifier(new QueryModifier<AbstractConfiguration>() {

            @Override
            public void modifyQuery(HQLQueryBuilder<AbstractConfiguration> queryBuilder,
                                    Map<String, Object> filterValuesApplied) {
                // Exclude dropped systems
                AbstractConfigurationQuery query = new AbstractConfigurationQuery(queryBuilder);

                queryBuilder.addRestriction(HQLRestrictions.or(query.configuration().systemInSession()
                        .registrationStatus().neqRestriction(SystemInSessionRegistrationStatus.DROPPED), query
                        .configuration().systemInSession().registrationStatus().isNullRestriction()));
                queryBuilder.addRestriction(query.configuration().systemInSession().testingSession()
                        .eqRestriction(TestingSession.getSelectedTestingSession()));
            }
        });

        Filter<AbstractConfiguration> result = new Filter<AbstractConfiguration>(hqlCriterions.getEntityClass(),
                hqlCriterions.getQueryModifiers(), criterions, queryModifiersForSuggest, requestParameterMap);
        result.getFilterValues().put(TEST_SESSION, TestingSession.getSelectedTestingSession());
        return result;
    }

    public void retrieveConfigurationTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieveConfigurationTypes");
        }
        EntityManager entityManagerForFactory = EntityManagerService.provideEntityManager();
        SessionFactoryImplementor factory = getFactory(entityManagerForFactory);
        ClassMetadata classMetadata = factory.getClassMetadata(AbstractConfiguration.class);
        UnionSubclassEntityPersister unionSubclassEntityPersister = (UnionSubclassEntityPersister) classMetadata;
        String[] closures = unionSubclassEntityPersister.getSubclassClosure();

        for (String closure : closures) {
            UnionSubclassEntityPersister subEntityPersister = (UnionSubclassEntityPersister) factory
                    .getClassMetadata(closure);
            if (subEntityPersister != null) {
                String discriminatorSQLValue = subEntityPersister.getDiscriminatorSQLValue();

                try {
                    Class<?> targetClass = Class.forName(closure);
                    friendlyNames.put(discriminatorSQLValue, AbstractConfiguration.getTypeLabel(targetClass));

                    int discriminator = Integer.parseInt(discriminatorSQLValue);
                    configurationTypes.put(discriminator, targetClass);

                    if (targetClass.equals(HL7V3ResponderConfiguration.class)) {
                        hl7V3ResponderConfigurationDiscriminator = discriminator;
                    }
                    if (targetClass.equals(WebServiceConfiguration.class)) {
                        webserviceConfigurationDiscriminator = discriminator;
                    }
                } catch (ClassNotFoundException e) {
                    LOG.error("Failed to load class " + closure, e);
                }
            }

        }
    }

    public SessionFactoryImplementor getFactory(EntityManager entityManagerForFactory) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFactory");
        }
        Object delegate = entityManagerForFactory.getDelegate();
        SessionFactoryImplementor factory = null;
        if (delegate instanceof Session) {
            Session session = (Session) delegate;
            SessionFactory sessionFactory = session.getSessionFactory();
            if (sessionFactory instanceof SessionFactoryImplementor) {
                factory = (SessionFactoryImplementor) sessionFactory;
            }
        }
        if (factory == null) {
            throw new IllegalArgumentException();
        }
        return factory;
    }

    public FilterDataModel<AbstractConfiguration> getDatamodel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDatamodel");
        }
        if (datamodel == null) {
            datamodel = new FilterDataModel<AbstractConfiguration>(getFilter()) {
                @Override
                protected Object getId(AbstractConfiguration t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return datamodel;
    }

    public boolean isSystemSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isSystemSelected");
        }
        return getFilter().getFilterValues().get(SYSTEM) != null;
    }

    public AbstractConfiguration getSelectedConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedConfiguration");
        }
        return selectedConfiguration;
    }

    public void setSelectedConfiguration(AbstractConfiguration selectedConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedConfiguration");
        }
        this.selectedConfiguration = selectedConfiguration;
    }

    public boolean isRenderPorts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRenderPorts");
        }
        if (getFilter().getFilterValues().get(CLASS) != null) {
            Integer value = (Integer) getFilter().getFilterValues().get(CLASS);
            Class<?> class1 = configurationTypes.get(value);
            if (AbstractConfiguration.classImplements(class1, ServerConfiguration.class)) {
                return true;
            } else if (AbstractConfiguration.classImplements(class1, HTTPConfiguration.class)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean isShowType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowType");
        }
        Integer value = (Integer) getFilter().getFilterValues().get(CLASS);
        return value == null;
    }

    public boolean isRenderTitle1() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRenderTitle1");
        }
        return (!"".equals(getTitle1()));
    }

    public boolean isRenderTitle2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRenderTitle2");
        }
        return (!"".equals(getTitle2()));
    }

    public boolean isRenderUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRenderUrl");
        }
        return (!"".equals(getUrl()));
    }

    public String getTitle1() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTitle1");
        }
        if (getFilter().getFilterValues().get(CLASS) != null) {
            Integer value = (Integer) getFilter().getFilterValues().get(CLASS);
            return AbstractConfiguration.getHeader1(configurationTypes.get(value));
        } else {
            return "Details 1";
        }
    }

    public String getUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUrl");
        }
        if (getFilter().getFilterValues().get(CLASS) != null) {
            Integer value = (Integer) getFilter().getFilterValues().get(CLASS);
            return AbstractConfiguration.getHeaderUrl(configurationTypes.get(value));
        } else {
            return "Details 2";
        }
    }

    public String getTitle2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTitle2");
        }
        if (getFilter().getFilterValues().get(CLASS) != null) {
            Integer value = (Integer) getFilter().getFilterValues().get(CLASS);
            return AbstractConfiguration.getHeader2(configurationTypes.get(value));
        } else {
            return "Details 3";
        }
    }

    public String getRestURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRestURL");
        }
        Integer value = (Integer) getFilter().getFilterValues().get(CLASS);
        Class<?> configurationClass = null;
        if (value != null) {
            configurationClass = configurationTypes.get(value);
        }
        TestingSession testingSession = (TestingSession) getFilter().getFilterValues().get(TEST_SESSION);
        SystemInSession selectedSystem = (SystemInSession) getFilter().getFilterValues().get(SYSTEM);

        StringBuilder sb = new StringBuilder(ApplicationPreferenceManager.instance().getApplicationUrl());
        sb.append("systemConfigurations.seam?");
        boolean first = true;
        if (testingSession != null) {
            first = false;
            sb.append("testingSessionId=");
            sb.append(testingSession.getId());
        }
        if (configurationClass != null) {
            if (!first) {
                sb.append("&");
            }
            first = false;
            sb.append("configurationType=");
            sb.append(configurationClass.getSimpleName());
        }
        if (selectedSystem != null) {
            if (!first) {
                sb.append("&");
            }
            first = false;
            sb.append("systemKeyword=");
            sb.append(selectedSystem.getSystem().getKeyword());
        }
        return sb.toString();
    }

    public boolean isShowActorToConfigsLinks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowActorToConfigsLinks");
        }
        return Authorizations.EDITOR.isGranted();
    }

    public boolean isRightToEdit(Institution selectedInstitution, SystemInSession selectedSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isRightToEdit");
        }

        if (Authorizations.ADMIN.isGranted()
                || Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()) {
            return true;
        }
        if (Authorizations.VENDOR.isGranted() || Authorizations.VENDOR_ADMIN.isGranted()) {
            if ((selectedSystem != null) && (selectedSystem.getSystem() != null)) {
                Set<InstitutionSystem> institutionSystems = selectedSystem.getSystem().getInstitutionSystems();
                for (InstitutionSystem institutionSystem : institutionSystems) {
                    User loggedInUser = User.loggedInUser();
                    Institution institution = institutionSystem.getInstitution();
                    if ((loggedInUser.getInstitution() != null)
                            && loggedInUser.getInstitution().getId().equals(institution.getId())) {
                        return true;
                    }
                }
            } else if (selectedInstitution != null) {
                User loggedInUser = User.loggedInUser();
                if ((loggedInUser.getInstitution() != null)
                        && loggedInUser.getInstitution().getId().equals(selectedInstitution.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEditRights(AbstractConfiguration abstractConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditRights");
        }
        abstractConfiguration = HQLReloader.reloadDetached(abstractConfiguration);

        SystemInSession systemInSession = null;
        if (abstractConfiguration != null) {
            Configuration configuration = abstractConfiguration.getConfiguration();
            if (configuration != null) {
                systemInSession = configuration.getSystemInSession();
            }
        }
        return isRightToEdit(null, systemInSession);
    }

    public boolean isEditRights() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEditRights");
        }
        Institution institution = (Institution) getFilter().getFilterValues().get(INSTITUTION);
        SystemInSession selectedSystem = (SystemInSession) getFilter().getFilterValues().get(SYSTEM);
        return isRightToEdit(institution, selectedSystem);
    }

    private void changeAllState(boolean isApproved) {
        if (isEditRights()) {
            List<AbstractConfiguration> configs = (List<AbstractConfiguration>) getDatamodel().getAllItems(
                    FacesContext.getCurrentInstance());
            for (AbstractConfiguration abstractConfiguration : configs) {
                changeConfigurationApproved(abstractConfiguration, isApproved);
            }
        }

        getFilter().modified();

    }

    public void approveAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("approveAll");
        }
        changeAllState(true);
    }

    public void disapproveAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("disapproveAll");
        }
        changeAllState(false);
    }

    public void approveConfiguration(AbstractConfiguration abstractConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("approveConfiguration");
        }
        changeConfigurationApproved(abstractConfiguration, true);
        getFilter().modified();
    }

    public void unapproveConfiguration(AbstractConfiguration abstractConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("unapproveConfiguration");
        }
        changeConfigurationApproved(abstractConfiguration, false);
        getFilter().modified();
    }

    public void changeConfigurationApproved(AbstractConfiguration abstractConfiguration, boolean isApproved) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeConfigurationApproved");
        }
        Configuration configuration = abstractConfiguration.getConfiguration();
        if ((configuration != null) && (configuration.getIsApproved() != isApproved)) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            configuration.setIsApproved(isApproved);
            entityManager.merge(configuration);
        }

    }

    public String editConfig(AbstractConfiguration inConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editConfig");
        }
        SystemConfigurationManagerLocal systemConfigurationManager = (SystemConfigurationManagerLocal) Component
                .getInstance("systemConfigurationManager");

        inConfiguration = EntityManagerService.provideEntityManager().find(inConfiguration.getClass(),
                inConfiguration.getId());

        inConfiguration.getConfiguration().getHost().getIp();
        inConfiguration.getConfiguration().getSystemInSession().getSystem().getInstitutionSystems().size();
        inConfiguration.getConfiguration().getActor().getKeyword();

        String params = getFilter().getUrlParameters();

        systemConfigurationManager.setCurrentConfiguration(inConfiguration);
        systemConfigurationManager.setSelectedSystemInSession(inConfiguration.getConfiguration()
                .getSystemInSession());
        systemConfigurationManager.setPreviousSystemInSession(inConfiguration.getConfiguration()
                .getSystemInSession());
        return "/configuration/create/addConfiguration.xhtml" + "?" + params;
    }

    private AbstractConfiguration copyAbstractConfiguration(AbstractConfiguration ac) {
        if (ac != null) {
            if (ac instanceof DicomSCUConfiguration) {
                return new DicomSCUConfiguration((DicomSCUConfiguration) ac);
            }
            if (ac instanceof DicomSCPConfiguration) {
                return new DicomSCPConfiguration((DicomSCPConfiguration) ac);
            }
            if (ac instanceof HL7V2InitiatorConfiguration) {
                return new HL7V2InitiatorConfiguration((HL7V2InitiatorConfiguration) ac);
            }
            if (ac instanceof HL7V3InitiatorConfiguration) {
                return new HL7V3InitiatorConfiguration((HL7V3InitiatorConfiguration) ac);
            }
            if (ac instanceof HL7V2ResponderConfiguration) {
                return new HL7V2ResponderConfiguration((HL7V2ResponderConfiguration) ac);
            }
            if (ac instanceof HL7V3ResponderConfiguration) {
                return new HL7V3ResponderConfiguration((HL7V3ResponderConfiguration) ac);
            }
            if (ac instanceof WebServiceConfiguration) {
                return new WebServiceConfiguration((WebServiceConfiguration) ac);
            }
            if (ac instanceof SyslogConfiguration) {
                return new SyslogConfiguration((SyslogConfiguration) ac);
            }
            if (ac instanceof RawConfiguration) {
                return new RawConfiguration((RawConfiguration) ac);
            }
        }
        return null;
    }

    public void copyConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyConfiguration");
        }
        if (this.selectedConfiguration != null) {
            AbstractConfiguration ac = this.copyAbstractConfiguration(selectedConfiguration);
            EntityManager em = EntityManagerService.provideEntityManager();
            Configuration conf = em.merge(ac.getConfiguration());
            em.flush();
            ac.setConfiguration(conf);
            ac.setId(conf.getId());
            em.merge(ac);
            em.flush();

            getFilter().modified();

            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Configuration for " +
                    selectedConfiguration.getConfiguration().getSystemInSession().getSystem().getKeyword() + " has been duplicated.");
        }
    }

    public void deleteConfigurations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteConfigurations");
        }
        if (isEditRights()) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();

            List<AbstractConfiguration> configs = (List<AbstractConfiguration>) getDatamodel().getAllItems(
                    FacesContext.getCurrentInstance());
            for (AbstractConfiguration abstractConfiguration : configs) {
                entityManager.remove(abstractConfiguration);
            }
            entityManager.flush();
        }

        getFilter().modified();
    }

    public void deleteConfiguration() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteConfiguration");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if ((selectedConfiguration == null) || (selectedConfiguration.getId() == null)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem, selected configuration is null ");
            return;
        }
        selectedConfiguration = HQLReloader.reloadDetached(selectedConfiguration);
        entityManager.remove(selectedConfiguration);
        entityManager.flush();

        Configuration c = entityManager.find(Configuration.class, selectedConfiguration.getConfiguration().getId());
        if (c != null) {
            entityManager.remove(c);
            entityManager.flush();
        }
        getFilter().modified();

        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Configuration for " +
                selectedConfiguration.getConfiguration().getSystemInSession().getSystem().getKeyword() + " has been removed.");

    }

    public boolean canAddConfig() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canAddConfig");
        }
        SystemInSession selectedSystem = (SystemInSession) getFilter().getFilterValues().get(SYSTEM);

        if (selectedSystem == null) {
            return false;
        }

        if (Role.isLoggedUserAdmin()) {
            return true;
        }

        Institution userInstitution = User.loggedInUser().getInstitution();

        Set<InstitutionSystem> institutionSystems = selectedSystem.getSystem().getInstitutionSystems();
        for (InstitutionSystem institutionSystem : institutionSystems) {
            if (institutionSystem.getInstitution().getId().equals(userInstitution.getId())) {
                return true;
            }
        }

        return false;
    }

    public String addConfig() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addConfig");
        }
        SystemInSession selectedSystem = (SystemInSession) getFilter().getFilterValues().get(SYSTEM);
        if (!Role.isLoggedUserAdmin() && (selectedSystem != null)) {
            Institution userInstitution = User.loggedInUser().getInstitution();

            boolean reset = true;
            Set<InstitutionSystem> institutionSystems = selectedSystem.getSystem().getInstitutionSystems();
            for (InstitutionSystem institutionSystem : institutionSystems) {
                if (institutionSystem.getInstitution().getId().equals(userInstitution.getId())) {
                    reset = false;
                }
            }
            if (reset) {
                selectedSystem = null;
            }
        }

        SystemConfigurationManagerLocal systemConfigurationManager = (SystemConfigurationManagerLocal) Component
                .getInstance("systemConfigurationManager");

        // We choose an arbitrary configuration, will be changed later when the user will change the configuration type
        AbstractConfiguration currentConfiguration = new HL7V2InitiatorConfiguration();
        currentConfiguration.getConfiguration().setConfigurationType(null);
        currentConfiguration.getConfiguration().setSystemInSession(selectedSystem);

        systemConfigurationManager.setCurrentConfiguration(currentConfiguration);
        systemConfigurationManager.setSelectedSystemInSession(selectedSystem);
        systemConfigurationManager.setPreviousSystemInSession(selectedSystem);

        return "/configuration/create/addConfiguration.xhtml";

    }

    public Institution getChoosenInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getChoosenInstitution");
        }
        return (Institution) getFilter().getFilterValues().get(INSTITUTION);
    }

    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return (SystemInSession) getFilter().getFilterValues().get(SYSTEM);
    }

    public void setSelectedSystemInSession(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        getFilter().getFilterValues().put(SYSTEM, systemInSession);
        getFilterSystems().getFilterValues().put(SYSTEM, systemInSession);
        getFilter().getFilterValues().put(INSTITUTION, null);
        getFilterSystems().getFilterValues().put(INSTITUTION, null);
        getFilter().modified();
        getFilterSystems().modified();
    }

    public void generateConfigurationsForSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurationsForSession");
        }
        generateConfigurations(null, null);
    }

    public void generateConfigurationsForForSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurationsForForSystem");
        }
        SystemInSession selectedSystemInSession = getSelectedSystemInSession();
        if (selectedSystemInSession != null) {
            generateConfigurations(null, selectedSystemInSession);
        }
    }

    public void generateConfigurationsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurationsForCompany");
        }
        Institution choosenInstitution = getChoosenInstitution();
        if (choosenInstitution != null) {
            generateConfigurations(choosenInstitution, null);
        }
    }

    private void generateConfigurations(Institution choosenInstitution, SystemInSession selectedSystemInSession) {
        TestingSession testingSession = (TestingSession) getFilter().getFilterValues().get(TEST_SESSION);
        generateConfigurations(choosenInstitution, selectedSystemInSession, testingSession);
        getFilter().modified();
    }

    public void generateConfigurations(Institution choosenInstitution, SystemInSession selectedSystemInSession,
                                       TestingSession testingSession) {
        if (testingSession != null) {
            if (selectedSystemInSession != null) {
                generateConfigurationForANotDroppedSystemInSessionJob
                        .generateConfigurationForANotDroppedSystemInSessionJob(selectedSystemInSession, testingSession);
            } else {
                SystemInSessionQuery systemInSessionQuery = new SystemInSessionQuery();
                systemInSessionQuery.testingSession().eq(testingSession);
                if (choosenInstitution != null) {
                    systemInSessionQuery.system().institutionSystems().institution().eq(choosenInstitution);
                }
                List<SystemInSession> systems = systemInSessionQuery.getList();

                for (SystemInSession systemInSession : systems) {
                    generateConfigurationForANotDroppedSystemInSessionJob
                            .generateConfigurationForANotDroppedSystemInSessionJob(systemInSession, testingSession);
                }
            }
        } else {
            LOG.error("Testing Session is null");
        }
        getFilter().modified();
    }

    /**
     * This method truncates a string if this string is too long (used in drop down list (criteria) during search)
     *
     * @param inString not truncated
     * @return truncated string if the given string is too long, else it returns the given string
     */
    public String truncateToTwenty(String inString) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("truncateToTwenty");
        }
        if (inString.length() > DROP_DOWN_LIST_MAX_LENGTH) {
            inString = inString.substring(0, DROP_DOWN_LIST_MAX_LENGTH);
        }
        return inString;
    }

    public String getConfigurationComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConfigurationComment");
        }
        selectedConfiguration = HQLReloader.reloadDetached(selectedConfiguration);
        if (selectedConfiguration != null) {
            String comment = selectedConfiguration.getConfiguration().getComment();
            if (comment != null && !comment.isEmpty()) {
                return comment;
            }
        }
        return null;
    }
}
