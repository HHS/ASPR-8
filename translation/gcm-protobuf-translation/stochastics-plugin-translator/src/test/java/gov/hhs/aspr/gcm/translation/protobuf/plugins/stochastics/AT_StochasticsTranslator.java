package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_StochasticsTranslator {

    @Test
    @UnitTestForCoverage
    public void testGetTranslationSpecs() throws ClassNotFoundException {
        List<TranslationSpec<?, ?>> translationSpecs = StochasticsTranslator.getTranslationSpecs();
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

                            if (!translationSpecClasses.contains(classRef)) {
                                // use this assertion to make it clear which spec is missing
                                assertEquals("", classRef.getSimpleName());
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    @UnitTestMethod(target = StochasticsTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                }).build();

        assertEquals(expectedTranslator, StochasticsTranslator.getTranslator());
    }
}
