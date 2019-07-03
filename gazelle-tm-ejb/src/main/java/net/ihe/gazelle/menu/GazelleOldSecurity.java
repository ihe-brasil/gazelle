package net.ihe.gazelle.menu;

import net.ihe.gazelle.common.action.CacheRequest;
import net.ihe.gazelle.common.action.CacheUpdater;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.pages.Authorization;
import net.ihe.gazelle.common.pages.AuthorizationOr;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.permission.PermissionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Name("gazelleOldSecurity")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@GenerateInterface("GazelleOldSecurityLocal")
public class GazelleOldSecurity implements PermissionResolver, GazelleOldSecurityLocal, Serializable {

    private static final String INVOICE_ADMIN_MANAGER = "InvoiceAdminManager";

    private static final String ANNOTATION_MANAGER = "AnnotationManager";

    private static final String TESTS_DEFINITIONS_ADMINISTRATION_MANAGER = "TestsDefinitionsAdministrationManager";

    private static final String SYSTEM_IN_SESSION_SELECTOR = "SystemInSessionSelector";

    private static final String DEMONSTRATION_MANAGER = "DemonstrationManager";

    private static final String SYSTEM_IN_SESSION_MANAGER = "SystemInSessionManager";

    private static final String SYSTEM_IN_SESSION_LIST_MANAGER = "SystemInSessionListManager";

    private static final String PERSON_FUNCTION_MANAGER = "PersonFunctionManager";

    private static final String MASTER_MODEL = "MasterModel";

    private static final String USER_MANAGER = "UserManager";

    private static final String TESTING_MANAGER = "TestingManager";

    private static final String SYSTEM_CONFIGURATION_MANAGER = "SystemConfigurationManager";

    private static final String PERSON_MANAGER = "PersonManager";

    private static final String OBJECT_FILE_TYPE_MANAGER = "ObjectFileTypeManager";

    private static final String OBJECT_TYPE_MANAGER = "ObjectTypeManager";

    private static final String TESTING_SESSION_MANAGER = "TestingSessionManager";

    private static final String SYSTEM_MANAGER = "SystemManager";

    private static final String DOMAIN_MANAGER = "DomainManager";

    private static final String INSTITUTION_MANAGER = "InstitutionManager";

    private static final String ADDRESS_MANAGER = "AddressManager";

    private static final String SYSTEM_IN_SESSION_CREATION = "SystemInSessionCreator";

    private static final String SYSTEM_IN_SESSION_EDTION = "SystemInSessionEditor";

    private static final String SYSTEM_IN_SESSION_VIEWER = "systemInSessionViewer";


    private static final long serialVersionUID = 8387100366231963177L;
    private static final Logger LOG = LoggerFactory.getLogger(GazelleOldSecurity.class);
    private static Map<MultiKey, Authorization> AUTHORIZATIONS;

