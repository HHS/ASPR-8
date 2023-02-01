package plugins.reports.testsupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import plugins.globalproperties.actors.GlobalPropertyReport;
import plugins.groups.actors.GroupPopulationReport;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;
import tools.annotations.UnitTestMethod;

public class AT_ReportsTestPluginFactory {
    @Test
    @UnitTestMethod(target = ReportsTestPluginFactory.class, name = "getPluginFromReport", args = {Consumer.class})
    public void testGetPluginFromReport() {
        GlobalPropertyReport globalPropertyReport = GlobalPropertyReport.builder()//
				.setReportId( new SimpleReportId("global property report"))//
				.includeAllExtantPropertyIds(true)//
				.includeNewPropertyIds(true)//
				.build();

        Plugin globalReportPlugin = ReportsTestPluginFactory.getPluginFromReport(globalPropertyReport::init);
        assertNotNull(globalReportPlugin);

        GroupPopulationReport groupPopulationReport = new GroupPopulationReport(new SimpleReportId("group population property report"), ReportPeriod.HOURLY);
        Plugin groupPopulationReportPlugin = ReportsTestPluginFactory.getPluginFromReport(groupPopulationReport::init);
        assertNotNull(groupPopulationReportPlugin);
    }
}
