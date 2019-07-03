package net.ihe.gazelle.jira.issue.search;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;

public class Request {

    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    private String baseUrl;

    public Request(String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    public ClientResponse<String> performRequest() throws ConnectException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performRequest");
        }
        ClientResponse<String> response = new NullClientResponse();
        try {
            String url = baseUrl;

            ClientRequest request = new ClientRequest(url);
            request.accept("application/json");
            response = request.get(String.class);

            if ((response.getStatus() == 404) || (response.getStatus() == 503)) {
                throw new ConnectException("Cannot acces server");
            } else if (!((response.getStatus() == 200) || (response.getStatus() == 400))) {
                response = new NullClientResponse();
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }
        } catch (ConnectException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("cannot parse response " + baseUrl);
        }
        return response;
    }
}
