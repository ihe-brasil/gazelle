package net.ihe.gazelle.tm.systems.ws;

import net.ihe.gazelle.tf.ws.data.InstitutionWrapper;
import net.ihe.gazelle.tm.systems.model.InstitutionSystem;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSessionRegistrationStatus;
import net.ihe.gazelle.tm.ws.data.SystemInSessionWrapper;
import net.ihe.gazelle.tm.ws.data.SystemsInSessionWrapper;
import net.ihe.gazelle.tm.ws.data.TestingSessionWrapper;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.model.UserQuery;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("systemInfoWs")
public class SystemInfoWs implements SystemInfoWsApi {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInfoWs.class);

    @Override
    public SystemsInSessionWrapper getSystemsForTestingSessionsForUser(String username, String apiKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsForTestingSessionsForUser");
        }
        SystemsInSessionWrapper wrapper = new SystemsInSessionWrapper();
        User user = null;
        if (username.isEmpty()) {
            return wrapper;
        } else {
            user = User.FindUserWithUsername(username);
            if (user == null) {
                LOG.error(username + " is not recorded");
                return wrapper;
            }
        }
        SystemInSessionQuery query = new SystemInSessionQuery();
        query.registrationStatus().eq(SystemInSessionRegistrationStatus.COMPLETED);
        query.testingSession().sessionClosed().eq(false);
        query.testingSession().activeSession().eq(true);
        query.system().institutionSystems().institution().eq(user.getInstitution());
        query.system().systemActorProfiles().actorIntegrationProfileOption().actorIntegrationProfile()
                .integrationProfile().keyword().eq("ATNA");
        List<SystemInSession> systems = query.getList();

        for (SystemInSession sis : systems) {

            List<InstitutionWrapper> institutions = new ArrayList<InstitutionWrapper>();
            for (InstitutionSystem sysInstitution : sis.getSystem().getInstitutionSystems()) {
                institutions.add(new InstitutionWrapper(sysInstitution.getInstitution().getName(), sysInstitution
                        .getInstitution().getKeyword()));
            }
            SystemInSessionWrapper single = new SystemInSessionWrapper(new TestingSessionWrapper(sis
                    .getTestingSession().getId(), sis.getTestingSession().getDescription()), institutions, sis
                    .getSystem().getKeyword());
            wrapper.addSystemInSessionWrapper(single);
        }
        return wrapper;
    }

    @Override
    public String getInstitutionKeywordForUser(String username, String apiKey) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionKeywordForUser");
        }
        UserQuery query = new UserQuery();
        query.username().eq(username);
        List<Institution> institutions = query.institution().getListDistinct();
        if (institutions != null && !institutions.isEmpty()) {
            return institutions.get(0).getKeyword();
        } else {
            return null;
        }
    }
}
