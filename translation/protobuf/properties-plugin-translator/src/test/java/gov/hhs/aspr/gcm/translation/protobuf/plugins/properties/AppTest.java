package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyValueMap;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.TestMessageSimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs.Layer1TranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs.TestMessageTranslatorSpec;

public class AppTest {

    @Test
    public void testPropertyValueMapTranslator() {

        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("properties-plugin-translator")) {
            basePath = basePath.resolve("properties-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "data.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder()
                        .addTranslatorSpec(new TestMessageTranslatorSpec())
                        .addTranslatorSpec(new Layer1TranslatorSpec()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), PropertyValueMapInput.class)
                .addWriter(outputFilePath.resolve(fileName), PropertyValueMap.class)
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        PropertyValueMap actualMap = (PropertyValueMap) objects.get(0);

        Layer1SimObject expectedLayer1SimObject = new Layer1SimObject();

        expectedLayer1SimObject.setX(0);
        expectedLayer1SimObject.setY(1);
        expectedLayer1SimObject.setZ(2);

        TestMessageSimObject expectedMessageSimObject = new TestMessageSimObject();

        expectedMessageSimObject.setLayer1(expectedLayer1SimObject);

        PropertyValueMap expectedMap = new PropertyValueMap();
        expectedMap.setPropertyId(expectedMessageSimObject);
        expectedMap.setPropertyValue("50");

        assertEquals(expectedMap.getPropertyId(), actualMap.getPropertyId());
        assertEquals(expectedMap.getPropertyValue(), actualMap.getPropertyValue());

        translatorController.writeOutput();

    }
}