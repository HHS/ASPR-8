package plugins.regions.actors;

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

import nucleus.ActorContext;
import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.ExperimentPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = RegionTransferReport.class)
public class AT_RegionTransferReport {

    @Test
    @UnitTestConstructor(args = { ReportId.class, ReportPeriod.class })
    public void testConstructor() {
        RegionTransferReport regionTransferReport = new RegionTransferReport(REPORT_ID, ReportPeriod.DAILY);

        // Show not null
        assertNotNull(regionTransferReport);

        // precondition: null report period
        ContractException contractException = assertThrows(ContractException.class,
                () -> new RegionTransferReport(REPORT_ID, null));
        assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

        // precondition: null report id
        contractException = assertThrows(ContractException.class,
                () -> new RegionTransferReport(null, ReportPeriod.DAILY));
        assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name = "init", args = { ActorContext.class })
    public void testInit() {

        Experiment.Builder builder = Experiment.builder();

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
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class)
                .build();
        regionBuilder.defineRegionProperty(prop_age, propertyDefinition);

        RegionsPluginData regionsPluginData = regionBuilder.build();
        builder.addPlugin(RegionsPlugin.getRegionsPlugin(regionsPluginData));

        // add the report, setting it report daily
        ReportsPluginData reportsPluginData = ReportsPluginData.builder()//
                .addReport(() -> new RegionTransferReport(REPORT_ID, ReportPeriod.DAILY)::init)//
                .build();//

        builder.addPlugin(ReportsPlugin.getReportsPlugin(reportsPluginData));

        // add remaining plugins
        builder.addPlugin(PeoplePlugin.getPeoplePlugin(PeoplePluginData.builder().build()));
        builder.addPlugin(StochasticsPlugin
                .getStochasticsPlugin(StochasticsPluginData.builder().setSeed(3054641152904904632L).build()));

        TestPluginData.Builder pluginBuilder = TestPluginData.builder();

        /*
         * Collect the expected report items. Note that order does not matter. *
         */
        Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();
        final int numPeople = 100;
        // create an actor to add people to the simulation
        // This will test adding people to a region, which on the report will say that
        // the person transfer to and from the same region
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            RegionId[] regionIds = { regionA, regionB, regionC, regionD };
            for (int i = 0; i < numPeople; i++) {
                PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionIds[i % 4])
                        .build();
                peopleDataManager.addPerson(personConstructionData);
            }
            expectedReportItems.put(getReportItem(0, regionA, regionA, 25), 1);
            expectedReportItems.put(getReportItem(0, regionB, regionB, 25), 1);
            expectedReportItems.put(getReportItem(0, regionC, regionC, 25), 1);
            expectedReportItems.put(getReportItem(0, regionD, regionD, 25), 1);
        }));

        // create an actor to move people from one region to another
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
            RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();

            List<PersonId> personIds = peopleDataManager.getPeople();
            // randomly move 25 people each to region a, region b, region c and region d
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

        // To get the report for day 1, must do 'something' on day 2 otherwise the
        // report for the previous day won't print
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
        }));

        TestPluginData testPluginData = pluginBuilder.build();
        Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
        builder.addPlugin(testPlugin);

        // build and execute the engine
        ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();

        TestReportItemOutputConsumer reportItemOutputConsumer = new TestReportItemOutputConsumer();
        builder.addExperimentContextConsumer(reportItemOutputConsumer::init);
        builder.addExperimentContextConsumer(experimentPlanCompletionObserver::init);
        builder.reportProgressToConsole(false);
        builder.reportFailuresToConsole(false);
        builder.build().execute();

        // show that all actions were executed
        assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).isPresent());
        assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).get().isComplete());

        Map<ReportItem, Integer> actualReportItems = reportItemOutputConsumer.getReportItems().get(0);

        assertEquals(expectedReportItems, actualReportItems);

        // precondition: Actor context is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            new RegionTransferReport(REPORT_ID, ReportPeriod.DAILY).init(null);
        });
        assertEquals(ReportError.NULL_CONTEXT, contractException.getErrorType());
    }

    private static ReportItem getReportItem(Object... values) {
        ReportItem.Builder builder = ReportItem.builder();
        builder.setReportId(REPORT_ID);
        builder.setReportHeader(REPORT_HEADER);
        for (Object value : values) {
            builder.addValue(value);
        }
        return builder.build();
    }

    private static final ReportId REPORT_ID = new SimpleReportId("region transfer report");

    private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("day").add("source_region")
            .add("destination_region")
            .add("transfers").build();
}
