package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.StochasticsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.support.input.WellStateInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.stochastics.support.WellState;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_WellStateTranslationSpec {

    @Test
    @UnitTestConstructor(target = WellStateTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new WellStateTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(StochasticsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        WellStateTranslationSpec translationSpec = new WellStateTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        WellState expectedAppValue = WellState.builder().setSeed(524805676405822016L).build();

        WellStateInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        WellState actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        inputValue = inputValue.toBuilder().clearVArray().build();

        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = WellStateTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        WellStateTranslationSpec translationSpec = new WellStateTranslationSpec();

        assertEquals(WellState.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = WellStateTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        WellStateTranslationSpec translationSpec = new WellStateTranslationSpec();

        assertEquals(WellStateInput.class, translationSpec.getInputObjectClass());
    }
}
