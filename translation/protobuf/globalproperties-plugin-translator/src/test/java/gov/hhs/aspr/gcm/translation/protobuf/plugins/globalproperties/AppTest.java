package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.TestGlobalPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import nucleus.PluginData;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class AppTest {

    @Test
    public void testGlobalPropertiesTranslator() {

        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("globalproperties-plugin-translator")) {
            basePath = basePath.resolve("globalproperties-plugin-translator");
        }
        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");
        
        outputFilePath.toFile().mkdir();

        String inputFileName = "input.json";
        String outputFileName = "output.json";


        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(GlobalPropertiesTranslator.getTranslatorRW(inputFilePath.resolve(inputFileName).toString(), outputFilePath.resolve(outputFileName).toString()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslatorSpec(new TestGlobalPropertyIdTranslatorSpec())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GlobalPropertiesPluginData actualPluginData = (GlobalPropertiesPluginData) pluginDatas.get(0);

        Set<TestGlobalPropertyId> expectedPropertyIds = EnumSet.allOf(TestGlobalPropertyId.class);

        Set<GlobalPropertyId> actualGlobalPropertyIds = actualPluginData.getGlobalPropertyIds();
        assertEquals(expectedPropertyIds, actualGlobalPropertyIds);

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = testGlobalPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getGlobalPropertyDefinition(testGlobalPropertyId);

            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

        }

        translatorController.writeOutput();
    }
}
