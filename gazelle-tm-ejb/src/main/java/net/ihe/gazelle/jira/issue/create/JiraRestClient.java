package net.ihe.gazelle.jira.issue.create;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.ihe.gazelle.tm.application.action.ApplicationManager;

import javax.naming.AuthenticationException;

public class JiraRestClient {

    public static String createIssue(String auth, IssueData issue)
            throws AuthenticationException, ClientHandlerException, IllegalArgumentException {
        Client client = new Client();
        String apiUrl = ApplicationManager.instance().getJiraUrl() + "/rest/api/2/issue/";
        WebResource webResource = client.resource(apiUrl);

        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").post(ClientResponse.class, issue.toJson());
        int statusCode = response.getStatus();
        if (statusCode == 400) {
            throw new IllegalArgumentException(response.toString() + " " + response.getEntity(String.class));
        }
        if (statusCode == 401) {
            throw new AuthenticationException("Invalid Username or Password");
        }
        return response.getEntity(String.class);
    }
}
