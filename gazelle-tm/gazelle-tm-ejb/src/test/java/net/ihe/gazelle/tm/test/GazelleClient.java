package net.ihe.gazelle.tm.test;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Ignore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Ignore
public class GazelleClient {

    private static String BASE_URL = "http://127.0.0.1:8180/TM/";

    private static int CLIENTS = 64;
    private static int THREADS = 8;

    private HttpClient httpClient;
    private CookieStore cookieStore;
    private HttpContext httpContext;

    public GazelleClient() throws ClientProtocolException, IOException {
        super();
        httpClient = new DefaultHttpClient();
        HttpClientParams.setRedirecting(httpClient.getParams(), true);
        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {
        List<GazelleClient> clients = new ArrayList<GazelleClient>();

        for (int i = 0; i < CLIENTS; i++) {
            clients.add(new GazelleClient());
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);

        List<String> commands = new ArrayList<String>();
        commands.add("start");

        for (GazelleClient gazelleClient : clients) {
            threadPool.execute(new ProcessGazelle(threadPool, gazelleClient, commands));
        }
    }

    public void startUp() throws ClientProtocolException, IOException {
        home();
        login();
    }

    public void loadCats() throws ClientProtocolException, IOException {
        for (int i = 0; i < 16; i++) {
            cat();
        }
    }

    private void home() throws ClientProtocolException, IOException {
        get(BASE_URL + "home.seam");
    }

    public String ti(String tiNumber) throws ClientProtocolException, IOException {
        return get(BASE_URL + "testing/test/test/TestInstance.seam?id=" + tiNumber);
    }

    public String cat() throws ClientProtocolException, IOException {
        return get(BASE_URL + "testing/test/cat.seam");
    }

    public String configs() throws ClientProtocolException, IOException {
        return get(BASE_URL + "configuration/configurations.seam");
    }

    public String users() throws ClientProtocolException, IOException {
        return get(BASE_URL + "users/user/listUsersInstitution.seam");
    }

    public String companies() throws ClientProtocolException, IOException {
        return get(BASE_URL + "administration/listInstitutions.seam");
    }

    public String get(String url) throws IOException, ClientProtocolException {
        long start = System.nanoTime();
        HttpUriRequest request = new HttpGet(url);
        String result = executeRequest(request);
        long end = System.nanoTime();
        long length = (end - start) / 1000000;
        System.out.println(length + " " + result.length() + " " + url);
        return result;
    }

    public String executeRequest(HttpUriRequest request) throws IOException, ClientProtocolException {
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String result = httpClient.execute(request, responseHandler, httpContext);
        return result;
    }

    private void login() throws ClientProtocolException, IOException {
        get(BASE_URL + "users/login/login.seam");

        HttpPost httpost = new HttpPost(BASE_URL + "users/login/login.seam");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        nvps.add(new BasicNameValuePair("AJAXREQUEST", "_viewRoot"));
        nvps.add(new BasicNameValuePair("login", "login"));
        nvps.add(new BasicNameValuePair("login:username", "jlabbe"));
        nvps.add(new BasicNameValuePair("login:passwordNotHashed", "aaaaaa"));
        nvps.add(new BasicNameValuePair("login:password", "ab4f63f9ac65152575886860dde480a1"));
        nvps.add(new BasicNameValuePair("login:rememberMe", "on"));
        nvps.add(new BasicNameValuePair("javax.faces.ViewState", "j_id2"));
        nvps.add(new BasicNameValuePair("login:submit", "login:submit"));

        httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

        executeRequest(httpost);
    }
}
