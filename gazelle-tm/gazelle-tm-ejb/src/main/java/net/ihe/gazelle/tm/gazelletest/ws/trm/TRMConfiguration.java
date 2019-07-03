package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration;
import net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration;
import net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration;
import net.ihe.gazelle.tm.configurations.model.SyslogConfiguration;
import net.ihe.gazelle.tm.configurations.model.TransportLayer;
import net.ihe.gazelle.tm.configurations.model.WebServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TRMConfiguration.class);

    private int id;

    private String host;

    private String actorName;

    private String systemName;

    private int port = -1;

    private TRMChannelType type = null;

    public int getId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getId");
        }
        return id;
    }

    public void setId(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setId");
        }
        this.id = id;
    }

    public String getHost() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHost");
        }
        return host;
    }

    public void setHost(String host) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHost");
        }
        this.host = host;
    }

    public String getActorName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorName");
        }
        return actorName;
    }

    public void setActorName(String actorName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorName");
        }
        this.actorName = actorName;
    }

    public String getSystemName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemName");
        }
        return systemName;
    }

    public void setSystemName(String systemName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemName");
        }
        this.systemName = systemName;
    }

    public int getPort() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPort");
        }
        return port;
    }

    public void setPort(int port) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPort");
        }
        this.port = port;
    }

    public TRMChannelType getType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getType");
        }
        return type;
    }

    public void setType(TRMChannelType type) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setType");
        }
        this.type = type;
    }

    public void loadFromConfiguration(AbstractConfiguration abstractConfiguration) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadFromConfiguration");
        }

        Integer port = -1;
        TRMChannelType type = null;

        if (abstractConfiguration instanceof DicomSCPConfiguration) {
            DicomSCPConfiguration tmConfig = (DicomSCPConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            type = TRMChannelType.DICOM_SCP;
        }
        if (abstractConfiguration instanceof DicomSCUConfiguration) {
            port = -1;
            type = TRMChannelType.DICOM_SCU;
        }
        if (abstractConfiguration instanceof HL7V2InitiatorConfiguration) {
            port = -1;
            type = TRMChannelType.HL7V2_INIT;
        }
        if (abstractConfiguration instanceof HL7V2ResponderConfiguration) {
            HL7V2ResponderConfiguration tmConfig = (HL7V2ResponderConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            type = TRMChannelType.HL7V2_RESP;
        }
        if (abstractConfiguration instanceof HL7V3InitiatorConfiguration) {
            port = -1;
            type = TRMChannelType.HL7V3_INIT;
        }
        if (abstractConfiguration instanceof HL7V3ResponderConfiguration) {
            HL7V3ResponderConfiguration tmConfig = (HL7V3ResponderConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            type = TRMChannelType.HL7V3_RESP;
        }
        if (abstractConfiguration instanceof WebServiceConfiguration) {
            WebServiceConfiguration tmConfig = (WebServiceConfiguration) abstractConfiguration;
            port = tmConfig.getPort();
            type = TRMChannelType.WEBSERVICE;
        }
        if (abstractConfiguration instanceof SyslogConfiguration) {
            SyslogConfiguration tmConfig = (SyslogConfiguration) abstractConfiguration;
            if (tmConfig.getTransportLayer().equals(TransportLayer.getTCP_Protocol())) {
                port = tmConfig.getPort();
                type = TRMChannelType.SYSLOG;
            }
        }

        if (type != null) {
            setId(abstractConfiguration.getId());
            setHost(abstractConfiguration.getConfiguration().getHost().getIp());
            setActorName(abstractConfiguration.getConfiguration().getActor().getKeyword());
            setSystemName(abstractConfiguration.getConfiguration().getSystemInSession().getSystem().getKeyword());

            if (port == null) {
                port = -1;
            }

            setPort(port);
            setType(type);
        }
    }

}
