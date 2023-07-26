package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.ExperimentParameterDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.translationSpecs.ExamplePlanDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.PlanDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.PlanQueueDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.PlannerTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.SimulationStateTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_NucleusTranslator {

    @Test
    @UnitTestForCoverage
    public void testGetTranslationSpecs() throws ClassNotFoundException {
        List<TranslationSpec<?, ?>> translationSpecs = NucleusTranslator.getTranslationSpecs();
        List<Class<?>> translationSpecClasses = new ArrayList<>();

        for (TranslationSpec<?, ?> translationSpec : translationSpecs) {
            translationSpecClasses.add(translationSpec.getClass());
        }

        String packageName = this.getClass().getPackageName() + ".translationSpecs";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL packageURL;

        packageURL = classLoader.getResource(packageName.replaceAll("[.]", "/"));

        if (packageURL != null) {
            String packagePath = packageURL.getPath();
            if (packagePath != null) {
                packagePath = packagePath.replaceAll("test-classes", "classes");
                File packageDir = new File(packagePath);
                if (packageDir.isDirectory()) {
                    File[] files = packageDir.listFiles();
                    for (File file : files) {
                        String className = file.getName();
                        if (className.endsWith(".class")) {
                            className = packageName + "." + className.substring(0,
                                    className.length() - 6);
                            Class<?> classRef = classLoader.loadClass(className);

                            assertTrue(translationSpecClasses.contains(classRef), classRef.getSimpleName());
                        }
                    }
                }
            }
        }
    }

    @Test
    @UnitTestMethod(target = NucleusTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {

        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new SimulationStateTranslationSpec())
                            .addTranslationSpec(new ExamplePlanDataTranslationSpec())
                            .addTranslationSpec(new PlanQueueDataTranslationSpec())
                            .addTranslationSpec(new PlannerTranslationSpec())
                            .addTranslationSpec(new PlanDataTranslationSpec())
                            .addTranslationSpec(new ExamplePlanDataTranslationSpec());

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(
                                    SimulationStateInput.getDescriptor()
                                            .findFieldByName("startTime"))
                            .addFieldToIncludeDefaultValue(
                                    PlanQueueDataInput.getDescriptor()
                                            .findFieldByName("time"))
                            .addFieldToIncludeDefaultValue(
                                    PlanQueueDataInput.getDescriptor()
                                            .findFieldByName("plannerId"))
                            .addFieldToIncludeDefaultValue(
                                    PlanQueueDataInput.getDescriptor()
                                            .findFieldByName("active"))
                            .addFieldToIncludeDefaultValue(
                                    ExperimentParameterDataInput.getDescriptor()
                                            .findFieldByName(
                                                    "experimentProgressLogPath"));
                }).build();

        assertEquals(expectedTranslator, NucleusTranslator.getTranslator());
    }
}
