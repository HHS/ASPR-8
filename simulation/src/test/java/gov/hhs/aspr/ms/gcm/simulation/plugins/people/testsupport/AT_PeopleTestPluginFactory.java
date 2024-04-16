package gov.hhs.aspr.ms.gcm.simulation.plugins.people.testsupport;

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
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.testsupport.PeopleTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_PeopleTestPluginFactory {

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.class, name = "factory", args = { long.class, Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = PeopleTestPluginFactory.factory(6489240163414718858L, c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> PeopleTestPluginFactory.factory(0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.class, name = "factory", args = { long.class,
            TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();

        Factory factory = PeopleTestPluginFactory.factory(3745668053390022091L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> PeopleTestPluginFactory.factory(0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = PeopleTestPluginFactory.factory(0, t -> {
        }).getPlugins();

        assertEquals(3, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);

    }

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));

        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = PeopleTestPluginFactory.factory(0, t -> {
        }).setPeoplePluginData(peoplePluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PeopleTestPluginFactory.factory(0, t -> {
                }).setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = WellState.builder().setSeed(2758378374654665699L).build();
        builder.setMainRNGState(wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = PeopleTestPluginFactory.factory(0, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PeopleTestPluginFactory.factory(0, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {})
    public void testGetStandardPeoplePluginData() {

        PeoplePluginData expectedPluginData = PeoplePluginData.builder().build();
        PeoplePluginData actualPluginData = PeopleTestPluginFactory.getStandardPeoplePluginData();
        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = PeopleTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 6072871729256538807L;

        WellState wellState = WellState.builder().setSeed(seed).build();

        StochasticsPluginData expectedPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
        StochasticsPluginData actualPluginData = PeopleTestPluginFactory.getStandardStochasticsPluginData(seed);
        assertEquals(expectedPluginData, actualPluginData);
    }

}