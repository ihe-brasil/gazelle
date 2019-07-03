package net.ihe.gazelle.jira.issue.search;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jlabbe on 17/03/15.
 */
public class ParseJiraResponse {

    @Test
    public void testResponse() throws ParseException {
        JSONParser parser = new JSONParser();

        String response = "{\"id\":\"21882\",\"key\":\"TES-96\",\"self\":\"http://gazelle.ihe.net/jira/rest/api/2/issue/21882\"}";
        JSONObject obj = (JSONObject) parser.parse(response);
        assertEquals("TES-96", obj.get("key"));
    }
}
