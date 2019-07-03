package net.ihe.gazelle.users.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.users.model.*;
import net.ihe.gazelle.users.model.factories.InstitutionFactory;
import net.ihe.gazelle.users.model.factories.InstitutionTypeFactory;
import net.ihe.gazelle.users.model.factories.RoleFactory;
import net.ihe.gazelle.users.model.factories.UserFactory;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

public class UserManagerTest extends AbstractTestQueryJunit4 {

    private static final Logger LOG = LoggerFactory.getLogger(UserManagerTest.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    ApplicationPreferenceManager mockedApplicationManager;
    EmailManagerLocal mockedEmailManager;
    private int usersBeforeTest;
    private int usersAfterTest;
    private int instituionsBeforTest;
    private int institutionsAfterTest;

    @Override
    protected String getDb() {
        return "gazelle-junit";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.setFlushMode(FlushModeType.COMMIT);
        mockedApplicationManager = mock(ApplicationPreferenceManager.class);
        mockedEmailManager = mock(EmailManagerLocal.class);
    }

    @After
    public void tearDown() throws Exception {
        cleanData();
        super.tearDown();
    }

    private void cleanData() {
        final EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        entityManager.getTransaction().rollback();
        UserFactory.cleanUsers();
        InstitutionFactory.cleanInstitution();
        RoleFactory.cleanRole();
    }

    @Test
    public void RegisterUser_AldreadyExistingUser() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();

        userManager.setSelectedUser(UserFactory.createUserWithAllFields());
        userManager.setSelectedInstitution(InstitutionFactory.createInstitutionWithMandatoryFields());

        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertNotNull(registerUser);
    }

