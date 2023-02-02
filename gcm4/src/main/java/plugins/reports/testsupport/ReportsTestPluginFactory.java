package plugins.reports.testsupport;

import java.util.function.Consumer;

import nucleus.Plugin;
import nucleus.ReportContext;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;

public final class ReportsTestPluginFactory {

    private ReportsTestPluginFactory() {
    }
    
    public static Plugin getPluginFromReport(Consumer<ReportContext> consumer) {
        ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> consumer)
                .build();
        return ReportsPlugin.getReportsPlugin(reportsPluginData);
    }
}
