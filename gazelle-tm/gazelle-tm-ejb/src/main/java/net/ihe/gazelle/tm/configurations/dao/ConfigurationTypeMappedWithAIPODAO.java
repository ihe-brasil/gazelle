package net.ihe.gazelle.tm.configurations.dao;

import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tf.ws.data.TFConfigurationTypeWrapper;
import net.ihe.gazelle.tf.ws.data.TFConfigurationTypesWrapper;
import net.ihe.gazelle.tm.configurations.model.ConfigurationTypeMappedWithAIPO;
import net.ihe.gazelle.tm.configurations.model.ConfigurationTypeMappedWithAIPOQuery;
import net.ihe.gazelle.tm.configurations.model.ConfigurationTypeWithPortsWSTypeAndSopClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConfigurationTypeMappedWithAIPODAO {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationTypeMappedWithAIPODAO.class);

    public static TFConfigurationTypesWrapper getTFConfigurationTypesFiltered(Actor actor, IntegrationProfile profile,
                                                                              IntegrationProfileOption option) {
        ConfigurationTypeMappedWithAIPOQuery query = new ConfigurationTypeMappedWithAIPOQuery();
        if (actor != null) {
            query.actorIntegrationProfileOption().actorIntegrationProfile().actor().eq(actor);
        }
        if (profile != null) {
            query.actorIntegrationProfileOption().actorIntegrationProfile().integrationProfile().eq(profile);
        }
        if (option != null) {
            query.actorIntegrationProfileOption().integrationProfileOption().eq(option);
        }
        List<ConfigurationTypeMappedWithAIPO> configurationTypes = query.getListDistinct();
        return toWrapper(configurationTypes);
    }

    private static TFConfigurationTypesWrapper toWrapper(List<ConfigurationTypeMappedWithAIPO> configurations) {
        TFConfigurationTypesWrapper wrapper = new TFConfigurationTypesWrapper();
        for (ConfigurationTypeMappedWithAIPO configType : configurations) {
            String confActorKeyword = configType.getActorIntegrationProfileOption().getActorIntegrationProfile()
                    .getActor().getKeyword();
            String confActorName = configType.getActorIntegrationProfileOption().getActorIntegrationProfile()
                    .getActor().getName();
            String confIntegrationProfileKeyword = configType.getActorIntegrationProfileOption()
                    .getActorIntegrationProfile().getIntegrationProfile().getKeyword();
            String confOptionKeyword = configType.getActorIntegrationProfileOption().getIntegrationProfileOption()
                    .getKeyword();
            for (ConfigurationTypeWithPortsWSTypeAndSopClass fullConfig : configType.getListOfConfigurationTypes()) {
                TFConfigurationTypeWrapper wrapperSingle = new TFConfigurationTypeWrapper();
                wrapperSingle.setActor(confActorName, confActorKeyword);
                wrapperSingle.setIntegrationProfile(confIntegrationProfileKeyword);
                wrapperSingle.setIntegrationProfileOption(confOptionKeyword);
                wrapperSingle.setConfigurationType(fullConfig.getConfigurationType().getTypeName());
                if (fullConfig.getSopClass() != null) {
                    wrapperSingle.setUsage(fullConfig.getSopClass().getKeyword());
                } else if (fullConfig.getWsTRansactionUsage() != null) {
                    wrapperSingle.setUsage(fullConfig.getWsTRansactionUsage().getUsage());
                } else if (fullConfig.getTransportLayer() != null) {
                    wrapperSingle.setUsage(fullConfig.getTransportLayer().getName());
                }
                wrapper.addConfiguration(wrapperSingle);
            }
        }
        return wrapper;
    }

    public static TFConfigurationTypesWrapper getTFConfigurationTypesForAIPOs(List<ActorIntegrationProfileOption> aipos) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTFConfigurationTypesForAIPOs");
        }
        ConfigurationTypeMappedWithAIPOQuery query = new ConfigurationTypeMappedWithAIPOQuery();
        query.actorIntegrationProfileOption().in(aipos);
        List<ConfigurationTypeMappedWithAIPO> configurationTypes = query.getListDistinct();
        return toWrapper(configurationTypes);
    }

}
