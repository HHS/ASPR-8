package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyValueMap;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.TestMessageSimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translationSpecs.Layer1TranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translationSpecs.TestMessageTranslatorSpec;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestResourceHelper;

public class AppTest {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    public void testPropertyValueMapTranslator() {
        String fileName = "data.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslationEngine.builder()
                        .addTranslatorSpec(new TestMessageTranslatorSpec())
                        .addTranslatorSpec(new Layer1TranslatorSpec()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), PropertyValueMapInput.class)
                .addOutputFilePath(filePath.resolve(fileName), PropertyValueMap.class)
                .build();

        Layer1SimObject expectedLayer1SimObject = new Layer1SimObject();
        TestMessageSimObject expectedMessageSimObject = new TestMessageSimObject();

        expectedMessageSimObject.setLayer1(expectedLayer1SimObject);

        PropertyValueMap expectedMap = new PropertyValueMap();
        expectedMap.setPropertyId(expectedMessageSimObject);
        expectedMap.setPropertyValue("50");

        translatorController.writeOutput(expectedMap);
        translatorController.readInput();

        PropertyValueMap actualMap = translatorController.getFirstObject(PropertyValueMap.class);

        assertEquals(expectedMap, actualMap);

    }
}
