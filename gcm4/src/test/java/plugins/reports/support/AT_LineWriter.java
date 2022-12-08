package plugins.reports.support;

import nucleus.ExperimentContext;
import org.junit.jupiter.api.Test;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

import java.nio.file.Path;

@UnitTest(target = LineWriter.class)
public class AT_LineWriter {

    @Test
    @UnitTestConstructor(args = {ExperimentContext.class, Path.class, boolean.class}, tags = {UnitTag.MANUAL})
    public void testConstructor() {
        // covered by test constructor in manual test
    }

    @Test
    @UnitTestMethod(name = "write", args = {ExperimentContext.class, int.class, ReportItem.class}, tags = {UnitTag.MANUAL})
    public void testWrite() {
        // covered by test write manual test
    }

    @Test
    @UnitTestMethod(name = "flush", args = {}, tags = {UnitTag.MANUAL})
    public void testFLush() {
        // covered by test flush in manual test
    }

    @Test
    @UnitTestMethod(name = "close", args = {}, tags = {UnitTag.MANUAL})
    public void testClose() {
        // covered by test close in manual test
    }

}