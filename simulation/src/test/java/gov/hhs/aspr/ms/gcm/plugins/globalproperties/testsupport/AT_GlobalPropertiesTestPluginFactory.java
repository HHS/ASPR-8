package gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.TestFactoryUtil;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginId;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.GlobalPropertiesPluginId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertiesError;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.SimpleGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_GlobalPropertiesTestPluginFactory {

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { long.class,
            Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "factory", args = { long.class,
            TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {

        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
        }).getPlugins();
        assertEquals(2, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, GlobalPropertiesPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "setGlobalPropertyReportPluginData", args = {
            GlobalPropertyReportPluginData.class })
    public void testSetGlobalPropertyReportPluginData() {
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        builder.setDefaultInclusion(false).setReportLabel(new SimpleReportLabel("global property report"));

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1086184935572375203L);
        for (GlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
            if (randomGenerator.nextBoolean()) {
                builder.includeGlobalProperty(globalPropertyId);
            } else {
                builder.excludeGlobalProperty(globalPropertyId);
            }
        }

        GlobalPropertyReportPluginData pluginData = builder.build();

        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
        }).setGlobalPropertyReportPluginData(pluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, pluginData, GlobalPropertiesPluginId.PLUGIN_ID);

        // precondition: globalPropReportPluginData is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
                }).setGlobalPropertyReportPluginData(null));
        assertEquals(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.Factory.class, name = "setGlobalPropertiesPluginData", args = {
            GlobalPropertiesPluginData.class })
    public void testSetGlobalPropertiesPluginData() {
        GlobalPropertiesPluginData.Builder initialDatabuilder = GlobalPropertiesPluginData.builder();

        GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3)
                .build();
        initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition, 0);

        GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
        propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
        initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition, 0);

        GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
        propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
        initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition, 0);

        GlobalPropertiesPluginData globalPropertiesPluginData = initialDatabuilder.build();

        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
        }).setGlobalPropertiesPluginData(globalPropertiesPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, globalPropertiesPluginData, GlobalPropertiesPluginId.PLUGIN_ID);

        // precondition: globalPropertiesPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertiesTestPluginFactory.factory(2050026532065791481L, t -> {
                }).setGlobalPropertiesPluginData(null));
        assertEquals(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesTestPluginFactory.class, name = "getStandardGlobalPropertiesPluginData", args = {
            long.class })
    public void testGetStandardGlobalPropertiesPluginData() {
        long seed = 2376369840099946020L;

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        GlobalPropertiesPluginData actualPluginData = GlobalPropertiesTestPluginFactory
                .getStandardGlobalPropertiesPluginData(seed);

        Set<TestGlobalPropertyId> expectedPropertyIds = EnumSet.allOf(TestGlobalPropertyId.class);
        assertFalse(expectedPropertyIds.isEmpty());

        Set<GlobalPropertyId> actualGlobalPropertyIds = actualPluginData.getGlobalPropertyIds();
        assertEquals(expectedPropertyIds, actualGlobalPropertyIds);

        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.getGlobalPropertyIds()) {
            builder.defineGlobalProperty(testGlobalPropertyId, testGlobalPropertyId.getPropertyDefinition(), 0);
            boolean hasDefaultValue = testGlobalPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
            if (!hasDefaultValue) {
                builder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getRandomPropertyValue(randomGenerator), 0);
            }
        }

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId
                .getShuffledGlobalPropertyIds(randomGenerator)) {

            boolean hasDefaultValue = testGlobalPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
            boolean setValue = randomGenerator.nextBoolean();
            if (hasDefaultValue && setValue) {
                // set a value to the default
                builder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getPropertyDefinition().getDefaultValue().get(), 0);
            } else if (setValue) {
                // set a value to not the default
                builder.setGlobalPropertyValue(testGlobalPropertyId,
                        testGlobalPropertyId.getRandomPropertyValue(randomGenerator), 0);
            }
        }

        GlobalPropertiesPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData, actualPluginData);

    }

}