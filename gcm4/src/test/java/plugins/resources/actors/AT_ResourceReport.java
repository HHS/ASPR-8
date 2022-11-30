package plugins.resources.actors;

import nucleus.ActorContext;
import org.junit.jupiter.api.Test;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportPeriod;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = ResourceReport.class)
public class AT_ResourceReport {

    @Test
    @UnitTestConstructor(args = {ReportId.class, ReportPeriod.class, ResourceId[].class})
    public void testConstructor() {
        // nothing to test
    }

    @Test
    @UnitTestMethod(name = "init", args = {ActorContext.class}, tags = UnitTag.INCOMPLETE)
    public void testInit() {
        // incomplete test
    }

}