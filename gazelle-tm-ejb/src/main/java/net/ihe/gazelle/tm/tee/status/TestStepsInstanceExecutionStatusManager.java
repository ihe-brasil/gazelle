package net.ihe.gazelle.tm.tee.status;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceExecutionStatusDAO;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;

/**
 * Manages execution status of a TestStepsInstance
 */
@Stateless
@Name("testStepsExecutionStatusMgr")
@GenerateInterface("TestStepsInstanceExecutionStatusManagerLocal")
public class TestStepsInstanceExecutionStatusManager implements Serializable, TestStepsInstanceExecutionStatusManagerLocal {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TestStepsInstanceExecutionStatusManager.class);

    /**
     * Get list of TestStepInstanceExecutionStatusEnum available for a
     * TestStepsInstance
     *
     * @return list of execution statuses
     * @see TestStepsInstance
     */
    public TestStepInstanceExecutionStatusEnum[] getExecutionStatusEnumArray() {
        return TestStepInstanceExecutionStatusEnum.getDisplayableStatuses().toArray(new
                TestStepInstanceExecutionStatusEnum[TestStepInstanceExecutionStatusEnum.getDisplayableStatuses().size()]);
    }

    /**
     * Get list of TestStepInstance objs.
     */
    public List<TestStepInstanceExecutionStatus> getExecutionStatusesList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getExecutionStatusesList");
        }
        return new TestStepsInstanceDAO().getListOfTestStepInstanceExcecutionStatuses();
    }

    public TestStepInstanceExecutionStatus getExecutionStatusByEnum(TestStepInstanceExecutionStatusEnum pEnum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getExecutionStatusByEnum");
        }
        return new TestStepsInstanceExecutionStatusDAO().getTestStepExecutionStatusByEnum(pEnum);
    }
}
