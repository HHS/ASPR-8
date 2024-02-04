package gov.hhs.aspr.ms.gcm.plugins.reports.support;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ExperimentContext;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_ExperimentLineWriter {

    @Test    
    @UnitTestConstructor(target = ExperimentLineWriter.class, args = { ExperimentContext.class, Path.class, String.class}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
    public void testConstructor() {
        // covered by manual test of NIOReportItemHandler
    }

    @Test
    @UnitTestMethod(target = ExperimentLineWriter.class, name = "write", args = { ExperimentContext.class, int.class}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
    public void testWrite() {
        // covered by manual test of NIOReportItemHandler
    }

    @Test
    @UnitTestMethod(target = ExperimentLineWriter.class, name = "flush", args = {}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
    public void testFLush() {
        // covered by manual test of NIOReportItemHandler
    }

    @Test
    @UnitTestMethod(target = ExperimentLineWriter.class, name = "close", args = {}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
    public void testClose() {
        // covered by manual test of NIOReportItemHandler
    }

}
