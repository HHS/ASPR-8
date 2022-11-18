package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

@UnitTest(target = RegionsActionSupport.class)
public class AT_RegionsActionSupport {
    @Test
    @UnitTestMethod(name = "testConsumer", args = { int.class, long.class, TimeTrackingPolicy.class, Consumer.class })
    public void testTestConsumer() {
        MutableBoolean executed = new MutableBoolean();
        RegionsActionSupport.testConsumer(100, 5785172948650781925L, TimeTrackingPolicy.TRACK_TIME, (c) -> {

            // show that there are 100 people
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            assertEquals(100, peopleDataManager.getPopulationCount());

            // show that time tracking policy
            RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
            assertEquals(TimeTrackingPolicy.TRACK_TIME, regionsDataManager.getPersonRegionArrivalTrackingPolicy());

            // show that there are regions
            assertTrue(!regionsDataManager.getRegionIds().isEmpty());

            // show that each region has a person
            for (RegionId regionId : regionsDataManager.getRegionIds()) {
                assertTrue(!regionsDataManager.getPeopleInRegion(regionId).isEmpty());
            }

            executed.setValue(true);
        });

        assertTrue(executed.getValue());
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = { int.class, long.class, TimeTrackingPolicy.class, Plugin.class })
    public void testTestConsumers() {
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsActionSupport.testConsumers(100, 5166994853007999229L, TimeTrackingPolicy.TRACK_TIME,
                        TestPlugin.getTestPlugin(TestPluginData.builder().build())));
        assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());
    }
}
