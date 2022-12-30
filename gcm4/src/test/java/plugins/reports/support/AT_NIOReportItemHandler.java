package plugins.reports.support;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import nucleus.ExperimentContext;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = NIOReportItemHandler.class)
public class AT_NIOReportItemHandler {

    @Test
    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "build", args = {}, tags = {UnitTag.MANUAL})
    public void testBuild() {
        // covered by test for accept in manual test
    }

    @Test
    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "addReport", args = {ReportId.class, Path.class}, tags = {UnitTag.MANUAL})
    public void testAddReport() {
        // covered by test for accept in manual test
    }

    @Test
    @UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "setDisplayExperimentColumnsInReports", args = {boolean.class}, tags = {UnitTag.MANUAL})
    public void testSetDisplayExperimentColumnsInReports() {
        // covered by test for accept in manual test
    }

    @Test
    @UnitTestMethod(name = "builder", args = {}, tags = {UnitTag.MANUAL})
    public void testBuilder() {
        // covered by test for builder in manual test
    }

    @Test
    @UnitTestMethod(name = "accept", args = {ExperimentContext.class}, tags = {UnitTag.MANUAL})
    public void testAccept() {
        // covered by test for accept in manual test
    }

}