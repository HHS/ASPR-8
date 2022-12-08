package plugins.reports.support;

import nucleus.ExperimentContext;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

import java.nio.file.Path;

@UnitTest(target = LineWriter.class)
public class MT_LineWriter {

    @UnitTestConstructor(args = {ExperimentContext.class, Path.class, boolean.class}, tags = {UnitTag.CLASS_PROXY})
    public void testConstructor() {
        // covered by test accept in MT_NIOReportItemHandler
    }

    @UnitTestMethod(name = "write", args = {ExperimentContext.class, int.class, ReportItem.class}, tags = {UnitTag.CLASS_PROXY})
    public void testWrite() {
        // covered by test accept in MT_NIOReportItemHandler
    }

    @UnitTestMethod(name = "flush", args = {}, tags = {UnitTag.CLASS_PROXY})
    public void testFLush() {
        // covered by test accept in MT_NIOReportItemHandler
    }

    @UnitTestMethod(name = "close", args = {}, tags = {UnitTag.CLASS_PROXY})
    public void testClose() {
        // covered by test accept in MT_NIOReportItemHandler
    }

}