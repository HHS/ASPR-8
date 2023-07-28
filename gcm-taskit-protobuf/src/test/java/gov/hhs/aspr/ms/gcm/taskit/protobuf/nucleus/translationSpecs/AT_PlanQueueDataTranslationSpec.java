package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.ExamplePlanData;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.nucleus.PlanQueueData;
import gov.hhs.aspr.ms.gcm.nucleus.Planner;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_PlanQueueDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = PlanQueueDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PlanQueueDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(NucleusTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        PlanQueueDataTranslationSpec translationSpec = new PlanQueueDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6625494580697137579L);

        long arrivalId = randomGenerator.nextLong();

        ExamplePlanData examplePlanData = new ExamplePlanData(15);
        Planner planner = Planner.DATA_MANAGER;
        double time = 10.0;
        Object key = "key";
        int plannerId = 0;
        arrivalId += 1;

        PlanQueueData.Builder planQueueBuilder = PlanQueueData.builder();

        planQueueBuilder
                .setArrivalId(arrivalId)
                .setKey(key)
                .setPlanData(examplePlanData)
                .setPlanner(planner)
                .setPlannerId(plannerId)
                .setTime(time);

        PlanQueueData expectedAppValue = planQueueBuilder.build();

        PlanQueueDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PlanQueueData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        inputValue = inputValue.toBuilder().setActive(false).build();

        expectedAppValue = planQueueBuilder.setActive(false).build();

        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        inputValue = inputValue.toBuilder().clearActive().build();

        expectedAppValue = planQueueBuilder.setActive(true).build();

        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PlanQueueDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PlanQueueDataTranslationSpec translationSpec = new PlanQueueDataTranslationSpec();

        assertEquals(PlanQueueData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PlanQueueDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PlanQueueDataTranslationSpec translationSpec = new PlanQueueDataTranslationSpec();

        assertEquals(PlanQueueDataInput.class, translationSpec.getInputObjectClass());
    }
}
