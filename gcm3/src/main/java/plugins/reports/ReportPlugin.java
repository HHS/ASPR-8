package plugins.reports;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.resolvers.ReportsResolver;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for the managment of reports
 * </p>
 *
 * <p>
 * <b>Events </b> The plugin defines and handles no events.
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>ReportsResolver:</b> Uses initializing data to create and publish data
 * views and create reports.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views: </b> The report plugin supplies one data view
 * <ul>
 * <li><b>Report Data View</b>: Supplies report information</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports: </b> The plugin introduces no reports
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the initial behaviors of
 * reports.
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>PeriodicReport: </b></li> Base class that support periodic reporting.
 * <li><b>ReportError: </b></li>Enumeration implementing.
 * {@linkplain ContractError} for this plugin.
 * <li><b>ReportHeader: </b></li>Provides a string-based header for report
 * items.
 * <li><b>ReportItem: </b></li>A string based container for storing the data for
 * one line in a report.
 * <li><b>ReportPeriod: </b></li>An enumeration defining the available reporting
 * periods
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b> This plugin has no plugin dependencies 
 * </p>
 *
 * @author Shawn Hatch
 *
 */

public final class ReportPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(ReportPlugin.class);
	private final ReportsInitialData reportsInitialData;

	public ReportPlugin(ReportsInitialData reportsInitialData) {
		this.reportsInitialData = reportsInitialData;
	}

	/**
	 * Initial behavior of this plugin. <BR>
	 *
	 * <UL>
	 * <li>defines the single resolver {@linkplain ReportsResolver}</li>
	 * <li>establishes dependencies on other plugins
	 * </UL>
	 */
	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(ReportsResolver.class), new ReportsResolver(reportsInitialData)::init);
	}
}
