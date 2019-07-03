package net.ihe.gazelle.jira.issue.search;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.util.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

public class NullClientResponse extends ClientResponse<String> {

    private static final Logger LOG = LoggerFactory.getLogger(NullClientResponse.class);

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHeaders");
        }
        return null;
    }

    @Override
    public Status getResponseStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResponseStatus");
        }
        return Status.NOT_FOUND;
    }

    @Override
    public String getEntity() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntity");
        }
        return "";
    }

    @Override
    public <T2> T2 getEntity(Class<T2> type) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntity");
        }
        T2 newInstance = null;
        try {
            newInstance = type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return newInstance;
    }

    @Override
    public <T2> T2 getEntity(Class<T2> type, Type genericType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntity");
        }
        return null;
    }

    @Override
    public <T2> T2 getEntity(Class<T2> type, Type genericType, Annotation[] annotations) {
        return null;
    }

    @Override
    public <T2> T2 getEntity(GenericType<T2> type) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntity");
        }
        return null;
    }

    @Override
    public <T2> T2 getEntity(GenericType<T2> type, Annotation[] annotations) {
        return null;
    }

    @Override
    public LinkHeader getLinkHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkHeader");
        }
        return null;
    }

    @Override
    public Link getLocation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLocation");
        }
        return null;
    }

    @Override
    public Link getHeaderAsLink(String headerName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHeaderAsLink");
        }
        return null;
    }

    @Override
    public void resetStream() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetStream");
        }

    }

    @Override
    public void releaseConnection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("releaseConnection");
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAttributes");
        }
        return null;
    }

    @Override
    public int getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return 0;
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetadata");
        }
        return null;
    }
}