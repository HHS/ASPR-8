package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus;

import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.translationSpecs.ExampleDimensionTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.translationSpecs.ExamplePlanDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.DimensionTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.ExperimentParameterDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.PlanDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.PlanQueueDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.PlannerTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs.SimulationStateTranslationSpec;

/**
 * Translator for Nucleus
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * the classes within Nucleus
 */
public class NucleusTranslator {

    private NucleusTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new SimulationStateTranslationSpec());
        list.add(new ExamplePlanDataTranslationSpec());
        list.add(new PlanQueueDataTranslationSpec());
        list.add(new PlannerTranslationSpec());
        list.add(new DimensionTranslationSpec());
        list.add(new ExampleDimensionTranslationSpec());
        list.add(new PlanDataTranslationSpec());
        list.add(new ExperimentParameterDataTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(NucleusTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(
                                    ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

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
                                            .findFieldByName("active"));
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
