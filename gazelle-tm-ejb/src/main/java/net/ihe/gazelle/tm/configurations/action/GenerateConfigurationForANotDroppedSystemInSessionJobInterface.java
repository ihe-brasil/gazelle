package net.ihe.gazelle.tm.configurations.action;

import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.annotations.async.Asynchronous;

import javax.ejb.Local;

@Local
public interface GenerateConfigurationForANotDroppedSystemInSessionJobInterface {

    @Asynchronous
    public void generateConfigurationForANotDroppedSystemInSessionJob(SystemInSession sIs, TestingSession testingSession);
}
