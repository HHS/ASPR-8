package plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.*;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

import java.util.List;
import java.util.Map;

public class AT_PersonPropertyReport {

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "builder", args = {})
	public void testBuilder() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();

		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		// TBD
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "build", args = {})
	public void testBuild() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();

		builder.setReportLabel(new SimpleReportLabel(1000));
		builder.setReportPeriod(ReportPeriod.DAILY);

		PersonPropertyReport report = builder.build();

		assertNotNull(report);

		// precondition: null report label
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportPeriod(ReportPeriod.DAILY).build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// precondition: null report period
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportLabel(new SimpleReportLabel(1000)).build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
			// provides an empty task so that the TestSimulation.execute does not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(ReportPeriod.DAILY);
			builder.setDefaultInclusion(true);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(reportItem.getReportLabel(), reportLabel);
		}
		// add case for excluded id

		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(reportPeriod);

		PersonPropertyReport report = builder.build();

		assertNotNull(report);

		// precondition: report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "setDefaultInclusion", args = { boolean.class })
	public void testSetDefaultInclusion() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "includePersonProperty", args = { PersonPropertyId.class })
	public void testIncludePersonProperty() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(reportPeriod);
		builder.includePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);

		PersonPropertyReport report = builder.build();

		assertNotNull(report);

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().includePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "excludePersonProperty", args = { PersonPropertyId.class })
	public void testExcludePersonProperty() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(reportPeriod);
		builder.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);

		PersonPropertyReport report = builder.build();

		assertNotNull(report);

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().excludePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}
}
