package net.ihe.gazelle.tm.gazelletest.action;

import org.jboss.seam.annotations.async.Asynchronous;

import javax.ejb.Local;

@Local
public interface ProxyStartTestInstanceJobInterface {

    @Asynchronous
    public void proxyStartTestInstanceJob(int testInstanceId);
}
