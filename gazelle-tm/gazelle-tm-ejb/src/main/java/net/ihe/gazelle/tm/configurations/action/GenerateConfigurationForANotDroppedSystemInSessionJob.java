package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.systems.model.InstitutionSystem;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionRegistrationStatus;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Stateless
@Name("generateConfigurationForANotDroppedSystemInSessionJob")
public class GenerateConfigurationForANotDroppedSystemInSessionJob implements
        GenerateConfigurationForANotDroppedSystemInSessionJobInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateConfigurationForANotDroppedSystemInSessionJob.class);

    public void generateConfigurationForANotDroppedSystemInSessionJob(SystemInSession sIs, TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigurationForANotDroppedSystemInSessionJob");
        }
        if (sIs.getRegistrationStatus() == null
                || !sIs.getRegistrationStatus().equals(SystemInSessionRegistrationStatus.DROPPED)) {
            Host host = null;
            List<Host> listOfHost = Host.getListOfHostForASystemInSession(sIs);
            Host hostToUse = Host.getAvailableHostForASystemInSession(listOfHost, sIs);
            if ((listOfHost.size() == 0 || hostToUse == null)) {
                if (hostToUse != null) {
                    host = hostToUse;
                } else {
                    Set<InstitutionSystem> institutionSystems = sIs.getSystem().getInstitutionSystems();
                    Institution institution = null;
                    if (institutionSystems.size() > 0) {
                        Iterator<InstitutionSystem> iterator = institutionSystems.iterator();
                        if (iterator.hasNext()) {
                            institution = iterator.next().getInstitution();
                        }
                    }
                    if (institution != null) {
                        host = Host.generateNewHostForSessionWithPersistance(institution, sIs);
                    }
                }
            } else {
                host = listOfHost.get(0);
            }
            GenerateSystemConfiguration gsc = new GenerateSystemConfiguration();

            gsc.generateSystemConfigurationsOnly(host, sIs, testingSession);
        }
    }
}
