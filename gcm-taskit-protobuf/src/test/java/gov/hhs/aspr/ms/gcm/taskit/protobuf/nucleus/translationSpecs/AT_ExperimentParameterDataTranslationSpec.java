package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.ExperimentParameterDataInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import nucleus.ExperimentParameterData;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ExperimentParameterDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = ExperimentParameterDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ExperimentParameterDataTranslationSpec());
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

        ExperimentParameterDataTranslationSpec translationSpec = new ExperimentParameterDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ExperimentParameterData.Builder builder = ExperimentParameterData.builder()
                .setThreadCount(8)
                .setRecordState(false)
                .setHaltOnException(true)
                .setContinueFromProgressLog(false);

        for (int i = 0; i < 10; i++) {
            builder.addExplicitScenarioId(i);
        }

        ExperimentParameterData expectedAppValue = builder.build();

        ExperimentParameterDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ExperimentParameterData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        expectedAppValue = builder.setSimulationHaltTime(10.0).build();
        inputValue = translationSpec.convertAppObject(expectedAppValue);
        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        expectedAppValue = builder.setExperimentProgressLog(Path.of("")).build();
        inputValue = translationSpec.convertAppObject(expectedAppValue);
        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ExperimentParameterDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ExperimentParameterDataTranslationSpec translationSpec = new ExperimentParameterDataTranslationSpec();

        assertEquals(ExperimentParameterData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ExperimentParameterDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ExperimentParameterDataTranslationSpec translationSpec = new ExperimentParameterDataTranslationSpec();

        assertEquals(ExperimentParameterDataInput.class, translationSpec.getInputObjectClass());
    }
}
