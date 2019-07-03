package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import net.ihe.gazelle.tm.configurations.model.AbstractConfigurationQuery;
import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.configurations.model.HostQuery;
import net.ihe.gazelle.tm.configurations.model.interfaces.ServerConfiguration;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Name("configurationsAdminChecks")
@Scope(ScopeType.PAGE)
public class ConfigurationsAdminChecks {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsAdminChecks.class);

    private String ipToTest;
    private List<Host> hostsAvailable = new ArrayList<>();
    private List<Host> duplicatesIpsList;
    private List<ServerConfiguration> duplicatesConfsList;

    public List<Host> getDuplicatesIpsList() {
        return duplicatesIpsList;
    }

    public List<ServerConfiguration> getDuplicatesConfsList() {
        return duplicatesConfsList;
    }

    public String getIpToTest() {
        return ipToTest;
    }

    public void setIpToTest(String ipToTest) {
        this.ipToTest = ipToTest.trim();
    }

    public List<Host> getHostsAvailable() {
        return hostsAvailable;
    }

    public void setHostsAvailable(List<Host> hostsAvailable) {
        this.hostsAvailable = hostsAvailable;
    }

    public GazelleListDataModel<ServerConfiguration> getDuplicatesConfsListDM() {
        return new GazelleListDataModel<ServerConfiguration>(getDuplicatesConfsList());
    }

    public GazelleListDataModel<Host> getDuplicatesIpsListDM() {
        return new GazelleListDataModel<Host>(getDuplicatesIpsList());
    }

    public boolean isIPAvailable() {
        return isIPAvailable(ipToTest);
    }

    private boolean isIPAvailable(String ipToTest) {
        HostQuery q = new HostQuery();
        q.testingSession().eq(TestingSession.getSelectedTestingSession());
        q.ip().eq(ipToTest);
        hostsAvailable = q.getListNullIfEmpty();
        return hostsAvailable == null;
    }

    public void checkIpIsUniqueInCurrentTestingSession() {
        HostQuery query = new HostQuery();
        query.testingSession().eq(TestingSession.getSelectedTestingSession());
        List<Host> hosts = query.getList();
        List<Host> duplicateHosts = new ArrayList<>();
        Map<Host, String> ipsList = new HashMap<>();
        for (Host host : hosts) {
            ipsList.put(host, host.getIp());
        }
        Map<Host, String> res = findDuplicates(ipsList);
        for (Host host : hosts) {
            String ip = host.getIp();
            for (String duplicateIp : res.values()) {
                if (duplicateIp!=null && ip != null && ip.equals(duplicateIp)) {
                    duplicateHosts.add(host);
                }
            }
        }
        duplicatesIpsList = new ArrayList<>(duplicateHosts);
    }

//    public void checkIpIsUniqueInCurrentTestingSession2() {
//        AbstractConfigurationQuery query = new AbstractConfigurationQuery();
//        query.configuration().systemInSession().testingSession().eq(TestingSession
//                .getSelectedTestingSession());
//        List<AbstractConfiguration> configurations = query.getList();
//        List<AbstractConfiguration> duplicatesConfigurations = new ArrayList<>();
//        Map<AbstractConfiguration, String> iPsUsed = new HashMap<>();
//        for (AbstractConfiguration abstractConfiguration : configurations) {
//            if (abstractConfiguration.getConfiguration().getHost().getIp() != null) {
//                iPsUsed.put(abstractConfiguration, abstractConfiguration.getConfiguration().getHost().getIp());
//            }
//        }
//        Map<AbstractConfiguration, String> res = findDuplicates(iPsUsed);
//
//        for (AbstractConfiguration abstractConfiguration : configurations) {
//            String ip = abstractConfiguration.getConfiguration().getHost().getIp();
//            for (String duplicateIp : res.values()) {
//                if (duplicateIp != null && ip != null && ip.equals(duplicateIp)) {
//                    duplicatesConfigurations.add(abstractConfiguration);
//                }
//            }
//        }
//        duplicatesIpsList = new ArrayList<>(duplicatesConfigurations);
//    }
//
//    public void checkProxyPortIsUniqueInCurrentTestingSession() {
//        AbstractConfigurationQuery query = new AbstractConfigurationQuery();
//        query.configuration().systemInSession().testingSession().eq(TestingSession
//                .getSelectedTestingSession());
//        List<AbstractConfiguration> configurations = query.getList();
//        Map<ServerConfiguration, Integer> portsUsed = new HashMap<>();
//        for (AbstractConfiguration abstractConfiguration : configurations) {
//            if (abstractConfiguration instanceof ServerConfiguration) {
//                Integer portProxy = ((ServerConfiguration) abstractConfiguration).getPortProxy();
//                if (portProxy != null) {
//                    portsUsed.put(((ServerConfiguration) abstractConfiguration), portProxy);
//                }
//            }
//        }
//        Map<ServerConfiguration, Integer> res = findDuplicates(portsUsed);
//        duplicatesConfsList = new ArrayList<>();
//        duplicatesConfsList.addAll(res.keySet());
//    }

    public void checkProxyPortIsUniqueInCurrentTestingSession() {
        AbstractConfigurationQuery query = new AbstractConfigurationQuery();
        query.configuration().systemInSession().testingSession().eq(TestingSession
                .getSelectedTestingSession());
        query.configuration().isSecured().eq(false);
        List<AbstractConfiguration> configurations = query.getList();
        List<ServerConfiguration> duplicatesConfigurations = new ArrayList<>();

        Map<ServerConfiguration, Integer> portsUsed = new HashMap<>();
        for (AbstractConfiguration abstractConfiguration : configurations) {
            if (abstractConfiguration instanceof ServerConfiguration) {
                Integer portProxy = ((ServerConfiguration) abstractConfiguration).getPortProxy();
                if (portProxy != null) {
                    portsUsed.put(((ServerConfiguration) abstractConfiguration), portProxy);
                }
            }
        }
        Map<ServerConfiguration, Integer> res = findDuplicates(portsUsed);


        for (AbstractConfiguration abstractConfiguration : configurations) {
            if (abstractConfiguration instanceof ServerConfiguration) {
                Integer portProxy = ((ServerConfiguration) abstractConfiguration).getPortProxy();
                for (Integer duplicateProxyPort : res.values()) {
                    if (portProxy != null && duplicateProxyPort != null && portProxy.equals(duplicateProxyPort)) {
                        duplicatesConfigurations.add(((ServerConfiguration) abstractConfiguration));
                    }
                }

            }
        }
        duplicatesConfsList = new ArrayList<>(duplicatesConfigurations);
    }

    private <K, V> Map<K, V> findDuplicates(Map<K, V> list) {
        Map<K, V> duplicates = new LinkedHashMap<>();
        Set<V> uniques = new HashSet<V>();
        for (Map.Entry<K, V> ttEntry : list.entrySet()) {
            if (!uniques.add(ttEntry.getValue())) {
                duplicates.put(ttEntry.getKey(), ttEntry.getValue());
            }
        }
        return duplicates;
    }


}
