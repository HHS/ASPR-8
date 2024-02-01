package gov.hhs.aspr.ms.gcm.plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
import gov.hhs.aspr.ms.gcm.plugins.materials.MaterialsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.materials.reports.BatchStatusReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.materials.reports.StageReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.MaterialsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.resources.ResourcesPluginId;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourcePropertyId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_MaterialsTestPluginFactory {

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class,
            int.class, long.class, Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 3328026739613106739L,
                c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "factory", args = { int.class, int.class,
            int.class, long.class, TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();

        Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 7995349318419680542L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).getPlugins();
        assertEquals(6, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, MaterialsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, ResourcesPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setMaterialsPluginData", args = {
            MaterialsPluginData.class })
    public void testSetMaterialsPluginData() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4328791012645031581L);
        MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

        for (TestMaterialId testMaterialId : TestMaterialId.values()) {
            materialsBuilder.addMaterial(testMaterialId);
        }

        for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
            materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);
        }

        for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
                .values()) {
            materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
                    testMaterialsProducerPropertyId.getPropertyDefinition());
        }

        for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
                .getPropertiesWithoutDefaultValues()) {
            for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
                Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
                materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId,
                        testMaterialsProducerPropertyId, randomPropertyValue);
            }
        }

        for (TestMaterialId testMaterialId : TestMaterialId.values()) {
            Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
            for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
                materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId,
                        testBatchPropertyId.getPropertyDefinition());
            }
        }

        MaterialsPluginData materialsPluginData = materialsBuilder.build();

        List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setMaterialsPluginData(materialsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, materialsPluginData, MaterialsPluginId.PLUGIN_ID);

        // precondition: materialsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setMaterialsPluginData(null));
        assertEquals(MaterialsError.NULL_MATERIALS_PLUGIN_DATA, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setResourcesPluginData", args = {
            ResourcesPluginData.class })
    public void testSetResourcesPluginData() {
        ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

        ResourcesPluginData resourcesPluginData = builder.build();

        List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setResourcesPluginData(resourcesPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, resourcesPluginData, ResourcesPluginId.PLUGIN_ID);

        // precondition: resourcesPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setResourcesPluginData(null));
        assertEquals(ResourceError.NULL_RESOURCE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
            RegionsPluginData.class })
    public void testSetRegionsPluginData() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(968101337385656117L);
        int initialPopulation = 30;
        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        // add the region plugin
        RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
        for (TestRegionId regionId : TestRegionId.values()) {
            regionPluginBuilder.addRegion(regionId);
        }

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
            regionPluginBuilder.defineRegionProperty(testRegionPropertyId,
                    testRegionPropertyId.getPropertyDefinition());
        }

        for (TestRegionId regionId : TestRegionId.values()) {
            for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
                if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
                        || randomGenerator.nextBoolean()) {
                    Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, randomPropertyValue);
                }
            }
        }
        TestRegionId testRegionId = TestRegionId.REGION_1;
        for (PersonId personId : people) {
            regionPluginBuilder.addPerson(personId, testRegionId, 0.0);
            testRegionId = testRegionId.next();
        }

        regionPluginBuilder.setPersonRegionArrivalTracking(true);

        RegionsPluginData regionsPluginData = regionPluginBuilder.build();

        List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setRegionsPluginData(regionsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

        // precondition: regionsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setRegionsPluginData(null));
        assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setPeoplePluginData(peoplePluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = WellState.builder().setSeed(2990359774692004249L).build();
        builder.setMainRNGState(wellState);

        wellState = WellState.builder().setSeed(1090094972994322820L).build();
        builder.addRNG(TestRandomGeneratorId.BLITZEN, wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardMaterialsPluginData", args = {
            int.class, int.class, int.class, long.class })
    public void testGetStandardMaterialsPluginData() {
        int numBatches = 50;
        int numStages = 10;
        int numBatchesInStage = 30;
        long seed = 9029198675932589278L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        MaterialsPluginData.Builder materialsBuilder = MaterialsPluginData.builder();

        int bId = 0;
        int sId = 0;
        for (TestMaterialId testMaterialId : TestMaterialId.values()) {
            materialsBuilder.addMaterial(testMaterialId);
        }

        for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

            List<BatchId> batches = new ArrayList<>();

            for (int i = 0; i < numBatches; i++) {

                TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
                double amount = randomGenerator.nextDouble();
                BatchId batchId = new BatchId(bId++);
                materialsBuilder.addBatch(batchId, testMaterialId, amount);
                batches.add(batchId);
                for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
                        .getTestBatchPropertyIds(testMaterialId)) {
                    boolean required = testBatchPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
                    if (required || randomGenerator.nextBoolean()) {
                        materialsBuilder.setBatchPropertyValue(batchId, testBatchPropertyId,
                                testBatchPropertyId.getRandomPropertyValue(randomGenerator));
                    }
                }

            }

            List<StageId> stages = new ArrayList<>();

            for (int i = 0; i < numStages; i++) {
                StageId stageId = new StageId(sId++);
                stages.add(stageId);
                boolean offered = i % 2 == 0;
                materialsBuilder.addStage(stageId, offered);
                materialsBuilder.addStageToMaterialProducer(stageId, testMaterialsProducerId);
            }

            Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
            for (int i = 0; i < numBatches; i++) {
                BatchId batchId = batches.get(i);
                if (i < numBatchesInStage) {
                    StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
                    materialsBuilder.addBatchToStage(stageId, batchId);
                } else {
                    materialsBuilder.addBatchToMaterialsProducerInventory(batchId, testMaterialsProducerId);
                }
            }
            materialsBuilder.addMaterialsProducerId(testMaterialsProducerId);

            for (ResourceId resourceId : TestResourceId.values()) {
                if (randomGenerator.nextBoolean()) {
                    materialsBuilder.setMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId,
                            randomGenerator.nextInt(10));
                } else {
					materialsBuilder.setMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId, 0);
				}
            }
        }

        for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
                .values()) {
            materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId,
                    testMaterialsProducerPropertyId.getPropertyDefinition());
        }

        for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
                .getPropertiesWithoutDefaultValues()) {
            for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
                Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
                materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId,
                        testMaterialsProducerPropertyId, randomPropertyValue);
            }
        }

        for (TestMaterialId testMaterialId : TestMaterialId.values()) {
            Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
            for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
                materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId,
                        testBatchPropertyId.getPropertyDefinition());
            }
        }

        MaterialsPluginData expectedPluginData = materialsBuilder.build();

        MaterialsPluginData actualPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches,
                numStages, numBatchesInStage, seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardResourcesPluginData", args = {
            long.class })
    public void testGetStandardResourcesPluginData() {

        long seed = 4800551796983227153L;

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();

        for (TestResourceId testResourceId : TestResourceId.values()) {
            resourcesBuilder.addResource(testResourceId, 0.0, testResourceId.getTimeTrackingPolicy());
        }

        for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
            TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
            PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
            Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
            resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
            resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
        }

        ResourcesPluginData expectedPluginData = resourcesBuilder.build();
        ResourcesPluginData actualPluginData = MaterialsTestPluginFactory.getStandardResourcesPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {})
    public void testGetStandardRegionsPluginData() {

        RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
        for (TestRegionId testRegionId : TestRegionId.values()) {
            regionsBuilder.addRegion(testRegionId);
        }
        RegionsPluginData expectedPluginData = regionsBuilder.build();
        RegionsPluginData actualPluginData = MaterialsTestPluginFactory.getStandardRegionsPluginData();

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {})
    public void testGetStandardPeoplePluginData() {

        PeoplePluginData expectedPluginData = PeoplePluginData.builder().build();
        PeoplePluginData actualPluginData = MaterialsTestPluginFactory.getStandardPeoplePluginData();
        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 6072871729256538807L;

        WellState wellState = WellState.builder().setSeed(seed).build();

        StochasticsPluginData expectedPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
        StochasticsPluginData actualPluginData = MaterialsTestPluginFactory.getStandardStochasticsPluginData(seed);
        assertEquals(expectedPluginData, actualPluginData);
    }
    
    
 
 
    
    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setMaterialsProducerResourceReportPluginData", args = {
    		MaterialsProducerResourceReportPluginData.class })
    public void testSetMaterialsProducerResourceReportPluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();
        
        MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = MaterialsProducerResourceReportPluginData.builder()//
        		.setReportLabel(new SimpleReportLabel("MaterialsProducerResourceReport"))//        		
        		.build();
        

        Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        });
        factory.setPeoplePluginData(peoplePluginData);
        factory.setMaterialsProducerResourceReportPluginData(materialsProducerResourceReportPluginData);

        List<Plugin> plugins = factory.getPlugins();
        
        
        TestFactoryUtil.checkPluginDataExists(plugins, materialsProducerResourceReportPluginData, MaterialsPluginId.PLUGIN_ID);
    }
    
    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setMaterialsProducerPropertyReportPluginData", args = {
    		MaterialsProducerPropertyReportPluginData.class })
    public void testSetMaterialsProducerPropertyReportPluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();
        
        MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = MaterialsProducerPropertyReportPluginData.builder()//
        		.setReportLabel(new SimpleReportLabel("MaterialsProducerPropertyReport"))// 
        		.build();
        

        Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        });
        factory.setPeoplePluginData(peoplePluginData);
        factory.setMaterialsProducerPropertyReportPluginData(materialsProducerPropertyReportPluginData);

        List<Plugin> plugins = factory.getPlugins();
        
        
        TestFactoryUtil.checkPluginDataExists(plugins, materialsProducerPropertyReportPluginData, MaterialsPluginId.PLUGIN_ID);
    }
    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setStageReportPluginData", args = {
    		StageReportPluginData.class })
    public void testSetStageReportPluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();
        
        StageReportPluginData stageReportPluginData = StageReportPluginData.builder()//
        		.setReportLabel(new SimpleReportLabel("StageReport"))//        		
        		.build();
        

        Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        });
        factory.setPeoplePluginData(peoplePluginData);
        factory.setStageReportPluginData(stageReportPluginData);

        List<Plugin> plugins = factory.getPlugins();
        
        
        TestFactoryUtil.checkPluginDataExists(plugins, stageReportPluginData, MaterialsPluginId.PLUGIN_ID);
    }
    
    @Test
    @UnitTestMethod(target = MaterialsTestPluginFactory.Factory.class, name = "setBatchStatusReportPluginData", args = {
    		BatchStatusReportPluginData.class })
    public void testSetBatchStatusReportPluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();
        
        BatchStatusReportPluginData batchStatusReportPluginData = BatchStatusReportPluginData.builder()//
        		.setReportLabel(new SimpleReportLabel("BatchStatusReport"))//
        		.build();
        

        Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        });
        factory.setPeoplePluginData(peoplePluginData);
        factory.setBatchStatusReportPluginData(batchStatusReportPluginData);

        List<Plugin> plugins = factory.getPlugins();
        
        
        TestFactoryUtil.checkPluginDataExists(plugins, batchStatusReportPluginData, MaterialsPluginId.PLUGIN_ID);
    }

}
