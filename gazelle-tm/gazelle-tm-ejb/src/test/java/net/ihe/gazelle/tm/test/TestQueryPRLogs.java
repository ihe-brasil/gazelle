package net.ihe.gazelle.tm.test;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.pr.action.PRLogs;
import net.ihe.gazelle.pr.bean.LogType;
import org.junit.Ignore;

import java.io.IOException;

@Ignore
public class TestQueryPRLogs extends AbstractTestQueryJunit4 {

    public void testQueryPRLogs() throws IOException {
        PRLogs prLogs = new PRLogs();
        prLogs.setType(LogType.DOWNLOAD);
        queries(prLogs);
        prLogs.setType(LogType.SEARCH);
        queries(prLogs);
    }

    private void queries(PRLogs prLogs) throws IOException {
        System.out.println(prLogs.javascriptSeries());
    }
}
