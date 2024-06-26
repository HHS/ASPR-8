package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.TestFactoryUtil;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.PartitionsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.PartitionsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.AttributesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.AttributesPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.TestAttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.testsupport.TestRandomGeneratorId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_PartitionsTestPluginFactory {

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            Consumer.class })
    public void testFtestFactory_Consumeractory1() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = PartitionsTestPluginFactory.factory(100, 9029198675932589278L, c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> PartitionsTestPluginFactory.factory(0, 0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();

        Factory factory = PartitionsTestPluginFactory.factory(100, 2990359774692004249L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> PartitionsTestPluginFactory.factory(0, 0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
        }).getPlugins();
        assertEquals(5, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, AttributesPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, PartitionsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setAttributesPluginData", args = {
            AttributesPluginData.class })
    public void testSetAttributesPluginData() {
        AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
        for (TestAttributeId testAttributeId : TestAttributeId.values()) {
            attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
        }

        AttributesPluginData attributesPluginData = attributesBuilder.build();

        List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
        }).setAttributesPluginData(attributesPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, attributesPluginData, AttributesPluginId.PLUGIN_ID);

        // precondition: attributesPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PartitionsTestPluginFactory.factory(0, 0, t -> {
                }).setAttributesPluginData(null));
        assertEquals(AttributeError.NULL_ATTRIBUTES_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setPartitionsPlugin", args = {
            Plugin.class })
    public void testSetPartitionsPlugin() {
        Plugin partitionsPlugin = PartitionsPlugin.builder()//
                .setPartitionsPluginData(PartitionsPluginData.builder().build())//
                .addPluginDependency(AttributesPluginId.PLUGIN_ID)//
                .getPartitionsPlugin();

        List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
        }).setPartitionsPlugin(partitionsPlugin).getPlugins();

        Plugin actualPlugin = TestFactoryUtil.checkPluginExists(plugins, PartitionsPluginId.PLUGIN_ID);
        assertTrue(partitionsPlugin == actualPlugin);

        // precondition: partitionsPlugin is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PartitionsTestPluginFactory.factory(0, 0, t -> {
                }).setPartitionsPlugin(null));
        assertEquals(PartitionError.NULL_PARTITION_PLUGIN, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));

        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
        }).setPeoplePluginData(peoplePluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PartitionsTestPluginFactory.factory(0, 0, t -> {
                }).setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = WellState.builder().setSeed(2990359774692004249L).build();
        builder.setMainRNGState(wellState);

        wellState = WellState.builder().setSeed(450787180090162111L).build();
        builder.addRNG(TestRandomGeneratorId.BLITZEN, wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = PartitionsTestPluginFactory.factory(0, 0, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PartitionsTestPluginFactory.factory(0, 0, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardAttributesPluginData", args = {})
    public void testGetStandardAttributesPluginData() {

        AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
        for (TestAttributeId testAttributeId : TestAttributeId.values()) {
            attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
        }

        AttributesPluginData expectedPluginData = attributesBuilder.build();
        AttributesPluginData actualPluginData = PartitionsTestPluginFactory.getStandardAttributesPluginData();

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardPartitionsPlugin", args = {})
    public void testGetStandardPartitionsPlugin() {

        Plugin partitionsPlugin = PartitionsTestPluginFactory.getStandardPartitionsPlugin();
        assertTrue(partitionsPlugin.getPluginDependencies().contains(AttributesPluginId.PLUGIN_ID));
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
            int.class })
    public void testGetStandardPeoplePluginData() {

        int initialPopulation = 100;

        PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
        if (initialPopulation > 0) {
            peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
        }

        PeoplePluginData expectedPluginData = peopleBuilder.build();
        PeoplePluginData actualPluginData = PartitionsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);
        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = PartitionsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 7995349318419680542L;

        WellState wellState = WellState.builder().setSeed(seed).build();

        StochasticsPluginData expectedPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
        StochasticsPluginData actualPluginData = PartitionsTestPluginFactory.getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }
}
