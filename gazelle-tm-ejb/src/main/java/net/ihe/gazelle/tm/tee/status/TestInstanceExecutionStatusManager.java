package net.ihe.gazelle.tm.tee.status;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceExecutionStatusDAO;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;

@Stateless
@Name("testInstanceExecutionStatusMgr")
@GenerateInterface("TestInstanceExecutionStatusManagerLocal")
public class TestInstanceExecutionStatusManager implements Serializable, TestInstanceExecutionStatusManagerLocal {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceExecutionStatusManager.class);

    /**
     * Change execution status of supplied TestInstance. Execution Status
     * is set to the {@code selectedExecutionStatus} if it is not NULL.
     *
     * @param instance
     */
    public void changeExecutionStatus(TestInstance instance, TestInstanceExecutionStatusEnum selectedExecutionStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeExecutionStatus");
        }
        if (instance != null) {
            if (selectedExecutionStatus != null && !selectedExecutionStatus.getKeyword().equals(instance.getExecutionStatus().getKey().getKeyword()
            )) {
                instance.setExecutionStatus(new TestInstanceExecutionStatusDAO().getTestInstanceExecutionStatusByEnum(selectedExecutionStatus));
                new TestInstanceDAO().persistTestInstance(instance);
            }
        } else {
            LOG.error("TestInstanceExecutionStatusManager.changeExecutionStatus: supplied TestInstance is NULL.");
        }
    }

    /**
     * Get a list of TestInstanceExecutionStatusEnum objs.
     *
     * @return
     */
    public TestInstanceExecutionStatusEnum[] getExecutionStatusEnumArray() {

        return TestInstanceExecutionStatusEnum.getDisplayableStatuses().toArray(new TestInstanceExecutionStatusEnum[TestInstanceExecutionStatusEnum
                .getDisplayableStatuses().size()]);
    }

    /**
     * Get a list of TestInstanceExecutionStatus objs.
     *
     * @return
     */
    public List<TestInstanceExecutionStatus> getExecutionStatusList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getExecutionStatusList");
        }
        return new TestInstanceDAO().getListOfTestInstanceExecutionStatuses();
    }

    /**
     * Returns a execution status
     *
     * @param pEnum
     * @return
     */
    public TestInstanceExecutionStatus getExecutionStatusByEnum(TestInstanceExecutionStatusEnum pEnum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getExecutionStatusByEnum");
        }
        return new TestInstanceExecutionStatusDAO().getTestInstanceExecutionStatusByEnum(pEnum);
    }
}
