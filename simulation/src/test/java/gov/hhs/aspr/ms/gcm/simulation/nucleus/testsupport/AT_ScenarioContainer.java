package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimulationState;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_ScenarioContainer {

    private static final class XPluginData implements PluginData {
        private final int value;

        public XPluginData(int value) {
            this.value = value;
        }

        @Override
        public PluginDataBuilder toBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            XPluginData other = (XPluginData) obj;
            return value == other.value;
        }
    }

    private static final class YPluginData implements PluginData {
        private final int value;

        public YPluginData(int value) {
            this.value = value;
        }

        @Override
        public PluginDataBuilder toBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            YPluginData other = (YPluginData) obj;
            return value == other.value;
        }
    }

    private static final class ZPluginData implements PluginData {
        private final int value;

        public ZPluginData(int value) {
            this.value = value;
        }

        @Override
        public PluginDataBuilder toBuilder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ZPluginData other = (ZPluginData) obj;
            return value == other.value;
        }
    }

    private static enum PluginIds implements PluginId {
        PLUGIN_ID_1, PLUGIN_ID_2, PLUGIN_ID_3, PLUGIN_ID_4, PLUGIN_ID_5;
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(ScenarioContainer.builder());
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "toBuilder", args = {})
    public void testToBuilder() {
        ScenarioContainer.Builder builder = ScenarioContainer.builder();

        // add a plugin
        Plugin plugin = Plugin.builder()//
                .addPluginData(new XPluginData(5))//
                .setPluginId(PluginIds.PLUGIN_ID_1)//
                .build();

        builder.addPlugin(plugin);

        // set simulation state
        SimulationState simState = SimulationState.builder()//
                .setBaseDate(LocalDate.of(2025, 5, 7))//
                .setStartTime(0.0)//
                .build();

        builder.setSimulationState(simState);

        ScenarioContainer scenarioContainer = builder.build();

        // show that the returned clone builder will build an identical instance if no
        // mutations are made
        ScenarioContainer.Builder cloneBuilder = scenarioContainer.toBuilder();
        assertNotNull(cloneBuilder);
        assertEquals(scenarioContainer, cloneBuilder.build());

        // show that the clone builder builds a distinct instance if any mutation is
        // made

        // addPlugin
        cloneBuilder = scenarioContainer.toBuilder();
        Plugin plugin2 = Plugin.builder()//
                .addPluginData(new XPluginData(99))//
                .setPluginId(PluginIds.PLUGIN_ID_2)//
                .build();
        cloneBuilder.addPlugin(plugin2);
        assertNotEquals(scenarioContainer, cloneBuilder.build());

        // setSimulationState
        cloneBuilder = scenarioContainer.toBuilder();
        SimulationState simState2 = SimulationState.builder()//
                .setBaseDate(LocalDate.of(2000, 10, 12))//
                .setStartTime(0.1)//
                .build();
        cloneBuilder.setSimulationState(simState2);
        assertNotEquals(scenarioContainer, cloneBuilder.build());
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "getSimulationState", args = {})
    public void testGetSimulationState() {
        testGetterSetterSimulationState(7334822934933740607L);
    }

    private void testGetterSetterSimulationState(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (int i = 0; i < 50; i++) {
            ScenarioContainer.Builder builder = ScenarioContainer.builder();

            SimulationState expectedValue = SimulationState.builder()//
                    .setBaseDate(LocalDate.of(randomGenerator.nextInt(20) + 2000, randomGenerator.nextInt(12) + 1,
                            randomGenerator.nextInt(28) + 1))//
                    .setStartTime(randomGenerator.nextDouble() * 100)//
                    .build();

            ScenarioContainer scenarioContainer = builder//
                    .setSimulationState(expectedValue)//
                    .build();

            SimulationState actualValue = scenarioContainer.getSimulationState();

            assertEquals(expectedValue, actualValue);
        }
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "getPlugin", args = { PluginId.class })
    public void testGetPlugin() {
        testGetterAdderPlugin(7334822399033740607L);
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        testGetterAdderPlugin(7334822392421740607L);
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "getPluginData", args = { Class.class })
    public void testGetPluginData() {
        testGetterAdderPlugin(7334822772341740607L);

        // precondition test: if the class reference is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            ScenarioContainer.Builder builder = ScenarioContainer.builder();
            Plugin plugin = Plugin.builder()//
                    .setPluginId(PluginIds.PLUGIN_ID_1)//
                    .addPluginData(new XPluginData(5))//
                    .build();

            builder.addPlugin(plugin);
            builder.build().getPluginData(null);
        });
        assertEquals(NucleusError.NULL_PLUGIN_DATA_CLASS, contractException.getErrorType());

        // precondition test: if more than one plugin data object matches the class reference
        contractException = assertThrows(ContractException.class, () -> {
            ScenarioContainer.Builder builder = ScenarioContainer.builder();
            Plugin plugin = Plugin.builder()//
                    .setPluginId(PluginIds.PLUGIN_ID_1)//
                    .addPluginData(new XPluginData(5))//
                    .addPluginData(new XPluginData(99))//
                    .build();

            builder.addPlugin(plugin);
            builder.build().getPluginData(XPluginData.class);
        });
        assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "getPluginDatas", args = { Class.class })
    public void testGetPluginDatas() {
        testGetterAdderPlugin(7334829908341740607L);

        // precondition test: if the class reference is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            ScenarioContainer.Builder builder = ScenarioContainer.builder();
            Plugin plugin = Plugin.builder()//
                    .setPluginId(PluginIds.PLUGIN_ID_1)//
                    .addPluginData(new XPluginData(5))//
                    .build();

            builder.addPlugin(plugin);
            builder.build().getPluginDatas(null);
        });
        assertEquals(NucleusError.NULL_PLUGIN_DATA_CLASS, contractException.getErrorType());
    }

    private void testGetterAdderPlugin(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (int i = 0; i < 50; i++) {
            ScenarioContainer.Builder scenarioContainerBuilder = ScenarioContainer.builder();
            Map<PluginId, Plugin> expectedPlugins = new LinkedHashMap<>();
            List<XPluginData> expectedXPluginDatas = new ArrayList<>();
            List<YPluginData> expectedYPluginDatas = new ArrayList<>();
            List<ZPluginData> expectedZPluginDatas = new ArrayList<>();
            boolean first = true;

            PluginIds[] pluginIdsValues = PluginIds.values();
            List<PluginIds> pluginIdsList = new ArrayList<>(List.of(pluginIdsValues));
            Random random = new Random(randomGenerator.nextLong());
            Collections.shuffle(pluginIdsList, random);

            int numOfPlugins = randomGenerator.nextInt(pluginIdsValues.length) + 1;
            for (int j = 0; j < numOfPlugins; j++) {
                Plugin.Builder pluginBuilder = Plugin.builder();
                PluginIds pluginIds = pluginIdsList.get(j);
                pluginBuilder.setPluginId(pluginIds);

                int numXPluginDatas = randomGenerator.nextInt(3) + 1;
                for (int k = 0; k < numXPluginDatas; k++) {
                    XPluginData xPluginData = new XPluginData(randomGenerator.nextInt());
                    pluginBuilder.addPluginData(xPluginData);
                    expectedXPluginDatas.add(xPluginData);
                }

                int numYPluginDatas = randomGenerator.nextInt(3) + 1;
                for (int k = 0; k < numYPluginDatas; k++) {
                    YPluginData yPluginData = new YPluginData(randomGenerator.nextInt());
                    pluginBuilder.addPluginData(yPluginData);
                    expectedYPluginDatas.add(yPluginData);
                }

                if (first) {
                    ZPluginData zPluginData = new ZPluginData(randomGenerator.nextInt());
                    pluginBuilder.addPluginData(zPluginData);
                    expectedZPluginDatas.add(zPluginData);
                    first = false;
                }

                Plugin plugin = pluginBuilder.build();
                scenarioContainerBuilder.addPlugin(plugin);
                expectedPlugins.put(plugin.getPluginId(), plugin);
            }

            // build the ScenarioContainer
            ScenarioContainer scenarioContainer = scenarioContainerBuilder.build();

            // getPlugins
            List<Plugin> actualPlugins = scenarioContainer.getPlugins();
            assertEquals(new ArrayList<>(expectedPlugins.values()), actualPlugins);

            // getPlugin
            for (PluginId pluginId : expectedPlugins.keySet()) {
                Optional<Plugin> actualPluginOptional = scenarioContainer.getPlugin(pluginId);
                assertTrue(actualPluginOptional.isPresent());
                assertEquals(expectedPlugins.get(pluginId), actualPluginOptional.get());
            }

            // getPluginDatas
            List<XPluginData> actualXPluginDatas = scenarioContainer.getPluginDatas(
                    XPluginData.class);
            assertEquals(expectedXPluginDatas, actualXPluginDatas);

            List<YPluginData> actualYPluginDatas = scenarioContainer.getPluginDatas(
                    YPluginData.class);
            assertEquals(expectedYPluginDatas, actualYPluginDatas);

            List<ZPluginData> actualZPluginDatas = scenarioContainer.getPluginDatas(
                    ZPluginData.class);
            assertEquals(expectedZPluginDatas, actualZPluginDatas);

            // getPluginData
            Optional<ZPluginData> zPluginDataOptional = scenarioContainer.getPluginData(
                    ZPluginData.class);
            assertTrue(zPluginDataOptional.isPresent());
            assertEquals(expectedZPluginDatas.get(0), zPluginDataOptional.get());
        }
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2645121388465183354L);

        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            ScenarioContainer scenarioContainer1 = getRandomScenarioContainer(
                    seed);
            ScenarioContainer scenarioContainer2 = getRandomScenarioContainer(
                    seed);

            assertEquals(scenarioContainer1, scenarioContainer2);
            assertEquals(scenarioContainer1.hashCode(), scenarioContainer2.hashCode());
        }

        // hash codes are reasonably distributed
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            ScenarioContainer scenarioContainer = getRandomScenarioContainer(
                    randomGenerator.nextLong());
            hashCodes.add(scenarioContainer.hashCode());
        }

        assertEquals(100, hashCodes.size());
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.class, name = "equals", args = { Object.class })
    public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7334224934933740607L);

        // never equal to another type
        for (int i = 0; i < 30; i++) {
            ScenarioContainer scenarioContainer = getRandomScenarioContainer(
                    randomGenerator.nextLong());
            assertFalse(scenarioContainer.equals(new Object()));
        }

        // never equal to null
        for (int i = 0; i < 30; i++) {
            ScenarioContainer scenarioContainer = getRandomScenarioContainer(
                    randomGenerator.nextLong());
            assertFalse(scenarioContainer.equals(null));
        }

        // reflexive
        for (int i = 0; i < 30; i++) {
            ScenarioContainer scenarioContainer = getRandomScenarioContainer(
                    randomGenerator.nextLong());
            assertTrue(scenarioContainer.equals(scenarioContainer));
        }

        // symmetric, transitive, consistent
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            ScenarioContainer scenarioContainer1 = getRandomScenarioContainer(
                    seed);
            ScenarioContainer scenarioContainer2 = getRandomScenarioContainer(
                    seed);
            assertFalse(scenarioContainer1 == scenarioContainer2);
            for (int j = 0; j < 10; j++) {
                assertTrue(scenarioContainer1.equals(scenarioContainer2));
                assertTrue(scenarioContainer2.equals(scenarioContainer1));
            }
        }

        // different inputs yield unequal objects
        Set<ScenarioContainer> set = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            ScenarioContainer scenarioContainer = getRandomScenarioContainer(
                    randomGenerator.nextLong());
            set.add(scenarioContainer);
        }
        assertEquals(100, set.size());
    }

    private ScenarioContainer getRandomScenarioContainer(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        ScenarioContainer.Builder builder = ScenarioContainer.builder();

        SimulationState simState = SimulationState.builder()//
                .setBaseDate(LocalDate.of(randomGenerator.nextInt(20) + 2000, randomGenerator.nextInt(12) + 1,
                        randomGenerator.nextInt(28) + 1))//
                .setStartTime(randomGenerator.nextDouble() * 100)//
                .build();

        builder.setSimulationState(simState);

        PluginIds[] pluginIdsValues = PluginIds.values();
        List<PluginIds> pluginIdsList = new ArrayList<>(List.of(pluginIdsValues));
        Random random = new Random(randomGenerator.nextLong());
        Collections.shuffle(pluginIdsList, random);

        int numOfPlugins = randomGenerator.nextInt(pluginIdsValues.length);
        for (int j = 0; j < numOfPlugins; j++) {
            Plugin.Builder pluginBuilder = Plugin.builder();
            PluginIds pluginIds = pluginIdsList.get(j);
            pluginBuilder.setPluginId(pluginIds);

            int numXPluginDatas = randomGenerator.nextInt(3) + 1;
            for (int k = 0; k < numXPluginDatas; k++) {
                XPluginData xPluginData = new XPluginData(randomGenerator.nextInt());
                pluginBuilder.addPluginData(xPluginData);
            }

            int numYPluginDatas = randomGenerator.nextInt(3) + 1;
            for (int k = 0; k < numYPluginDatas; k++) {
                YPluginData yPluginData = new YPluginData(randomGenerator.nextInt());
                pluginBuilder.addPluginData(yPluginData);
            }

            int numZPluginDatas = randomGenerator.nextInt(3) + 1;
            for (int k = 0; k < numZPluginDatas; k++) {
                ZPluginData zPluginData = new ZPluginData(randomGenerator.nextInt());
                pluginBuilder.addPluginData(zPluginData);
            }

            Plugin plugin = pluginBuilder.build();
            builder.addPlugin(plugin);
        }

        return builder.build();
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.Builder.class, name = "build", args = {})
    public void testBuild() {
        // no preconditions to test
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.Builder.class, name = "setSimulationState", args = {
            SimulationState.class })
    public void testSetSimulationState() {
        testGetterSetterSimulationState(7334855434933740607L);

        // precondition test: if the simulation state is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            ScenarioContainer.Builder builder = ScenarioContainer.builder();
            builder.setSimulationState(null);
        });
        assertEquals(NucleusError.NULL_SIMULATION_STATE, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = ScenarioContainer.Builder.class, name = "addPlugin", args = { Plugin.class })
    public void testAddPlugin() {
        testGetterAdderPlugin(7334839908341740607L);

        // precondition test: if the plugin is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            ScenarioContainer.Builder builder = ScenarioContainer.builder();
            builder.addPlugin(null);
        });
        assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());
    }
}
