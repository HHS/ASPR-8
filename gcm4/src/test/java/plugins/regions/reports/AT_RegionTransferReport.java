package plugins.regions.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_RegionTransferReport {

	@Test
	@UnitTestConstructor(target = RegionTransferReport.class, args = { ReportLabel.class, ReportPeriod.class })
	public void testConstructor() {
		RegionTransferReport regionTransferReport = new RegionTransferReport(REPORT_LABEL, ReportPeriod.DAILY);

		// Show not null
		assertNotNull(regionTransferReport);

		// precondition: null report period
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionTransferReport(REPORT_LABEL, null));
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition: null report label
		contractException = assertThrows(ContractException.class, () -> new RegionTransferReport(null, ReportPeriod.DAILY));
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionTransferReport.class, name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {

		/*
		 * TEST IS INCOMPLETE
		 * 
		 * This test has to force times to be non integer values and include a
		 * last task to for proper flushing in the report
		 */

		

		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();

		// add regions A, B, C and D
		RegionId regionA = new SimpleRegionId("Region_A");
		regionBuilder.addRegion(regionA);
		RegionId regionB = new SimpleRegionId("Region_B");
		regionBuilder.addRegion(regionB);
		RegionId regionC = new SimpleRegionId("Region_C");
		regionBuilder.addRegion(regionC);
		RegionId regionD = new SimpleRegionId("Region_D");
		regionBuilder.addRegion(regionD);

		// add the region properties
		RegionPropertyId prop_age = new SimpleRegionPropertyId("prop_age");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		regionBuilder.defineRegionProperty(prop_age, propertyDefinition);

		RegionsPluginData regionsPluginData = regionBuilder.build();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();
		final int numPeople = 100;
		// create an actor to add people to the simulation
		// This will test adding people to a region, which on the report will
		// say that
		// the person transfer to and from the same region
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RegionId[] regionIds = { regionA, regionB, regionC, regionD };
			for (int i = 0; i < numPeople; i++) {
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionIds[i % 4]).build();
				peopleDataManager.addPerson(personConstructionData);
			}
			expectedReportItems.put(getReportItem(0, regionA, regionA, 25), 1);
			expectedReportItems.put(getReportItem(0, regionB, regionB, 25), 1);
			expectedReportItems.put(getReportItem(0, regionC, regionC, 25), 1);
			expectedReportItems.put(getReportItem(0, regionD, regionD, 25), 1);
		}));

		// create an actor to move people from one region to another
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();

			List<PersonId> personIds = peopleDataManager.getPeople();
			// randomly move 25 people each to region a, region b, region c and
			// region d
			RegionId[] regionIds = { regionA, regionB, regionC, regionD };
			Map<Pair<RegionId, RegionId>, Integer> transfermap = new LinkedHashMap<>();

			for (int i = 0; i < numPeople; i++) {
				int person = randomGenerator.nextInt(personIds.size());
				PersonId personId = personIds.remove(person);
				RegionId prevRegionId = regionsDataManager.getPersonRegion(personId);
				RegionId nextRegionId = regionIds[i % 4];
				Pair<RegionId, RegionId> transfer = new Pair<>(prevRegionId, nextRegionId);
				int numTransfers = 1;
				if (transfermap.containsKey(transfer)) {
					numTransfers += transfermap.get(transfer);
				}
				transfermap.put(transfer, numTransfers);

				regionsDataManager.setPersonRegion(personId, nextRegionId);
			}

			for (Pair<RegionId, RegionId> regionTransfer : transfermap.keySet()) {
				RegionId sourceRegionId = regionTransfer.getFirst();
				RegionId destRegionId = regionTransfer.getSecond();
				int numTransfers = transfermap.get(regionTransfer);

				expectedReportItems.put(getReportItem(1, sourceRegionId, destRegionId, numTransfers), 1);
			}
		}));

		/*
		 * To get the report for day 1, must do 'something' on day 2 otherwise
		 * the report for the previous day won't print
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();
		List<Plugin> pluginsToAdd = RegionsTestPluginFactory.factory(0, 3054641152904904632L, TimeTrackingPolicy.TRACK_TIME, testPluginData).setRegionsPluginData(regionsPluginData).getPlugins();
		pluginsToAdd.add(ReportsTestPluginFactory.getPluginFromReport(new RegionTransferReport(REPORT_LABEL, ReportPeriod.DAILY)::init));

		TestSimulation.executeSimulation(pluginsToAdd, outputConsumer);

		assertTrue(outputConsumer.isComplete());
		Map<ReportItem, Integer> actualReportItems = outputConsumer.getOutputItems(ReportItem.class);

		// for (ReportItem reportItem : expectedReportItems.keySet()) {
		// System.out.println(reportItem.getValue(0) + "\t" +
		// reportItem.getValue(1) + "\t" + reportItem.getValue(2) + "\t" +
		// reportItem.getValue(3));
		//
		// }
		// System.out.println();
		// for (ReportItem reportItem : actualReportItems.keySet()) {
		//
		// System.out.println(reportItem.getValue(0) + "\t" +
		// reportItem.getValue(1) + "\t" + reportItem.getValue(2) + "\t" +
		// reportItem.getValue(3));
		// }

		assertEquals(expectedReportItems, actualReportItems);

		// precondition: Actor context is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			new RegionTransferReport(REPORT_LABEL, ReportPeriod.DAILY).init(null);
		});
		assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
	}

	private static ReportItem getReportItem(Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);
		builder.setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("region transfer report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("day").add("source_region").add("destination_region").add("transfers").build();
}