    @Override
    @Create
    public void initAuthorizations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initAuthorizations");
        }
        AUTHORIZATIONS = computeAuthorizations();
    }

    @Override
    public boolean hasPermissionDo(Object target, String action) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasPermissionDo");
        }
        MultiKey key = new MultiKey(target, action);
        if (AUTHORIZATIONS.get(key) != null) {
            return AUTHORIZATIONS.get(key).isGranted();
        } else {
            LOG.error("Failed to get permission for " + target + " - " + action);
        }
        return false;
    }

    @Override
    public boolean hasPermission(final Object target, final String action) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasPermission");
        }
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance("cacheRequest");
        Object result = cacheRequest.getValueUpdater(target + " - " + action, new CacheUpdater() {
            @Override
            public Object getValue(String key, Object parameter) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getValue");
                }
                return hasPermissionDo(target, action);
            }
        }, null);
        return (Boolean) result;
    }

    @Override
    public void filterSetByAction(Set<Object> targets, String action) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("filterSetByAction");
        }
        Set<Object> toRemove = new HashSet<Object>();
        for (Object target : targets) {
            if (hasPermission(target, action)) {
                toRemove.add(target);
            }
        }
        for (Object target : toRemove) {
            targets.remove(target);
        }
    }

    private Map<MultiKey, Authorization> computeAuthorizations() {
        Map<MultiKey, Authorization> result = new HashMap<MultiKey, Authorization>();

        result.put(new MultiKey(MASTER_MODEL, "view"), Authorizations.ALL);
        result.put(new MultiKey(MASTER_MODEL, "edit"), Authorizations.EDITOR);
        result.put(new MultiKey(ADDRESS_MANAGER, "addAddress"), Authorizations.ALL);
        result.put(new MultiKey(ADDRESS_MANAGER, "countryAutoComplete"), Authorizations.ALL);
        result.put(new MultiKey(ADDRESS_MANAGER, "getIso3166CountryCodes"), Authorizations.ALL);
        result.put(new MultiKey(ADDRESS_MANAGER, "updateAddress"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(ADDRESS_MANAGER, "deleteAddress"), Authorizations.ADMIN);
        result.put(new MultiKey(ADDRESS_MANAGER, "viewAddress"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(ADDRESS_MANAGER, "editAddress"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(ADDRESS_MANAGER, "createAddress"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(INSTITUTION_MANAGER, "addInstitution"), Authorizations.ALL);
        result.put(new MultiKey(INSTITUTION_MANAGER, "viewInstitution"), Authorizations.ALL);
        result.put(new MultiKey(INSTITUTION_MANAGER, "editInstitution"), Authorizations.ALL);
        result.put(new MultiKey(INSTITUTION_MANAGER, "validateInstitutionName"), Authorizations.ALL);
        result.put(new MultiKey(INSTITUTION_MANAGER, "validateInstitutionKeyword"), Authorizations.ALL);
        result.put(new MultiKey(INSTITUTION_MANAGER, "validateInstitutionAddress"), Authorizations.ALL);
        result.put(new MultiKey(INSTITUTION_MANAGER, "updateInstitution"), Authorizations.REGISTRATION_ADMIN);
        result.put(new MultiKey(INSTITUTION_MANAGER, "createFinancialInformationsForInstitution"),
                Authorizations.REGISTRATION_ADMIN);
        result.put(new MultiKey(INSTITUTION_MANAGER, "deleteInstitution"), Authorizations.ADMIN);
        result.put(new MultiKey(INSTITUTION_MANAGER, "addNewInstitutionButton"), Authorizations.PROJECT_MANAGER);
        result.put(new MultiKey(INSTITUTION_MANAGER, "editInstitutionSession"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(DOMAIN_MANAGER, "viewDomain"), Authorizations.ALL);

        result.put(new MultiKey(SYSTEM_MANAGER, "validateSystemNameAndSystemVersion"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_MANAGER, "validateSystemKeyword"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_MANAGER, "generateSystemKeyword"), Authorizations.REGISTRATION_EDITOR);

        result.put(new MultiKey(SYSTEM_IN_SESSION_CREATION, "addSystemForPR"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_CREATION, "addSystem"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_CREATION, "generateSystemKeyword"), Authorizations.REGISTRATION_EDITOR);

        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "addSystemForPR"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "editSystem"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "generateSystemKeyword"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "saveHL7URLInModalPanel"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "saveDicomURLInModalPanel"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "persistHL7DocumentsFiles"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "persistDicomDocumentsFiles"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "removeHL7URL"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "removeDicomURL"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "removeHL7Document"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "removeDicomDocument"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_EDTION, "updateSystemInSession"), Authorizations.REGISTRATION_EDITOR);

        result.put(new MultiKey(SYSTEM_IN_SESSION_LIST_MANAGER, "getSystemsInSessionListDependingInstitution"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_LIST_MANAGER, "deleteSystemInSession"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_LIST_MANAGER, "deleteSystemInAllSession"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_LIST_MANAGER, "createSystemSessionForSystem"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_LIST_MANAGER, "updateSelectedSystemInSession"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_LIST_MANAGER, "addInstitutionToSystem"), Authorizations.REGISTRATION_EDITOR);

        result.put(new MultiKey(USER_MANAGER, "addUser"), Authorizations.ALL);
        result.put(new MultiKey(USER_MANAGER, "addUserByAdmin"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(USER_MANAGER, "updateUser"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(USER_MANAGER, "deleteUser"), Authorizations.ADMIN_OR_VENDOR_ADMIN);
        result.put(new MultiKey(USER_MANAGER, "viewUser"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(USER_MANAGER, "editUser"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(USER_MANAGER, "createUser"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(USER_MANAGER, "getInstitution"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(USER_MANAGER, "getUsersListDependingInstitution"),
                Authorizations.PROJECT_MANAGER_OR_VENDOR_ADMIN);
        result.put(new MultiKey(USER_MANAGER, "getAllUsersListForAdmin"), Authorizations.PROJECT_MANAGER);
        result.put(new MultiKey(USER_MANAGER, "addNewUserButton"), Authorizations.PROJECT_MANAGER_OR_VENDOR_ADMIN);
        result.put(new MultiKey(USER_MANAGER, "updatePassword"), Authorizations.PROJECT_MANAGER);

        result.put(new MultiKey(PERSON_FUNCTION_MANAGER, "changeFunction"),
                Authorizations.PROJECT_MANAGER_OR_VENDOR_ADMIN);
        result.put(new MultiKey(PERSON_FUNCTION_MANAGER, "setFunction"), Authorizations.PROJECT_MANAGER_OR_VENDOR_ADMIN);
        result.put(new MultiKey(PERSON_MANAGER, "addNewContactButton"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "listContacts"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "addPerson"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "updatePerson"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "deleteContact"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "viewContact"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "editContact"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "updateContact"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "createPerson"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(PERSON_MANAGER, "createContact"), Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(DOMAIN_MANAGER, "addDomain"), Authorizations.EDITOR);
        result.put(new MultiKey(DOMAIN_MANAGER, "updateDomain"), Authorizations.EDITOR);
        result.put(new MultiKey(DOMAIN_MANAGER, "deleteDomain"), Authorizations.EDITOR);
        result.put(new MultiKey(DOMAIN_MANAGER, "editDomain"), Authorizations.EDITOR);
        result.put(new MultiKey(DOMAIN_MANAGER, "createDomain"), Authorizations.EDITOR);

        result.put(new MultiKey(SYSTEM_IN_SESSION_MANAGER, "canModifyTableSession"), Authorizations.ADMIN_OR_MONITOR);
        result.put(new MultiKey(DEMONSTRATION_MANAGER, "editDemonstration"), Authorizations.PROJECT_MANAGER);
        result.put(new MultiKey(DEMONSTRATION_MANAGER, "isDemonRegistered"),
                Authorizations.PROJECT_MANAGER_OR_VENDOR_ADMIN);
        result.put(new MultiKey(DEMONSTRATION_MANAGER, "setDemonstration"),
                Authorizations.PROJECT_MANAGER_OR_VENDOR_ADMIN);

        result.put(new MultiKey(TESTING_SESSION_MANAGER, "viewTestingSession"), Authorizations.ALL);
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "addTestingSession"), new AuthorizationOr(Authorizations.PROJECT_MANAGER, Authorizations
                .TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION));
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "updateTestingSession"), new AuthorizationOr(Authorizations.PROJECT_MANAGER,
                Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION));
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "deleteTestingSession"), Authorizations.PROJECT_MANAGER);
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "editTestingSession"), new AuthorizationOr(Authorizations.PROJECT_MANAGER, Authorizations
                .TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION));
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "createTestingSession"), new AuthorizationOr(Authorizations.PROJECT_MANAGER,
                Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION));
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "addNewTestingSessionButton"), Authorizations.ADMIN);
        result.put(new MultiKey(TESTING_SESSION_MANAGER, "activateSession"), Authorizations
                .ADMIN_OR_TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "getSyslogConfigurationListDependingSystemInSession"),
                Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_IN_SESSION_SELECTOR, "getPossibleSystemsInSession"),
                Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "generateConfigurationsForCompany"), Authorizations.ADMIN);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "generateConfigurationsForSystem"),
                Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "generateAllConfigurationsForSession"),
                Authorizations.ADMIN);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "deleteConfiguration"),
                Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "addEditConfiguration"),
                Authorizations.REGISTRATION_EDITOR);
        result.put(new MultiKey(SYSTEM_CONFIGURATION_MANAGER, "deleteAllConfigurationsForSystem"),
                Authorizations.ADMIN_OR_VENDOR);
        result.put(new MultiKey("ConnectathonResultManager", "manageSystemAIPOResultsForAdmin"),
                Authorizations.PROJECT_MANAGER);
        result.put(new MultiKey("ConnectathonResultManager", "manageSystemAIPOResultsForNonAdmin"),
                Authorizations.MONITOR_OR_VENDOR);
        result.put(new MultiKey(TESTING_MANAGER, "manageInstanceOfTestForAdmin"), Authorizations.ADMIN_OR_MONITOR);
        result.put(new MultiKey(TESTING_MANAGER, "listOfPreconnectathonTestsForAdmin"), Authorizations.ADMIN_OR_MONITOR);
        result.put(new MultiKey(TESTING_MANAGER, "listOfPreconnectathonTestsForNonAdmin"), Authorizations.VENDOR);
        result.put(new MultiKey(TESTING_MANAGER, "findSystemsInSessionForTestingAsAdmin"),
                Authorizations.ADMIN_OR_MONITOR);
        result.put(new MultiKey(TESTING_MANAGER, "findSystemsInSessionForTestingAsNonAdmin"), Authorizations.VENDOR);
        result.put(new MultiKey(TESTS_DEFINITIONS_ADMINISTRATION_MANAGER, "EditTest"), Authorizations.EDITOR);
        result.put(new MultiKey(TESTS_DEFINITIONS_ADMINISTRATION_MANAGER, "ReadTest"), Authorizations.LOGGED);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfObjectCreatorForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfObjectReaderForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfObjectFileForSelectedObjectTypeForCreator"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfObjectFileForSelectedObjectTypeForReaders"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfObjectAttributeForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "addNewCreatorOfcurrentObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "addNewReaderOfcurrentObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getPossibleFileTypes"), Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "addNewObjectFileTypeToObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "addNewObjectAttributeToObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "updateSelectedObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getNumberOfObjectInstanceByObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getNumberOfObjectInstanceBySISForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListSISofSelectedObjectTypeForCreation"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getTableOfSIS"), Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfAIPOCreatorImplementedBySISForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListSISofSelectedObjectTypeForReading"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListOfAIPOReaderImplementedBySISForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "mergeSelectedObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "updateSelectedCreatorOfcurrentObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "updateSelectedReaderOfcurrentObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "mergeObjectFileTypeOfObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "mergeObjectAttributeOfObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListObjectInstanceForObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "viewListOIFOnDeleting"), Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "deleteSelectedObjectTypeFromDataBaseForSelectedObjectType"),
                Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "deleteSelectedCreatorFromDataBase"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "deleteSelectedReaderFromDataBase"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "deleteSelectedObjectFileFromDataBase"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "deleteSelectedObjectAttributeFromDataBase"),
                Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "mergeObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "getListObjectInstanceForSelectedObjectType"),
                Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(OBJECT_TYPE_MANAGER, "editObjectType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_FILE_TYPE_MANAGER, "mergeSelectedObjectFileType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_FILE_TYPE_MANAGER, "generateMessageForDeleteObjectFileType"),
                Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_FILE_TYPE_MANAGER, "deleteSelectedObjectFileType"), Authorizations.EDITOR);
        result.put(new MultiKey(OBJECT_FILE_TYPE_MANAGER, "persistObjectFileType"), Authorizations.EDITOR);
        result.put(new MultiKey(ANNOTATION_MANAGER, "getListObjectInstanceAnnotation"), Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(ANNOTATION_MANAGER, "deleteSelectedAnnotation"), Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(ANNOTATION_MANAGER, "getSamplePermanentlink"), Authorizations.OBJECT_VIEWER);
        result.put(new MultiKey(INVOICE_ADMIN_MANAGER, "saveInvoice"), Authorizations.ACCOUNTING_OR_TESTING_SESSION_ADMIN);

        return result;
    }

}
