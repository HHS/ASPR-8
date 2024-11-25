package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.SimpleRegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.SimpleRegionPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.RegionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_RegionTransferReport {

	@Test
	@UnitTestConstructor(target = RegionTransferReport.class, args = { RegionTransferReportPluginData.class})
	public void testConstructor() {
		RegionTransferReport regionTransferReport = new RegionTransferReport(RegionTransferReportPluginData.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.DAILY).build());

		// Show not null
		assertNotNull(regionTransferReport);

		// precondition: null report period
		assertThrows(NullPointerException.class, () -> new RegionTransferReport(null));
	}

	@Test
	@UnitTestMethod(target = RegionTransferReport.class, name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
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

			testOutputConsumer.accept(getReportItem(1, regionA, regionA, 25));
			testOutputConsumer.accept(getReportItem(1, regionB, regionB, 25));
			testOutputConsumer.accept(getReportItem(1, regionC, regionC, 25));
			testOutputConsumer.accept(getReportItem(1, regionD, regionD, 25));
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
			Map<Pair<RegionId, RegionId>, Integer> transferMap = new LinkedHashMap<>();

			for (int i = 0; i < numPeople; i++) {
				int person = randomGenerator.nextInt(personIds.size());
				PersonId personId = personIds.remove(person);
				RegionId prevRegionId = regionsDataManager.getPersonRegion(personId);
				RegionId nextRegionId = regionIds[i % 4];
				Pair<RegionId, RegionId> transfer = new Pair<>(prevRegionId, nextRegionId);
				int numTransfers = 1;
				if (transferMap.containsKey(transfer)) {
					numTransfers += transferMap.get(transfer);
				}
				transferMap.put(transfer, numTransfers);

				regionsDataManager.setPersonRegion(personId, nextRegionId);
			}

			for (Pair<RegionId, RegionId> regionTransfer : transferMap.keySet()) {
				RegionId sourceRegionId = regionTransfer.getFirst();
				RegionId destRegionId = regionTransfer.getSecond();
				int numTransfers = transferMap.get(regionTransfer);

				testOutputConsumer.accept(getReportItem(2, sourceRegionId, destRegionId, numTransfers));
			}
		}));

		/*
		 * To get the report for day 1, must do 'something' on day 2 otherwise
		 * the report for the previous day won't print
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		
		RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.DAILY).build();
		
		Factory factory = RegionsTestPluginFactory//
				.factory(0, 3054641152904904632L, true, testPluginData)
				.setRegionsPluginData(regionsPluginData)//
				.setRegionTransferReportPluginData(regionTransferReportPluginData);

		TestOutputConsumer actualConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();

		Map<ReportItem, Integer> expectedReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = actualConsumer.getOutputItemMap(ReportItem.class);

		assertEquals(expectedReportItems, actualReportItems);

		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertEquals(REPORT_HEADER, reportHeader);

		// precondition: Actor context is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			new RegionTransferReport(RegionTransferReportPluginData.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.DAILY).build()).init(null);
		});
		assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionTransferReport.class, name = "init", args = { ReportContext.class })
	public void testInit_State() {
		// Test with producing simulation state

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
			Map<Pair<RegionId, RegionId>, Integer> transferMap = new LinkedHashMap<>();

			for (int i = 0; i < numPeople; i++) {
				int person = randomGenerator.nextInt(personIds.size());
				PersonId personId = personIds.remove(person);
				RegionId prevRegionId = regionsDataManager.getPersonRegion(personId);
				RegionId nextRegionId = regionIds[i % 4];
				Pair<RegionId, RegionId> transfer = new Pair<>(prevRegionId, nextRegionId);
				int numTransfers = 1;
				if (transferMap.containsKey(transfer)) {
					numTransfers += transferMap.get(transfer);
				}
				transferMap.put(transfer, numTransfers);

				regionsDataManager.setPersonRegion(personId, nextRegionId);
			}
		}));

		/*
		 * To get the report for day 1, must do 'something' on day 2 otherwise
		 * the report for the previous day won't print
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData.builder()
				.setReportLabel(REPORT_LABEL)
				.setReportPeriod(ReportPeriod.DAILY)
				.build();

		Factory factory = RegionsTestPluginFactory//
				.factory(0, 3054641152904904632L, true, testPluginData).setRegionsPluginData(regionsPluginData)//
				.setRegionTransferReportPluginData(regionTransferReportPluginData);

		TestOutputConsumer actualConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		Map<RegionTransferReportPluginData, Integer> outputItems = actualConsumer.getOutputItemMap(RegionTransferReportPluginData.class);
		assertEquals(1, outputItems.size());
		RegionTransferReportPluginData regionTransferReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(regionTransferReportPluginData, regionTransferReportPluginData2);

		// Test without producing simulation state

		actualConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		outputItems = actualConsumer.getOutputItemMap(RegionTransferReportPluginData.class);
		assertEquals(0, outputItems.size());
	}


	private static ReportItem getReportItem(Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("region transfer report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("day").add("source_region").add("destination_region").add("transfers").build();
}
