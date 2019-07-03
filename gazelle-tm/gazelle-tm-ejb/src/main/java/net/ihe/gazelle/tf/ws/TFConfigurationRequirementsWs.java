package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfileOptionQuery;
import net.ihe.gazelle.tf.ws.data.TFConfigurationTypesWrapper;
import net.ihe.gazelle.tm.configurations.dao.ConfigurationTypeMappedWithAIPODAO;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;

@Stateless
@Name("tfConfigurationRequirementsWs")
public class TFConfigurationRequirementsWs implements TFConfigurationRequirementsWSApi {

    private static final String ALL = "ALL";

    @Override
    public TFConfigurationTypesWrapper listTFConfigurationsForAIPO(String actorKeyword,
                                                                   String integrationprofileKeyword, String optionKeyword) {
        TFConfigurationTypesWrapper wrapper = new TFConfigurationTypesWrapper();
        Actor actor = null;
        IntegrationProfile profile = null;
        IntegrationProfileOption option = null;
        if (!actorKeyword.isEmpty() && !actorKeyword.equalsIgnoreCase(ALL)) {
            actor = Actor.findActorWithKeyword(actorKeyword);
            if (actor == null) {
                return wrapper;
            }
        }
        if (!integrationprofileKeyword.isEmpty() && !integrationprofileKeyword.equalsIgnoreCase(ALL)) {
            profile = IntegrationProfile.findIntegrationProfileWithKeyword(integrationprofileKeyword);
            if (profile == null) {
                return wrapper;
            }
        }
        if (!optionKeyword.isEmpty() && !optionKeyword.equalsIgnoreCase(ALL)) {
            IntegrationProfileOptionQuery query = new IntegrationProfileOptionQuery();
            query.keyword().eq(optionKeyword);
            option = query.getUniqueResult();
            if (option == null) {
                return wrapper;
            }
        }
        return ConfigurationTypeMappedWithAIPODAO.getTFConfigurationTypesFiltered(actor, profile, option);
    }
}
