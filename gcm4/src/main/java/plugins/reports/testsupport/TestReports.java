package plugins.reports.testsupport;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import nucleus.Plugin;
import nucleus.Simulation;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.Report;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

public class TestReports<T extends Report> {

    private Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();
    private ReportId reportId;
    private ReportHeader reportHeader;

    public TestReports(ReportId reportId, ReportHeader reportHeader) {
        this.reportId = reportId;
        this.reportHeader = reportHeader;
    }

    public void testConsumers(Plugin testPlugin, T report, long seed, List<Plugin> pluginsToAdd,
            Consumer<Object> outputConsumer) {

        Simulation.Builder builder = Simulation.builder();

        for (Plugin plugin : pluginsToAdd) {
            builder.addPlugin(plugin);
        }

        // add the report plugin
        ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> report::init)
                .build();
        builder.addPlugin(ReportsPlugin.getReportsPlugin(reportsPluginData));

        // add the stochastics plugin
        StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(seed).build();
        builder.addPlugin(StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData));

        builder.addPlugin(testPlugin);
        builder.setOutputConsumer(outputConsumer);

        // // build and execute the engine
        builder.build().execute();
    }

    private ReportItem getReportItem(Integer day, Integer hour, Object... values) {
        ReportItem.Builder builder = ReportItem.builder();
        builder.setReportId(this.reportId);
        builder.setReportHeader(this.reportHeader);

        builder.addValue(day);

        if (hour != null) {
            builder.addValue(hour);
        }

        for (Object value : values) {
            builder.addValue(value);
        }
        return builder.build();
    }

    public void addExpectedReportItem(int day, int occurances, Object... values) {
        this.addExpectedDailyReportItem(day, occurances, Arrays.copyOf(values, values.length));
    }

    public void addExpectedDailyReportItem(int day, int occurances, Object... values) {
        ReportItem item = getReportItem(day, null, Arrays.copyOf(values, values.length));
        this.expectedReportItems.put(item, occurances);
    }

    public void addExpectedHourlyReportItem(int day, int hour, int occurances, Object... values) {
        ReportItem item = getReportItem(day, hour, Arrays.copyOf(values, values.length));
        this.expectedReportItems.put(item, occurances);
    }

    public Map<ReportItem, Integer> getExpectedReportItems() {
        return this.expectedReportItems;
    }
}
