package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestDataManager;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_PluginContext {

    private static class TestDataManager1 extends TestDataManager {

        @Override
        public void init(final DataManagerContext dataManagerContext) {
            super.init(dataManagerContext);
        }
    }

    private static class TestDataManager2 extends TestDataManager {

        @Override
        public void init(final DataManagerContext dataManagerContext) {
            super.init(dataManagerContext);
        }
    }

    private static class TestDataManager3 extends TestDataManager {

        @Override
        public void init(final DataManagerContext dataManagerContext) {
            super.init(dataManagerContext);
        }
    }

    @Test
    @UnitTestMethod(target = PluginContext.class, name = "addActor", args = { Consumer.class })
    public void testAddActor() {

        /*
         * Create a plugin initializer that will add a few actors. Each actor
         * will signal when it has initialized and the initializer will record
         * that signal.
         */
        Set<ActorId> addedActors = new LinkedHashSet<>();
        int numberOfActorsToAdd = 5;

        /*
         * Create a plugin that has its initializer add 5 actors. Each actor
         * will retrieve its own actor id and record them
         */
        Plugin plugin = Plugin.builder()//
                .setPluginId(new SimplePluginId("plugin id"))//
                .setInitializer((c) -> {
                    for (int i = 0; i < numberOfActorsToAdd; i++) {
                        c.addActor((c2) -> {
                            addedActors.add(c2.getActorId());
                        });
                    }
                })//
                .build();//

        // build and execute the simulation
        Simulation.builder()//
                .addPlugin(plugin)//
                .build()//
                .execute();//

        // show that the correct number of actors were added to the simulation
        assertEquals(numberOfActorsToAdd, addedActors.size());
    }

    @Test
    @UnitTestMethod(target = PluginContext.class, name = "addDataManager", args = { DataManager.class })
    public void testAddDataManager() {

        /*
         * The TestPluginInitialzer uses the PluginContext to add data managers.
         * If we add data managers via the TestPlugin and have an actor show
         * that each data manager exists during the simulation run, we can infer
         * that the addDataManager of the plugin context must be working
         * correctly.
         */

        final MutableBoolean actorExecuted = new MutableBoolean();

        // add the actors to the action plugin
        final TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
        pluginDataBuilder.addTestDataManager("A", () -> new TestDataManager1());
        pluginDataBuilder.addTestDataManager("B", () -> new TestDataManager2());
        pluginDataBuilder.addTestDataManager("C", () -> new TestDataManager3());
        pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
            actorExecuted.setValue(true);
        }));

        // build the action plugin
        final TestPluginData testPluginData = pluginDataBuilder.build();
        Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

        // build and execute the engine
        Simulation.builder()//
                .addPlugin(testPlugin)//
                .build()//
                .execute();//

        // show that the assertions were executed
        assertTrue(actorExecuted.getValue());

    }

    private static class PluginData1 implements PluginData {

        @Override
        public PluginDataBuilder getCloneBuilder() {
            throw new UnsupportedOperationException();
        }

    }

    private static class PluginData2 implements PluginData {

        @Override
        public PluginDataBuilder getCloneBuilder() {
            throw new UnsupportedOperationException();
        }
    }

    private static class PluginData3 implements PluginData {
        @Override
        public PluginDataBuilder getCloneBuilder() {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    @UnitTestMethod(target = PluginContext.class, name = "getPluginData", args = { Class.class })
    public void testGetPluginData() {

        MutableBoolean assertionsExecuted = new MutableBoolean();

        /*
         * Create a plugin with an initialization method that that execute the
         * getPluginData method for each three plugin data items.
         */
        Plugin plugin = Plugin.builder()//
                .setPluginId(new SimplePluginId("plugin id"))//
                .setInitializer((c) -> {
                    assertNotNull(c.getPluginData(PluginData1.class));
                    assertNotNull(c.getPluginData(PluginData2.class));
                    assertNotNull(c.getPluginData(PluginData3.class));

                    ContractException contractException = assertThrows(ContractException.class, () -> {
                        c.getPluginData(PluginData.class);
                    });

                    assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS, contractException.getErrorType());

                    contractException = assertThrows(ContractException.class, () -> {
                        c.getPluginData(null);
                    });

                    assertEquals(NucleusError.NULL_PLUGIN_DATA_CLASS, contractException.getErrorType());

                    assertionsExecuted.setValue(true);
                })//
                .addPluginData(new PluginData1())//
                .addPluginData(new PluginData2())//
                .addPluginData(new PluginData3())//
                .build();//

        Simulation.builder()//
                .addPlugin(plugin)//
                .build()//
                .execute();//

        // show that the initializer assertions were executed
        assertTrue(assertionsExecuted.getValue());

    }

    @Test
    @UnitTestMethod(target = PluginContext.class, name = "addReport", args = { Consumer.class })
    public void testAddReport() {

        /*
         * Create a plugin initializer that will add a few reports. Each report
         * will signal when it has initialized and the initializer will record
         * that signal.
         */
        Set<ReportId> addedReports = new LinkedHashSet<>();
        int numberOfReportsToAdd = 5;

        /*
         * Create a plugin that has its initializer add 5 actors. Each actor
         * will retrieve its own actor id and record them
         */
        Plugin plugin = Plugin.builder()//
                .setPluginId(new SimplePluginId("plugin id"))//
                .setInitializer((c) -> {
                    for (int i = 0; i < numberOfReportsToAdd; i++) {
                        c.addReport((c2) -> {
                            addedReports.add(c2.getReportId());
                        });
                    }
                })//
                .build();//

        // build and execute the simulation
        Simulation.builder()//
                .addPlugin(plugin)//
                .build()//
                .execute();//

        // show that the correct number of actors were added to the simulation
        assertEquals(numberOfReportsToAdd, addedReports.size());
    }

    @Test
    @UnitTestMethod(target = PluginContext.class, name = "getPluginDatas", args = { Class.class })
    public void testGetPluginDatas() {
        MutableBoolean assertionsExecuted = new MutableBoolean();

        /*
         * Create a plugin with an initialization method that that execute the
         * getPluginData method for each three plugin data items.
         */
        Plugin plugin = Plugin.builder()//
                .setPluginId(new SimplePluginId("plugin id"))//
                .setInitializer((c) -> {
                    assertNotNull(c.getPluginDatas(PluginData1.class));
                    assertNotNull(c.getPluginDatas(PluginData2.class));
                    assertNotNull(c.getPluginDatas(PluginData3.class));

                    ContractException contractException = assertThrows(ContractException.class, () -> {
                        c.getPluginDatas(null);
                    });

                    assertEquals(NucleusError.NULL_PLUGIN_DATA_CLASS, contractException.getErrorType());

                    assertionsExecuted.setValue(true);
                })//
                .addPluginData(new PluginData1())//
                .addPluginData(new PluginData2())//
                .addPluginData(new PluginData3())//
                .build();//

        Simulation.builder()//
                .addPlugin(plugin)//
                .build()//
                .execute();//

        // show that the initializer assertions were executed
        assertTrue(assertionsExecuted.getValue());
    }
}