    @Test
    public void RegisterUser_without_user() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals("/users/user/register.seam", registerUser);
    }

    @Test
    public void RegisterUser_without_user2() {

        final UserManager userManager = new UserManager();
        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals(null, registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_with_existing_userName() {
        final EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.setFlushMode(FlushModeType.AUTO);

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        userManager.setSelectedInstitution(InstitutionFactory.createInstitutionWithMandatoryFields());
        final User user = UserFactory.createUserWithMandatoryFields();

        userManager.setSelectedUser(UserFactory.createUserWithAllFields());
        userManager.getSelectedUser().setUsername(user.getUsername());
        String registerUser = "";
        thrown.expect(PersistenceException.class);
        thrown.expectMessage("Could not execute JDBC batch update");
        registerUser = callRegisterUser(userManager);
        assertEquals(1, numberOfnewinstitutionsCreated());
        assertEquals(1, numberOfnewUserCreated());
        assertEquals("/users/user/register.seam", registerUser);
    }

    @Test
    public void RegisterUser_with_existing_email() {

        final UserManager userManager = new UserManager();
        final User user = UserFactory.createUserWithMandatoryFields();
        userManager.registerAccountButton();
        userManager.setSelectedUser(UserFactory.createUserWithAllFields());
        userManager.getSelectedUser().setEmail(user.getEmail());
        userManager.setSelectedInstitution(user.getInstitution());

        final EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.setFlushMode(FlushModeType.AUTO);
        thrown.expect(PersistenceException.class);
        // Throws an exception but it's what we expect
        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals("/users/user/register.seam", registerUser);
    }

    @Test
    public void RegisterUser_with_non_matching_passwords() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();

        setUser(userManager);

        userManager.getSelectedUser().setPasswordConfirmation("1234562");
        userManager.setSelectedInstitution(InstitutionFactory.createInstitutionWithMandatoryFields());

        final String registerUser = callRegisterUser(userManager);

        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals("/users/user/register.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_withNewInstitution() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();

        setUser(userManager);

        final InstitutionType institutionType = InstitutionTypeFactory.createInstitutionTypeWithMandatoryFields();
        userManager.getSelectedUser().getInstitution().setInstitutionType(institutionType);
        final Institution institutionNull = InstitutionFactory.createInstitutionWithNULLKeyword(institutionType);
        userManager.setSelectedInstitution(institutionNull);

        userManager.getSelectedUser().getInstitution().setKeyword("Toutouyoutou");
        userManager.getSelectedUser().getInstitution().setName("Newinstitut");
        userManager.getSelectedUser().getInstitution().setUrl("http://institutionUrl.com");

        final String registerUser = callRegisterUser(userManager);

        assertEquals(1, numberOfnewinstitutionsCreated());
        assertEquals(1, numberOfnewUserCreated());
        assertEquals("/users/login/login.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_NewInstitution_EmailSendingFails() throws Exception {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        userManager.getSelectedUser().getInstitution().setKeyword("Toutouyoutou");
        userManager.getSelectedUser().getInstitution().setName("Newinstitut");
        userManager.getSelectedUser().getInstitution().setUrl("http://institutionUrl.com");

        final InstitutionType institutionType = InstitutionTypeFactory.createInstitutionTypeWithMandatoryFields();
        userManager.getSelectedUser().getInstitution().setInstitutionType(institutionType);
        userManager.setSelectedInstitution(InstitutionFactory.createInstitutionWithNULLKeyword(institutionType));

        doThrow(new Exception()).when(mockedEmailManager).sendEmail(any(EmailTemplate.class));

        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());

        assertEquals("/users/login/login.seam", registerUser);
    }

    @Test
    public void RegisterUser_toAnExistingInstitution_EmailSendingFails() throws Exception {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final User user = UserFactory.createUserWithAllFields();

        userManager.setSelectedInstitution(user.getInstitution());

        doThrow(new Exception()).when(mockedEmailManager).sendEmail(any(EmailTemplate.class));

        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals("/users/login/login.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_withNewInstitution_UsingExisting_Insitution_name() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final Institution institution = InstitutionFactory.createInstitutionWithMandatoryFields();

        userManager.getSelectedUser().getInstitution().setKeyword("Toutouyoutou");
        userManager.getSelectedUser().getInstitution().setName(institution.getName());
        userManager.getSelectedUser().getInstitution().setUrl("http://institutionUrl.com");

        final InstitutionType institutionType = InstitutionTypeFactory.createInstitutionTypeWithMandatoryFields();
        userManager.getSelectedUser().getInstitution().setInstitutionType(institutionType);
        userManager.setSelectedInstitution(InstitutionFactory.createInstitutionWithNULLKeyword(institutionType));
        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals("/users/user/register.seam", registerUser);
    }

    @Test
    public void RegisterUser_withNewInstitution_UsingExisting_Insitution_keyword() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final Institution institution = InstitutionFactory.createInstitutionWithMandatoryFields();

        userManager.getSelectedUser().getInstitution().setKeyword(institution.getKeyword());
        userManager.getSelectedUser().getInstitution().setName("institution2");
        userManager.getSelectedUser().getInstitution().setUrl("http://institutionUrl.com");

        final InstitutionType institutionType = InstitutionTypeFactory.createInstitutionTypeWithMandatoryFields();
        userManager.getSelectedUser().getInstitution().setInstitutionType(institutionType);
        userManager.setSelectedInstitution(InstitutionFactory.createInstitutionWithNULLKeyword(institutionType));
        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(0, numberOfnewUserCreated());
        assertEquals("/users/user/register.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_withExistingInstitution() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final Institution institution = InstitutionFactory.createInstitutionWithMandatoryFields();
        userManager.setSelectedInstitution(institution);

        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(1, numberOfnewUserCreated());
        assertEquals("/users/login/login.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_toAnExistingInstitution() {

        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final User user = UserFactory.createUserWithAllFields();

        userManager.setSelectedInstitution(user.getInstitution());

        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(1, numberOfnewUserCreated());
        assertEquals("/users/login/login.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_toAnExistingInstitution_VendorAdminRoleUsed_NoUserWithThisRole() {

        RoleFactory.createVendorAdminRole();
        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final User user = UserFactory.createUserWithAllFields();

        userManager.setSelectedInstitution(user.getInstitution());

        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(1, numberOfnewUserCreated());
        assertEquals("/users/login/login.seam", registerUser);
    }

    @Test
    @Ignore
    public void RegisterUser_toAnExistingInstitution_VendorAdminRoleUsed_NoUserWithThisRole_fakedAdminEmail() {

        RoleFactory.createVendorAdminRole();
        final UserManager userManager = new UserManager();
        userManager.registerAccountButton();
        setUser(userManager);

        final User user = UserFactory.createUserWithAllFields();

        userManager.setSelectedInstitution(user.getInstitution());

        when(mockedApplicationManager.getApplicationAdminEmail()).thenReturn(user.getEmail());
        final String registerUser = callRegisterUser(userManager);
        assertEquals(0, numberOfnewinstitutionsCreated());
        assertEquals(1, numberOfnewUserCreated());
        assertEquals("/users/login/login.seam", registerUser);
    }

    private int get_User_Table_Size() {
        final UserQuery query = new UserQuery();
        return query.getList().size();
    }

    private int get_Institution_Table_Size() {
        final InstitutionQuery query = new InstitutionQuery();
        return query.getList().size();
    }

    private int numberOfnewUserCreated() {
        return usersAfterTest - usersBeforeTest;
    }

    private int numberOfnewinstitutionsCreated() {
        return institutionsAfterTest - instituionsBeforTest;
    }

    private void setUser(UserManager userManager) {
        userManager.getSelectedUser().setFirstname("testing");
        userManager.getSelectedUser().setLastname("testingName");
        userManager.getSelectedUser().setEmail("jle@kereval.com");
        userManager.getSelectedUser().setUsername("testing");
        userManager.getSelectedUser().setPassword("123456");
        userManager.getSelectedUser().setPasswordConfirmation("123456");
    }

    private String callRegisterUser(UserManager userManager) {
        final EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        instituionsBeforTest = get_Institution_Table_Size();
        usersBeforeTest = get_User_Table_Size();
        final String registerUser = userManager.registerUser(mockedApplicationManager, mockedEmailManager);
        entityManager.flush();
        entityManager.getTransaction().commit();

        usersAfterTest = get_User_Table_Size();
        institutionsAfterTest = get_Institution_Table_Size();
        return registerUser;
    }
}
