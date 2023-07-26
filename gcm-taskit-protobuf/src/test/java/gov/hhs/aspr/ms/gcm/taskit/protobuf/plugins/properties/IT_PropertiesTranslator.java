package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.core.testsupport.TestResourceHelper;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestForCoverage;

public class IT_PropertiesTranslator {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testPropertyValueMapTranslator() {
        String fileName = "data.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), PropertyDefinitionInput.class)
                .addOutputFilePath(filePath.resolve(fileName), PropertyDefinition.class)
                .build();

        PropertyDefinition expectedPropertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("defaultValue")
                .setPropertyValueMutability(true)
                .setType(String.class)
                .build();

        translatorController.writeOutput(expectedPropertyDefinition);
        translatorController.readInput();

        PropertyDefinition actualPropertyDefiniton = translatorController.getFirstObject(PropertyDefinition.class);

        assertEquals(expectedPropertyDefinition, actualPropertyDefiniton);

    }
}
