package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.simulator.ws.Hl7V2InitiatorConfiguration;
import net.ihe.gazelle.simulator.ws.Hl7V2ResponderConfiguration;
import net.ihe.gazelle.simulator.ws.Hl7V3InitiatorConfiguration;
import net.ihe.gazelle.simulator.ws.Hl7V3ResponderConfiguration;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.configurations.model.*;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.gazelletest.model.definition.ContextualInformation;
import net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance;

import java.util.ArrayList;
import java.util.List;

public class Gazelle2SimulatorClassConverter {

    public static net.ihe.gazelle.simulator.ws.Actor convertActor(Actor inActor) {
        if (inActor != null) {
            net.ihe.gazelle.simulator.ws.Actor actor = new net.ihe.gazelle.simulator.ws.Actor();
            actor.setName(inActor.getName());
            actor.setKeyword(inActor.getKeyword());
            actor.setDescription(inActor.getDescription());
            return actor;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.IntegrationProfile convertIntegrationProfile(
            IntegrationProfile inIntegrationProfile) {
        if (inIntegrationProfile != null) {
            net.ihe.gazelle.simulator.ws.IntegrationProfile integrationProfile = new net.ihe.gazelle.simulator.ws.IntegrationProfile();
            integrationProfile.setName(inIntegrationProfile.getName());
            integrationProfile.setKeyword(inIntegrationProfile.getKeyword());
            integrationProfile.setDescription(inIntegrationProfile.getDescription());
            return integrationProfile;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.IntegrationProfileOption convertIntegrationProfileOption(
            IntegrationProfileOption inIntegrationProfileOption) {
        if (inIntegrationProfileOption != null) {
            net.ihe.gazelle.simulator.ws.IntegrationProfileOption integrationProfileOption = new net.ihe.gazelle.simulator.ws
                    .IntegrationProfileOption();
            integrationProfileOption.setName(inIntegrationProfileOption.getName());
            integrationProfileOption.setKeyword(inIntegrationProfileOption.getKeyword());
            integrationProfileOption.setDescription(inIntegrationProfileOption.getDescription());
            return integrationProfileOption;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.Transaction convertTransaction(Transaction inTransaction) {
        if (inTransaction != null) {
            net.ihe.gazelle.simulator.ws.Transaction transaction = new net.ihe.gazelle.simulator.ws.Transaction();
            transaction.setName(inTransaction.getName());
            transaction.setKeyword(inTransaction.getKeyword());
            transaction.setDescription(inTransaction.getDescription());
            return transaction;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.ActorIntegrationProfile convertActorIntegrationProfile(
            ActorIntegrationProfile inActorIntegrationProfile) {
        if (inActorIntegrationProfile != null) {
            net.ihe.gazelle.simulator.ws.ActorIntegrationProfile actorIntegrationProfile = new net.ihe.gazelle.simulator.ws.ActorIntegrationProfile();
            actorIntegrationProfile.setActor(convertActor(inActorIntegrationProfile.getActor()));
            actorIntegrationProfile.setIntegrationProfile(convertIntegrationProfile(inActorIntegrationProfile
                    .getIntegrationProfile()));
            return actorIntegrationProfile;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.ActorIntegrationProfileOption convertActorIntegrationProfileOption(
            ActorIntegrationProfileOption inActorIntegrationProfileOption) {
        if (inActorIntegrationProfileOption != null) {
            net.ihe.gazelle.simulator.ws.ActorIntegrationProfileOption actorIntegrationProfileOption = new net.ihe.gazelle.simulator.ws
                    .ActorIntegrationProfileOption();
            actorIntegrationProfileOption
                    .setActorIntegrationProfile(convertActorIntegrationProfile(inActorIntegrationProfileOption
                            .getActorIntegrationProfile()));
            actorIntegrationProfileOption
                    .setIntegrationProfileOption(convertIntegrationProfileOption(inActorIntegrationProfileOption
                            .getIntegrationProfileOption()));
            return actorIntegrationProfileOption;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.Host convertHost(Host inHost) {
        if (inHost != null) {
            net.ihe.gazelle.simulator.ws.Host host = new net.ihe.gazelle.simulator.ws.Host();
            host.setAlias(inHost.getAlias());
            host.setComment(inHost.getComment());
            host.setHostname(inHost.getHostname());
            host.setIp(inHost.getIp());
            return host;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.Configuration convertConfiguration(Configuration inConfiguration) {
        if (inConfiguration != null) {
            net.ihe.gazelle.simulator.ws.Configuration configuration = new net.ihe.gazelle.simulator.ws.Configuration();
            configuration.setComment(inConfiguration.getComment());
            configuration.setHost(convertHost(inConfiguration.getHost()));
            configuration.setIsApproved(inConfiguration.getIsApproved());
            configuration.setIsSecured(inConfiguration.getIsSecured());
            return configuration;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.SopClass convertSOPClass(SopClass inSOPClass) {
        if (inSOPClass != null) {
            net.ihe.gazelle.simulator.ws.SopClass sopClass = new net.ihe.gazelle.simulator.ws.SopClass();
            sopClass.setKeyword(inSOPClass.getKeyword());
            sopClass.setName(inSOPClass.getKeyword());
            return sopClass;
        }
        return null;
    }

    public static Hl7V2InitiatorConfiguration convertHl7V2InitiatorConfiguration(
            HL7V2InitiatorConfiguration inHl7v2InitiatorConfiguration) {
        if (inHl7v2InitiatorConfiguration != null) {
            Hl7V2InitiatorConfiguration hl7v2InitiatorConfiguration = new Hl7V2InitiatorConfiguration();
            hl7v2InitiatorConfiguration.setAssigningAuthority(inHl7v2InitiatorConfiguration.getAssigningAuthority());
            hl7v2InitiatorConfiguration.setComments(inHl7v2InitiatorConfiguration.getComments());
            hl7v2InitiatorConfiguration.setConfiguration(convertConfiguration(inHl7v2InitiatorConfiguration
                    .getConfiguration()));
            hl7v2InitiatorConfiguration.setSendingReceivingApplication(inHl7v2InitiatorConfiguration
                    .getSendingReceivingApplication());
            hl7v2InitiatorConfiguration.setSendingReceivingFacility(inHl7v2InitiatorConfiguration
                    .getSendingReceivingFacility());
            return hl7v2InitiatorConfiguration;
        }
        return null;
    }

    public static Hl7V2ResponderConfiguration convertHl7V2ResponderConfiguration(
            HL7V2ResponderConfiguration inHl7v2ResponderConfiguration, boolean proxyUsed) {
        if (inHl7v2ResponderConfiguration != null) {
            Hl7V2ResponderConfiguration hl7v2ResponderConfiguration = new Hl7V2ResponderConfiguration();
            hl7v2ResponderConfiguration.setAssigningAuthority(inHl7v2ResponderConfiguration.getAssigningAuthority());
            hl7v2ResponderConfiguration.setComments(inHl7v2ResponderConfiguration.getComments());
            hl7v2ResponderConfiguration.setConfiguration(convertConfiguration(inHl7v2ResponderConfiguration
                    .getConfiguration()));
            hl7v2ResponderConfiguration.setSendingReceivingApplication(inHl7v2ResponderConfiguration
                    .getSendingReceivingApplication());
            hl7v2ResponderConfiguration.setSendingReceivingFacility(inHl7v2ResponderConfiguration
                    .getSendingReceivingFacility());
            if (inHl7v2ResponderConfiguration.getPort() != null) {
                hl7v2ResponderConfiguration.setPort(inHl7v2ResponderConfiguration.getPort());
            }
            if (inHl7v2ResponderConfiguration.getPortOut() != null) {
                hl7v2ResponderConfiguration.setPortOut(inHl7v2ResponderConfiguration.getPortOut());
            }

            if (inHl7v2ResponderConfiguration.getPortSecured() != null) {
                hl7v2ResponderConfiguration.setPortSecured(inHl7v2ResponderConfiguration.getPortSecured());
            }

            if (proxyUsed) {
                if (inHl7v2ResponderConfiguration.getPortProxy() != null) {
                    hl7v2ResponderConfiguration.setPort(inHl7v2ResponderConfiguration.getPortProxy());
                }
            }
            return hl7v2ResponderConfiguration;
        }
        return null;
    }

    public static Hl7V3InitiatorConfiguration convertHl7V3InitiatorConfiguration(
            HL7V3InitiatorConfiguration inHl7v3InitiatorConfiguration) {
        if (inHl7v3InitiatorConfiguration != null) {
            Hl7V3InitiatorConfiguration hl7v3InitiatorConfiguration = new Hl7V3InitiatorConfiguration();
            hl7v3InitiatorConfiguration.setAssigningAuthority(inHl7v3InitiatorConfiguration.getAssigningAuthority());
            hl7v3InitiatorConfiguration.setComments(inHl7v3InitiatorConfiguration.getComments());
            hl7v3InitiatorConfiguration.setConfiguration(convertConfiguration(inHl7v3InitiatorConfiguration
                    .getConfiguration()));
            hl7v3InitiatorConfiguration.setSendingReceivingApplication(inHl7v3InitiatorConfiguration
                    .getSendingReceivingApplication());
            hl7v3InitiatorConfiguration.setSendingReceivingFacility(inHl7v3InitiatorConfiguration
                    .getSendingReceivingFacility());
            return hl7v3InitiatorConfiguration;
        }
        return null;
    }

    public static Hl7V3ResponderConfiguration convertHl7V3ResponderConfiguration(
            HL7V3ResponderConfiguration inHl7v3ResponderConfiguration, boolean proxyUsed) {
        if (inHl7v3ResponderConfiguration != null) {
            Hl7V3ResponderConfiguration hl7v3ResponderConfiguration = new Hl7V3ResponderConfiguration();
            hl7v3ResponderConfiguration.setAssigningAuthority(inHl7v3ResponderConfiguration.getAssigningAuthority());
            hl7v3ResponderConfiguration.setComments(inHl7v3ResponderConfiguration.getComments());
            hl7v3ResponderConfiguration.setConfiguration(convertConfiguration(inHl7v3ResponderConfiguration
                    .getConfiguration()));
            hl7v3ResponderConfiguration.setSendingReceivingApplication(inHl7v3ResponderConfiguration
                    .getSendingReceivingApplication());
            hl7v3ResponderConfiguration.setSendingReceivingFacility(inHl7v3ResponderConfiguration
                    .getSendingReceivingFacility());
            if (inHl7v3ResponderConfiguration.getPort() != null) {
                hl7v3ResponderConfiguration.setPort(inHl7v3ResponderConfiguration.getPort());
            }

            if (inHl7v3ResponderConfiguration.getPortSecured() != null) {
                hl7v3ResponderConfiguration.setPortSecured(inHl7v3ResponderConfiguration.getPortSecured());
            }
            hl7v3ResponderConfiguration.setUrl(inHl7v3ResponderConfiguration.getUrl());
            hl7v3ResponderConfiguration.setUsage(inHl7v3ResponderConfiguration.getUsage());

            if (proxyUsed) {
                if (inHl7v3ResponderConfiguration.getPortProxy() != null) {
                    hl7v3ResponderConfiguration.setPort(inHl7v3ResponderConfiguration.getPortProxy());
                }
            }

            return hl7v3ResponderConfiguration;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.DicomSCUConfiguration convertDICOMSCUConfiguration(
            DicomSCUConfiguration inDicomSCUConfiguration, boolean proxyUsed) {
        if (inDicomSCUConfiguration != null) {
            net.ihe.gazelle.simulator.ws.DicomSCUConfiguration dicomSCUConfiguration = new net.ihe.gazelle.simulator.ws.DicomSCUConfiguration();
            dicomSCUConfiguration.setConfiguration(convertConfiguration(inDicomSCUConfiguration.getConfiguration()));
            dicomSCUConfiguration.setSopClass(convertSOPClass(inDicomSCUConfiguration.getSopClass()));
            dicomSCUConfiguration.setAeTitle(inDicomSCUConfiguration.getAeTitle());
            dicomSCUConfiguration.setComments(inDicomSCUConfiguration.getComments());
            dicomSCUConfiguration.setModalityType(inDicomSCUConfiguration.getModalityType());
            dicomSCUConfiguration.setPort(inDicomSCUConfiguration.getPort());
            dicomSCUConfiguration.setPortSecured(inDicomSCUConfiguration.getPortSecured());
            dicomSCUConfiguration.setTransferRole(inDicomSCUConfiguration.getTransferRole());

            return dicomSCUConfiguration;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.DicomSCPConfiguration convertDICOMSCPConfiguration(
            DicomSCPConfiguration inDicomSCPConfiguration, boolean proxyUsed) {
        if (inDicomSCPConfiguration != null) {
            net.ihe.gazelle.simulator.ws.DicomSCPConfiguration dicomSCPConfiguration = new net.ihe.gazelle.simulator.ws.DicomSCPConfiguration();
            dicomSCPConfiguration.setConfiguration(convertConfiguration(inDicomSCPConfiguration.getConfiguration()));
            dicomSCPConfiguration.setSopClass(convertSOPClass(inDicomSCPConfiguration.getSopClass()));
            dicomSCPConfiguration.setAeTitle(inDicomSCPConfiguration.getAeTitle());
            dicomSCPConfiguration.setComments(inDicomSCPConfiguration.getComments());
            dicomSCPConfiguration.setModalityType(inDicomSCPConfiguration.getModalityType());
            dicomSCPConfiguration.setPort(inDicomSCPConfiguration.getPort());
            if (inDicomSCPConfiguration.getPortSecured() != null) {
                dicomSCPConfiguration.setPortSecured(inDicomSCPConfiguration.getPortSecured());
            }
            if (inDicomSCPConfiguration.getTransferRole() != null) {
                dicomSCPConfiguration.setTransferRole(inDicomSCPConfiguration.getTransferRole());
            }

            return dicomSCPConfiguration;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.SyslogConfiguration convertSyslogConfiguration(
            SyslogConfiguration inSyslogConfiguration, boolean proxyUsed) {
        if (inSyslogConfiguration != null) {
            net.ihe.gazelle.simulator.ws.SyslogConfiguration syslogConfiguration = new net.ihe.gazelle.simulator.ws.SyslogConfiguration();
            syslogConfiguration.setConfiguration(convertConfiguration(inSyslogConfiguration.getConfiguration()));
            syslogConfiguration.setComments(inSyslogConfiguration.getComments());
            if (inSyslogConfiguration.getPort() != null) {
                syslogConfiguration.setPort(inSyslogConfiguration.getPort());
            }

            if (proxyUsed) {
                if (inSyslogConfiguration.getPortProxy() != null) {
                    syslogConfiguration.setPort(inSyslogConfiguration.getPortProxy());
                }
            }

            return syslogConfiguration;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.WebServiceConfiguration convertWebServiceConfigurationn(
            WebServiceConfiguration inWebServiceConfiguration, boolean proxyUsed) {
        if (inWebServiceConfiguration != null) {
            net.ihe.gazelle.simulator.ws.WebServiceConfiguration webServiceConfiguration = new net.ihe.gazelle.simulator.ws.WebServiceConfiguration();
            webServiceConfiguration
                    .setConfiguration(convertConfiguration(inWebServiceConfiguration.getConfiguration()));
            webServiceConfiguration.setAssigningAuthority(inWebServiceConfiguration.getAssigningAuthority());
            webServiceConfiguration.setComments(inWebServiceConfiguration.getAssigningAuthority());
            if (inWebServiceConfiguration.getPort() != null) {
                webServiceConfiguration.setPort(inWebServiceConfiguration.getPort());
            }

            if (inWebServiceConfiguration.getPortSecured() != null) {
                webServiceConfiguration.setPortSecured(inWebServiceConfiguration.getPortSecured());
            }
            webServiceConfiguration.setUrl(inWebServiceConfiguration.getUrl());
            webServiceConfiguration.setUsage(inWebServiceConfiguration.getUsage());

            if (proxyUsed) {
                if (inWebServiceConfiguration.getPortProxy() != null) {
                    webServiceConfiguration.setPort(inWebServiceConfiguration.getPortProxy());
                }
            }

            return webServiceConfiguration;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.ContextualInformation convertContextualInformation(
            ContextualInformation inContextualInformation) {
        if (inContextualInformation != null) {
            net.ihe.gazelle.simulator.ws.ContextualInformation ci = new net.ihe.gazelle.simulator.ws.ContextualInformation();
            ci.setLabel(inContextualInformation.getLabel());
            if (inContextualInformation.getPath() != null) {
                ci.setPath(new net.ihe.gazelle.simulator.ws.Path());
                ci.getPath().setDescription(inContextualInformation.getPath().getDescription());
                ci.getPath().setKeyword(inContextualInformation.getPath().getKeyword());
                ci.getPath().setType(inContextualInformation.getPath().getType());
            }
            ci.setValue(inContextualInformation.getValue());
            return ci;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.ContextualInformationInstance convertContextualInformationInstance(
            ContextualInformationInstance inContextualInformationInstance) {
        if (inContextualInformationInstance != null) {
            net.ihe.gazelle.simulator.ws.ContextualInformationInstance cii = new net.ihe.gazelle.simulator.ws.ContextualInformationInstance();
            if (inContextualInformationInstance.getContextualInformation() != null) {
                net.ihe.gazelle.simulator.ws.ContextualInformation ci = convertContextualInformation(inContextualInformationInstance
                        .getContextualInformation());
                cii.setContextualInformation(ci);
            }
            cii.setValue(inContextualInformationInstance.getValue());
            return cii;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.OidConfiguration convertOIDConfiguration(OIDSystemAssignment osa) {
        if (osa != null) {
            net.ihe.gazelle.simulator.ws.OidConfiguration oidconf = new net.ihe.gazelle.simulator.ws.OidConfiguration();
            oidconf.setLabel(osa.getOidRequirement().getLabel());
            oidconf.setOid(osa.getOid());
            return oidconf;
        }
        return null;
    }

    public static List<net.ihe.gazelle.simulator.ws.ContextualInformationInstance> convertListContextualInformationInstance(
            List<ContextualInformationInstance> inListContextualInformationInstance) {
        if (inListContextualInformationInstance != null) {
            List<net.ihe.gazelle.simulator.ws.ContextualInformationInstance> lcii = new ArrayList<net.ihe.gazelle.simulator.ws
                    .ContextualInformationInstance>();
            for (ContextualInformationInstance inContextualInformationInstance : inListContextualInformationInstance) {
                net.ihe.gazelle.simulator.ws.ContextualInformationInstance cii = convertContextualInformationInstance
                        (inContextualInformationInstance);
                lcii.add(cii);
            }
            return lcii;
        }
        return null;
    }

    public static net.ihe.gazelle.simulator.ws.ContextualInformationInstance[] convertListContextualInformationInstanceToArray(
            List<ContextualInformationInstance> inListContextualInformationInstance) {
        List<net.ihe.gazelle.simulator.ws.ContextualInformationInstance> lcii = convertListContextualInformationInstance
                (inListContextualInformationInstance);
        net.ihe.gazelle.simulator.ws.ContextualInformationInstance[] tcii = null;
        if (lcii != null) {
            tcii = lcii.toArray(new net.ihe.gazelle.simulator.ws.ContextualInformationInstance[lcii.size()]);
        }
        return tcii;
    }

    public static net.ihe.gazelle.simulator.ws.ConfigurationForWS convertAbstractConfiguration(
            AbstractConfiguration ac, boolean proxyUsed) {
        net.ihe.gazelle.simulator.ws.ConfigurationForWS res = new net.ihe.gazelle.simulator.ws.ConfigurationForWS();
        if (ac instanceof HL7V2InitiatorConfiguration) {
            net.ihe.gazelle.simulator.ws.Hl7V2InitiatorConfiguration hlv2init = Gazelle2SimulatorClassConverter
                    .convertHl7V2InitiatorConfiguration((HL7V2InitiatorConfiguration) ac);
            res.setHL7V2InitiatorConfiguration(hlv2init);
        }
        if (ac instanceof HL7V2ResponderConfiguration) {
            net.ihe.gazelle.simulator.ws.Hl7V2ResponderConfiguration hlv2resp = Gazelle2SimulatorClassConverter
                    .convertHl7V2ResponderConfiguration((HL7V2ResponderConfiguration) ac, proxyUsed);
            res.setHL7V2ResponderConfiguration(hlv2resp);
        }
        if (ac instanceof HL7V3InitiatorConfiguration) {
            net.ihe.gazelle.simulator.ws.Hl7V3InitiatorConfiguration hlv3init = Gazelle2SimulatorClassConverter
                    .convertHl7V3InitiatorConfiguration((HL7V3InitiatorConfiguration) ac);
            res.setHL7V3InitiatorConfiguration(hlv3init);
        }
        if (ac instanceof HL7V3ResponderConfiguration) {
            net.ihe.gazelle.simulator.ws.Hl7V3ResponderConfiguration hlv3resp = Gazelle2SimulatorClassConverter
                    .convertHl7V3ResponderConfiguration((HL7V3ResponderConfiguration) ac, proxyUsed);
            res.setHL7V3ResponderConfiguration(hlv3resp);
        }
        if (ac instanceof DicomSCPConfiguration) {
            net.ihe.gazelle.simulator.ws.DicomSCPConfiguration dicomscp = Gazelle2SimulatorClassConverter
                    .convertDICOMSCPConfiguration((DicomSCPConfiguration) ac, proxyUsed);
            res.setDicomSCPConfiguration(dicomscp);
        }
        if (ac instanceof DicomSCUConfiguration) {
            net.ihe.gazelle.simulator.ws.DicomSCUConfiguration dicomscu = Gazelle2SimulatorClassConverter
                    .convertDICOMSCUConfiguration((DicomSCUConfiguration) ac, proxyUsed);
            res.setDicomSCUConfiguration(dicomscu);
        }
        if (ac instanceof WebServiceConfiguration) {
            net.ihe.gazelle.simulator.ws.WebServiceConfiguration wsconf = Gazelle2SimulatorClassConverter
                    .convertWebServiceConfigurationn((WebServiceConfiguration) ac, proxyUsed);
            res.setWebServiceConfiguration(wsconf);
        }
        if (ac instanceof SyslogConfiguration) {
            net.ihe.gazelle.simulator.ws.SyslogConfiguration SYSLOG = Gazelle2SimulatorClassConverter
                    .convertSyslogConfiguration((SyslogConfiguration) ac, proxyUsed);
            res.setSyslogConfiguration(SYSLOG);
        }
        return res;
    }

}
