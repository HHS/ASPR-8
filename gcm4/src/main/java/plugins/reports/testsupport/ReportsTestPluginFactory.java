package plugins.reports.testsupport;

import nucleus.Plugin;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.Report;

public final class ReportsTestPluginFactory {

    private ReportsTestPluginFactory() {
    }

    public static <T extends Report> Plugin getPluginFromReport(T report) {
        ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> report::init)
                .build();

        return ReportsPlugin.getReportsPlugin(reportsPluginData);
    }
}
