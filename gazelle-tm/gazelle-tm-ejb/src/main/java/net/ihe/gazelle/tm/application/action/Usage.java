package net.ihe.gazelle.tm.application.action;

import net.ihe.gazelle.common.session.GazelleSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Usage {
    private static final Logger LOG = LoggerFactory.getLogger(Usage.class);
    private int sessions;
    private long usedMem;

    public Usage() {
        super();
        sessions = GazelleSessionListener.getSessions().size();

        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        usedMem = totalMem - freeMem;
    }

    public int getSessions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSessions");
        }
        return sessions;
    }

    public long getUsedMem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUsedMem");
        }
        return usedMem;
    }
}
