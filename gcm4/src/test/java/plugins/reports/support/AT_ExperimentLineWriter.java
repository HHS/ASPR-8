package plugins.reports.support;

import nucleus.ExperimentContext;
import org.junit.jupiter.api.Test;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

import java.nio.file.Path;

public class AT_ExperimentLineWriter {

    @Test
    @UnitTestConstructor(target = ExperimentLineWriter.class, args = { ExperimentContext.class, Path.class, boolean.class,String.class}, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
    public void testConstructor() {
        // covered by manual test of NIOReportItemHandler
    }

    @Test
    @UnitTestMethod(target = ExperimentLineWriter.class, name = "write", args = { ExperimentContext.class, int.class, ReportItem.class }, tags = { UnitTag.MANUAL , UnitTag.CLASS_PROXY})
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
