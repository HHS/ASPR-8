package plugins.reports.support;

import tools.annotations.UnitTest;

@UnitTest(target = LineWriter.class)
public class MT_LineWriter {

    //@UnitTestConstructor(args = {ExperimentContext.class, Path.class, boolean.class}, tags = {UnitTag.CLASS_PROXY})
    public void testConstructor() {
        // covered by test accept in MT_NIOReportItemHandler
    }

    //@UnitTestMethod(name = "write", args = {ExperimentContext.class, int.class, ReportItem.class}, tags = {UnitTag.CLASS_PROXY})
    public void testWrite() {
        // covered by test accept in MT_NIOReportItemHandler
    }

    //@UnitTestMethod(name = "flush", args = {}, tags = {UnitTag.CLASS_PROXY, UnitTag.INCOMPLETE})
    public void testFlush() {
        // covered by test accept in MT_NIOReportItemHandler
    }

    //@UnitTestMethod(name = "close", args = {}, tags = {UnitTag.CLASS_PROXY, UnitTag.INCOMPLETE})
    public void testClose() {
        // covered by test accept in MT_NIOReportItemHandler
    }

}