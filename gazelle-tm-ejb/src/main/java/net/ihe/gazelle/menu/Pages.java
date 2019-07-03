package net.ihe.gazelle.menu;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.pages.Authorization;
import net.ihe.gazelle.common.pages.AuthorizationOr;
import net.ihe.gazelle.common.pages.Page;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Pages implements
        Page {

    // menu elements

    HOME("/home.xhtml", null, "Defined at runtime", Authorizations.ALL),

    VERSION("/version.xhtml", Icons.GAZELLE, "Gazelle version", Authorizations.ALL),

    SEARCH("/search/search.xhtml", Icons.SEARCH, "gazelle.common.button.Search", Authorizations.LOGGED, Authorizations.ALL),

    CHANGE_TESTING_SESSION("/systems/session/changeSession.xhtml", Icons.CALENDAR, "gazelle.common.menu.ChangeTestingSession", Authorizations
            .LOGGED, Authorizations.TM),

    USER_PREFERENCES("/users/user/userPreferences.xhtml", Icons.USER_PREFERENCES, "gazelle.users.common.MyAccount", Authorizations.LOGGED),

    REGISTER("/users/user/register.xhtml", Icons.REGISTER_ACCOUNT, "gazelle.common.menu.RegisterAccount", Authorizations.NOT_ONLY_GMM,
            Authorizations.NOT_LOGGED),

    REGISTRATION("", Icons.REGISTRATION, "gazelle.common.menu.Registration", Authorizations
            .VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.NOT_ONLY_GMM),

    REGISTRATION_COMPANY("/users/institution/editInstitution.xhtml", Icons.COMPANY, "gazelle.common.menu.MenuViewAndEditInformations",
            Authorizations.ADMIN_OR_VENDOR_ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.NOT_ONLY_GMM),

    REGISTRATION_SYSTEMS("/systems/system/listSystemsInSession.xhtml", Icons.SYSTEMS, "gazelle.common.menu.MenuManageSystems", Authorizations.LOGGED,
            Authorizations.NOT_ONLY_GMM),

    REGISTRATION_OVERVIEW("/reports/registrationOverview.xhtml", Icons.REGISTRATION, "gazelle.common.menu.MenuRegistrationStatusOverview",
            Authorizations.VENDOR, Authorizations.TM),

    REGISTRATION_USERS("/users/user/listUsersInstitution.xhtml", Icons.USERS, "gazelle.common.menu.MenuManageUsers", Authorizations
            .ADMIN_OR_VENDOR_ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.NOT_ONLY_GMM),

    REGISTRATION_CONTACTS("/users/contact/listContacts.xhtml", Icons.CONTACTS, "gazelle.common.menu.MenuManageContacts", Authorizations
            .ADMIN_OR_VENDOR_ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    REGISTRATION_PARTICIPANTS("/users/connectathon/listParticipants.xhtml", Icons.PARTICIPANTS, "gazelle.common.menu.ConnectathonParticipants",
            Authorizations.VENDOR_ADMIN, Authorizations.TM, Authorizations.SESSION_WITH_PARTICIPANTS),

    REGISTRATION_FINANCIAL("/financial/financialSummary.xhtml", Icons.FINANCIAL, "gazelle.common.menu.MenuFinancialSummary", Authorizations
            .VENDOR_ADMIN, Authorizations.TM, Authorizations.SESSION_WITH_CONTRACT),

    REGISTRATION_DEMONSTRATIONS("/demonstrations/listCurrentDemonstrations.xhtml", Icons.ADMINISTRATION, "gazelle.common.menu.DemonstrationsList",
            Authorizations.LOGGED, Authorizations.TM),

    PR_INTRO("/pr/introPR.xhtml", Icons.INTRO, "net.ihe.gazelle.tm.Introduction", Authorizations.PR),

    PR_SEARCH("/pr/search.xhtml", Icons.SEARCH, "gazelle.productregistry.search.Search", Authorizations.PR),

    PR_STATS("/pr/logs.xhtml", Icons.STATISTICS, "net.ihe.gazelle.tm.ConsultSearchStatistics", Authorizations.LOGGED, Authorizations.PR),

    TF("/tf/overview/overview.xhtml", null, "gazelle.tf.menu.TF", Authorizations.ALL),

    TF_OVERVIEW("/tf/overview/overview.xhtml", Icons.TF_OVERVIEW, "gazelle.tf.menu.overview", Authorizations.ALL),

    TF_DOMAINS("/tf/domain/listDomains.xhtml", Icons.EDITOR, "gazelle.tf.menu.DomainBrowsing", Authorizations.ALL),

    TF_PROFILES("/tf/integrationProfile/listIntegrationProfiles.xhtml", Icons.EDITOR, "gazelle.tf.menu.IntegrationProfilesBrowsing", Authorizations
            .ALL),

    TF_ACTORS("/tf/actor/listActors.xhtml", Icons.EDITOR, "gazelle.tf.menu.ActorBrowsing", Authorizations.ALL),

    TF_AUDIT_MESSAGES("/tf/auditMessage/index.xhtml", Icons.EDITOR, "gazelle.tf.menu.AuditMessage", Authorizations.ALL),

    TF_AUDIT_MESSAGES_NEW("/tf/auditMessage/new.xhtml", null, null, Authorizations.ALL),

    TF_AUDIT_MESSAGES_EDIT("/tf/auditMessage/edit.xhtml", null, null, Authorizations.ALL),

    TF_AUDIT_MESSAGES_SHOW("/tf/auditMessage/show.xhtml", null, null, Authorizations.ALL),

    TF_AUDIT_MESSAGES_ADD_DOC_SECTION("/tf/auditMessage/addDocumentSection.xhtml", null, null, Authorizations.ALL),

    TF_STANDARDS("/tf/standard/index.xhtml", Icons.EDITOR, "gazelle.tf.menu.Standards", Authorizations.ALL),

    TF_STANDARD_EDIT("/tf/standard/edit.xhtml", null, null, Authorizations.EDITOR),

    TF_STANDARD_SHOW("/tf/standard/show.xhtml", null, null, Authorizations.ALL),

    TF_OPTIONS("/tf/integrationProfileOption/listIntegrationProfileOptions.xhtml", Icons.EDITOR, "gazelle.tf.menu" +
            ".IntegrationProfileOptionBrowsing", Authorizations.ALL),

    TF_DOCUMENTS("/tf/documents/document.xhtml", Icons.DOCUMENT, "gazelle.common.menu.Documents", Authorizations.ALL),

    TF_SHOW_DOCUMENTS("/tf/documents/showDocument.xhtml", null, null, Authorizations.ALL),

    TF_TRANSACTIONS("/tf/transaction/listTransactions.xhtml", Icons.EDITOR, "gazelle.tf.menu.TransactionsBrowsing", Authorizations.ALL),

    TF_TRANSACTIONS_ADD_STANDARD("/tf/transaction/addStandard.xhtml", null, null, Authorizations.EDITOR),

    TF_TRANSACTIONS_OPTION_TYPES("/tf/transactionOptionType/listTransactionOptionTypes.xhtml", Icons.EDITOR, "gazelle.tf.menu" +
            ".TransactionsOptionTypeBrowsing", Authorizations.ALL),

    TF_HL7V2_MESSAGE_PROFILES("/tf/messageProfile/listMessageProfiles.xhtml", Icons.EDITOR, "gazelle.tf.menu.MessageProfileBrowsing",
            Authorizations.ALL),

    TF_HL7V2_MESSAGE_PROFILE("/tf/messageProfile/messageProfile.xhtml", null, null, Authorizations.LOGGED, Authorizations.ALL),

    TF_WEBSERVICE_TRANSACTION_USAGE("/tf/wsTransactionUsage/listWSTransactionUsages.xhtml", Icons.EDITOR, "gazelle.tm.WebserviceTransactionUsage",
            Authorizations.EDITOR),

    TF_CONF_DEPENDING_AIPO("/configuration/list/listConfTypedependingAllAIPO.xhtml", Icons.CONFIGURATIONS, "gazelle.tf.configuration" +
            ".SystemConfigurationForTF", Authorizations.EDITOR),

    TF_RULES("/tf/rules/index.xhtml", Icons.EDITOR, "net.ihe.gazelle.tm.EditTFRules", Authorizations.EDITOR),

    TF_SHOW_RULE("/tf/rules/show.xhtml", null, null, Authorizations.LOGGED),

    TD("/testing/testsDefinition/testsList.xhtml", null, "gazelle.tm.TestsList", Authorizations.DISPLAY_TESTS, Authorizations.NOT_PR),

    TD_LIST_TESTS("/testing/testsDefinition/testsList.xhtml", Icons.TD, "gazelle.common.menu.TestDefinitionsManagement", Authorizations.DISPLAY_TESTS,
            Authorizations.NOT_PR),

    TD_LIST_TESTS_ITB("/testingITBtestsDefinition/testsList.xhtml", Icons.TD, "gazelle.common.menu.TestDefinitionsManagement", Authorizations
            .DISPLAY_TESTS, Authorizations.NOT_PR),

    TD_LIST_METATESTS("/testing/testsDefinition/metaTestList.xhtml", Icons.TD, "gazelle.common.menu.metaTestList", Authorizations.DISPLAY_TESTS,
            Authorizations.NOT_PR),

    TD_ROLE_IN_TEST("/testing/roleInTest/listRoleInTest.xhtml", Icons.TD, "gazelle.common.menu.RoleInTestManagement", Authorizations.EDITOR,
            Authorizations.NOT_PR),

    TD_TEST_REQUIREMENTS("/testing/testsDefinition/listAllTestsRequirements.xhtml", Icons.TD, "gazelle.common.menu.MenuManageTestsRequirements",
            Authorizations.LOGGED, Authorizations.NOT_PR),

    TD_PATHS("/testing/testsDefinition/listPaths.xhtml", Icons.TD, "gazelle.common.menu.ListPath", Authorizations.LOGGED, Authorizations.NOT_PR),

    CONFIG("/configuration/configurations.xhtml", null, "gazelle.configuration.Configurations", Authorizations.LOGGED, Authorizations.TM),

    CONFIG_ALL("/configuration/configurations.xhtml", Icons.CONFIGURATIONS, "gazelle.configuration.SystemConfigurationAll", Authorizations.LOGGED,
            Authorizations.TM),

    CONFIG_NETWORK_OVERVIEW("/configuration/parameter/configureIPsAndOIDSForSession.xhtml", Icons.NETWORK_SERVER, "gazelle.common.menu" +
            ".NetworkConfigurationOverview", Authorizations.LOGGED, Authorizations.TM),

    CONFIG_LIST_VENDOR("/configuration/list/configurationMainWindow.xhtml", Icons.CONFIGURATIONS, "gazelle.configuration.SystemConfiguration",
            Authorizations.VENDOR, Authorizations.TM),

    CONFIG_OIDS("/configuration/parameter/listOfOIDsForSession.xhtml", Icons.CONFIGURATIONS, "gazelle.common.menu.OIDs", Authorizations.LOGGED,
            Authorizations.TM),

    CONFIG_CERTIFICATES("/pki.xhtml", Icons.CERTIFICATES, "gazelle.common.menu.Certificates", Authorizations.LOGGED, Authorizations.TM,
            Authorizations.SESSION_WITH_CERTIFICATE_URL),

    SYSTEM_LIST_ALL("/systems/listSystems.xhtml", Icons.SYSTEMS, "gazelle.common.menu.FindSystems", Authorizations.LOGGED, Authorizations
            .NOT_ONLY_GMM),

    CAT("/testing/test/cat.xhtml", null, "gazelle.common.menu.Testing", Authorizations.LOGGED, Authorizations.TM),

    CAT_SYSTEMS("/systems/listSystems.xhtml", Icons.SYSTEMS, "gazelle.common.menu.FindSystems", Authorizations.LOGGED, Authorizations.NOT_ONLY_GMM),

    CAT_PRECAT("/testing/test/mesa/listAllSystemsForMesaTesting.xhtml", Icons.CAT_PRE, "gazelle.common.menu.PreConnectathonTesting", Authorizations
            .LOGGED, Authorizations.TM, Authorizations.SESSION_WITH_PRECAT),


    CAT_PRECAT_MENU("", Icons.CAT_PRE, "gazelle.common.menu.PreCatMenu", Authorizations.LOGGED, Authorizations.TM, Authorizations
            .SESSION_WITH_PRECAT),

    CAT_PRECAT_VALIDATION("/testing/test/mesa/validateMesaTest.xhtml", Icons.CAT_PRE, "gazelle.common.menu.PreConnectathonResults", Authorizations
            .ADMIN_OR_MONITOR_OR_VENDOR, Authorizations.TM, Authorizations.SESSION_WITH_PRECAT),

    CAT_PRECAT_OVERVIEW_MENU("", Icons.CAT_PRE, "gazelle.common.menu.PreConnectathonResults", Authorizations.VENDOR, Authorizations.TM),

    CAT_MENU("", Icons.CAT_RUNNING, "gazelle.common.menu.CatMenu", Authorizations.LOGGED, Authorizations.TM),

    CAT_MONITOR_WORKLIST("/testing/research/testInstancesMonitor.xhtml", Icons.MONITOR, "gazelle.common.menu.MonitorWorkList", Authorizations
            .MONITOR, Authorizations.TM),

    CAT_INSTANCES("/testing/research/testInstances.xhtml", Icons.CAT_RUNNING, "gazelle.common.menu.TestsPerSystem", Authorizations.LOGGED,
            Authorizations.ADMIN_OR_MONITOR, Authorizations.TM),

    CAT_OVERVIEW("/testing/test/cat.xhtml", Icons.CAT_RUNNING, "gazelle.common.menu.ConnectathonTestingPage", Authorizations.LOGGED, Authorizations
            .TM),

    CAT_OVERVIEW_MENU("", Icons.CAT_RUNNING, "gazelle.common.menu.ConnectathonTestingPage", Authorizations.LOGGED, Authorizations.TM),

    CAT_RESULTS("/testing/results/connectathonResults.xhtml", Icons.CAT_RESULTS, "gazelle.common.menu.ConnectathonResults", Authorizations.LOGGED,
            Authorizations.ADMIN_OR_VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    CAT_REPORT("/testing/reporting/reporting4TestingSession.xhtml", Icons.CAT_RESULTS, "net.ihe.gazelle.tm.DownloadConnectathonReport",
            Authorizations.ADMIN_OR_VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    CAT_REPORT_VENDOR("/testing/reporting/reporting4TestingSession.xhtml", Icons.CAT_RESULTS, "net.ihe.gazelle.tm.DownloadConnectathonReport",
            Authorizations.VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    CAT_MONITORS("/testing/research/researchMonitor.xhtml", Icons.SEARCH_MONITOR, "gazelle.common.menu.MonitorResearch", Authorizations.LOGGED,
            Authorizations.TM),

    CAT_SAMPLES("/objects/system_object.xhtml", Icons.SAMPLE, "gazelle.common.menu.ConnectathonObjectInstance", Authorizations.LOGGED,
            Authorizations.TM),

    CAT_SAMPLES_SEARCH("/objects/searchSample.xhtml", Icons.SAMPLE, "gazelle.common.menu.SearchSample", Authorizations.LOGGED, Authorizations.TM),

    ADMIN("", Icons.ADMINISTRATION, "gazelle.common.menu.MenuAdministration", new AuthorizationOr(
            Authorizations.ADMIN_OR_ACCOUNTING, Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.EDITOR)),

    ADMIN_PR_VALIDATE("/pr/systemsForAdmin.xhtml", Icons.CHECK, "net.ihe.gazelle.tm.ValidateUnreferencedIntegrationStatements", Authorizations
            .ADMIN, Authorizations.PR),

    ADMIN_PR_CRAWLER("/pr/listAllCrawlerLogs.xhtml", Icons.CRAWLER, "net.ihe.gazelle.tm.ConsultCrawlerLogs", Authorizations.ADMIN, Authorizations.PR),

    ADMIN_MANAGE_CONFIGS("", Icons.CONFIGURATIONS, "gazelle.configuration.Configurations", Authorizations.ADMIN, Authorizations.NOT_PR),

    ADMIN_MANAGE_CONFIGS_HOSTS("/configuration/NETWORK/list/listAllHostsForAdmin.xhtml", Icons.NETWORK_SERVER, "gazelle.common.menu" +
            ".MenuManageAllHosts", Authorizations.ADMIN, Authorizations.TM),

    ADMIN_MANAGE_CONFIGS_HOSTS_WITHOUT_CONF("/configuration/NETWORK/list/listAllHostsWithoutConfsForAdmin.xhtml", null, null, Authorizations.ADMIN,
            Authorizations.TM),

    ADMIN_MANAGE_CONFIGS_OIDS("/configuration/oid/oidManagement.xhtml", Icons.CONFIGURATIONS, "gazelle.common.menu.MenuOIDManagement",
            Authorizations.ADMIN, Authorizations.NOT_PR),

    ADMIN_MANAGE_OIDS_LABELS("/configuration/oid/oidManagementLabels.xhtml", Icons.CONFIGURATIONS, "OIDs root label definition",
            Authorizations.ADMIN, Authorizations.NOT_PR),

    ADMIN_MANAGE("", Icons.EDITOR, "net.ihe.gazelle.tm.Manage", new AuthorizationOr(Authorizations.ADMIN_OR_ACCOUNTING,
            Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.EDITOR)),

    ADMIN_MANAGE_SESSIONS("/administration/listSessions.xhtml", Icons.CALENDAR, "gazelle.common.menu.MenuManageSessions", Authorizations
            .ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    ADMIN_MANAGE_DEMONSTRATIONS("/administration/listAllDemonstrations.xhtml", Icons.ADMINISTRATION, "gazelle.common.menu" +
            ".MenuManageDemonstrations", Authorizations.ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    ADMIN_MANAGE_COMPANIES("/administration/listInstitutions.xhtml", Icons.COMPANY, "gazelle.common.menu.MenuManageAllInstitutions", new
            AuthorizationOr(
            Authorizations.ADMIN, Authorizations.ACCOUNTING,
            Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION)),

    ADMIN_MANAGE_SYSTEMS("/administration/listAllSystemsPage.xhtml", Icons.SYSTEMS, "gazelle.tm.monitors.Systems", Authorizations.ADMIN,
            Authorizations.NOT_ONLY_GMM),

    ADMIN_MANAGE_SYSTEMS_LIST("/administration/listAllSystemsPage.xhtml", Icons.SYSTEMS, "gazelle.common.menu.FindSystems", Authorizations.ADMIN,
            Authorizations.NOT_ONLY_GMM),

    ADMIN_MANAGE_SYSTEMS_SIMULATORS("/administration/listSimulators.xhtml", Icons.SYSTEMS, "gazelle.common.menu.MenuManageAllSimulators",
            Authorizations.ADMIN, Authorizations.TM),

    ADMIN_MANAGE_SYSTEMS_ACCEPTATION("/administration/sessionOverview/acceptedSystems.xhtml", Icons.SYSTEMS, "gazelle.testmanagement.session" +
            ".AcceptedSystems", Authorizations.ADMIN, Authorizations.TM),

    ADMIN_MANAGE_SYSTEMS_TEST_TYPES("/administration/sessionOverview/supportiveAIPOs.xhtml", Icons.SYSTEMS, "gazelle.testmanagement.session" +
            ".SupportiveAIPOs", Authorizations.ADMIN, Authorizations.TM),

    ADMIN_REGISTRATION_OVERVIEW("/reports/registrationOverview.xhtml", Icons.REGISTRATION, "gazelle.common.menu.MenuRegistrationStatusOverview",
            Authorizations.ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    ADMIN_MANAGE_USERS("/users/user/listUsersInstitution.xhtml", Icons.USERS, "gazelle.common.menu.MenuManageAllUsers", Authorizations.ADMIN),

    ADMIN_MANAGE_MONITORS("/administration/listMonitors.xhtml", Icons.MONITOR, "gazelle.tm.monitors.Administration", Authorizations
            .ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    MONITORS_ADDMONITORTOTEST("/monitors/addMonitorToTest.xhtml", Icons.MONITOR, "gazelle.tm.AssignMonitorsToTests", Authorizations
            .ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION, Authorizations.TM),

    ADMIN_MANAGE_CONTACTS("/users/contact/listContacts.xhtml", Icons.CONTACTS, "gazelle.common.menu.MenuManageAllContacts", Authorizations.ADMIN,
            Authorizations.TM),

    ADMIN_MANAGE_PARTICIPANTS("/users/connectathon/listParticipants.xhtml", Icons.PARTICIPANTS, "gazelle.common.menu.ConnectathonParticipants",
            Authorizations.ADMIN, Authorizations.TM, Authorizations.SESSION_WITH_PARTICIPANTS),

    ADMIN_MANAGE_INVOICES("/administration/listInvoices.xhtml", Icons.FINANCIAL, "gazelle.common.menu.MenuManageAllInvoices", Authorizations
            .TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION_OR_ACCOUNTING, Authorizations.TM, Authorizations.SESSION_WITH_CONTRACT),

    ADMIN_MANAGE_FINANCIAL_SUMMARY("/financial/financialSummary.xhtml", Icons.FINANCIAL_SUMMARY, "gazelle.common.menu.MenuFinancialSummary", new
            AuthorizationOr(
            Authorizations.ADMIN, Authorizations.ACCOUNTING,
            Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION), Authorizations.TM, Authorizations.SESSION_WITH_CONTRACT),

    ADMIN_CHECK("", Icons.CHECK, "net.ihe.gazelle.tm.Check", Authorizations.ADMIN),

    ADMIN_CHECK_TF("/tf/utilities/tfConsistencyCheckList.xhtml", Icons.CHECK, "gazelle.tf.tfConsistencyCheck.tfConsistencyCheckList",
            Authorizations.ADMIN),

    ADMIN_CHECK_TD("/testing/utilities/tmCheckList.xhtml", Icons.CHECK, "gazelle.tm.TestsDefinitionCheckList", Authorizations.ADMIN, Authorizations
            .NOT_PR),

    ADMIN_CHECK_REGISTRATION("/reports/registrationOverview.xhtml", Icons.CHECK, "gazelle.common.menu.MenuRegistrationStatusOverview",
            Authorizations.ADMIN, Authorizations.TM),

    ADMIN_CHECK_SESSION("/administration/listSessionOverview.xhtml", Icons.CHECK, "gazelle.common.menu.SessionOverview", Authorizations.ADMIN,
            Authorizations.TM),

    ADMIN_CHECK_CONF("/administration/checkConfigurations.xhtml", Icons.CHECK, "Configurations checking", Authorizations.ADMIN,
            Authorizations.TM),

    ADMIN_USAGE("/usage.xhtml", Icons.STATISTICS, "gazelle.common.menu.Usage", Authorizations.ADMIN),

    ADMIN_KPI("", Icons.STATISTICS, "gazelle.tm.KPI", Authorizations.ADMIN, Authorizations.TM),

    ADMIN_KPI_TESTSINSTANCES("/testing/kpi/kpiTestsInstances.xhtml", Icons.STATISTICS, "gazelle.common.menu.KPITestsInstances", Authorizations
            .ADMIN, Authorizations.TM),

    ADMIN_KPI_TESTS("/testing/kpi/kpiTests.xhtml", Icons.STATISTICS, "gazelle.common.menu.KPITests", Authorizations.ADMIN, Authorizations.TM),

    ADMIN_KPI_SYSTEMS("/testing/kpi/kpiSystems.xhtml", Icons.STATISTICS, "gazelle.common.menu.KPISystems", Authorizations.ADMIN, Authorizations.TM),

    ADMIN_KPI_MONITORS("/testing/kpi/kpiMonitors.xhtml", Icons.STATISTICS, "gazelle.common.menu.KPIMonitors", Authorizations.ADMIN, Authorizations
            .TM),

    ADMIN_SAMPLES("", Icons.SAMPLE, "net.ihe.gazelle.tm.Samples_2", new AuthorizationOr(Authorizations.EDITOR,
            Authorizations.ADMIN), Authorizations.NOT_PR),

    ADMIN_SAMPLES_OBJECT_TYPE("/objects/listObjectType.xhtml", Icons.SAMPLE, "gazelle.common.menu.ConnectathonObjectType", new AuthorizationOr(
            Authorizations.EDITOR, Authorizations.ADMIN), Authorizations.NOT_PR),

    ADMIN_SAMPLES_ANNOTATIONS("/objects/listAnnotations.xhtml", Icons.SAMPLE, "gazelle.testmanagement.object.ManageAnnotations", Authorizations
            .ADMIN, Authorizations.NOT_PR),

    ADMIN_PREFERENCES("/administration/preferences.xhtml", Icons.ADMINISTRATION, "gazelle.common.ApplicationPreferences", Authorizations.ADMIN),

    ADMIN_IMPORT_TESTS("/administration/tests/import.xhtml", Icons.ADMINISTRATION, "importTests", Authorizations.ADMIN),

    // elements not in menu

    REGISTRATION_SYSTEMS_CREATION("/systems/system/createSystemInSession.xhtml", null, null, Authorizations.LOGGED, Authorizations.NOT_ONLY_GMM),

    REGISTRATION_SYSTEMS_EDITION("/systems/system/editSystemInSession.xhtml", null, null, Authorizations.LOGGED, Authorizations.NOT_ONLY_GMM),

    REGISTRATION_SYSTEMS_VIEW("/systems/system/showSystemInSession.xhtml", null, null, Authorizations.LOGGED, Authorizations.NOT_ONLY_GMM),

    SYSTEMS_VIEW("/systemInSession.xhtml", null, null, Authorizations.LOGGED, Authorizations.NOT_ONLY_GMM),

    REGISTRATION_SYSTEMS_COPY("/systems/system/copySystemInSession.xhtml", null, null, Authorizations.LOGGED, Authorizations.NOT_ONLY_GMM),

    OBJECTS_EDITOBJECTTYPE("/objects/editObjectType.xhtml", null, null, Authorizations.EDITOR),

    OBJECTS_VIEWOBJECTLIST("/objects/viewObjectList.xhtml", null, null, new AuthorizationOr(Authorizations.EDITOR,
            Authorizations.MONITOR)),

    OBJECTS_EDITSAMPLE("/objects/EditSample.xhtml", null, null, Authorizations.LOGGED),

    OBJECTS_EDITREADERSAMPLE("/objects/editReaderSample.xhtml", null, null, Authorizations.LOGGED),

    OBJECTS_SAMPLE("/objects/sample.xhtml", null, null, Authorizations.LOGGED),

    FINANCIAL_SHOWINVOICEDETAILS("/financial/showInvoiceDetails.xhtml", null, null, Authorizations
            .ACCOUNTING_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION),

    FINANCIAL_LISTINVOICECOPIES("/financial/listInvoiceCopies.xhtml", null, null, Authorizations
            .ACCOUNTING_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION),

    FINANCIAL_EDITINVOICEDETAILS("/financial/editInvoiceDetails.xhtml", null, null, Authorizations
            .ACCOUNTING_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION),

    VALIDATOR_EDITMETHODVALIDATOR("/validator/editMethodValidator.xhtml", null, null, Authorizations.ADMIN),

    PATIENT("/patient.xhtml", null, null, Authorizations.LOGGED),

    SESSION("/session.xhtml", null, null, Authorizations.ADMIN),

    USAGEGRAPH("/usageGraph.xhtml", null, null, Authorizations.ADMIN),

    ERROR("/error.xhtml", null, null, Authorizations.ALL),

    SYSTEMS_SIMULATORS_EDITSIMULATOR("/systems/simulators/editSimulator.xhtml", null, null, Authorizations.ADMIN),

    SYSTEMS_SESSION_EDITSESSION("/administration/editSession.xhtml", null, null, Authorizations
            .ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION),

    TESTING_TEST_TEST_TESTINSTANCE("/testing/test/test/TestInstance.xhtml", null, null, Authorizations.LOGGED),

    TESTING_TEST_TEST_TESTINSTANCE_ITB("/testingITB/test/test/TestInstance.xhtml", null, null, Authorizations.LOGGED),

    TESTING_TEST_TEST_STARTTESTINSTANCE("/testing/test/test/StartTestInstance.xhtml", null, null, Authorizations.LOGGED),

    TESTING_TEST_MESA_LOGRETURNFORMESATEST("/testing/test/mesa/logReturnForMesaTest.xhtml", null, null, Authorizations.LOGGED),

    TESTING_TEST_MESA_LISTOFPRECONNECTATHONTESTS("/testing/test/mesa/listOfPreconnectathonTests.xhtml", null, null, Authorizations.LOGGED),

    TESTING_TESTSDEFINITION_EDITTESTPAGE("/testing/testsDefinition/editTestPage.xhtml", null, null, Authorizations.EDITOR),
    TESTING_TESTSDEFINITION_EDIT_DESCRIPTION_PAGE("/testing/testsDefinition/editTestDescription.xhtml", null, null, Authorizations.EDITOR),
    TESTING_TESTSDEFINITION_TRANSLATE_DESCRIPTION_PAGE("/testing/testsDefinition/translateDescription.xhtml", null, null, Authorizations.EDITOR),
    TESTING_TESTSDEFINITION_SHOWPAGE("/testing/testsDefinition/showTestDescriptions.xhtml", null, null, Authorizations.LOGGED),
    TESTING_TESTSDEFINITION_EDITTESTPAGE_ITB("/testingITB/testsDefinition/editTestPage.xhtml", null, null, Authorizations.EDITOR),

    TESTING_TESTSDEFINITION_NEWVIEWTESTPAGE("/testing/testsDefinition/viewTestPage.xhtml", null, null, Authorizations.DISPLAY_TESTS),
    TESTING_TESTSDEFINITION_NEWEDITTESTSUMMARYPAGE("/testing/testsDefinition/editTestSummary.xhtml", null, null, Authorizations.EDITOR),
    TESTING_TESTSDEFINITION_NEWEDITTESTDESCRIPTIONPAGE("/testing/testsDefinition/editTestDescription.xhtml", null, null, Authorizations.EDITOR),
    TESTING_TESTSDEFINITION_NEWEDITTESTROLESPAGE("/testing/testsDefinition/editTestRoles.xhtml", null, null, Authorizations.EDITOR),
    TESTING_TESTSDEFINITION_NEWEDITTESTSTEPSPAGE("/testing/testsDefinition/editTestSteps.xhtml", null, null, Authorizations.EDITOR),

    TESTING_TESTSDEFINITION_VIEWTESTPAGE_ITB("/testingITB/testsDefinition/viewTestPage.xhtml", null, null, Authorizations.DISPLAY_TESTS),

    TESTING_METATEST_METATEST("/testing/metatest/metaTest.xhtml", null, null, Authorizations.LOGGED),

    TESTING_ROLEINTEST_TESTROLESTABLEFORTESTREQUIREMENTS("/testing/roleInTest/testRolesTableForTestRequirements.xhtml", null, null, Authorizations
            .LOGGED),

    MONITORS_MONITOR("/monitors/monitor.xhtml", null, null, Authorizations.LOGGED),

    CONFIGURATION_COMMON_CREATE_ADDCONFIGURATION("/configuration/create/addConfiguration.xhtml", null, null, Authorizations.LOGGED),

    CONFIGURATION_NETWORK_CREATE_ADDNETWORKCONFIGURATION("/configuration/NETWORK/create/addNetworkConfiguration.xhtml", null, null, Authorizations
            .LOGGED),

    CONFIGURATION_OID_EDITOIDREQUIREMENT("/configuration/oid/editOIDRequirement.xhtml", null, null, Authorizations.LOGGED),

    PR_SYSTEMIMPLEMENTATIONS("/pr/systemImplementations.xhtml", null, null, Authorizations.ALL),

    PR_REVIEWSYSTEM("/pr/reviewSystem.xhtml", null, null, Authorizations.ADMIN),

    TF_DOCUMENTS_EDITDOCUMENT("/tf/documents/editDocument.xhtml", null, null, Authorizations.EDITOR),

    TF_DOCUMENTS_SHOWDOCUMENT("/tf/documents/showDocument.xhtml", null, null, Authorizations.LOGGED),

    TF_INTEGRATIONPROFILEOPTION_EDITINTEGRATIONPROFILEOPTION("/tf/integrationProfileOption/editIntegrationProfileOption.xhtml", null, null,
            Authorizations.ALL),

    TF_INTEGRATIONPROFILEOPTION_INTEGRATIONPROFILEOPTIONDELETESIDEEFFECTS("/tf/integrationProfileOption/integrationProfileOptionDeleteSideEffects" +
            ".xhtml", null, null, Authorizations.EDITOR),

    TF_INTEGRATIONPROFILE_INTEGRATIONPROFILEDELETESIDEEFFECTS("/tf/integrationProfile/integrationProfileDeleteSideEffects.xhtml", null, null,
            Authorizations.EDITOR),

    TF_INTEGRATIONPROFILE_ADDDOCUMENTSECTION("/tf/integrationProfile/addDocumentSection.xhtml", null, null, Authorizations.EDITOR),

    TF_INTEGRATIONPROFILE_SHOWINTEGRATIONPROFILE("/tf/integrationProfile/showIntegrationProfile.xhtml", null, null, Authorizations.ALL),

    TF_INTEGRATIONPROFILE_EDITINTEGRATIONPROFILE("/tf/integrationProfile/editIntegrationProfile.xhtml", null, null, Authorizations.EDITOR),

    TF_TRANSACTIONLINK_ADDTRANSACTIONLINKS("/tf/transactionLink/addTransactionLinks.xhtml", null, null, Authorizations.EDITOR),

    TF_ACTORINTEGRATIONPROFILE_LINKINTEGRATIONPROFILESFORACTOR("/tf/actorIntegrationProfile/linkIntegrationProfilesForActor.xhtml", null, null,
            Authorizations.EDITOR),

    TF_ACTORINTEGRATIONPROFILE_LINKINTEGRATIONPROFILESFORACTORSIDEEFFECTS("/tf/actorIntegrationProfile/linkIntegrationProfilesForActorSideEffects" +
            ".xhtml", null, null, Authorizations.EDITOR),

    TF_ACTORINTEGRATIONPROFILE_LINKACTORINTEGRATIONPROFILE("/tf/actorIntegrationProfile/linkActorIntegrationProfile.xhtml", null, null,
            Authorizations.EDITOR),

    TF_ACTORINTEGRATIONPROFILE_LINKACTORINTEGRATIONPROFILESIDEEFFECTS("/tf/actorIntegrationProfile/linkActorIntegrationProfileSideEffects.xhtml",
            null, null, Authorizations.EDITOR),

    TF_RULES_EDIT("/tf/rules/edit.xhtml", null, null, Authorizations.EDITOR),

    TF_PROFILELINK_ADDPROFILELINKS("/tf/profileLink/addProfileLinks.xhtml", null, null, Authorizations.EDITOR),

    TF_PROFILELINK_ROFILELINKS("/tf/profileLink/linkProfileDomains.xhtml", null, null, Authorizations.EDITOR),

    TF_ACTORINTEGRATIONPROFILEOPTION_ADDACTORINTEGRATIONPROFILEOPTIONS("/tf/actorIntegrationProfileOption/addActorIntegrationProfileOptions.xhtml",
            null, null, Authorizations.EDITOR),

    TF_ACTOR_EDITACTOR("/tf/actor/editActor.xhtml", null, null, Authorizations.ALL),

    TF_ACTOR_ACTORDELETESIDEEFFECTS("/tf/actor/actorDeleteSideEffects.xhtml", null, null, Authorizations.EDITOR),

    TF_TRANSACTION_LINKTRANSACTION("/tf/transaction/linkTransaction.xhtml", null, null, Authorizations.EDITOR),

    TF_TRANSACTION_ADDDOCUMENTSECTION("/tf/transaction/addDocumentSection.xhtml", null, null, Authorizations.EDITOR),

    TF_TRANSACTION_TRANSACTIONDELETESIDEEFFECTS("/tf/transaction/transactionDeleteSideEffects.xhtml", null, null, Authorizations.EDITOR),

    TF_TRANSACTION_EDITTRANSACTION("/tf/transaction/editTransaction.xhtml", null, null, Authorizations.ALL),

    TF_DOMAIN_DOMAINDELETESIDEEFFECTS("/tf/domain/domainDeleteSideEffects.xhtml", null, null, Authorizations.EDITOR),

    TF_DOMAIN_LINKDOMAINPROFILES("/tf/domain/linkDomainProfiles.xhtml", null, null, Authorizations.EDITOR),

    TF_DOMAIN_EDITDOMAIN("/tf/domain/editDomain.xhtml", null, null, Authorizations.EDITOR),

    TF_DOMAIN_SHOWDOMAIN("/tf/domain/showDomain.xhtml", null, null, Authorizations.ALL),

    INSTALL_INSTALLATION("/install/installation.xhtml", null, null, Authorizations.ALL),

    USERS_SENDMESSAGE("/users/sendMessage.xhtml", null, null, Authorizations.ADMIN),

    USERS_INSTITUTION_CREATEINSTITUTION("/users/institution/createInstitution.xhtml", null, null, Authorizations.LOGGED),

    USERS_INSTITUTION_SHOWINSTITUTION("/users/institution/showInstitution.xhtml", null, null, Authorizations
            .ADMIN_OR_VENDOR_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION),

    USERS_LOGINCAS_LOGINPRIVILEGED("/users/loginCAS/loginPrivileged.xhtml", null, null, Authorizations.ALL),

    USERS_LOGINCAS_LOGGEDIN("/users/loginCAS/loggedIn.xhtml", null, null, Authorizations.ALL),

    USERS_LOGINCAS_LOGIN("/users/loginCAS/login.xhtml", null, null, Authorizations.ALL),

    USERS_CONTACT_CREATECONTACT("/users/contact/createContact.xhtml", null, null, Authorizations.LOGGED),

    USERS_LOGIN_LOGINPRIVILEGED("/users/login/loginPrivileged.xhtml", null, null, Authorizations.ALL),

    USERS_LOGIN_LOGGEDIN("/users/login/loggedIn.xhtml", null, null, Authorizations.ALL),

    USERS_RESTARTED("/users/restarted.xhtml", null, null, Authorizations.ALL),

    USERS_LOGIN_LOGIN("/users/login/login.xhtml", null, null, Authorizations.ALL),

    USERS_USER("/users/user.xhtml", null, null, Authorizations.LOGGED),

    USERS_X("/users/x.xhtml", null, null, Authorizations.ALL),

    USERS_USER_CHANGEPASSWORD("/users/user/changePassword.xhtml", null, null, Authorizations.ALL),

    USERS_USER_LOSTPASSWORD("/users/login/passwordAssist.xhtml", null, null, Authorizations.ALL),

    USERS_USER_EDITOTHERUSERPREFERENCES("/users/user/editOtherUserPreferences.xhtml", null, null, Authorizations.ADMIN),

    USERS_USER_CHANGEUSERPASSWORDAFTERREGISTRATION("/users/user/changeUserPasswordAfterRegistration.xhtml", null, null, Authorizations.ALL),

    USERS_USER_CHANGEUSERPASSWORD("/users/user/changeUserPassword.xhtml", null, null, Authorizations.ALL),

    USERS_USER_CHANGEPASSWORDCURRENTLOGGEDUSER("/users/user/changePasswordCurrentLoggedUser.xhtml", null, null, Authorizations.ALL),

    OBJECTS_SAMPLESSUMMARY("/objects/samplesSummary.xhtml", null, null, Authorizations.LOGGED),

    FINANCIAL_GAZELLEEXTRACTINVOICEREPORT("/financial/GazelleExtractInvoiceReport.xhtml", null, null, Authorizations.ACCOUNTING),

    FINANCIAL_PAYMENTTRACKINGREPORT("/financial/PaymentTrackingReport.xhtml", null, null, Authorizations.ACCOUNTING),

    ADMINISTRATION_MONITORSSUMMARY("/administration/monitorsSummary.xhtml", null, null, Authorizations.ADMIN),

    SYSTEMS_SYSTEM_SYSTEMDEPENDENCIES("/systems/system/systemDependencies.xhtml", null, null, Authorizations.ADMIN),

    SYSTEMS_SYSTEM_STATEMENT("/systems/system/integrationStatement.xhtml", null, null, Authorizations.ALL),

    TESTING_TEST_MESA_MESATESTSSUMMARY("/testing/test/mesa/mesaTestsSummary.xhtml", null, null, Authorizations.LOGGED),

    TESTING_TESTSDEFINITION_LISTTESTASXLS("/testing/testsDefinition/listTestAsXLS.xhtml", null, null, Authorizations.LOGGED),

    TESTING_KPI_KPITESTCANVIZ("/testing/kpi/kpiTestCanviz.xhtml", null, null, Authorizations.ADMIN),

    TESTING_KPI_TIMELINE_KPITESTTIMELINE("/testing/kpi/timeline/kpiTestTimeline.xhtml", null, null, Authorizations.ADMIN),

    LAYOUT_TEMPLATE("/layout/template.xhtml", null, null, Authorizations.NONE),

    CONFIGURATION_HL7_LIST_LISTEXISTINGHL7V3INITIATORCONFIGURATIONSASCSV("/configuration/HL7/list/listExistingHL7V3InitiatorConfigurationsAsCSV" +
            ".xhtml", null, null, Authorizations.LOGGED),

    CONFIGURATION_HL7_LIST_LISTEXISTINGHL7RESPONDERCONFIGURATIONSASCSV("/configuration/HL7/list/listExistingHL7ResponderConfigurationsAsCSV.xhtml",
            null, null, Authorizations.LOGGED),

    CONFIGURATION_HL7_LIST_LISTEXISTINGHL7V3RESPONDERCONFIGURATIONSASCSV("/configuration/HL7/list/listExistingHL7V3ResponderConfigurationsAsCSV" +
            ".xhtml", null, null, Authorizations.LOGGED),

    CONFIGURATION_HL7_LIST_LISTEXISTINGHL7INITIATORCONFIGURATIONSASCSV("/configuration/HL7/list/listExistingHL7InitiatorConfigurationsAsCSV.xhtml",
            null, null, Authorizations.LOGGED),

    CONFIGURATION_DICOM_LIST_LISTEXISTINGDICOMSCUCONFIGURATIONSASCSV("/configuration/DICOM/list/listExistingDicomSCUConfigurationsAsCSV.xhtml",
            null, null, Authorizations.LOGGED),

    CONFIGURATION_DICOM_LIST_LISTEXISTINGDICOMSCPCONFIGURATIONSASCSV("/configuration/DICOM/list/listExistingDicomSCPConfigurationsAsCSV.xhtml",
            null, null, Authorizations.LOGGED),

    CONFIGURATION_OTHER_LIST_LISTEXISTINGSYSLOGCONFIGURATIONSASCSV("/configuration/OTHER/list/listExistingSyslogConfigurationsAsCSV.xhtml", null,
            null, Authorizations.LOGGED),

    CONFIGURATION_OTHER_LIST_LISTEXISTINGWSCONFIGURATIONSASCSV("/configuration/OTHER/list/listExistingWSConfigurationsAsCSV.xhtml", null, null,
            Authorizations.LOGGED),

    PR_LOGSIFRAMETIMELINE("/pr/logsiFrameTimeline.xhtml", null, null, Authorizations.LOGGED),

    TF_INTEGRATIONPROFILEOPTION_INTEGRATIONPROFILEOPTIONSSUMMARY("/tf/integrationProfileOption/integrationProfileOptionsSummary.xhtml", null, null,
            Authorizations.LOGGED),

    TF_WSTRANSACTIONUSAGE_WSTRANSACTIONUSAGESUMMARY("/tf/wsTransactionUsage/wsTransactionUsageSummary.xhtml", null, null, Authorizations.LOGGED),

    TF_INTEGRATIONPROFILE_INTEGRATIONPROFILESUMMARY("/tf/integrationProfile/integrationProfileSummary.xhtml", null, null, Authorizations.LOGGED),

    TF_TRANSACTIONOPTIONTYPE_TRANSACTIONOPTIONTYPESSUMMARY("/tf/transactionOptionType/transactionOptionTypesSummary.xhtml", null, null,
            Authorizations.LOGGED),

    TF_MESSAGEPROFILE_MESSAGEPROFILESSUMMARY("/tf/messageProfile/messageProfilesSummary.xhtml", null, null, Authorizations.LOGGED),

    TF_ACTOR_ACTORSSUMMARY("/tf/actor/actorsSummary.xhtml", null, null, Authorizations.LOGGED),

    TF_TRANSACTION_TRANSACTIONSUMMARY("/tf/transaction/transactionSummary.xhtml", null, null, Authorizations.LOGGED),

    TF_DOMAIN_LISTDOMAINASXLS("/tf/domain/listDomainAsXLS.xhtml", null, null, Authorizations.LOGGED),

    USERS_MESSAGES("/users/messages.xhtml", null, null, Authorizations.LOGGED),

    USERS_CONTACT_LISTCONTACTSASCSV("/users/contact/listContactsAsCSV.xhtml", null, null, Authorizations.VENDOR_ADMIN),

    USERS_MESSAGENOTIFICATIONS("/users/messageNotifications.xhtml", null, null, Authorizations.LOGGED),

    USERS_CONNECTATHON_LISTPARTICIPANTSASCSV("/users/connectathon/listParticipantsAsCSV.xhtml", null, null, Authorizations.ADMIN_OR_VENDOR_ADMIN),

    USERS_USER_USERSSUMMARY("/users/user/usersSummary.xhtml", null, null, Authorizations.ADMIN_OR_VENDOR_ADMIN),

    USERS_USER_USERSLISTSUMMARY("/users/user/listUsersSummary.xhtml", null, null, Authorizations.ADMIN_OR_VENDOR_ADMIN),

    // pages.xml

    CONFIRMREGISTRATION("/confirmRegistration.xhtml", null, null, Authorizations.ALL),

    CONFIRMREGISTRATIONBYVENDORADMIN("/confirmRegistrationByVendorAdmin.xhtml", null, null, Authorizations.ALL),

    FIRSTUSERREGISTRATION("/firstUserRegistration.xhtml", null, null, Authorizations.ALL),

    FIRSTUSERAFTERLOGGEDOUT("/firstUserAfterLoggedOut.xhtml", null, null, Authorizations.ALL),

    CHANGEIHEPASSWORD("/changeIHEPassword.xhtml", null, null, Authorizations.ALL),

    CONFIGURATION_DNS("/configuration/dns.xhtml", null, null, Authorizations.ALL),

    CONFIGURATION_HOSTS("/configuration/hosts.xhtml", null, null, Authorizations.ALL),
    
    CONFIGURATION_DCMLDIF("/configuration/dcmldif.xhtml", null, null, Authorizations.ALL),

    CONFIGURATION_REVERSE("/configuration/reverse.xhtml", null, null, Authorizations.ALL),

    TESTSEQUENCEDIAGRAM("/testSequenceDiagram.xhtml", null, null, Authorizations.ALL),

    TESTINSTANCESEQUENCEDIAGRAM("/testInstanceSequenceDiagram.xhtml", null, null, Authorizations.ALL),

    TESTDESCRIPTIONIMAGE("/testDescriptionImage.xhtml", null, null, Authorizations.ALL),

    USERPHOTO("/userPhoto.xhtml", null, null, Authorizations.LOGGED),

    PING("/ping.xhtml", null, null, Authorizations.ALL),

    ISSNAPSHOT("/isSnapshot.xhtml", null, null, Authorizations.ALL),

    PR_GETIS("/pr/getIS.xhtml", null, null, Authorizations.ALL),

    PR_GENERATEIS("/pr/generateIS.xhtml", null, null, Authorizations.LOGGED),

    DOCUMENT_GETPDFDOCUMENT("/document/getPDFdocument.xhtml", null, null, Authorizations.ALL),

    DOCUMENT_DOCUMENTINSTANCE("/document/documentInstance.xhtml", null, null, Authorizations.ALL),

    SEARCH_OPENSEARCHDESCRIPTION("/search/opensearchdescription.xhtml", null, null, Authorizations.ALL),

    SEARCH_SEARCHJSON("/search/searchJSON.xhtml", null, null, Authorizations.LOGGED),

    DOMAIN("/domain.xhtml", null, null, Authorizations.ALL),

    ACTOR("/actor.xhtml", null, null, Authorizations.ALL),

    TESTING_OBJECTS_SYSTEM_OBJECT("/testing/objects/system_object.xhtml", null, null, Authorizations.LOGGED),

    // For REST access from crons
    SYSTEMCONFIGURATIONS("/systemConfigurations.xhtml", null, null, Authorizations.ALL),

    OIDSYSTEMS("/oidSystems.xhtml", null, null, Authorizations.LOGGED),

    TRANSACTION("/transaction.xhtml", null, null, Authorizations.ALL),

    PROFILE("/profile.xhtml", null, null, Authorizations.ALL),

    PROFILEOPTION("/profileOption.xhtml", null, null, Authorizations.ALL),

    AIPO("/aipo.xhtml", null, null, Authorizations.ALL),

    TEST("/test.xhtml", null, null, Authorizations.DISPLAY_TESTS),

    TESTINSTANCE("/testInstance.xhtml", null, null, Authorizations.LOGGED),

    MESATESTINSTANCE("/mesaTestInstance.xhtml", null, null, Authorizations.LOGGED),

    // added 8/23 by rizwan.tanoli@aegis.net as part of work for NIST Interoperability Testbed
    INTEROP_MENU("/testingITB/test/interop.xhtml", Icons.CAT, "gazelle.common.menu.Interop", Authorizations.LOGGED, Authorizations.TM,
            Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP("/testingITB/test/interop.xhtml", Icons.CAT, "gazelle.common.menu.Interop", Authorizations.LOGGED, Authorizations.TM, Authorizations
            .SESSION_WITH_INTEROPERABILITY),

    INTEROP_DASHBOARD("/testingITB/test/interop.xhtml", Icons.CAT, "gazelle.common.menu.Interop.InteroperabilityDashboard", Authorizations.LOGGED,
            Authorizations.TM, Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP_SYSTEMS("/systems/listSystems.xhtml", Icons.SYSTEMS, "gazelle.common.menu.FindSystems", Authorizations.LOGGED, Authorizations.TM,
            Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP_INTEROP("/testingITB/test/interop/listAllSystemsForInteropTesting.xhtml", Icons.CAT_PRE, "gazelle.common.menu.InteropTesting",
            Authorizations.LOGGED, Authorizations.TM, Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP_INSTANCES("/testingITB/research/testInstances.xhtml", Icons.CAT_RUNNING, "gazelle.common.menu.TestsPerSystem", Authorizations.LOGGED,
            Authorizations.ADMIN_OR_MONITOR, Authorizations.TM, Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP_RESULTS("/testingITB/results/interopResults.xhtml", Icons.CAT_RESULTS, "gazelle.common.menu.InteropResults", Authorizations.LOGGED,
            Authorizations.TM, Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP_REPORT("/testingITB/reporting/reporting4InteropTestingSession.xhtml", Icons.CAT_RESULTS, "net.ihe.gazelle.tm.DownloadInteropReport",
            Authorizations.ADMIN_OR_VENDOR, Authorizations.TM, Authorizations.SESSION_WITH_INTEROPERABILITY),

    INTEROP_VALIDATION_REPORT("/testingITB/test/interop/validationResultDetails.xhtml", null, null, Authorizations.LOGGED, Authorizations
            .SESSION_WITH_INTEROPERABILITY),

    INTEROP_VALIDATION_CONTEXT("/testingITB/test/interop/validationContext.xhtml", null, null, Authorizations.LOGGED, Authorizations
            .SESSION_WITH_INTEROPERABILITY),

    INTEROP_EXAMPLE_MESSAGE("/testingITB/test/interop/exampleMessage.xhtml", null, null, Authorizations.LOGGED, Authorizations
            .SESSION_WITH_INTEROPERABILITY);

    private static final Logger LOG = LoggerFactory.getLogger(Pages.class);
    private String link;
    private Authorization[] authorizations;
    private String label;
    private String icon;

    Pages(String link, Icons icon, String label, Authorization... authorizations) {
        this.link = link;
        this.authorizations = authorizations;
        this.label = label;
        if (icon != null) {
            this.icon = icon.getIcon();
        } else {
            this.icon = null;
        }
    }

    @Override
    public String getId() {
        return name();
    }

    @Override
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLink");
        }
        this.link = link;
    }

    public String getMenuLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMenuLink");
        }
        return link.replace(".xhtml", ".seam");
    }

    @Override
    public String getIcon() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIcon");
        }
        return icon;
    }

    @Override
    public Authorization[] getAuthorizations() {
        return authorizations;
    }

    @Override
    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        if (this == HOME) {
            return ApplicationPreferenceManager.instance().getApplicationName();
        } else if (this == CONFIG_LIST_VENDOR) {
            return Institution.getLoggedInInstitution().getKeyword()
                    + " : "
                    + StatusMessage.getBundleMessage("gazelle.configuration.SystemConfiguration",
                    "gazelle.configuration.SystemConfiguration");
        }
        return label;
    }

    public void setLabel(String label) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLabel");
        }
        this.label = label;
    }

}
