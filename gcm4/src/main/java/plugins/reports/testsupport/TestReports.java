package plugins.reports.testsupport;

import java.util.List;
import java.util.function.Consumer;

import nucleus.Plugin;
import nucleus.Simulation;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.Report;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

public class TestReports {

    public static <T extends Report> void testConsumers(Plugin testPlugin, T report, long seed, List<Plugin> pluginsToAdd,
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
}
