package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyValueMap;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.TestMessageSimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs.Layer1TranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs.TestMessageTranslatorSpec;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;

public class AppTest {
    Path basePath = getCurrentDir();
    Path inputFilePath = basePath.resolve("json");
    Path outputFilePath = makeOutputDir();

    private Path getCurrentDir() {
        try {
            return Path.of(this.getClass().getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Path makeOutputDir() {
        Path path = basePath.resolve("json/output");

        path.toFile().mkdirs();

        return path;
    }

    @Test
    public void testPropertyValueMapTranslator() {
        String fileName = "data.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder()
                        .addTranslatorSpec(new TestMessageTranslatorSpec())
                        .addTranslatorSpec(new Layer1TranslatorSpec()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addInputFilePath(inputFilePath.resolve(fileName), PropertyValueMapInput.class)
                .addOutputFilePath(outputFilePath.resolve(fileName), PropertyValueMap.class)
                .build();

        translatorController.readInput();

        PropertyValueMap actualMap = translatorController.getObject(PropertyValueMap.class);

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
