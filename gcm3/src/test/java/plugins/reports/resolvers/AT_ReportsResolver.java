package plugins.reports.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.DataManagerContext;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import plugins.components.ComponentPlugin;
import plugins.reports.ReportId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportError;
import util.ContractException;
import util.MutableBoolean;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ReportsResolver.class)
public class AT_ReportsResolver {

	@Test
	@UnitTestConstructor(args = { ReportsInitialData.class })
	public void testConstructor() {

		assertNotNull(new ReportsResolver(ReportsInitialData.builder().build()));

		ContractException contractException = assertThrows(ContractException.class, () -> new ReportsResolver(null));
		assertEquals(ReportError.NULL_REPORT_INITIAL_DATA, contractException.getErrorType());

	}


	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testInit() {
		//add two reports to the reports plugin
		ReportId reportId_A = new SimpleReportId("A");
		ReportId reportId_B = new SimpleReportId("B");
		
		//create containers that will show that the reports exist
		MutableBoolean proofOfReportA = new MutableBoolean();
		assertFalse(proofOfReportA.getValue());
		
		MutableBoolean proofOfReportB = new MutableBoolean();
		assertFalse(proofOfReportB.getValue());
		
		ReportsInitialData reportsInitialData = ReportsInitialData
				.builder()
				.addReport(reportId_A, ()->(c)->proofOfReportA.setValue(true))
				.addReport(reportId_B, ()->(c)->proofOfReportB.setValue(true))				
				.build();
		
		Builder builder = Simulation.builder();

		// add the reports plugin
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsInitialData)::init);

		// add the remaining plugins that are needed for dependencies
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		/*
		 * Show that each report actually exists by generating events for the
		 * reports and having each report alter its corresponding mutable
		 * boolean
		 */
		
		assertTrue(proofOfReportA.getValue());
		assertTrue(proofOfReportB.getValue());
		
		// precondition tests		
		ContractException contractException = assertThrows(ContractException.class,()->new ReportsResolver(reportsInitialData).init(null));
		assertEquals(ReportError.NULL_REPORT_CONTEXT, contractException.getErrorType());

	}

}
