package net.ihe.gazelle.menu;

import net.ihe.gazelle.common.pages.menu.Menu;
import net.ihe.gazelle.common.pages.menu.MenuEntry;
import net.ihe.gazelle.common.pages.menu.MenuGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GazelleMenu {

    private static final Logger LOG = LoggerFactory.getLogger(GazelleMenu.class);
    private static MenuGroup MENU = null;

    private GazelleMenu() {
        super();
    }

    public static final MenuGroup getMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("final MenuGroup getMenu");
        }
        if (MENU == null) {
            synchronized (GazelleMenu.class) {
                if (MENU == null) {
                    ArrayList<Menu> children = new ArrayList<Menu>();

                    children.add(getRegistration());

                    children.add(new MenuEntry(Pages.PR_INTRO));
                    children.add(new MenuEntry(Pages.PR_SEARCH));
                    children.add(new MenuEntry(Pages.PR_STATS));

                    children.add(getTechnicalFramework());
                    children.add(getTestDefinitions());
                    children.add(getConfigurations());

                    children.add(getConnectathon());

                    children.add(getAdministration());

                    MENU = new MenuGroup(null, children);
                }
            }
        }
        return MENU;
    }

    public static final MenuGroup refreshMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("final MenuGroup refreshMenu");
        }
        MENU = null;
        return getMenu();
    }

    private static Menu getHome() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.CHANGE_TESTING_SESSION));
        children.add(new MenuEntry(Pages.SEARCH));
        children.add(new MenuEntry(Pages.USER_PREFERENCES));

        MenuGroup result = new MenuGroup(Pages.HOME, children);
        return result;
    }

    private static Menu getRegistration() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.REGISTRATION_COMPANY));
        children.add(new MenuEntry(Pages.REGISTRATION_SYSTEMS));
        children.add(new MenuEntry(Pages.REGISTRATION_OVERVIEW));
        children.add(new MenuEntry(Pages.REGISTRATION_USERS));
        children.add(new MenuEntry(Pages.REGISTRATION_CONTACTS));
        children.add(new MenuEntry(Pages.REGISTRATION_PARTICIPANTS));
        children.add(new MenuEntry(Pages.REGISTRATION_FINANCIAL));
        children.add(new MenuEntry(Pages.REGISTRATION_DEMONSTRATIONS));

        MenuGroup result = new MenuGroup(Pages.REGISTRATION, children);
        return result;
    }

    private static Menu getTechnicalFramework() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.TF_OVERVIEW));
        children.add(new MenuEntry(Pages.TF_DOMAINS));
        children.add(new MenuEntry(Pages.TF_PROFILES));
        children.add(new MenuEntry(Pages.TF_ACTORS));
        children.add(new MenuEntry(Pages.TF_AUDIT_MESSAGES));
        children.add(new MenuEntry(Pages.TF_OPTIONS));
        children.add(new MenuEntry(Pages.TF_DOCUMENTS));
        children.add(new MenuEntry(Pages.TF_TRANSACTIONS));
        children.add(new MenuEntry(Pages.TF_TRANSACTIONS_OPTION_TYPES));
        children.add(new MenuEntry(Pages.TF_HL7V2_MESSAGE_PROFILES));
        children.add(new MenuEntry(Pages.TF_WEBSERVICE_TRANSACTION_USAGE));
        children.add(new MenuEntry(Pages.TF_CONF_DEPENDING_AIPO));
        children.add(new MenuEntry(Pages.TF_RULES));
        children.add(new MenuEntry(Pages.TF_STANDARDS));

        MenuGroup result = new MenuGroup(Pages.TF, children);
        return result;

    }

    private static Menu getTestDefinitions() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.TD_LIST_TESTS));
        children.add(new MenuEntry(Pages.TD_LIST_METATESTS));
        children.add(new MenuEntry(Pages.TD_ROLE_IN_TEST));
        children.add(new MenuEntry(Pages.TD_TEST_REQUIREMENTS));
        children.add(new MenuEntry(Pages.TD_PATHS));

        MenuGroup result = new MenuGroup(Pages.TD, children);
        return result;

    }

    private static Menu getConfigurations() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.CONFIG_NETWORK_OVERVIEW));
        children.add(new MenuEntry(Pages.CONFIG_LIST_VENDOR));
        children.add(new MenuEntry(Pages.CONFIG_ALL));
        children.add(new MenuEntry(Pages.CONFIG_OIDS));
        children.add(new MenuEntry(Pages.CONFIG_CERTIFICATES));
        MenuGroup result = new MenuGroup(Pages.CONFIG, children);
        return result;
    }

    private static Menu getConnectathon() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.CAT_SYSTEMS));

        children.add(getPreCatSubMenu());
        children.add(getCatSubMenu());
        children.add(getInteroperabilitySubMenu());

        MenuGroup result = new MenuGroup(Pages.CAT, children);
        return result;
    }

    private static Menu getPreCatSubMenu() {
        List<Menu> children = new ArrayList<Menu>();
        addPreCatEntries(children);

        MenuGroup result = new MenuGroup(Pages.CAT_PRECAT_MENU, children);
        return result;
    }

    private static void addPreCatEntries(List<Menu> children) {
        children.add(new MenuEntry(Pages.CAT_PRECAT));
        children.add(new MenuEntry(Pages.CAT_PRECAT_VALIDATION));
    }

    private static Menu getInteroperabilitySubMenu() {
        List<Menu> children = new ArrayList<Menu>();
        addInteroperabilityEntries(children);
        MenuGroup result = new MenuGroup(Pages.INTEROP_MENU, children);
        return result;
    }

    private static void addInteroperabilityEntries(List<Menu> children) {
        children.add(new MenuEntry(Pages.INTEROP_DASHBOARD));
        children.add(new MenuEntry(Pages.INTEROP_INSTANCES));
        children.add(new MenuEntry(Pages.INTEROP_RESULTS));
        children.add(new MenuEntry(Pages.INTEROP_REPORT));
    }

    private static Menu getCatSubMenu() {
        List<Menu> children = new ArrayList<Menu>();
        addCatEntries(children);

        MenuGroup result = new MenuGroup(Pages.CAT_MENU, children);
        return result;
    }

    private static void addCatEntries(List<Menu> children) {
        children.add(new MenuEntry(Pages.CAT_OVERVIEW));
        children.add(new MenuEntry(Pages.CAT_INSTANCES));
        children.add(new MenuEntry(Pages.CAT_MONITOR_WORKLIST));
        children.add(new MenuEntry(Pages.CAT_RESULTS));
        children.add(new MenuEntry(Pages.CAT_REPORT_VENDOR));
        children.add(new MenuEntry(Pages.CAT_MONITORS));
        children.add(new MenuEntry(Pages.CAT_SAMPLES));
        children.add(new MenuEntry(Pages.CAT_SAMPLES_SEARCH));
    }

    private static Menu getAdministration() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_PR_VALIDATE));
        children.add(new MenuEntry(Pages.ADMIN_PR_CRAWLER));

        children.add(getAdministrationManage());
        children.add(getAdministrationCheck());
        children.add(getAdministrationKPI());
        children.add(new MenuEntry(Pages.ADMIN_USAGE));
        children.add(new MenuEntry(Pages.ADMIN_PREFERENCES));

        MenuGroup result = new MenuGroup(Pages.ADMIN, children);
        return result;
    }

    private static Menu getAdministrationManage() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_MANAGE_SESSIONS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_DEMONSTRATIONS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_COMPANIES));
        children.add(getAdministrationManageSystems());
        children.add(getAdministrationManageConfigs());
        children.add(getAdministrationSamples());
        children.add(new MenuEntry(Pages.ADMIN_REGISTRATION_OVERVIEW));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_USERS));
        children.add(getAdministrationManageMonitors());
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_CONTACTS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_PARTICIPANTS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_INVOICES));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_FINANCIAL_SUMMARY));

        MenuGroup result = new MenuGroup(Pages.ADMIN_MANAGE, children);
        return result;
    }

    private static Menu getAdministrationManageSystems() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.REGISTRATION_SYSTEMS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_SYSTEMS_LIST));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_SYSTEMS_SIMULATORS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_SYSTEMS_ACCEPTATION));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_SYSTEMS_TEST_TYPES));

        MenuGroup result = new MenuGroup(Pages.ADMIN_MANAGE_SYSTEMS, children);
        return result;
    }

    private static Menu getAdministrationManageConfigs() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_MANAGE_CONFIGS_HOSTS));
        children.add(new MenuEntry(Pages.CONFIG_ALL));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_CONFIGS_OIDS));
        children.add(new MenuEntry(Pages.ADMIN_MANAGE_OIDS_LABELS));

        MenuGroup result = new MenuGroup(Pages.ADMIN_MANAGE_CONFIGS, children);
        return result;
    }

    private static Menu getAdministrationCheck() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_CHECK_TF));
        children.add(new MenuEntry(Pages.ADMIN_CHECK_TD));
        children.add(new MenuEntry(Pages.ADMIN_CHECK_REGISTRATION));
        children.add(new MenuEntry(Pages.ADMIN_CHECK_SESSION));
        children.add(new MenuEntry(Pages.ADMIN_CHECK_CONF));

        MenuGroup result = new MenuGroup(Pages.ADMIN_CHECK, children);
        return result;
    }

    private static Menu getAdministrationKPI() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_KPI_TESTSINSTANCES));
        children.add(new MenuEntry(Pages.ADMIN_KPI_TESTS));
        children.add(new MenuEntry(Pages.ADMIN_KPI_SYSTEMS));
        children.add(new MenuEntry(Pages.ADMIN_KPI_MONITORS));

        MenuGroup result = new MenuGroup(Pages.ADMIN_KPI, children);
        return result;
    }

    private static Menu getAdministrationSamples() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_SAMPLES_OBJECT_TYPE));
        children.add(new MenuEntry(Pages.ADMIN_SAMPLES_ANNOTATIONS));

        MenuGroup result = new MenuGroup(Pages.ADMIN_SAMPLES, children);
        return result;
    }

    private static Menu getAdministrationManageMonitors() {
        List<Menu> children = new ArrayList<Menu>();

        children.add(new MenuEntry(Pages.ADMIN_MANAGE_MONITORS));
        children.add(new MenuEntry(Pages.MONITORS_ADDMONITORTOTEST));

        MenuGroup result = new MenuGroup(Pages.ADMIN_MANAGE_MONITORS, children);
        return result;
    }

}
